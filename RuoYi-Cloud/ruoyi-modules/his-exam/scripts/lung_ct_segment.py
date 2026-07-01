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
    contours = find_contours(binary_mask.astype(float), 0.5)
    if not contours:
        return []
    contour = max(contours, key=len)
    step = max(1, int(np.ceil(len(contour) / max_points)))
    points = contour[::step]
    return [[round(float(p[0] + offset_y), 1), round(float(p[1] + offset_x), 1)] for p in points]


def _is_truthy(value, default: bool = False) -> bool:
    if value is None:
        return default
    if isinstance(value, bool):
        return value
    return str(value).strip().lower() in ("1", "true", "yes", "y", "on")


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

    # 策略1: 实性/软组织结节 (-100 ~ 200 HU)
    solid_mask = (slice_hu > soft_min) & (slice_hu < soft_max) & lung_mask
    solid_mask = remove_small_objects(solid_mask, min_size=min_size)
    # 限制最大面积，过滤掉大血管/心脏
    solid_mask = _remove_large_objects(solid_mask, max_size=max_size)

    if solid_mask.any():
        solid_cands = _extract_candidates(slice_hu, solid_mask, spacing_xy, "solid")
        candidates.extend(solid_cands)

    # 策略2: 磨玻璃结节 (-750 ~ -100 HU)
    ggo_mask = (slice_hu > -750) & (slice_hu < -100) & lung_mask
    ggo_mask = remove_small_objects(ggo_mask, min_size=min_size)
    ggo_mask = _remove_large_objects(ggo_mask, max_size=max_size)

    if ggo_mask.any():
        ggo_cands = _extract_candidates(slice_hu, ggo_mask, spacing_xy, "ggo")
        candidates.extend(ggo_cands)

    # 策略3: 钙化结节 (> 200 HU)
    calc_mask = (slice_hu > 200) & (slice_hu < 1000) & lung_mask
    calc_mask = remove_small_objects(calc_mask, min_size=20)

    if calc_mask.any():
        calc_cands = _extract_candidates(slice_hu, calc_mask, spacing_xy, "calcified")
        candidates.extend(calc_cands)

    # 去重 - 合并重叠的候选
    candidates = _merge_overlapping_candidates(candidates)

    return candidates


def _remove_large_objects(mask: np.ndarray, max_size: int) -> np.ndarray:
    """移除面积过大的连通域"""
    labeled = label(mask)
    props = regionprops(labeled)
    for p in props:
        if p.area > max_size:
            mask[labeled == p.label] = False
    return mask


