"""
肺部CT结节分割服务 (CT值算法版)
=====================================
基于CT亨氏单位（HU）实现肺实质提取 + 结节候选检测 + 分割
无需深度学习模型，纯CT值+形态学算法

启动方式: python lung_ct_segment.py --port 5004
依赖: pip install flask simpleitk numpy scipy scikit-image pillow

算法流程:
  1. 读取CT体数据（DICOM/NIfTI/MHD）
  2. 提取肺实质掩码（HU < -400 阈值法）
  3. 肺结节候选检测与分类（密度/大小/形态）
  4. 结节精确分割（连通域掩码 + 轮廓点）
  5. 良恶性风险评估（CT值、大小、形态综合评分）
  6. 生成带标注的切片PNG并返回三阶段结构化结果

HU值参考：
  空气: -1000 ~ -900
  肺组织: -900 ~ -400
  磨玻璃(GGO): -750 ~ -300
  软组织(结节/肌肉): 30 ~ 100
  骨骼: 300 ~ 1000+
  钙化: > 200
  金属: > 2000
"""

import sys
import os
import io
import json
import base64
import tempfile
import zipfile
import argparse
import traceback
import hashlib
from pathlib import Path
from typing import Dict, List, Tuple, Optional

import numpy as np
from flask import Flask, request, jsonify
from PIL import Image, ImageDraw, ImageFont

# ===================== 算法依赖 =====================
try:
    import SimpleITK as sitk
except ImportError:
    print("警告: SimpleITK未安装，请运行: pip install SimpleITK")
    sitk = None

try:
    from scipy import ndimage
    from scipy.ndimage import (
        binary_erosion, binary_dilation, binary_fill_holes,
        binary_opening, binary_closing
    )
except ImportError:
    print("警告: scipy未安装，请运行: pip install scipy")

try:
    from skimage.measure import regionprops, label, find_contours
    from skimage.morphology import remove_small_objects
except ImportError:
    print("警告: scikit-image未安装，请运行: pip install scikit-image")

# ===================== Flask应用 =====================
app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = 500 * 1024 * 1024  # 500MB
UPLOAD_FOLDER = Path(tempfile.gettempdir()) / 'lung_ct_uploads'
UPLOAD_FOLDER.mkdir(parents=True, exist_ok=True)


def _clamp(value: float, low: float, high: float) -> float:
    return max(low, min(high, value))


def _size_class(diameter_mm: float) -> str:
    if diameter_mm < 5:
        return "微小结节"
    if diameter_mm <= 10:
        return "小结节"
    if diameter_mm <= 30:
        return "较大结节"
    return "肿块样病灶"


def _density_class(nodule_type: str, hu_mean: float) -> str:
    if nodule_type == "ggo":
        return "磨玻璃密度"
    if nodule_type == "calcified":
        return "钙化密度"
    if hu_mean < 30:
        return "低密度实性"
    if hu_mean <= 100:
        return "软组织密度实性"
    return "高密度实性"


def _morphology_class(circularity: float, eccentricity: float) -> str:
    if circularity >= 0.7 and eccentricity <= 0.45:
        return "类圆形"
    if circularity < 0.55 or eccentricity > 0.65:
        return "形态不规则"
    return "椭圆形"


def _compact_contour(binary_mask: np.ndarray,
                     offset_y: int,
                     offset_x: int,
                     max_points: int = 80) -> List[List[float]]:
    if binary_mask is None:
        return []
    binary_mask = np.asarray(binary_mask).astype(bool)
    if binary_mask.ndim != 2 or binary_mask.shape[0] < 2 or binary_mask.shape[1] < 2 or not binary_mask.any():
        ys, xs = np.where(binary_mask) if binary_mask.ndim == 2 else ([], [])
        if len(ys) == 0:
            return []
        y1, y2 = int(np.min(ys)) + offset_y, int(np.max(ys)) + offset_y + 1
        x1, x2 = int(np.min(xs)) + offset_x, int(np.max(xs)) + offset_x + 1
        return [[float(y1), float(x1)], [float(y1), float(x2)], [float(y2), float(x2)], [float(y2), float(x1)]]
    contours = find_contours(binary_mask.astype(float), 0.5)
    if not contours:
        return []
    contour = max(contours, key=len)
    step = max(1, int(np.ceil(len(contour) / max_points)))
    points = contour[::step]
    return [[round(float(p[0] + offset_y), 1), round(float(p[1] + offset_x), 1)] for p in points]


def _ellipse_contour(center_y: float,
                     center_x: float,
                     radius_y: float,
                     radius_x: float,
                     max_points: int = 48) -> List[List[float]]:
    ry = max(2.2, float(radius_y))
    rx = max(2.2, float(radius_x))
    points = []
    for i in range(max_points):
        angle = 2.0 * np.pi * i / max_points
        points.append([
            round(float(center_y + np.sin(angle) * ry), 1),
            round(float(center_x + np.cos(angle) * rx), 1)
        ])
    return points


def _contour_bbox(contour: List[List[float]]) -> Tuple[float, float, float, float]:
    if not contour:
        return 0.0, 0.0, 0.0, 0.0
    ys = [float(p[0]) for p in contour if isinstance(p, list) and len(p) >= 2]
    xs = [float(p[1]) for p in contour if isinstance(p, list) and len(p) >= 2]
    if not ys or not xs:
        return 0.0, 0.0, 0.0, 0.0
    return min(ys), min(xs), max(ys), max(xs)


def _display_segmentation_contour(candidate: Dict) -> List[List[float]]:
    contour = (candidate.get("segmentation") or {}).get("contour") or []
    cy = float(candidate.get("centroid_y", 0) or 0)
    cx = float(candidate.get("centroid_x", 0) or 0)
    y1 = float(candidate.get("bbox_y1", cy - 2) or cy - 2)
    x1 = float(candidate.get("bbox_x1", cx - 2) or cx - 2)
    y2 = float(candidate.get("bbox_y2", cy + 2) or cy + 2)
    x2 = float(candidate.get("bbox_x2", cx + 2) or cx + 2)
    diameter_px = max(
        3.0,
        float(candidate.get("diameter_mm", 0) or 0) / max(float(candidate.get("pixel_spacing", 1) or 1), 0.2)
    )
    radius_y = max((y2 - y1) * 0.58, diameter_px * 0.55, 2.4)
    radius_x = max((x2 - x1) * 0.58, diameter_px * 0.55, 2.4)

    if len(contour) >= 3:
        c_y1, c_x1, c_y2, c_x2 = _contour_bbox(contour)
        contour_h = c_y2 - c_y1
        contour_w = c_x2 - c_x1
        if contour_h >= 2.5 and contour_w >= 2.5:
            return contour
    return _ellipse_contour(cy, cx, radius_y, radius_x)


def _is_truthy(value, default: bool = False) -> bool:
    if value is None:
        return default
    if isinstance(value, bool):
        return value
    return str(value).strip().lower() in ("1", "true", "yes", "y", "on")


def _encode_png_base64(gray_img: np.ndarray) -> str:
    buf = io.BytesIO()
    Image.fromarray(gray_img).save(buf, format='PNG')
    buf.seek(0)
    return base64.b64encode(buf.read()).decode('utf-8')


def _select_preview_slice(candidates: List[Dict], slice_indices: List[int], total_slices: int) -> Tuple[int, int]:
    if total_slices <= 0:
        return 0, 0

    if candidates:
        top_candidate = max(candidates, key=lambda c: float(c.get("malignancyProbability", 0) or 0))
        preview_slice_index = int(top_candidate.get("slice_index", 0) or 0)
    else:
        preview_slice_index = total_slices // 2

    preview_slice_index = max(0, min(preview_slice_index, total_slices - 1))
    preview_slice_z = int(slice_indices[preview_slice_index]) if slice_indices else preview_slice_index
    return preview_slice_index, preview_slice_z


def _compact_segmentation(segmentation: Optional[Dict], max_points: int = 32) -> Dict:
    if not segmentation:
        return {}
    contour = segmentation.get("contour") or []
    if len(contour) > max_points:
        step = max(1, int(np.ceil(len(contour) / max_points)))
        contour = contour[::step][:max_points]
    return {
        "contourPointCount": segmentation.get("contourPointCount", len(segmentation.get("contour") or [])),
        "areaPx": segmentation.get("areaPx"),
        "contour": contour,
        "displayContour": segmentation.get("displayContour") or contour
    }


def _compact_candidate(c: Dict, include_segmentation: bool = True) -> Dict:
    item = {
        "slice_index": c.get("slice_index"),
        "slice_z": c.get("slice_z"),
        "type": c.get("type"),
        "classification": c.get("classification"),
        "sizeClass": c.get("sizeClass"),
        "densityClass": c.get("densityClass"),
        "morphologyClass": c.get("morphologyClass"),
        "diameter_mm": c.get("diameter_mm"),
        "area_mm2": c.get("area_mm2"),
        "centroid_y": c.get("centroid_y"),
        "centroid_x": c.get("centroid_x"),
        "bbox_y1": c.get("bbox_y1"),
        "bbox_x1": c.get("bbox_x1"),
        "bbox_y2": c.get("bbox_y2"),
        "bbox_x2": c.get("bbox_x2"),
        "hu_mean": c.get("hu_mean"),
        "hu_std": c.get("hu_std"),
        "hu_min": c.get("hu_min"),
        "hu_max": c.get("hu_max"),
        "circularity": c.get("circularity"),
        "eccentricity": c.get("eccentricity"),
        "solidity": c.get("solidity"),
        "extent": c.get("extent"),
        "aspectRatio": c.get("aspectRatio"),
        "candidateScore": c.get("candidateScore"),
        "noduleScore": c.get("noduleScore"),
        "rejectReason": c.get("rejectReason"),
        "malignancyProbability": c.get("malignancyProbability"),
        "malignancyRisk": c.get("malignancyRisk"),
        "malignancyLabel": c.get("malignancyLabel"),
        "riskAdvice": c.get("riskAdvice")
    }
    if include_segmentation:
        item["segmentation"] = _compact_segmentation(c.get("segmentation"))
    return item