def _extract_candidates(slice_hu: np.ndarray,
                         mask: np.ndarray,
                         spacing_xy: float,
                         nodule_type: str) -> List[Dict]:
    """提取候选区域并计算特征"""
    candidates = []
    labeled = label(mask)
    props = regionprops(labeled, intensity_image=slice_hu)

    for p in props:
        # 尺寸过滤 (直径 < 3mm 忽略，> 30mm 大概率不是结节)
        area_mm2 = p.area * spacing_xy * spacing_xy
        diameter_mm = 2 * np.sqrt(area_mm2 / np.pi)
        if diameter_mm < 2.0 or diameter_mm > 35.0:
            continue

        # 计算HU统计
        hu_vals = p.intensity_image[p.image]
        hu_mean = float(np.mean(hu_vals))
        hu_std = float(np.std(hu_vals))
        hu_min = float(np.min(hu_vals))
        hu_max = float(np.max(hu_vals))

        # 计算形态特征
        perimeter = p.perimeter * spacing_xy
        circularity = 4 * np.pi * p.area / (p.perimeter * p.perimeter) if p.perimeter > 0 else 0

        # 离心率
        eccentricity = p.eccentricity if hasattr(p, 'eccentricity') else 0
        y1, x1, y2, x2 = p.bbox
        contour_points = _compact_contour(p.image, y1, x1)
        density_class = _density_class(nodule_type, hu_mean)
        size_class = _size_class(diameter_mm)
        morphology_class = _morphology_class(circularity, eccentricity)

        cand = {
            "centroid_y": float(p.centroid[0]),
            "centroid_x": float(p.centroid[1]),
            "bbox_y1": float(y1),
            "bbox_x1": float(x1),
            "bbox_y2": float(y2),
            "bbox_x2": float(x2),
            "bbox": [float(y1), float(x1), float(y2), float(x2)],
            "area_pixels": int(p.area),
            "area_mm2": round(area_mm2, 2),
            "diameter_mm": round(diameter_mm, 2),
            "perimeter_mm": round(perimeter, 1),
            "circularity": round(circularity, 3),
            "eccentricity": round(eccentricity, 3),
            "solidity": round(float(getattr(p, "solidity", 0) or 0), 3),
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
                "algorithm": "HU阈值 + 连通域 + 形态学过滤",
                "maskAreaPixels": int(p.area),
                "maskAreaMM2": round(area_mm2, 2),
                "contour": contour_points,
                "contourPointCount": len(contour_points),
                "bbox": [float(y1), float(x1), float(y2), float(x2)]
            }
        }
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
        best_area = best['area_pixels']
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
                if cj['area_pixels'] > best_area:
                    best = cj
                    best_area = cj['area_pixels']
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

    score = 0.12
    if diameter >= 8:
        score += 0.24
    elif diameter >= 5:
        score += 0.14
    elif diameter >= 3:
        score += 0.06

    if nodule_type == "ggo":
        score += 0.18
    elif nodule_type == "solid":
        score += 0.12
    elif nodule_type == "calcified":
        score -= 0.18

    if circularity < 0.55:
        score += 0.14
    if eccentricity > 0.65:
        score += 0.10
    if hu_std > 35:
        score += 0.08
    if hu_mean > 120 and nodule_type != "calcified":
        score += 0.05

    probability = round(_clamp(score, 0.03, 0.92), 2)
    if probability >= 0.65:
        risk = "高风险"
        label = "高度疑似恶性"
        advice = "建议薄层增强CT/胸外科会诊，必要时PET-CT或穿刺病理。"
    elif probability >= 0.35:
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
    candidate["cancerSuspicion"] = probability >= 0.35
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


# ===================== 主分析函数 =====================