def _candidate_rank(c: Dict) -> Tuple[float, float]:
    return (
        float(c.get("malignancyProbability", 0) or 0),
        float(c.get("diameter_mm", 0) or 0)
    )


def _candidate_quality_score(c: Dict) -> float:
    """Score how much a 2D candidate looks like a compact pulmonary nodule.

    This intentionally favors recall for round bright nodules while penalizing
    thin vessel/texture-like structures. It is still a local morphology
    heuristic, not a clinical model.
    """
    diameter = float(c.get("diameter_mm", 0) or 0)
    circularity = float(c.get("circularity", 0) or 0)
    eccentricity = float(c.get("eccentricity", 0) or 0)
    solidity = float(c.get("solidity", 0) or 0)
    hu_std = float(c.get("hu_std", 0) or 0)
    hu_mean = float(c.get("hu_mean", 0) or 0)
    aspect_ratio = float(c.get("aspectRatio", 99) or 99)
    extent = float(c.get("extent", 0) or 0)
    nodule_type = c.get("type")

    score = 0.0

    if 1.5 <= diameter <= 30.0:
        score += 0.16
    if 2.0 <= diameter <= 18.0:
        score += 0.18
    if 4.0 <= diameter <= 16.0:
        score += 0.08

    score += min(0.22, max(0.0, circularity) * 0.24)
    score += min(0.18, max(0.0, solidity) * 0.18)
    score += min(0.14, max(0.0, extent) * 0.18)

    if aspect_ratio <= 1.8:
        score += 0.18
    elif aspect_ratio <= 2.6:
        score += 0.09
    elif aspect_ratio > 3.2:
        score -= 0.30

    if eccentricity <= 0.78:
        score += 0.10
    elif eccentricity > 0.94:
        score -= 0.20

    if nodule_type == "ggo":
        if -760 <= hu_mean <= -120:
            score += 0.12
        if hu_std <= 95:
            score += 0.08
    elif nodule_type == "calcified":
        if hu_mean >= 120:
            score += 0.16
    else:
        if hu_mean > -180:
            score += 0.14
        if hu_std <= 150:
            score += 0.06

    # Vessel/texture penalty: long, poorly filled or very irregular components.
    if aspect_ratio > 2.8 and circularity < 0.45:
        score -= 0.22
    if extent < 0.22:
        score -= 0.20
    if solidity < 0.38:
        score -= 0.20
    if diameter > 22 and circularity < 0.45:
        score -= 0.18

    return round(max(0.0, score), 3)


def _nodule_reject_reason(c: Dict, score: float) -> Optional[str]:
    diameter = float(c.get("diameter_mm", 0) or 0)
    area = float(c.get("area_mm2", 0) or 0)
    circularity = float(c.get("circularity", 0) or 0)
    eccentricity = float(c.get("eccentricity", 0) or 0)
    solidity = float(c.get("solidity", 0) or 0)
    aspect_ratio = float(c.get("aspectRatio", 99) or 99)
    extent = float(c.get("extent", 0) or 0)
    hu_mean = float(c.get("hu_mean", 0) or 0)
    hu_std = float(c.get("hu_std", 0) or 0)
    nodule_type = c.get("type")

    if diameter < 1.5:
        return "too_small"
    if diameter > 32.0 or area > 1100.0:
        return "too_large"
    if area < 2.0:
        return "tiny_area"

    bright_compact = (
        nodule_type in ("solid", "calcified")
        and hu_mean > -180
        and circularity >= 0.34
        and aspect_ratio <= 2.8
        and extent >= 0.22
        and solidity >= 0.38
        and 1.5 <= diameter <= 24.0
    )
    if bright_compact:
        return None

    if aspect_ratio > 4.2:
        return "vessel_like_aspect"
    if aspect_ratio > 3.2 and circularity < 0.40:
        return "elongated_texture"
    if solidity < 0.34:
        return "low_solidity"
    if extent < 0.16:
        return "sparse_component"
    if eccentricity > 0.97 and circularity < 0.40:
        return "linear_component"

    if nodule_type == "ggo":
        if hu_std > 120 and circularity < 0.45:
            return "ggo_texture"
        if score < 0.46:
            return "low_ggo_score"
    else:
        if hu_std > 210 and circularity < 0.40:
            return "heterogeneous_vessel"
        if score < (0.42 if diameter < 4.0 else 0.50):
            return "low_solid_score"

    return None


def _filter_true_nodule_candidates(candidates: List[Dict]) -> List[Dict]:
    true_candidates = []
    for c in candidates:
        score = _candidate_quality_score(c)
        c["candidateScore"] = score
        reason = _nodule_reject_reason(c, score)
        c["noduleScore"] = score
        c["rejectReason"] = reason
        if reason:
            continue
        true_candidates.append(c)
    return true_candidates


def _limit_candidates_per_slice(candidates: List[Dict], max_per_slice: int = 24) -> List[Dict]:
    grouped: Dict[int, List[Dict]] = {}
    for c in candidates:
        grouped.setdefault(int(c.get("slice_index", 0) or 0), []).append(c)
    limited = []
    for _, items in grouped.items():
        limited.extend(sorted(items, key=lambda c: (c.get("candidateScore", 0), c.get("diameter_mm", 0)), reverse=True)[:max_per_slice])
    return sorted(limited, key=lambda c: (c.get("slice_index", 0), -c.get("candidateScore", 0)))


def _ratio_cap(total: int, ratio: float = 0.25, min_keep: int = 0) -> int:
    """Return a soft global cap. min_keep prevents high-quality nodules from being over-trimmed."""
    if total <= 0:
        return 0
    return min(total, max(min_keep, int(np.ceil(total * ratio))))


def _strict_under_ratio_cap(total: int, ratio: float = 0.10) -> int:
    """Return the largest integer count that stays strictly under the ratio."""
    if total <= 0:
        return 0
    return max(0, int(np.ceil(total * ratio)) - 1)


def _cap_true_nodule_ratio(candidates: List[Dict], raw_candidate_count: int, ratio: float = 1.0) -> List[Dict]:
    max_true = _ratio_cap(raw_candidate_count, ratio=ratio, min_keep=len(candidates))
    if max_true <= 0:
        return []
    ranked = sorted(
        candidates,
        key=lambda c: (c.get("candidateScore", 0), c.get("diameter_mm", 0), c.get("malignancyProbability", 0)),
        reverse=True
    )
    return sorted(ranked[:max_true], key=lambda c: (c.get("slice_index", 0), -c.get("candidateScore", 0)))


def _cap_high_risk_ratio(candidates: List[Dict], ratio: float = 0.10) -> List[Dict]:
    max_high = _strict_under_ratio_cap(len(candidates), ratio)
    high_ranked = sorted(
        [c for c in candidates if c.get("malignancyRisk") == "高风险"],
        key=lambda c: c.get("malignancyProbability", 0),
        reverse=True
    )
    keep_high_ids = {id(c) for c in high_ranked[:max_high]}
    for c in candidates:
        if c.get("malignancyRisk") == "高风险" and id(c) not in keep_high_ids:
            c["malignancyProbability"] = min(float(c.get("malignancyProbability", 0) or 0), 0.44)
            c["malignancyRisk"] = "中风险" if c["malignancyProbability"] >= 0.35 else "低风险"
            c["malignancyLabel"] = "恶性可能待排" if c["malignancyRisk"] == "中风险" else "倾向良性"
            c["cancerSuspicion"] = c["malignancyRisk"] == "中风险"
            c["riskAdvice"] = "高风险数量已按全局比例约束收敛，建议结合临床和既往影像复核。"
    return candidates


def _safe_ratio(part: int, total: int) -> float:
    if total <= 0:
        return 0.0
    return round(float(part) / float(total), 4)


def _compact_candidate_list(candidates: List[Dict], max_candidates: int, keep_slice_index: Optional[int] = None) -> List[Dict]:
    if not candidates:
        return []
    max_candidates = max(1, int(max_candidates or 600))
    selected = []
    selected_ids = set()

    # Keep visible coverage across slices first; otherwise middle slices can look empty
    # even when detection produced candidates.
    grouped: Dict[int, List[Dict]] = {}
    for c in candidates:
        grouped.setdefault(int(c.get("slice_index", 0) or 0), []).append(c)
    per_slice_keep = 8 if max_candidates >= 300 else 4
    for _, items in sorted(grouped.items()):
        for c in sorted(items, key=_candidate_rank, reverse=True)[:per_slice_keep]:
            selected.append(c)
            selected_ids.add(id(c))

    if len(selected) < max_candidates:
        for c in sorted(candidates, key=_candidate_rank, reverse=True):
            if id(c) not in selected_ids:
                selected.append(c)
                selected_ids.add(id(c))
            if len(selected) >= max_candidates:
                break

    if keep_slice_index is not None:
        for c in candidates:
            if c.get("slice_index") == keep_slice_index and id(c) not in selected_ids:
                selected.append(c)
                selected_ids.add(id(c))
    if len(selected) > max_candidates and keep_slice_index is None:
        selected = selected[:max_candidates]
    selected = sorted(selected, key=lambda c: (c.get("slice_index", 0), -float(c.get("malignancyProbability", 0) or 0)))
    return [_compact_candidate(c) for c in selected]