def analyze_lung_ct(volume: np.ndarray,
                     metadata: dict,
                     max_slices: int = 80) -> Dict:
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
    raw_slices = []       # 原始切片Base64
    annotated_slices = [] # 标注切片Base64
    slice_hu_stats = []   # HU统计
    all_candidates = []   # 所有结节候选
    lung_areas_mm2 = []   # 肺面积

    for scan_idx, z in enumerate(slice_indices):
        slice_hu = volume[z, :, :].copy()

        hu_min = float(np.min(slice_hu))
        hu_max = float(np.max(slice_hu))
        hu_mean = float(np.mean(slice_hu))

        # 转换灰度图（肺窗）
        gray = hu_to_grayscale(slice_hu, ww=1600, wl=-600)

        # 原始切片（无标注）
        raw_buf = io.BytesIO()
        Image.fromarray(gray).save(raw_buf, format='PNG')
        raw_b64 = base64.b64encode(raw_buf.getvalue()).decode('utf-8')

        # 提取肺实质
        lung_mask = extract_lung_mask(slice_hu, lung_threshold=-400)
        lung_area_px = int(np.sum(lung_mask))

        # 检测结节候选
        candidates = []
        if lung_mask.any():
            candidates = detect_nodule_candidates(
                slice_hu, lung_mask,
                soft_min=-100, soft_max=200,
                min_size=30, max_size=6000,
                spacing_xy=pixel_spacing_x
            )

        # 标注该切片的结节，附上切片信息
        for cand in candidates:
            cand['slice_index'] = scan_idx
            cand['slice_z'] = int(z)
            cand['pixel_spacing'] = pixel_spacing_x
            estimate_malignancy_features(cand)
        all_candidates.extend(candidates)

        # 生成标注图像
        annotated_b64 = draw_segmentation_overlay(
            gray, candidates=candidates
        )

        raw_slices.append(raw_b64)
        annotated_slices.append(annotated_b64)

        slice_hu_stats.append({
            "slice_index": scan_idx,
            "slice_z": int(z),
            "hu_min": round(hu_min, 1),
            "hu_max": round(hu_max, 1),
            "hu_mean": round(hu_mean, 1),
            "lung_area_px": lung_area_px,
            "lung_area_mm2": round(lung_area_px * pixel_spacing_x * pixel_spacing_y, 1),
            "candidate_count": len(candidates)
        })

        lung_areas_mm2.append(lung_area_px * pixel_spacing_x * pixel_spacing_y)

    # ===== 汇总统计 =====
    all_candidates = enrich_malignancy(all_candidates)
    total_nodules = len(all_candidates)

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

    # 生成分割掩码数据：每个结节的精确轮廓、面积、边界框和风险字段
    segmentation_data = {}
    for z_idx, z in enumerate(slice_indices):
        slice_cands = [c for c in all_candidates if c.get('slice_index') == z_idx]
        if slice_cands:
            seg_key = f"z{int(z)}"
            contours_list = []
            for cand in slice_cands:
                contours_list.append({
                    "centroid": [cand['centroid_y'], cand['centroid_x']],
                    "diameter_mm": cand['diameter_mm'],
                    "bbox": [cand['bbox_y1'], cand['bbox_x1'],
                             cand['bbox_y2'], cand['bbox_x2']],
                    "type": cand['type'],
                    "classification": cand.get("classification"),
                    "segmentation": cand.get("segmentation"),
                    "hu_mean": cand['hu_mean'],
                    "malignancyProbability": cand.get('malignancyProbability'),
                    "malignancyRisk": cand.get('malignancyRisk')
                })
            segmentation_data[seg_key] = contours_list

    top_risk = sorted(all_candidates, key=lambda c: c.get("malignancyProbability", 0), reverse=True)[:5]
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
    } for c in all_candidates]
    segmentation_items = [{
        "sliceIndex": c.get("slice_index"),
        "sliceZ": c.get("slice_z"),
        "type": c.get("type"),
        "diameterMm": c.get("diameter_mm"),
        "areaMm2": c.get("area_mm2"),
        "contourPointCount": (c.get("segmentation") or {}).get("contourPointCount", 0),
        "segmentation": c.get("segmentation")
    } for c in all_candidates]
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
    } for c in all_candidates]
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
        "rawSlices": raw_slices,
        "slices": annotated_slices,  # 标注后的切片
        "sliceHuStats": slice_hu_stats,
        "stats": {
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
            "maxMalignancyProbability": round(max([c.get('malignancyProbability', 0) for c in all_candidates], default=0), 2),
            "meanLungAreaMM2": round(np.mean(lung_areas_mm2), 1) if lung_areas_mm2 else 0,
            "maxLungAreaMM2": round(np.max(lung_areas_mm2), 1) if lung_areas_mm2 else 0
        },
        "candidates": all_candidates,
        "segmentationData": segmentation_data,
        "detectionClassification": {
            "enabled": True,
            "method": "CT值阈值 + 肺实质掩码 + 连通域形态学分类",
            "model": "HU-Morphology-Detector; AutoDL YOLOv8/ResNet接口预留",
            "completed": True,
            "count": total_nodules,
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
            "mediumRiskCount": len(medium_risk),
            "lowRiskCount": len(low_risk),
            "overallRisk": overall_risk,
            "maxProbability": round(max([c.get('malignancyProbability', 0) for c in all_candidates], default=0), 2),
            "topRiskItems": top_risk,
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

    max_slices = params.get('max_slices', 80)
    mock_nodule = _is_truthy(params.get('mock_nodule'), default=False)

    # 保存临时文件
    filename_lower = file.filename.lower()
    ext = '.nii.gz' if filename_lower.endswith('.nii.gz') else Path(file.filename).suffix
    if not ext:
        ext = '.zip'
    tmp_path = UPLOAD_FOLDER / f"lung_{os.urandom(8).hex()}{ext}"

    try:
        file.save(str(tmp_path))

        if mock_nodule:
            volume, metadata = build_demo_lung_volume(file.filename)
            result = analyze_lung_ct(volume, metadata, max_slices=max_slices)
            result = apply_autodl_config(result, params)
            result["mockMode"] = True
            result["sourceFile"] = file.filename
            result["message"] = "已启用肺结节检测/分割/恶性风险模拟流程"
            return jsonify(result)

        volume, metadata = read_ct_volume(str(tmp_path))
        result = analyze_lung_ct(volume, metadata, max_slices=max_slices)
        result = apply_autodl_config(result, params)
        result["mockMode"] = False
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