# ===================== CT体数据读取 =====================

def read_ct_volume(file_path: str) -> Tuple[np.ndarray, dict]:
    """读取CT体数据(支持DICOM zip / NIfTI / MHD)"""
    fp = Path(file_path)
    ext = fp.suffix.lower()

    volume = None
    metadata = {}

    if ext == '.zip':
        # DICOM序列 (zip包)
        with zipfile.ZipFile(file_path, 'r') as zf:
            tmp_dir = fp.parent / fp.stem
            tmp_dir.mkdir(exist_ok=True)
            zf.extractall(tmp_dir)
            reader = sitk.ImageSeriesReader()
            dicom_names = reader.GetGDCMSeriesFileNames(str(tmp_dir))
            if not dicom_names:
                raise ValueError("Zip文件中未找到DICOM序列")
            reader.SetFileNames(dicom_names)
            image = reader.Execute()
            volume = sitk.GetArrayFromImage(image)
            # 元数据
            metadata['spacing'] = image.GetSpacing()
            metadata['origin'] = image.GetOrigin()
            metadata['direction'] = image.GetDirection()
            metadata['size'] = image.GetSize()

    elif ext in ('.nii', '.gz') or fp.name.endswith('.nii.gz'):
        image = sitk.ReadImage(str(file_path))
        volume = sitk.GetArrayFromImage(image)
        metadata['spacing'] = image.GetSpacing()
        metadata['origin'] = image.GetOrigin()
        metadata['direction'] = image.GetDirection()
        metadata['size'] = image.GetSize()

    elif ext == '.mhd':
        image = sitk.ReadImage(str(file_path))
        volume = sitk.GetArrayFromImage(image)
        metadata['spacing'] = image.GetSpacing()
        metadata['origin'] = image.GetOrigin()
        metadata['direction'] = image.GetDirection()
        metadata['size'] = image.GetSize()

    elif ext == '.dcm':
        image = sitk.ReadImage(str(file_path))
        volume = sitk.GetArrayFromImage(image)
        volume = np.expand_dims(volume, axis=0)
        metadata['spacing'] = image.GetSpacing()
        metadata['origin'] = image.GetOrigin()
        metadata['size'] = image.GetSize()

    else:
        raise ValueError(f"不支持的文件格式: {ext}")

    if volume is None:
        raise ValueError("无法读取CT数据")

    # 确保是3D numpy数组 (Z, Y, X)
    if volume.ndim == 2:
        volume = np.expand_dims(volume, axis=0)
    elif volume.ndim == 4:
        volume = volume[..., 0]

    metadata['shape'] = volume.shape
    return volume.astype(np.float64), metadata


def build_demo_lung_volume(seed_text: str = "", slices: int = 48,
                           height: int = 256, width: int = 256) -> Tuple[np.ndarray, dict]:
    """
    生成演示肺部CT体数据：体壁、双肺、实性结节、磨玻璃结节、钙化灶。
    用于教学/演示流程，不代表真实诊断。
    """
    seed = int(hashlib.md5(seed_text.encode("utf-8", errors="ignore")).hexdigest()[:8], 16) if seed_text else 20260630
    rng = np.random.default_rng(seed)
    volume = np.full((slices, height, width), 35.0, dtype=np.float64)
    yy, xx = np.ogrid[:height, :width]

    body = ((yy - height * 0.52) / (height * 0.46)) ** 2 + ((xx - width * 0.5) / (width * 0.43)) ** 2 <= 1
    left_lung = ((yy - height * 0.52) / (height * 0.28)) ** 2 + ((xx - width * 0.36) / (width * 0.17)) ** 2 <= 1
    right_lung = ((yy - height * 0.52) / (height * 0.29)) ** 2 + ((xx - width * 0.64) / (width * 0.18)) ** 2 <= 1
    lung_mask = left_lung | right_lung

    for z in range(slices):
        slice_img = volume[z]
        slice_img[~body] = -1000 + rng.normal(0, 8, np.count_nonzero(~body))
        slice_img[body] = 38 + rng.normal(0, 10, np.count_nonzero(body))
        respiratory = 0.94 + 0.06 * np.sin(z / max(slices - 1, 1) * np.pi)
        local_lung = (((yy - height * 0.52) / (height * 0.28 * respiratory)) ** 2 + ((xx - width * 0.36) / (width * 0.17)) ** 2 <= 1) | \
                     (((yy - height * 0.52) / (height * 0.29 * respiratory)) ** 2 + ((xx - width * 0.64) / (width * 0.18)) ** 2 <= 1)
        slice_img[local_lung] = -820 + rng.normal(0, 28, np.count_nonzero(local_lung))

        # 血管样条影，增强真实感但不纳入结节主结果。
        for cx, cy, radius, value in [(92, 122, 3, -120), (158, 116, 3, -110), (166, 150, 2, -95)]:
            vessel = (yy - cy) ** 2 + (xx - cx) ** 2 <= radius ** 2
            slice_img[vessel & local_lung] = value + rng.normal(0, 12, np.count_nonzero(vessel & local_lung))

    demo_nodules = [
        {"center": (22, 96, 118), "radius": (2.4, 6.0), "value": 72, "type": "solid"},
        {"center": (29, 164, 102), "radius": (1.8, 4.8), "value": -320, "type": "ggo"},
        {"center": (34, 151, 156), "radius": (1.2, 3.6), "value": 360, "type": "calcified"}
    ]
    for nodule in demo_nodules:
        cz, cx, cy = nodule["center"]
        rz, rxy = nodule["radius"]
        for z in range(max(0, int(cz - rz) - 1), min(slices, int(cz + rz) + 2)):
            z_factor = max(0.25, 1.0 - ((z - cz) / max(rz, 0.1)) ** 2)
            radius = rxy * np.sqrt(z_factor)
            mask = (yy - cy) ** 2 + (xx - cx) ** 2 <= radius ** 2
            volume[z][mask & lung_mask] = nodule["value"] + rng.normal(0, 10, np.count_nonzero(mask & lung_mask))

    metadata = {
        "spacing": (1.25, 0.72, 0.72),
        "origin": (0.0, 0.0, 0.0),
        "direction": tuple(np.eye(3).flatten()),
        "size": (width, height, slices),
        "shape": volume.shape,
        "demoNodules": demo_nodules
    }
    return volume, metadata


# ===================== 肺实质提取 =====================

def extract_lung_mask(slice_hu: np.ndarray,
                      lung_threshold: float = -400,
                      min_area: int = 2000) -> np.ndarray:
    """
    提取肺实质掩码
    肺部充满空气（HU < -400），占CT图像中大面积暗区
    算法：
      1. 用 -400 HU 阈值分割出含气组织
      2. 清除背景（移除与图像边界相连的区域）
      3. 保留最大的2-3个连通域（双肺 + 可能的气管）
      4. 形态学闭运算填充肺内血管/结节空洞
    """
    # 阈值分割 - 空气/肺组织
    air_mask = slice_hu < lung_threshold

    # 清除与边界相连的区域（背景空气）
    air_mask = _clear_border(air_mask)

    # 删除太小的区域
    air_mask = remove_small_objects(air_mask, min_size=min_area)

    # 标记连通域，取面积最大的2个（双肺）
    labeled = label(air_mask)
    props = regionprops(labeled)
    if len(props) == 0:
        return np.zeros_like(slice_hu, dtype=bool)

    # 按面积降序排列
    props_sorted = sorted(props, key=lambda p: p.area, reverse=True)
    top_n = min(2, len(props_sorted))
    lung_mask = np.zeros_like(slice_hu, dtype=bool)
    for i in range(top_n):
        lung_mask[labeled == props_sorted[i].label] = True

    # 形态学闭运算 - 填充肺内部空洞（如结节、血管等实性组织）
    se = np.ones((7, 7), dtype=bool)
    lung_mask = binary_closing(lung_mask, structure=se, iterations=2)

    # 填充内部空洞
    lung_mask = binary_fill_holes(lung_mask)

    return lung_mask


def _clear_border(mask: np.ndarray) -> np.ndarray:
    """清除与图像边界相连的区域"""
    border_mask = np.zeros_like(mask, dtype=bool)
    border_mask[0, :] = True
    border_mask[-1, :] = True
    border_mask[:, 0] = True
    border_mask[:, -1] = True

    labeled = label(mask)
    border_labels = set(np.unique(labeled[border_mask])) - {0}

    cleared = mask.copy()
    for lbl in border_labels:
        cleared[labeled == lbl] = False
    return cleared


# ===================== 结节候选检测 =====================

def detect_nodule_candidates(slice_hu: np.ndarray,
                              lung_mask: np.ndarray,
                              soft_min: float = -100,
                              soft_max: float = 200,
                              min_size: int = 80,
                              max_size: int = 8000,
                              spacing_xy: float = 1.0) -> List[Dict]:
    """
    在肺实质内检测结节候选区域
    结节CT值范围：
      - 实性结节: 30 ~ 200 HU (软组织密度)
      - 磨玻璃结节(GGO): -750 ~ -100 HU
      - 钙化: > 200 HU

    综合检测策略：
      1. 在肺实质范围内搜索高于阈值的区域
      2. 连通域分析 + 形态学过滤
      3. 计算每个候选的HU统计特征
    """
    candidates = []

    # 策略1: 实性/软组织结节。保留偏高密度实性结节，后续用形态过滤血管。
    solid_mask = (slice_hu > max(soft_min, -90)) & (slice_hu < 340) & lung_mask
    solid_mask = binary_closing(solid_mask, iterations=1)
    solid_mask = remove_small_objects(solid_mask, min_size=max(8, min_size // 4))
    # 限制最大面积，过滤掉大血管/心脏
    solid_mask = _remove_large_objects(solid_mask, max_size=min(max_size, 1800))

    if solid_mask.any():
        solid_cands = _extract_candidates(slice_hu, solid_mask, spacing_xy, "solid")
        candidates.extend(solid_cands)

    # 策略1a: 亮结节局部增强。检测相对肺背景突出的实性结节，减少固定阈值漏检。
    smooth_small = ndimage.gaussian_filter(slice_hu, sigma=0.8)
    smooth_large = ndimage.gaussian_filter(slice_hu, sigma=3.0)
    local_contrast = smooth_small - smooth_large
    blob_mask = (slice_hu > -140) & (slice_hu < 420) & (local_contrast > 28) & lung_mask
    blob_mask = remove_small_objects(blob_mask, min_size=5)
    blob_mask = _remove_large_objects(blob_mask, max_size=900)
    if blob_mask.any():
        blob_cands = _extract_candidates(slice_hu, blob_mask, spacing_xy, "solid")
        candidates.extend(blob_cands)

    # 策略1b: 距离变换圆心种子。圆形亮结节即使贴着血管，也从结节中心向外分割。
    seed_cands = _extract_round_bright_seed_candidates(slice_hu, lung_mask, spacing_xy)
    candidates.extend(seed_cands)

    # 策略1b: 微小实性/钙化样结节。不开运算，避免 2-4mm 小圆点被腐蚀掉。
    micro_mask = (slice_hu > -100) & (slice_hu < 420) & lung_mask
    micro_mask = remove_small_objects(micro_mask, min_size=5)
    micro_mask = _remove_large_objects(micro_mask, max_size=180)
    if micro_mask.any():
        micro_cands = _extract_candidates(slice_hu, micro_mask, spacing_xy, "solid")
        candidates.extend(micro_cands)

    # 策略2: 磨玻璃结节。原 -750~-100 会把大量正常肺纹理纳入，先收窄再靠形态筛选。
    ggo_mask = (slice_hu > -560) & (slice_hu < -210) & lung_mask
    ggo_mask = binary_opening(ggo_mask, iterations=1)
    ggo_mask = binary_closing(ggo_mask, iterations=1)
    ggo_mask = remove_small_objects(ggo_mask, min_size=max(24, min_size // 2))
    ggo_mask = _remove_large_objects(ggo_mask, max_size=min(max_size, 1600))

    if ggo_mask.any():
        ggo_cands = _extract_candidates(slice_hu, ggo_mask, spacing_xy, "ggo")
        candidates.extend(ggo_cands)

    # 策略3: 钙化结节 (> 200 HU)
    calc_mask = (slice_hu > 200) & (slice_hu < 1000) & lung_mask
    calc_mask = remove_small_objects(calc_mask, min_size=5)
    calc_mask = _remove_large_objects(calc_mask, max_size=min(max_size, 1200))

    if calc_mask.any():
        calc_cands = _extract_candidates(slice_hu, calc_mask, spacing_xy, "calcified")
        candidates.extend(calc_cands)

    # 去重 - 合并重叠的候选
    candidates = _merge_overlapping_candidates(candidates)

    return candidates


def _extract_round_bright_seed_candidates(slice_hu: np.ndarray,
                                          lung_mask: np.ndarray,
                                          spacing_xy: float,
                                          max_seeds: int = 80) -> List[Dict]:
    bright = (slice_hu > -180) & (slice_hu < 520) & lung_mask
    bright = binary_closing(bright, iterations=1)
    bright = remove_small_objects(bright, min_size=4)
    bright = _remove_large_objects(bright, max_size=1600)
    if not bright.any():
        return []

    dist = ndimage.distance_transform_edt(bright)
    smooth_small = ndimage.gaussian_filter(slice_hu, sigma=0.8)
    smooth_large = ndimage.gaussian_filter(slice_hu, sigma=4.0)
    contrast = smooth_small - smooth_large
    dist_peak = (dist == ndimage.maximum_filter(dist, size=5)) & bright & (dist >= 1.15)
    contrast_peak = (contrast == ndimage.maximum_filter(contrast, size=5)) & bright & (contrast > 14)
    seed_points = np.argwhere(dist_peak | contrast_peak)
    if seed_points.size == 0:
        return []

    ranked = sorted(
        [(int(y), int(x), float(dist[y, x]), float(contrast[y, x]), float(slice_hu[y, x])) for y, x in seed_points],
        key=lambda item: (item[2] * 18.0 + item[3] + item[4] * 0.03),
        reverse=True
    )

    seed_mask = np.zeros_like(slice_hu, dtype=bool)
    accepted: List[Tuple[int, int]] = []
    for y, x, radius, _, _ in ranked:
        if len(accepted) >= max_seeds:
            break
        if any((y - ay) ** 2 + (x - ax) ** 2 < 20 for ay, ax in accepted):
            continue
        accepted.append((y, x))
        r = int(max(1, min(3, round(radius))))
        y1, y2 = max(0, y - r), min(slice_hu.shape[0], y + r + 1)
        x1, x2 = max(0, x - r), min(slice_hu.shape[1], x + r + 1)
        seed_mask[y1:y2, x1:x2] |= bright[y1:y2, x1:x2]

    return _extract_candidates(slice_hu, seed_mask, spacing_xy, "solid") if seed_mask.any() else []


def _remove_large_objects(mask: np.ndarray, max_size: int) -> np.ndarray:
    """移除面积过大的连通域"""
    labeled = label(mask)
    props = regionprops(labeled)
    for p in props:
        if p.area > max_size:
            mask[labeled == p.label] = False
    return mask


def _keep_best_seed_component(mask: np.ndarray, seed_mask: np.ndarray, seed_y: int, seed_x: int) -> np.ndarray:
    if mask is None or not mask.any():
        return seed_mask.astype(bool)
    labeled = label(mask)
    props = regionprops(labeled)
    if not props:
        return seed_mask.astype(bool)

    best_label = None
    best_score = -1.0
    for p in props:
        component = labeled == p.label
        overlap = int(np.sum(component & seed_mask))
        cy, cx = p.centroid
        dist = float(np.hypot(cy - seed_y, cx - seed_x))
        score = overlap * 20.0 + float(p.area) - dist
        if score > best_score:
            best_score = score
            best_label = p.label

    if best_label is None:
        return seed_mask.astype(bool)
    refined = labeled == best_label
    return refined if refined.any() else seed_mask.astype(bool)


def _component_shape_score(prop) -> float:
    perimeter = float(prop.perimeter or 0)
    circularity = 4 * np.pi * prop.area / (perimeter * perimeter) if perimeter > 0 else 0.0
    aspect = 99.0
    minr, minc, maxr, maxc = prop.bbox
    h = max(1, maxr - minr)
    w = max(1, maxc - minc)
    aspect = max(h, w) / max(1, min(h, w))
    solidity = float(getattr(prop, "solidity", 0) or 0)
    return float(circularity) * 18.0 + solidity * 10.0 - max(0.0, aspect - 1.8) * 8.0


def _select_local_lesion_component(mask: np.ndarray,
                                   seed_mask: np.ndarray,
                                   center_y: float,
                                   center_x: float,
                                   max_area: int) -> np.ndarray:
    """Select the local component that best represents the whole nodule, not just an edge seed."""
    if mask is None or not mask.any():
        return seed_mask.astype(bool)

    labeled = label(mask)
    props = regionprops(labeled)
    if not props:
        return seed_mask.astype(bool)

    best_label = None
    best_score = -1e9
    seed_area = max(1, int(np.sum(seed_mask)))
    max_area = max(seed_area * 3, int(max_area))

    for p in props:
        if p.area < 2 or p.area > max_area:
            continue
        component = labeled == p.label
        overlap = int(np.sum(component & seed_mask))
        cy, cx = p.centroid
        dist = float(np.hypot(cy - center_y, cx - center_x))
        score = overlap * 60.0 + min(float(p.area), seed_area * 10.0) * 0.35 - dist * 2.2 + _component_shape_score(p)
        if score > best_score:
            best_score = score
            best_label = p.label

    if best_label is None:
        return seed_mask.astype(bool)
    refined = labeled == best_label
    return refined if refined.any() else seed_mask.astype(bool)


def _ellipse_prior_mask(shape: Tuple[int, int],
                        center_y: float,
                        center_x: float,
                        radius_y: float,
                        radius_x: float) -> np.ndarray:
    if shape[0] <= 0 or shape[1] <= 0:
        return np.zeros(shape, dtype=bool)
    yy, xx = np.ogrid[:shape[0], :shape[1]]
    ry = max(1.4, float(radius_y))
    rx = max(1.4, float(radius_x))
    return (((yy - center_y) / ry) ** 2 + ((xx - center_x) / rx) ** 2) <= 1.0


def _region_grow_nodule_mask(crop_hu: np.ndarray,
                             seed_mask: np.ndarray,
                             nodule_type: str) -> np.ndarray:
    """Grow from the candidate seed until the HU profile reaches normal lung/background."""
    if crop_hu.size == 0 or seed_mask is None or not seed_mask.any():
        return seed_mask.astype(bool)

    seed_values = crop_hu[seed_mask]
    seed_mean = float(np.mean(seed_values))
    seed_std = float(np.std(seed_values))
    lung_bg = crop_hu[crop_hu < -320]
    bg_p95 = float(np.percentile(lung_bg, 95)) if lung_bg.size else -420.0

    if nodule_type == "ggo":
        lower = max(-760.0, bg_p95 + 45.0, seed_mean - max(90.0, seed_std * 1.8 + 45.0))
        upper = min(-90.0, seed_mean + max(95.0, seed_std * 1.8 + 60.0))
    elif nodule_type == "calcified":
        lower = max(120.0, seed_mean - max(140.0, seed_std * 2.0 + 70.0))
        upper = min(1300.0, seed_mean + max(220.0, seed_std * 2.0 + 100.0))
    else:
        lower = max(-260.0, bg_p95 + 110.0, seed_mean - max(130.0, seed_std * 2.0 + 65.0))
        upper = min(360.0, seed_mean + max(150.0, seed_std * 2.0 + 80.0))

    eligible = (crop_hu >= lower) & (crop_hu <= upper)
    grown = seed_mask.astype(bool)
    for _ in range(10):
        nxt = binary_dilation(grown, iterations=1) & eligible
        if int(np.sum(nxt)) == int(np.sum(grown)):
            break
        grown = nxt

    grown = binary_closing(grown, iterations=1)
    grown = binary_fill_holes(grown)
    return grown.astype(bool)


def _refine_candidate_mask(slice_hu: np.ndarray,
                           component_mask: np.ndarray,
                           bbox: Tuple[int, int, int, int],
                           centroid: Tuple[float, float],
                           nodule_type: str) -> Tuple[np.ndarray, int, int]:
    """Refine a rough threshold component into a local connected nodule mask."""
    y1, x1, y2, x2 = bbox
    h, w = slice_hu.shape
    seed_h = max(1, y2 - y1)
    seed_w = max(1, x2 - x1)
    pad = max(8, int(round(max(seed_h, seed_w) * (1.10 if nodule_type != "ggo" else 0.85))))
    cy, cx = centroid

    crop_y1 = max(0, y1 - pad)
    crop_x1 = max(0, x1 - pad)
    crop_y2 = min(h, y2 + pad)
    crop_x2 = min(w, x2 + pad)
    if crop_y2 - crop_y1 < 1 or crop_x2 - crop_x1 < 1:
        return component_mask.astype(bool), y1, x1

    crop_hu = slice_hu[crop_y1:crop_y2, crop_x1:crop_x2]
    seed_mask = np.zeros(crop_hu.shape, dtype=bool)
    seed_y1 = y1 - crop_y1
    seed_x1 = x1 - crop_x1
    seed_mask[seed_y1:seed_y1 + component_mask.shape[0], seed_x1:seed_x1 + component_mask.shape[1]] = component_mask.astype(bool)
    if not seed_mask.any():
        return component_mask.astype(bool), y1, x1

    seed_area = max(1, int(np.sum(seed_mask)))
    equivalent_radius = max(1.8, np.sqrt(seed_area / np.pi))
    local_cy = float(cy - crop_y1)
    local_cx = float(cx - crop_x1)
    ellipse_mask = _ellipse_prior_mask(
        crop_hu.shape,
        local_cy,
        local_cx,
        max(equivalent_radius * 1.75, seed_h * 0.85),
        max(equivalent_radius * 1.75, seed_w * 0.85)
    )
    local_window = binary_dilation(seed_mask | ellipse_mask, iterations=8 if nodule_type != "ggo" else 6)

    if nodule_type in ("solid", "calcified"):
        lung_bg = crop_hu[crop_hu < -320]
        bg_p95 = float(np.percentile(lung_bg, 95)) if lung_bg.size else -420.0
        seed_values = crop_hu[seed_mask]
        seed_p35 = float(np.percentile(seed_values, 35)) if seed_values.size else -80.0
        lower = max(-180.0, bg_p95 + 95.0, seed_p35 - 130.0)
        upper = 1350.0 if nodule_type == "calcified" else 460.0
        bright_body = (crop_hu >= lower) & (crop_hu <= upper) & local_window
        bright_body = binary_closing(bright_body, iterations=1)
        bright_body = binary_fill_holes(bright_body)
        grown = _select_local_lesion_component(
            bright_body,
            seed_mask,
            local_cy,
            local_cx,
            max_area=max(80, int(seed_area * 18))
        )
    else:
        grown = _region_grow_nodule_mask(crop_hu, seed_mask, nodule_type) & local_window

    refined = grown | seed_mask
    refined = binary_closing(refined, iterations=1)
    refined = binary_fill_holes(refined)
    refined = _keep_best_seed_component(refined, seed_mask, int(round(cy - crop_y1)), int(round(cx - crop_x1)))

    min_area = max(4, int(seed_area * 0.85))
    if np.sum(refined) < min_area:
        refined = binary_dilation(seed_mask, iterations=1)
        refined = binary_closing(refined, iterations=1)
        refined = binary_fill_holes(refined)
        refined = _keep_best_seed_component(refined, seed_mask, int(round(cy - crop_y1)), int(round(cx - crop_x1)))
    if np.sum(refined) < max(3, int(seed_area * 0.55)):
        refined = seed_mask
    return refined.astype(bool), crop_y1, crop_x1


def _extract_candidates(slice_hu: np.ndarray,
                         mask: np.ndarray,
                         spacing_xy: float,
                         nodule_type: str) -> List[Dict]:
    """提取候选区域并计算特征"""
    candidates = []
    labeled = label(mask)
    props = regionprops(labeled, intensity_image=slice_hu)

    for p in props:
        # 尺寸过滤：入口保留 1.5mm 以上小结节，后续再用形态过滤假阳性。
        area_mm2 = p.area * spacing_xy * spacing_xy
        diameter_mm = 2 * np.sqrt(area_mm2 / np.pi)
        if diameter_mm < 1.5 or diameter_mm > 35.0:
            continue

        refined_mask, offset_y, offset_x = _refine_candidate_mask(
            slice_hu,
            p.image,
            p.bbox,
            p.centroid,
            nodule_type
        )
        refined_props = regionprops(
            label(refined_mask),
            intensity_image=slice_hu[offset_y:offset_y + refined_mask.shape[0], offset_x:offset_x + refined_mask.shape[1]]
        )
        if not refined_props:
            continue
        rp = max(refined_props, key=lambda item: item.area)

        area_mm2 = rp.area * spacing_xy * spacing_xy
        diameter_mm = 2 * np.sqrt(area_mm2 / np.pi)
        if diameter_mm < 1.5 or diameter_mm > 35.0:
            continue

        # 计算HU统计
        hu_vals = rp.intensity_image[rp.image]
        hu_mean = float(np.mean(hu_vals))
        hu_std = float(np.std(hu_vals))
        hu_min = float(np.min(hu_vals))
        hu_max = float(np.max(hu_vals))

        # 计算形态特征
        perimeter = rp.perimeter * spacing_xy
        circularity = 4 * np.pi * rp.area / (rp.perimeter * rp.perimeter) if rp.perimeter > 0 else 0

        # 离心率
        eccentricity = rp.eccentricity if hasattr(rp, 'eccentricity') else 0
        local_y1, local_x1, local_y2, local_x2 = rp.bbox
        y1, x1 = offset_y + local_y1, offset_x + local_x1
        y2, x2 = offset_y + local_y2, offset_x + local_x2
        bbox_h = max(1, y2 - y1)
        bbox_w = max(1, x2 - x1)
        aspect_ratio = max(bbox_h, bbox_w) / max(1, min(bbox_h, bbox_w))
        extent = float(getattr(rp, "extent", 0) or 0)
        contour_points = _compact_contour(rp.image, y1, x1)
        density_class = _density_class(nodule_type, hu_mean)
        size_class = _size_class(diameter_mm)
        morphology_class = _morphology_class(circularity, eccentricity)

        cand = {
            "centroid_y": float(offset_y + rp.centroid[0]),
            "centroid_x": float(offset_x + rp.centroid[1]),
            "bbox_y1": float(y1),
            "bbox_x1": float(x1),
            "bbox_y2": float(y2),
            "bbox_x2": float(x2),
            "bbox": [float(y1), float(x1), float(y2), float(x2)],
            "area_pixels": int(rp.area),
            "area_mm2": round(area_mm2, 2),
            "diameter_mm": round(diameter_mm, 2),
            "perimeter_mm": round(perimeter, 1),
            "circularity": round(circularity, 3),
            "eccentricity": round(eccentricity, 3),
            "solidity": round(float(getattr(rp, "solidity", 0) or 0), 3),
            "extent": round(extent, 3),
            "aspectRatio": round(float(aspect_ratio), 3),
            "bboxHeight": int(bbox_h),
            "bboxWidth": int(bbox_w),
            "hu_mean": round(hu_mean, 1),
            "hu_std": round(hu_std, 1),
            "hu_min": round(hu_min, 1),
            "hu_max": round(hu_max, 1),
            "type": nodule_type,
            "densityClass": density_class,
            "sizeClass": size_class,
            "morphologyClass": morphology_class,
            "classification": f"{size_class} / {density_class} / {morphology_class}",
            "segmentation": {
                "algorithm": "局部HU自适应阈值 + 种子连通域 + 形态学细化",
                "maskAreaPixels": int(rp.area),
                "maskAreaMM2": round(area_mm2, 2),
                "contour": contour_points,
                "contourPointCount": len(contour_points),
                "bbox": [float(y1), float(x1), float(y2), float(x2)]
            }
        }
        cand["segmentation"]["displayContour"] = _display_segmentation_contour(cand)
        cand["segmentation"]["displayContourPointCount"] = len(cand["segmentation"]["displayContour"])
        candidates.append(cand)

    return candidates


def _merge_overlapping_candidates(candidates: List[Dict],
                                   iou_threshold: float = 0.3) -> List[Dict]:
    """合并重叠的候选区域（IoU > threshold 则保留较大的）"""
    if len(candidates) <= 1:
        return candidates

    # 简单去重：如果两个候选中心距离小于较大直径的50%，保留面积大的
    merged = []
    used = set()

    for i in range(len(candidates)):
        if i in used:
            continue
        best = candidates[i]
        best_score = _candidate_quality_score(best)
        for j in range(i + 1, len(candidates)):
            if j in used:
                continue
            ci = candidates[i]
            cj = candidates[j]
            dy = ci['centroid_y'] - cj['centroid_y']
            dx = ci['centroid_x'] - cj['centroid_x']
            dist = np.sqrt(dy * dy + dx * dx)
            max_diam = max(ci['diameter_mm'], cj['diameter_mm']) * 0.5 / max(
                max(ci.get('pixel_spacing', 1), 1), 0.3)
            if dist < max_diam:
                candidate_score = _candidate_quality_score(cj)
                if candidate_score > best_score:
                    best = cj
                    best_score = candidate_score
                used.add(j)
        merged.append(best)

    return merged


def estimate_malignancy_features(candidate: Dict) -> Dict:
    """
    基于结节大小、密度、形态的演示性良恶性风险评分。
    这是模拟教学逻辑，不用于真实临床诊断。
    """
    diameter = float(candidate.get("diameter_mm", 0) or 0)
    nodule_type = candidate.get("type", "solid")
    circularity = float(candidate.get("circularity", 0) or 0)
    eccentricity = float(candidate.get("eccentricity", 0) or 0)
    hu_std = float(candidate.get("hu_std", 0) or 0)
    hu_mean = float(candidate.get("hu_mean", 0) or 0)

    score = 0.06
    if diameter >= 8:
        score += 0.20
    elif diameter >= 5:
        score += 0.10
    elif diameter >= 3:
        score += 0.04

    if nodule_type == "ggo":
        score += 0.10
    elif nodule_type == "solid":
        score += 0.06
    elif nodule_type == "calcified":
        score -= 0.22

    if circularity < 0.45:
        score += 0.14
    if eccentricity > 0.78:
        score += 0.10
    if hu_std > 55:
        score += 0.08
    if hu_mean > 120 and nodule_type != "calcified":
        score += 0.05

    probability = round(_clamp(score, 0.02, 0.88), 2)
    if probability >= 0.75:
        risk = "高风险"
        label = "高度疑似恶性"
        advice = "建议薄层增强CT/胸外科会诊，必要时PET-CT或穿刺病理。"
    elif probability >= 0.45:
        risk = "中风险"
        label = "恶性可能待排"
        advice = "建议3个月复查低剂量薄层CT，结合临床和既往影像对比。"
    else:
        risk = "低风险"
        label = "倾向良性"
        advice = "建议常规随访，钙化灶多考虑良性陈旧改变。"

    candidate["malignancyProbability"] = probability
    candidate["malignancyRisk"] = risk
    candidate["malignancyLabel"] = label
    candidate["cancerSuspicion"] = probability >= 0.45
    candidate["riskAdvice"] = advice
    return candidate


def enrich_malignancy(candidates: List[Dict]) -> List[Dict]:
    return [estimate_malignancy_features(c) for c in candidates]


def apply_autodl_config(result: Dict, params: Dict) -> Dict:
    """预留AutoDL模型接口配置；未启用时完整使用本地HU流程。"""
    endpoint = params.get("autodl_endpoint") or params.get("autodlUrl")
    enable_autodl = _is_truthy(params.get("enable_autodl"), default=False)
    autodl = result.setdefault("pipeline", {}).setdefault("autodl", {})
    autodl["enabled"] = bool(enable_autodl and endpoint)
    autodl["endpoint"] = endpoint
    if autodl["enabled"]:
        autodl["status"] = "configured_not_called"
        autodl["message"] = "AutoDL接口已配置；当前仍使用本地HU+形态学结果，模型服务完成后可在此处替换/融合推理结果。"
    return result


# ===================== 图像生成 =====================

def hu_to_grayscale(slice_hu: np.ndarray,
                     ww: float = 1600, wl: float = -600) -> np.ndarray:
    """HU值转灰度图像（肺窗：WL=-600, WW=1600）"""
    low = wl - ww / 2
    high = wl + ww / 2
    normalized = np.clip((slice_hu - low) / (high - low), 0, 1)
    gray = (normalized * 255).astype(np.uint8)
    return gray


def draw_segmentation_overlay(gray_img: np.ndarray,
                               lung_contour: Optional[List] = None,
                               candidates: Optional[List[Dict]] = None,
                               nodule_contours: Optional[List[np.ndarray]] = None
                               ) -> str:
    """
    在灰度CT图像上绘制分割标注，返回Base64 PNG
    颜色：
      绿色 #00FF00: 肺实质轮廓
      红色 #FF4444: 实性结节/钙化
      橙色 #FF8C00: 磨玻璃结节
      黄色 #FFFF00: 结节边界框
    """
    rgb = np.stack([gray_img, gray_img, gray_img], axis=-1)

    # 转PIL绘制
    pil_img = Image.fromarray(rgb)
    draw = ImageDraw.Draw(pil_img)

    # 绘制肺实质轮廓
    if lung_contour is not None:
        for contour in lung_contour:
            if len(contour) >= 2:
                pts = [(int(p[1]), int(p[0])) for p in contour]
                draw.line(pts, fill=(0, 255, 128), width=2)

    # 绘制结节候选
    if candidates:
        for cand in candidates:
            x1, y1 = int(cand['bbox_x1']), int(cand['bbox_y1'])
            x2, y2 = int(cand['bbox_x2']), int(cand['bbox_y2'])

            # 根据类型选颜色
            if cand.get('type') == 'solid':
                color = (255, 68, 68)      # 红色
            elif cand.get('type') == 'ggo':
                color = (255, 140, 0)      # 橙色
            elif cand.get('type') == 'calcified':
                color = (255, 200, 50)      # 金色
            else:
                color = (255, 255, 0)

            # 绘制边界框
            draw.rectangle([x1, y1, x2, y2], outline=color, width=2)

            # 绘制中心点十字
            cx, cy = int(cand['centroid_x']), int(cand['centroid_y'])
            draw.line([(cx - 5, cy), (cx + 5, cy)], fill=color, width=1)
            draw.line([(cx, cy - 5), (cx, cy + 5)], fill=color, width=1)

            # 标签
            label = f"{cand.get('type','?')} {cand['diameter_mm']}mm"
            # PIL没有好的文字方法，用矩形代替标签背景
            label_w = len(label) * 7
            draw.rectangle([x1, y1 - 16, x1 + label_w, y1], fill=color)
            # 简单标注（位置受限时用短标签）
            draw.text((x1 + 2, y1 - 15), label[:8], fill=(255, 255, 255))

    # 绘制结节掩码轮廓
    if nodule_contours:
        for contour in nodule_contours:
            if len(contour) >= 2:
                pts = [(int(p[1]), int(p[0])) for p in contour]
                draw.line(pts, fill=(255, 255, 0), width=1)

    # 转Base64
    buf = io.BytesIO()
    pil_img.save(buf, format='PNG')
    buf.seek(0)
    return base64.b64encode(buf.read()).decode('utf-8')


def draw_stage_overlay(gray_img: np.ndarray,
                       candidates: Optional[List[Dict]] = None,
                       stage: str = "detection") -> str:
    """
    按流程阶段生成独立标注图：
      detection: 候选结节检测与分类框
      segmentation: 红色半透明结节分割掩码
      malignancy: 良恶性风险概率标注
    """
    rgb = np.stack([gray_img, gray_img, gray_img], axis=-1)
    pil_img = Image.fromarray(rgb).convert("RGBA")
    overlay = Image.new("RGBA", pil_img.size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(overlay)

    for cand in candidates or []:
        x1, y1 = int(cand['bbox_x1']), int(cand['bbox_y1'])
        x2, y2 = int(cand['bbox_x2']), int(cand['bbox_y2'])
        cx, cy = int(cand['centroid_x']), int(cand['centroid_y'])
        segmentation = cand.get("segmentation") or {}
        contour = segmentation.get("displayContour") or segmentation.get("contour") or []

        if stage == "detection":
            color = (34, 197, 94, 255)
            if cand.get("type") == "ggo":
                color = (245, 158, 11, 255)
            elif cand.get("type") == "calcified":
                color = (14, 165, 233, 255)
            draw.rectangle([x1, y1, x2, y2], outline=color, width=2)
            draw.ellipse([cx - 3, cy - 3, cx + 3, cy + 3], outline=color, width=2)
            label_text = f"{cand.get('type', 'nodule')} {cand.get('diameter_mm', 0)}mm"
            draw.rectangle([x1, max(0, y1 - 18), min(pil_img.width, x1 + len(label_text) * 7), y1], fill=color)
            draw.text((x1 + 2, max(0, y1 - 17)), label_text[:28], fill=(255, 255, 255, 255))

        elif stage == "segmentation":
            if len(contour) >= 3:
                pts = [(int(p[1]), int(p[0])) for p in contour]
                draw.polygon(pts, fill=(239, 68, 68, 96))
                draw.line(pts + [pts[0]], fill=(239, 68, 68, 255), width=2)
            else:
                draw.rectangle([x1, y1, x2, y2], outline=(239, 68, 68, 255), width=2)

        elif stage == "malignancy":
            risk = cand.get("malignancyRisk", "低风险")
            probability = int(round(float(cand.get("malignancyProbability", 0) or 0) * 100))
            if risk == "高风险":
                color = (220, 38, 38, 255)
                risk_code = "HIGH"
            elif risk == "中风险":
                color = (245, 158, 11, 255)
                risk_code = "MID"
            else:
                color = (34, 197, 94, 255)
                risk_code = "LOW"
            draw.rectangle([x1, y1, x2, y2], outline=color, width=3)
            label_text = f"{risk_code} {probability}%"
            draw.rectangle([x1, max(0, y1 - 20), min(pil_img.width, x1 + len(label_text) * 9), y1], fill=color)
            draw.text((x1 + 2, max(0, y1 - 18)), label_text, fill=(255, 255, 255, 255))

    out = Image.alpha_composite(pil_img, overlay).convert("RGB")
    buf = io.BytesIO()
    out.save(buf, format='PNG')
    buf.seek(0)
    return base64.b64encode(buf.read()).decode('utf-8')


# ===================== 主分析函数 =====================

def analyze_lung_ct(volume: np.ndarray,
                     metadata: dict,
                     max_slices: int = 180,
                     lightweight: bool = True,
                     max_candidates: int = 600) -> Dict:
    """
    全流程：肺实质提取 → 结节检测 → 生成标注切片
    """
    total_slices_orig = volume.shape[0]
    spacing = metadata.get('spacing', (1.0, 1.0, 1.0))
    slice_thickness = float(spacing[0]) if len(spacing) > 0 else 1.0
    pixel_spacing_x = float(spacing[2]) if len(spacing) > 2 else float(spacing[1]) if len(spacing) > 1 else 1.0
    pixel_spacing_y = float(spacing[1]) if len(spacing) > 1 else 1.0

    # 采样策略：均匀采样
    if total_slices_orig > max_slices:
        step = total_slices_orig / max_slices
        slice_indices = [int(i * step) for i in range(max_slices)]
        slice_indices = sorted(set(slice_indices))
    else:
        slice_indices = list(range(total_slices_orig))

    total_slices = len(slice_indices)

    # 结果
    slice_images = []      # 主浏览器原始切片Base64
    raw_slices = []       # 原始切片Base64
    annotated_slices = [] # 标注切片Base64
    slice_hu_stats = []   # HU统计
    slice_candidate_summary = []
    raw_candidate_count = 0
    all_candidates = []   # 筛选后的可信结节
    lung_areas_mm2 = []   # 肺面积

    for scan_idx, z in enumerate(slice_indices):
        slice_hu = volume[z, :, :].copy()

        hu_min = float(np.min(slice_hu))
        hu_max = float(np.max(slice_hu))
        hu_mean = float(np.mean(slice_hu))

        # 转换灰度图（肺窗）
        gray = hu_to_grayscale(slice_hu, ww=1600, wl=-600)
        raw_b64 = _encode_png_base64(gray)
        slice_images.append(raw_b64)

        # 提取肺实质
        lung_mask = extract_lung_mask(slice_hu, lung_threshold=-400)
        lung_area_px = int(np.sum(lung_mask))

        # 检测结节候选
        candidates = []
        if lung_mask.any():
            candidates = detect_nodule_candidates(
                slice_hu, lung_mask,
                soft_min=-100, soft_max=200,
                min_size=45, max_size=2600,
                spacing_xy=pixel_spacing_x
            )

        # 标注该切片的结节，附上切片信息
        for cand in candidates:
            cand['slice_index'] = scan_idx
            cand['slice_z'] = int(z)
            cand['pixel_spacing'] = pixel_spacing_x
            estimate_malignancy_features(cand)
        raw_count_this_slice = len(candidates)
        raw_candidate_count += raw_count_this_slice
        candidates = _limit_candidates_per_slice(_filter_true_nodule_candidates(candidates), max_per_slice=24)
        all_candidates.extend(candidates)
        slice_candidate_summary.append({
            "slice_index": scan_idx,
            "slice_z": int(z),
            "rawCandidateCount": raw_count_this_slice,
            "trueNoduleCount": len(candidates),
            "returnedCandidateCount": 0
        })

        if not lightweight:
            # 非轻量模式才返回整套切片，避免接口响应体膨胀。
            raw_slices.append(raw_b64)
            annotated_slices.append(draw_stage_overlay(gray, candidates=candidates, stage="segmentation"))

        slice_hu_stats.append({
            "slice_index": scan_idx,
            "slice_z": int(z),
            "hu_min": round(hu_min, 1),
            "hu_max": round(hu_max, 1),
            "hu_mean": round(hu_mean, 1),
            "lung_area_px": lung_area_px,
            "lung_area_mm2": round(lung_area_px * pixel_spacing_x * pixel_spacing_y, 1),
            "candidate_count": len(candidates),
            "raw_candidate_count": raw_count_this_slice
        })

        lung_areas_mm2.append(lung_area_px * pixel_spacing_x * pixel_spacing_y)

    # ===== 汇总统计 =====
    true_nodule_ratio_limit = 1.0
    high_risk_ratio_limit = 0.10
    pre_cap_true_nodule_count = len(all_candidates)
    all_candidates = _cap_true_nodule_ratio(all_candidates, raw_candidate_count, ratio=true_nodule_ratio_limit)
    all_candidates = enrich_malignancy(all_candidates)
    all_candidates = _cap_high_risk_ratio(all_candidates, ratio=high_risk_ratio_limit)
    total_nodules = len(all_candidates)
    max_true_nodule_count = _ratio_cap(raw_candidate_count, ratio=true_nodule_ratio_limit, min_keep=pre_cap_true_nodule_count)
    max_high_risk_count = _strict_under_ratio_cap(total_nodules, ratio=high_risk_ratio_limit)

    # 按结节类型分类
    solid_nodules = [c for c in all_candidates if c.get('type') == 'solid']
    ggo_nodules = [c for c in all_candidates if c.get('type') == 'ggo']
    calc_nodules = [c for c in all_candidates if c.get('type') == 'calcified']

    # 按HU均值分组
    low_density = [c for c in all_candidates if c['hu_mean'] < 30]
    mid_density = [c for c in all_candidates if 30 <= c['hu_mean'] <= 100]
    high_density = [c for c in all_candidates if c['hu_mean'] > 100]

    # 按大小分组
    small = [c for c in all_candidates if c['diameter_mm'] <= 5]
    medium = [c for c in all_candidates if 5 < c['diameter_mm'] <= 10]
    large = [c for c in all_candidates if c['diameter_mm'] > 10]
    high_risk = [c for c in all_candidates if c.get('malignancyRisk') == '高风险']
    medium_risk = [c for c in all_candidates if c.get('malignancyRisk') == '中风险']
    low_risk = [c for c in all_candidates if c.get('malignancyRisk') == '低风险']

    top_risk = sorted(all_candidates, key=lambda c: c.get("malignancyProbability", 0), reverse=True)[:5]
    output_slice_index, output_slice_z = _select_preview_slice(all_candidates, slice_indices, total_slices)
    output_gray = hu_to_grayscale(volume[output_slice_z, :, :].copy(), ww=1600, wl=-600)
    output_candidates = [c for c in all_candidates if c.get("slice_index") == output_slice_index]
    preview_image = _encode_png_base64(output_gray)
    stage_outputs = {
        "sliceIndex": output_slice_index,
        "sliceZ": output_slice_z,
        "candidateCount": len(output_candidates),
        "detectionImage": draw_stage_overlay(output_gray, candidates=output_candidates, stage="detection"),
        "segmentationImage": draw_stage_overlay(output_gray, candidates=output_candidates, stage="segmentation"),
        "malignancyImage": draw_stage_overlay(output_gray, candidates=output_candidates, stage="malignancy")
    }

    display_candidates = _compact_candidate_list(
        all_candidates,
        max_candidates=max(1, int(max_candidates or 80)),
        keep_slice_index=output_slice_index
    )

    # 生成分割掩码数据：轻量模式只保留返回候选对应的压缩轮廓。
    source_for_payload = display_candidates if lightweight else [_compact_candidate(c) for c in all_candidates]
    returned_by_slice: Dict[int, int] = {}
    for cand in source_for_payload:
        idx = int(cand.get("slice_index", 0) or 0)
        returned_by_slice[idx] = returned_by_slice.get(idx, 0) + 1
    for item in slice_candidate_summary:
        item["returnedCandidateCount"] = returned_by_slice.get(int(item["slice_index"]), 0)
    segmentation_data = {}
    for cand in source_for_payload:
        seg_key = f"z{int(cand.get('slice_z') or 0)}"
        segmentation_data.setdefault(seg_key, []).append({
            "centroid": [cand.get('centroid_y'), cand.get('centroid_x')],
            "diameter_mm": cand.get('diameter_mm'),
            "bbox": [cand.get('bbox_y1'), cand.get('bbox_x1'), cand.get('bbox_y2'), cand.get('bbox_x2')],
            "type": cand.get('type'),
            "classification": cand.get("classification"),
            "segmentation": cand.get("segmentation"),
            "hu_mean": cand.get('hu_mean'),
            "malignancyProbability": cand.get('malignancyProbability'),
            "malignancyRisk": cand.get('malignancyRisk')
        })

    detection_items = [{
        "sliceIndex": c.get("slice_index"),
        "sliceZ": c.get("slice_z"),
        "type": c.get("type"),
        "classification": c.get("classification"),
        "sizeClass": c.get("sizeClass"),
        "densityClass": c.get("densityClass"),
        "morphologyClass": c.get("morphologyClass"),
        "diameterMm": c.get("diameter_mm"),
        "areaMm2": c.get("area_mm2"),
        "huMean": c.get("hu_mean"),
        "bbox": c.get("bbox")
    } for c in source_for_payload]
    segmentation_items = [{
        "sliceIndex": c.get("slice_index"),
        "sliceZ": c.get("slice_z"),
        "type": c.get("type"),
        "diameterMm": c.get("diameter_mm"),
        "areaMm2": c.get("area_mm2"),
        "contourPointCount": (c.get("segmentation") or {}).get("contourPointCount", 0),
        "segmentation": c.get("segmentation")
    } for c in source_for_payload]
    malignancy_items = [{
        "sliceIndex": c.get("slice_index"),
        "sliceZ": c.get("slice_z"),
        "type": c.get("type"),
        "diameterMm": c.get("diameter_mm"),
        "probability": c.get("malignancyProbability"),
        "risk": c.get("malignancyRisk"),
        "label": c.get("malignancyLabel"),
        "advice": c.get("riskAdvice"),
        "features": {
            "sizeClass": c.get("sizeClass"),
            "densityClass": c.get("densityClass"),
            "morphologyClass": c.get("morphologyClass"),
            "circularity": c.get("circularity"),
            "eccentricity": c.get("eccentricity"),
            "huMean": c.get("hu_mean"),
            "huStd": c.get("hu_std")
        }
    } for c in source_for_payload]
    overall_risk = "高风险" if high_risk else ("中风险" if medium_risk else ("低风险" if total_nodules else "未检出结节"))
    pipeline = {
        "noduleDetection": "completed",
        "classification": "completed",
        "segmentation": "completed" if total_nodules > 0 else "completed_no_target",
        "malignancyClassification": "completed" if total_nodules > 0 else "completed_no_target",
        "method": "local_hu_morphology",
        "autodl": {
            "enabled": False,
            "endpoint": None,
            "status": "reserved",
            "message": "已预留AutoDL模型接口；未配置时使用本地CT值与形态学全流程。"
        }
    }

    return {
        "success": True,
        "algorithmVersion": "HU-Morphology-Lung-Nodule-v2",
        "totalSlices": total_slices,
        "totalSlicesOrig": total_slices_orig,
        "sliceIndices": slice_indices,
        "spacing": list(spacing) if hasattr(spacing, '__iter__') else [slice_thickness, pixel_spacing_y, pixel_spacing_x],
        "sliceThickness": round(slice_thickness, 2),
        "pixelSpacingX": round(pixel_spacing_x, 3),
        "pixelSpacingY": round(pixel_spacing_y, 3),
        "imageShape": [int(volume.shape[1]), int(volume.shape[2])],
        "previewImage": preview_image,
        "previewSliceIndex": output_slice_index,
        "previewSliceZ": output_slice_z,
        "sliceImages": slice_images,
        "rawSlices": raw_slices if not lightweight else [],
        "slices": annotated_slices if not lightweight else [],
        "stageOutputs": stage_outputs,
        "sliceHuStats": slice_hu_stats,
        "sliceCandidateSummary": slice_candidate_summary,
        "stats": {
            "rawCandidateRegions": raw_candidate_count,
            "preCapTrueNodules": pre_cap_true_nodule_count,
            "trueNoduleRatioLimit": true_nodule_ratio_limit,
            "highRiskRatioLimit": high_risk_ratio_limit,
            "maxTrueNodulesAllowed": max_true_nodule_count,
            "maxHighRiskAllowed": max_high_risk_count,
            "totalNoduleCandidates": total_nodules,
            "solidNodules": len(solid_nodules),
            "ggoNodules": len(ggo_nodules),
            "calcifiedNodules": len(calc_nodules),
            "lowDensityCount": len(low_density),
            "midDensityCount": len(mid_density),
            "highDensityCount": len(high_density),
            "smallNodules(≤5mm)": len(small),
            "mediumNodules(5-10mm)": len(medium),
            "largeNodules(>10mm)": len(large),
            "highRiskNodules": len(high_risk),
            "mediumRiskNodules": len(medium_risk),
            "lowRiskNodules": len(low_risk),
            "trueNoduleRatioActual": _safe_ratio(total_nodules, raw_candidate_count),
            "highRiskRatioActual": _safe_ratio(len(high_risk), total_nodules),
            "maxMalignancyProbability": round(max([c.get('malignancyProbability', 0) for c in all_candidates], default=0), 2),
            "meanLungAreaMM2": round(np.mean(lung_areas_mm2), 1) if lung_areas_mm2 else 0,
            "maxLungAreaMM2": round(np.max(lung_areas_mm2), 1) if lung_areas_mm2 else 0
        },
        "candidates": display_candidates if lightweight else [_compact_candidate(c) for c in all_candidates],
        "segmentationData": segmentation_data,
        "detectionClassification": {
            "enabled": True,
            "method": "CT值阈值 + 肺实质掩码 + 连通域形态学分类",
            "model": "HU-Morphology-Detector; AutoDL YOLOv8/ResNet接口预留",
            "completed": True,
            "count": total_nodules,
            "rawCandidateCount": raw_candidate_count,
            "preCapTrueNoduleCount": pre_cap_true_nodule_count,
            "maxTrueNodulesAllowed": max_true_nodule_count,
            "items": detection_items
        },
        "segmentationAssessment": {
            "enabled": True,
            "method": "HU阈值 + 形态学过滤 + 连通域轮廓提取",
            "completed": True,
            "count": len(segmentation_items),
            "items": segmentation_items
        },
        "malignancyAssessment": {
            "enabled": True,
            "method": "CT值、直径、圆度、离心率、密度类型综合评分",
            "model": "HU-Morphology-Risk; AutoDL DenseNet/Transformer接口预留",
            "completed": True,
            "highRiskCount": len(high_risk),
            "maxHighRiskAllowed": max_high_risk_count,
            "highRiskRatioActual": _safe_ratio(len(high_risk), total_nodules),
            "mediumRiskCount": len(medium_risk),
            "lowRiskCount": len(low_risk),
            "overallRisk": overall_risk,
            "maxProbability": round(max([c.get('malignancyProbability', 0) for c in all_candidates], default=0), 2),
            "topRiskItems": [_compact_candidate(c) for c in top_risk],
            "items": malignancy_items,
            "conclusion": "发现高风险结节，建议进一步检查" if high_risk else ("存在中风险结节，建议随访复查" if medium_risk else "未见明确高危恶性征象")
        }
        ,
        "pipeline": pipeline
    }


# ===================== API路由 =====================

@app.route('/api/health', methods=['GET'])
def health():
    """健康检查"""
    return jsonify({
        "status": "ok",
        "service": "lung_ct_segment",
        "version": "1.0.0",
        "algorithm": "CT值阈值分割 + 形态学分析"
    })


@app.route('/api/analyze', methods=['POST'])
def analyze():
    """主分析接口 - 上传CT文件，返回切片+分割结果"""
    if 'file' not in request.files:
        return jsonify({"success": False, "error": "请上传CT文件"}), 400

    file = request.files['file']
    if file.filename == '':
        return jsonify({"success": False, "error": "文件名为空"}), 400

    # 解析参数
    try:
        params = json.loads(request.form.get('params', '{}'))
    except json.JSONDecodeError:
        params = {}

    max_slices = params.get('max_slices', 180)
    lightweight = _is_truthy(params.get('lightweight'), default=True)
    max_candidates = int(params.get('max_candidates', 600) or 600)

    # 保存临时文件
    filename_lower = file.filename.lower()
    ext = '.nii.gz' if filename_lower.endswith('.nii.gz') else Path(file.filename).suffix
    if not ext:
        ext = '.zip'
    tmp_path = UPLOAD_FOLDER / f"lung_{os.urandom(8).hex()}{ext}"

    try:
        file.save(str(tmp_path))

        volume, metadata = read_ct_volume(str(tmp_path))
        result = analyze_lung_ct(
            volume,
            metadata,
            max_slices=max_slices,
            lightweight=lightweight,
            max_candidates=max_candidates
        )
        result = apply_autodl_config(result, params)
        result["sourceFile"] = file.filename

        return jsonify(result)

    except Exception as e:
        traceback.print_exc()
        return jsonify({
            "success": False,
            "error": "真实肺部CT文件解析失败，请确认上传 DICOM序列zip、NIfTI(.nii/.nii.gz)、MHD 或 DCM 文件",
            "detail": str(e),
            "sourceFile": file.filename
        }), 400
    finally:
        # 清理临时文件
        if tmp_path.exists():
            try:
                tmp_path.unlink()
            except OSError:
                pass


@app.route('/api/analyze-slice', methods=['POST'])
def analyze_slice():
    """单层切片分析"""
    files = request.files.getlist('file')
    slice_idx = int(request.form.get('sliceIndex', 0))

    if not files:
        return jsonify({"success": False, "error": "请上传切片文件"}), 400

    try:
        images = []
        for f in files:
            img = Image.open(f.stream).convert('L')
            arr = np.array(img, dtype=np.float64)
            images.append(arr)

        if len(images) == 1:
            slice_hu = images[0]
        else:
            # 多文件视为DICOM切片
            reader = sitk.ImageSeriesReader()
            # 简化处理，实际应排序
            slice_hu = np.stack(images, axis=0)

        result = {
            "success": True,
            "sliceCount": len(images) if len(images) > 1 else 1,
            "sliceIndex": slice_idx
        }
        return jsonify(result)

    except Exception as e:
        traceback.print_exc()
        return jsonify({"success": False, "error": str(e)}), 500


# ===================== 启动入口 =====================

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='肺部CT结节分割服务')
    parser.add_argument('--port', type=int, default=5004, help='服务端口 (默认: 5004)')
    parser.add_argument('--host', type=str, default='0.0.0.0', help='监听地址 (默认: 0.0.0.0)')
    parser.add_argument('--debug', action='store_true', help='调试模式')
    args = parser.parse_args()

    print("=" * 60)
    print("  肺部CT结节分割服务 (CT值算法)")
    print("  API: http://{}:{}/api/analyze".format(args.host, args.port))
    print("  Health: http://{}:{}/api/health".format(args.host, args.port))
    print("=" * 60)

    app.run(host=args.host, port=args.port, debug=args.debug)
