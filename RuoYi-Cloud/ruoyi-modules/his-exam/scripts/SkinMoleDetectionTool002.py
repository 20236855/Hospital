"""
Skin Mole Detection and Annotation Tool
======================================

This file follows the single-file PySide6 tool style used by CTAnnotationTool03.py,
but works on one 2D skin image instead of a CT volume.

Main workflow:
  1. Upload a skin mole image.
  2. Estimate the valid skin area and remove background/borders.
  3. Build a lesion probability map from local darkness and color contrast.
  4. Filter connected components by shape, skin containment and local contrast.
  5. Draw mask, contour, bounding box and region statistics.

Dependencies:
  PySide6, OpenCV, NumPy, SciPy, scikit-image
"""

import os
import sys

import cv2
import numpy as np
from PySide6.QtCore import QPoint, Qt, QThread, Signal
from PySide6.QtGui import QColor, QImage, QPainter, QPainterPath, QPen, QPixmap
from PySide6.QtWidgets import (
    QApplication,
    QCheckBox,
    QFileDialog,
    QFrame,
    QGridLayout,
    QGroupBox,
    QHBoxLayout,
    QLabel,
    QListWidget,
    QMainWindow,
    QMessageBox,
    QProgressBar,
    QPushButton,
    QScrollArea,
    QSlider,
    QVBoxLayout,
    QWidget,
    QDialog,
)

from scipy.ndimage import binary_dilation, binary_fill_holes
from skimage.measure import label, regionprops
from skimage.morphology import remove_small_holes, remove_small_objects
from skimage.filters import threshold_otsu


# ===================== Global stylesheet =====================
APP_STYLESHEET = """
QMainWindow, QWidget {
    background-color: #1a1a2e;
    color: #e0e0e0;
}
QWidget#controlPanel {
    background-color: #16213e;
    border-right: 2px solid #0f3460;
}
QPushButton {
    background: qlineargradient(x1:0, y1:0, x2:0, y2:1,
                stop:0 #0f3460, stop:1 #0a1a3a);
    color: #e0e0e0;
    border: 1px solid #0f3460;
    border-radius: 6px;
    padding: 7px 10px;
    font-size: 12px;
    font-weight: bold;
    min-height: 26px;
}
QPushButton:hover {
    background: qlineargradient(x1:0, y1:0, x2:0, y2:1,
                stop:0 #1a4a8a, stop:1 #0f3460);
    border-color: #00d4ff;
}
QPushButton:pressed {
    background: qlineargradient(x1:0, y1:0, x2:0, y2:1,
                stop:0 #0a1a3a, stop:1 #0f3460);
}
QPushButton#btnRun {
    background: qlineargradient(x1:0, y1:0, x2:0, y2:1,
                stop:0 #00cc66, stop:1 #009944);
    color: #ffffff;
    border: 1px solid #00aa55;
}
QPushButton#btnRun:hover {
    background: qlineargradient(x1:0, y1:0, x2:0, y2:1,
                stop:0 #00dd77, stop:1 #00bb55);
    border-color: #00ff88;
}
QPushButton#btnSave {
    background: qlineargradient(x1:0, y1:0, x2:0, y2:1,
                stop:0 #ffaa00, stop:1 #cc8800);
    color: #1a1a2e;
    border: 1px solid #cc8800;
}
QPushButton#btnSave:hover {
    background: qlineargradient(x1:0, y1:0, x2:0, y2:1,
                stop:0 #ffbb22, stop:1 #dd9900);
    border-color: #ffcc44;
}
QSlider::groove:horizontal {
    background: #0f3460;
    height: 6px;
    border-radius: 3px;
}
QSlider::handle:horizontal {
    background: #00d4ff;
    width: 14px;
    height: 14px;
    margin: -4px 0;
    border-radius: 7px;
}
QSlider::handle:horizontal:hover {
    background: #00e5ff;
}
QLabel {
    color: #c0c0c0;
    font-size: 11px;
}
QLabel#viewTitle {
    color: #00d4ff;
    font-weight: bold;
    font-size: 13px;
    padding: 2px 6px;
}
QLabel#valueLabel {
    color: #00d4ff;
    font-weight: bold;
    font-size: 11px;
    background-color: #0f3460;
    padding: 2px 6px;
    border-radius: 3px;
}
QProgressBar {
    background-color: #0f3460;
    border: 1px solid #0a1a3a;
    border-radius: 4px;
    text-align: center;
    color: #00d4ff;
    font-weight: bold;
}
QProgressBar::chunk {
    background: qlineargradient(x1:0, y1:0, x2:1, y2:0,
                stop:0 #00d4ff, stop:1 #0099cc);
    border-radius: 3px;
}
QCheckBox {
    color: #c0c0c0;
    font-size: 11px;
    spacing: 6px;
}
QCheckBox::indicator {
    width: 14px;
    height: 14px;
}
QCheckBox::indicator:checked {
    background-color: #00cc66;
    border: 1px solid #00cc66;
    border-radius: 3px;
}
QCheckBox::indicator:unchecked {
    background-color: #333333;
    border: 1px solid #666666;
    border-radius: 3px;
}
QGroupBox {
    color: #00d4ff;
    font-weight: bold;
    border: 1px solid #0f3460;
    border-radius: 6px;
    margin-top: 8px;
    padding-top: 12px;
}
QGroupBox::title {
    subcontrol-origin: margin;
    left: 10px;
    padding: 0 6px;
}
QListWidget {
    background-color: #10182f;
    color: #d0d0d0;
    border: 1px solid #0f3460;
    border-radius: 4px;
}
QListWidget::item:selected {
    background-color: #0f3460;
    color: #00d4ff;
}
QDialog, QMessageBox {
    background-color: #16213e;
}
"""


# ===================== Core image algorithms =====================
def _odd(value):
    value = int(max(3, value))
    return value if value % 2 == 1 else value + 1


def _normalize_uint8(arr, mask=None):
    arr = arr.astype(np.float32)
    if mask is not None and np.any(mask):
        vals = arr[mask]
    else:
        vals = arr.reshape(-1)
    lo = float(np.percentile(vals, 1))
    hi = float(np.percentile(vals, 99))
    if hi - lo < 1e-6:
        return np.zeros(arr.shape, dtype=np.uint8)
    out = (arr - lo) / (hi - lo)
    return np.clip(out * 255.0, 0, 255).astype(np.uint8)


def _largest_components(mask, keep_count=1, min_area=100):
    labeled = label(mask)
    if labeled.max() == 0:
        return np.zeros_like(mask, dtype=bool)

    regions = sorted(regionprops(labeled), key=lambda r: r.area, reverse=True)
    out = np.zeros_like(mask, dtype=bool)
    kept = 0
    for region in regions:
        if region.area < min_area:
            continue
        out[labeled == region.label] = True
        kept += 1
        if kept >= keep_count:
            break
    return out


def gray_world_white_balance(image_bgr):
    """Simple color constancy for photos taken under warm/cold light."""
    img = image_bgr.astype(np.float32)
    channel_mean = img.reshape(-1, 3).mean(axis=0)
    gray_mean = float(channel_mean.mean())
    scale = gray_mean / np.maximum(channel_mean, 1.0)
    balanced = img * scale.reshape(1, 1, 3)
    return np.clip(balanced, 0, 255).astype(np.uint8)


def remove_hair_and_specular_noise(image_bgr, enable_hair_removal=True):
    """Suppress thin dark hairs and very small dark noise before segmentation."""
    if not enable_hair_removal:
        return image_bgr.copy()

    gray = cv2.cvtColor(image_bgr, cv2.COLOR_BGR2GRAY)
    kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (17, 17))
    blackhat = cv2.morphologyEx(gray, cv2.MORPH_BLACKHAT, kernel)
    _, hair_mask = cv2.threshold(blackhat, 12, 255, cv2.THRESH_BINARY)

    small_kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (3, 3))
    hair_mask = cv2.dilate(hair_mask, small_kernel, iterations=1)

    # Avoid inpainting broad lesion zones: only remove thin structures.
    contours, _ = cv2.findContours(hair_mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    thin_mask = np.zeros_like(hair_mask)
    for contour in contours:
        area = cv2.contourArea(contour)
        x, y, w, h = cv2.boundingRect(contour)
        if area <= 0:
            continue
        elongation = max(w, h) / max(1, min(w, h))
        if area < 1200 and elongation > 2.0:
            cv2.drawContours(thin_mask, [contour], -1, 255, -1)

    if np.sum(thin_mask) == 0:
        return image_bgr.copy()
    return cv2.inpaint(image_bgr, thin_mask, 3, cv2.INPAINT_TELEA)


def estimate_skin_mask(image_bgr, expand_radius=12):
    """
    Build a usable skin/tissue mask.

    It is intentionally broader than a strict skin-color classifier because dark
    moles may not look like normal skin. The mask mainly removes black borders,
    white rulers, clothes, backgrounds and strong non-skin areas.
    """
    h, w = image_bgr.shape[:2]
    blur = cv2.GaussianBlur(image_bgr, (5, 5), 0)
    hsv = cv2.cvtColor(blur, cv2.COLOR_BGR2HSV)
    ycrcb = cv2.cvtColor(blur, cv2.COLOR_BGR2YCrCb)
    lab = cv2.cvtColor(blur, cv2.COLOR_BGR2LAB)

    hue = hsv[:, :, 0]
    sat = hsv[:, :, 1]
    val = hsv[:, :, 2]
    cr = ycrcb[:, :, 1]
    cb = ycrcb[:, :, 2]
    light = lab[:, :, 0]

    valid_brightness = (val > 25) & (light > 18)
    not_overexposed = (val < 252) | (sat > 15)
    broad_skin_color = (cr > 108) & (cr < 190) & (cb > 60) & (cb < 155)
    warm_hue = ((hue < 35) | (hue > 165)) & (sat < 210)
    low_saturation_skin = (sat < 95) & (val > 65)

    mask = valid_brightness & not_overexposed & (broad_skin_color | warm_hue | low_saturation_skin)
    mask = remove_small_objects(mask, min_size=max(80, int(0.0005 * h * w)))
    mask = binary_fill_holes(mask)
    mask = remove_small_holes(mask, area_threshold=max(200, int(0.002 * h * w)))

    largest = _largest_components(mask, keep_count=2, min_area=max(300, int(0.01 * h * w)))
    if np.sum(largest) < 0.20 * h * w:
        # Fallback for dermoscopy photos with unusual color calibration.
        fallback = valid_brightness & (sat < 245)
        largest = _largest_components(fallback, keep_count=1, min_area=max(300, int(0.08 * h * w)))

    if expand_radius > 0:
        largest = binary_dilation(largest, iterations=int(expand_radius))
    largest = binary_fill_holes(largest)
    return largest.astype(bool)


def build_lesion_score(image_bgr, skin_mask, local_radius=41):
    """
    Lesions are usually locally darker and chromatically different from nearby
    skin. This function turns those two cues into a 0-255 score image.
    """
    balanced = gray_world_white_balance(image_bgr)
    smooth = cv2.bilateralFilter(balanced, 11, 60, 60)
    lab = cv2.cvtColor(smooth, cv2.COLOR_BGR2LAB).astype(np.float32)
    hsv = cv2.cvtColor(smooth, cv2.COLOR_BGR2HSV).astype(np.float32)
    ycrcb = cv2.cvtColor(smooth, cv2.COLOR_BGR2YCrCb).astype(np.float32)

    k = _odd(local_radius * 2 + 1)
    l_channel = lab[:, :, 0]
    a_channel = lab[:, :, 1]
    b_channel = lab[:, :, 2]
    s_channel = hsv[:, :, 1]
    cr_channel = ycrcb[:, :, 1]

    local_l = cv2.GaussianBlur(l_channel, (k, k), 0)
    local_a = cv2.GaussianBlur(a_channel, (k, k), 0)
    local_b = cv2.GaussianBlur(b_channel, (k, k), 0)
    local_s = cv2.GaussianBlur(s_channel, (k, k), 0)
    local_cr = cv2.GaussianBlur(cr_channel, (k, k), 0)

    dark_delta = np.maximum(local_l - l_channel, 0.0)
    color_delta = np.sqrt((a_channel - local_a) ** 2 + (b_channel - local_b) ** 2)
    sat_delta = np.maximum(s_channel - local_s, 0.0)
    red_delta = np.maximum(cr_channel - local_cr, 0.0)

    dark_score = _normalize_uint8(dark_delta, skin_mask).astype(np.float32)
    color_score = _normalize_uint8(color_delta, skin_mask).astype(np.float32)
    sat_score = _normalize_uint8(sat_delta, skin_mask).astype(np.float32)
    red_score = _normalize_uint8(red_delta, skin_mask).astype(np.float32)

    score = 0.52 * dark_score + 0.24 * color_score + 0.14 * sat_score + 0.10 * red_score
    score = np.where(skin_mask, score, 0)
    return np.clip(score, 0, 255).astype(np.uint8), balanced


def _region_contrast_features(l_channel, skin_mask, region_mask, dilation=16):
    ring = binary_dilation(region_mask, iterations=dilation) & (~region_mask) & skin_mask
    if np.sum(ring) < 20:
        return 0.0, 0.0
    region_l = l_channel[region_mask]
    ring_l = l_channel[ring]
    darkness = float(np.mean(ring_l) - np.mean(region_l))
    contrast = float(np.std(ring_l) + np.std(region_l))
    return darkness, contrast


def segment_skin_moles(
    image_bgr,
    local_radius=41,
    threshold_bias=0,
    min_area=200,
    max_area_ratio=0.80,
    min_circularity=0.06,
    min_solidity=0.30,
    max_elongation=9.0,
    morph_close_radius=16,
    morph_open_radius=1,
    enable_hair_removal=True,
    enable_component_filter=True,
    force_largest_only=True,
):
    """
    v3: 单痣优先逻辑 — 强保留全局最大病灶, 提高面积阈值直接过滤微小干扰.

    核心改进:
      1. close_r 默认 16 → 阈值后碎片先融合成整块
      2. hole_fill 阈值放大到 min_area*5 → 内部孔洞一次填平
      3. max_area_ratio=0.80 → 不再误杀大痣
      4. min_area=200 → 直接过滤微小碎片/毛发/毛孔
      5. force_largest_only=True → 最终只输出面积最大的单个连通块
      6. 兜底: 规则过滤后若 0 输出, 强制保留最大连通块
      7. 处理顺序: 阈值 → 大半径融合 → 填孔 → 去小噪 → 判定
    """
    h, w = image_bgr.shape[:2]
    cleaned = remove_hair_and_specular_noise(image_bgr, enable_hair_removal)
    skin_mask = estimate_skin_mask(cleaned, expand_radius=12)
    score_map, balanced = build_lesion_score(cleaned, skin_mask, local_radius=local_radius)

    stats = {
        "total_regions": 0,
        "kept_moles": 0,
        "removed_small": 0,
        "removed_large": 0,
        "removed_shape": 0,
        "removed_low_contrast": 0,
        "removed_outside_skin": 0,
        "threshold": 0,
        "fallback_used": False,
    }

    if not np.any(skin_mask):
        return np.zeros((h, w), dtype=bool), score_map, skin_mask, [], stats

    skin_scores = score_map[skin_mask]
    try:
        otsu = float(threshold_otsu(skin_scores))
    except ValueError:
        otsu = 128.0

    percentile_th = float(np.percentile(skin_scores, 82))
    threshold = int(np.clip(max(otsu, percentile_th) + threshold_bias, 20, 245))
    stats["threshold"] = threshold

    # ===== 步骤1: 阈值 → 得到粗候选 =====
    candidate = (score_map >= threshold) & skin_mask

    # ===== 步骤2: 先大半径闭运算融合碎片 (关键!) =====
    close_r = max(3, int(morph_close_radius))
    kernel_close = cv2.getStructuringElement(
        cv2.MORPH_ELLIPSE, (2 * close_r + 1, 2 * close_r + 1))
    candidate = cv2.morphologyEx(
        candidate.astype(np.uint8), cv2.MORPH_CLOSE, kernel_close) > 0

    # ===== 步骤3: 大面积孔洞填充 (修复内部纹理孔洞) =====
    candidate = binary_fill_holes(candidate)
    # 再用 remove_small_holes 消除残余小孔, 阈值大幅提高
    hole_thresh = max(int(min_area * 5), 200)
    candidate = remove_small_holes(candidate, area_threshold=hole_thresh)

    # ===== 步骤4: 去小噪点 (稍后再精确筛选) =====
    if morph_open_radius > 0:
        open_r = max(1, int(morph_open_radius))
        kernel_open = cv2.getStructuringElement(
            cv2.MORPH_ELLIPSE, (2 * open_r + 1, 2 * open_r + 1))
        candidate = cv2.morphologyEx(
            candidate.astype(np.uint8), cv2.MORPH_OPEN, kernel_open) > 0

    candidate = remove_small_objects(candidate, min_size=max(8, int(min_area * 0.35)))

    # ===== 步骤5: 连通域分析 =====
    lab = cv2.cvtColor(balanced, cv2.COLOR_BGR2LAB).astype(np.float32)
    l_channel = lab[:, :, 0]
    labeled = label(candidate)
    all_regions = sorted(
        regionprops(labeled, intensity_image=score_map),
        key=lambda r: r.area, reverse=True  # 按面积降序
    )
    stats["total_regions"] = len(all_regions)

    # 面积上限: 放宽到 80% 图幅或 85% 皮肤 — 不再误杀大痣
    max_area = max(int(min(h * w * max_area_ratio, np.sum(skin_mask) * 0.85)), min_area + 1)

    # ===== 步骤6: 规则过滤(宽松版) =====
    final = np.zeros_like(candidate, dtype=bool)
    kept_regions = []

    for region in all_regions:
        region_mask = labeled == region.label
        area = int(region.area)
        darkness, mean_score = 0.0, 0.0  # 默认值

        # 必须在皮肤内
        skin_ratio = float(np.sum(region_mask & skin_mask) / max(area, 1))
        if skin_ratio < 0.75:
            stats["removed_outside_skin"] += 1
            continue
        if area < min_area:
            stats["removed_small"] += 1
            continue
        if area > max_area:
            stats["removed_large"] += 1
            continue

        perimeter = max(float(region.perimeter), 1.0)
        circularity = float(4.0 * np.pi * area / (perimeter * perimeter))
        solidity = float(region.solidity)
        if region.minor_axis_length > 1e-6:
            elongation = float(region.major_axis_length / region.minor_axis_length)
        else:
            elongation = 999.0

        if enable_component_filter:
            shape_ok = (
                circularity >= min_circularity
                and solidity >= min_solidity
                and elongation <= max_elongation
            )
            if not shape_ok:
                stats["removed_shape"] += 1
                continue

            darkness, local_var = _region_contrast_features(l_channel, skin_mask, region_mask)
            mean_score = float(np.mean(score_map[region_mask]))
            contrast_ok = darkness >= 3.0 or mean_score >= threshold + 12 or local_var > 12.0
            if not contrast_ok:
                stats["removed_low_contrast"] += 1
                continue

        final[region_mask] = True
        minr, minc, maxr, maxc = region.bbox
        kept_regions.append({
            "label": len(kept_regions) + 1,
            "area": area,
            "bbox": (int(minc), int(minr), int(maxc), int(maxr)),
            "centroid": (float(region.centroid[1]), float(region.centroid[0])),
            "circularity": circularity, "solidity": solidity,
            "elongation": elongation,
            "mean_score": mean_score,
            "darkness": darkness,
        })

    # ===== 步骤7: 单痣优先 — 强制仅保留面积最大的病灶 =====
    if force_largest_only and len(kept_regions) > 1:
        kept_regions.sort(key=lambda r: r["area"], reverse=True)
        largest = kept_regions[0]
        # 找到这个region在labeled图中的label号
        x1, y1, x2, y2 = largest["bbox"]
        crop = labeled[y1:y2, x1:x2]
        # 用bbox中心点的label来确定是哪个连通域
        cy_l = (y1 + y2) // 2 - y1
        cx_l = (x1 + x2) // 2 - x1
        target_label = crop[max(0, min(cy_l, crop.shape[0]-1)),
                            max(0, min(cx_l, crop.shape[1]-1))]
        # 重建掩码
        final = np.zeros_like(candidate, dtype=bool)
        final[labeled == target_label] = True
        kept_regions = [largest]
        kept_regions[0]["label"] = 1

    # ===== 步骤8: 兜底 — 若规则全排除了, 强制保留最大候选 =====
    if len(kept_regions) == 0 and len(all_regions) > 0:
        # 找面积最大且在皮肤内的连通块
        for region in all_regions:
            region_mask = labeled == region.label
            area = int(region.area)
            skin_ratio = float(np.sum(region_mask & skin_mask) / max(area, 1))
            if area >= min(min_area, 50) and skin_ratio >= 0.5:
                final = region_mask.copy()
                minr, minc, maxr, maxc = region.bbox
                mean_score = float(np.mean(score_map[region_mask]))
                kept_regions.append({
                    "label": 1,
                    "area": area,
                    "bbox": (int(minc), int(minr), int(maxc), int(maxr)),
                    "centroid": (float(region.centroid[1]), float(region.centroid[0])),
                    "circularity": 0.0, "solidity": 0.0, "elongation": 0.0,
                    "mean_score": mean_score, "darkness": 0.0,
                })
                stats["fallback_used"] = True
                break

    final = binary_fill_holes(final)
    stats["kept_moles"] = len(kept_regions)
    return final.astype(bool), score_map, skin_mask.astype(bool), kept_regions, stats


def build_annotation_image(image_bgr, mask_bool, regions, alpha=0.42):
    image_rgb = cv2.cvtColor(image_bgr, cv2.COLOR_BGR2RGB)
    annotated = image_rgb.copy()

    if mask_bool is None or not np.any(mask_bool):
        return annotated

    overlay = annotated.copy()
    overlay[mask_bool] = np.array([255, 45, 45], dtype=np.uint8)
    annotated = cv2.addWeighted(overlay, alpha, annotated, 1.0 - alpha, 0)

    mask_uint8 = (mask_bool.astype(np.uint8) * 255)
    contours, _ = cv2.findContours(mask_uint8, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    cv2.drawContours(annotated, contours, -1, (0, 255, 80), 2)

    for item in regions:
        x1, y1, x2, y2 = item["bbox"]
        cv2.rectangle(annotated, (x1, y1), (x2, y2), (0, 215, 255), 2)
        label_text = f"mole {item['label']}  score:{item['mean_score']:.0f}"
        text_y = max(18, y1 - 6)
        cv2.putText(
            annotated,
            label_text,
            (x1, text_y),
            cv2.FONT_HERSHEY_SIMPLEX,
            0.55,
            (0, 0, 0),
            4,
            cv2.LINE_AA,
        )
        cv2.putText(
            annotated,
            label_text,
            (x1, text_y),
            cv2.FONT_HERSHEY_SIMPLEX,
            0.55,
            (255, 255, 255),
            1,
            cv2.LINE_AA,
        )
    return annotated


def build_mask_rgb(mask_bool):
    if mask_bool is None:
        return None
    mask = (mask_bool.astype(np.uint8) * 255)
    return np.stack([mask, mask, mask], axis=-1)


def build_skin_mask_rgb(image_bgr, skin_mask):
    image_rgb = cv2.cvtColor(image_bgr, cv2.COLOR_BGR2RGB)
    if skin_mask is None:
        return image_rgb
    out = image_rgb.copy()
    dark = (out * 0.22).astype(np.uint8)
    out[~skin_mask] = dark[~skin_mask]
    return out


# ===================== UI helpers =====================
class MagnifierLabel(QLabel):
    """QLabel with a small hover magnifier."""

    def __init__(self, parent=None):
        super().__init__(parent)
        self.setMouseTracking(True)
        self.original_image = None
        self.zoom_factor = 3
        self.magnifier_size = 130
        self.is_hovering = False
        self.hover_pos = QPoint()
        self.setAlignment(Qt.AlignCenter)
        self.setStyleSheet("background-color:#111; border: 1px solid #0f3460; border-radius: 4px;")

    def set_original_image(self, arr):
        self.original_image = arr.copy() if arr is not None else None

    def enterEvent(self, event):
        self.is_hovering = True
        super().enterEvent(event)

    def leaveEvent(self, event):
        self.is_hovering = False
        self.update()
        super().leaveEvent(event)

    def mouseMoveEvent(self, event):
        self.hover_pos = event.position().toPoint()
        self.update()
        super().mouseMoveEvent(event)

    def paintEvent(self, event):
        super().paintEvent(event)
        if not self.is_hovering or self.original_image is None:
            return

        pix = self.pixmap()
        if pix is None:
            return

        pos = self.hover_pos
        w, h = self.width(), self.height()
        pw, ph = pix.width(), pix.height()
        offset_x = (w - pw) // 2
        offset_y = (h - ph) // 2
        ix = pos.x() - offset_x
        iy = pos.y() - offset_y
        if ix < 0 or iy < 0 or ix >= pw or iy >= ph:
            return

        img_h, img_w = self.original_image.shape[:2]
        orig_x = int(ix / max(pw, 1) * img_w)
        orig_y = int(iy / max(ph, 1) * img_h)
        half = self.magnifier_size // (2 * self.zoom_factor)
        x1 = max(0, orig_x - half)
        y1 = max(0, orig_y - half)
        x2 = min(img_w, orig_x + half)
        y2 = min(img_h, orig_y + half)
        region = np.ascontiguousarray(self.original_image[y1:y2, x1:x2])
        if region.size == 0:
            return

        qimg = QImage(region.data, region.shape[1], region.shape[0], 3 * region.shape[1], QImage.Format_RGB888)
        pixmap = QPixmap.fromImage(qimg).scaled(
            self.magnifier_size,
            self.magnifier_size,
            Qt.KeepAspectRatio,
            Qt.SmoothTransformation,
        )

        painter = QPainter(self)
        painter.setRenderHint(QPainter.Antialiasing)

        mag_x = pos.x() + 16
        mag_y = pos.y() + 16
        if mag_x + self.magnifier_size > w:
            mag_x = pos.x() - self.magnifier_size - 16
        if mag_y + self.magnifier_size > h:
            mag_y = pos.y() - self.magnifier_size - 16

        path = QPainterPath()
        path.addEllipse(mag_x, mag_y, self.magnifier_size, self.magnifier_size)
        painter.setClipPath(path)
        painter.drawPixmap(mag_x, mag_y, pixmap)
        painter.setClipping(False)

        painter.setPen(QPen(QColor("#00d4ff"), 2))
        painter.drawEllipse(mag_x, mag_y, self.magnifier_size, self.magnifier_size)
        painter.setPen(QPen(QColor("#ff4444"), 1, Qt.DashLine))
        cx = mag_x + self.magnifier_size // 2
        cy = mag_y + self.magnifier_size // 2
        painter.drawLine(cx - 10, cy, cx + 10, cy)
        painter.drawLine(cx, cy - 10, cx, cy + 10)


class ProgressDialog(QDialog):
    def __init__(self, title="Processing...", parent=None):
        super().__init__(parent)
        self.setWindowTitle(title)
        self.setFixedSize(430, 120)
        self.setModal(True)
        layout = QVBoxLayout(self)
        layout.setContentsMargins(30, 20, 30, 20)
        layout.setSpacing(12)
        self.label = QLabel("Initializing...")
        self.label.setAlignment(Qt.AlignCenter)
        layout.addWidget(self.label)
        self.progress = QProgressBar()
        self.progress.setRange(0, 100)
        layout.addWidget(self.progress)

    def set_progress(self, value, text=""):
        self.progress.setValue(value)
        if text:
            self.label.setText(text)


class Worker(QThread):
    progress = Signal(int, str)
    finished = Signal(object)
    error = Signal(str)

    def __init__(self, func, *args, parent=None):
        super().__init__(parent)
        self.func = func
        self.args = args

    def run(self):
        try:
            result = self.func(self.report_progress, *self.args)
            self.finished.emit(result)
        except Exception as exc:
            self.error.emit(str(exc))

    def report_progress(self, value, text=""):
        self.progress.emit(value, text)


# ===================== Main window =====================
class SkinMoleDetectionWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("皮肤痣识别与标注工具 - 上传图片自动生成掩码和轮廓")
        self.resize(1500, 900)

        self.image_path = None
        self.image_bgr = None
        self.image_rgb = None
        self.mask_bool = None
        self.score_map = None
        self.skin_mask = None
        self.regions = []
        self.annotation_rgb = None
        self._image_files = []
        self.worker = None
        self.progress_dialog = None

        self.init_ui()

    def init_ui(self):
        central = QWidget()
        self.setCentralWidget(central)
        main_layout = QHBoxLayout(central)
        main_layout.setContentsMargins(0, 0, 0, 0)

        control = QWidget()
        control.setObjectName("controlPanel")
        control.setFixedWidth(275)
        control_layout = QVBoxLayout(control)
        control_layout.setContentsMargins(12, 12, 12, 12)
        control_layout.setSpacing(8)

        self.btn_load = QPushButton("加载皮肤痣图片")
        self.btn_load.clicked.connect(self.load_image_dialog)
        control_layout.addWidget(self.btn_load)

        self.btn_folder = QPushButton("加载图片文件夹")
        self.btn_folder.clicked.connect(self.load_folder_dialog)
        control_layout.addWidget(self.btn_folder)

        self.file_list = QListWidget()
        self.file_list.setMaximumHeight(110)
        self.file_list.currentRowChanged.connect(self.on_file_select)
        self.file_list.hide()
        control_layout.addWidget(self.file_list)

        control_layout.addWidget(self._separator())

        params_box = QGroupBox("算法参数")
        params_layout = QVBoxLayout(params_box)

        self.slider_radius, self.label_radius = self._add_slider(
            params_layout, "局部背景半径", 15, 90, 41
        )
        self.slider_bias, self.label_bias = self._add_slider(
            params_layout, "阈值偏置", -35, 55, 0
        )
        self.slider_minarea, self.label_minarea = self._add_slider(
            params_layout, "最小痣面积(px)", 50, 5000, 200
        )
        self.slider_circularity, self.label_circularity = self._add_slider(
            params_layout, "最小圆形度(%)", 1, 50, 6
        )
        self.slider_solidity, self.label_solidity = self._add_slider(
            params_layout, "最小实心度(%)", 10, 90, 30
        )
        self.slider_close, self.label_close = self._add_slider(
            params_layout, "碎片融合半径", 3, 30, 16
        )
        self.slider_alpha, self.label_alpha = self._add_slider(
            params_layout, "标注透明度(%)", 10, 85, 42
        )
        control_layout.addWidget(params_box)

        opt_box = QGroupBox("处理选项")
        opt_layout = QVBoxLayout(opt_box)
        self.chk_hair = QCheckBox("去除毛发细线")
        self.chk_hair.setChecked(True)
        self.chk_filter = QCheckBox("启用连通域形态过滤")
        self.chk_filter.setChecked(True)
        self.chk_largest = QCheckBox("单痣优先 (仅保留最大病灶)")
        self.chk_largest.setChecked(True)
        opt_layout.addWidget(self.chk_hair)
        opt_layout.addWidget(self.chk_filter)
        opt_layout.addWidget(self.chk_largest)
        control_layout.addWidget(opt_box)

        control_layout.addWidget(self._separator())

        self.btn_run = QPushButton("识别并标注皮肤痣")
        self.btn_run.setObjectName("btnRun")
        self.btn_run.clicked.connect(self.run_detection_async)
        control_layout.addWidget(self.btn_run)

        self.btn_save_mask = QPushButton("保存掩码 PNG")
        self.btn_save_mask.setObjectName("btnSave")
        self.btn_save_mask.clicked.connect(self.save_mask)
        control_layout.addWidget(self.btn_save_mask)

        self.btn_save_annot = QPushButton("保存标注图 PNG")
        self.btn_save_annot.setObjectName("btnSave")
        self.btn_save_annot.clicked.connect(self.save_annotation)
        control_layout.addWidget(self.btn_save_annot)

        self.btn_clear = QPushButton("清除结果")
        self.btn_clear.clicked.connect(self.clear_result)
        control_layout.addWidget(self.btn_clear)

        self.stats_label = QLabel("请先加载一张皮肤痣图片")
        self.stats_label.setWordWrap(True)
        self.stats_label.setStyleSheet("color:#00d4ff; font-weight:bold; padding:6px;")
        control_layout.addWidget(self.stats_label)
        control_layout.addStretch()
        main_layout.addWidget(control)

        right = QWidget()
        right_layout = QVBoxLayout(right)
        right_layout.setContentsMargins(12, 10, 12, 10)

        title = QLabel("皮肤痣识别结果")
        title.setObjectName("viewTitle")
        title.setStyleSheet("font-size:16px;")
        right_layout.addWidget(title)

        grid_container = QWidget()
        grid = QGridLayout(grid_container)
        grid.setSpacing(10)

        self.label_original = self._make_view("原图")
        self.label_skin = self._make_view("皮肤区域")
        self.label_score = self._make_view("痣概率热图")
        self.label_annot = self._make_view("识别标注")
        self.label_mask = self._make_view("二值掩码")

        grid.addWidget(self._view_panel("原图", self.label_original), 0, 0)
        grid.addWidget(self._view_panel("皮肤区域估计", self.label_skin), 0, 1)
        grid.addWidget(self._view_panel("痣概率热图", self.label_score), 0, 2)
        grid.addWidget(self._view_panel("识别标注", self.label_annot), 1, 0, 1, 2)
        grid.addWidget(self._view_panel("二值掩码", self.label_mask), 1, 2)

        scroll = QScrollArea()
        scroll.setWidgetResizable(True)
        scroll.setWidget(grid_container)
        right_layout.addWidget(scroll)
        main_layout.addWidget(right, 1)

        self._connect_live_refresh()

    def _separator(self):
        line = QFrame()
        line.setFrameShape(QFrame.HLine)
        line.setStyleSheet("background-color:#0f3460;")
        return line

    def _add_slider(self, parent_layout, text, min_value, max_value, default):
        label = QLabel(text)
        value_label = QLabel(str(default))
        value_label.setObjectName("valueLabel")
        slider = QSlider(Qt.Horizontal)
        slider.setRange(min_value, max_value)
        slider.setValue(default)
        slider.valueChanged.connect(lambda v, lab=value_label: lab.setText(str(v)))
        parent_layout.addWidget(label)
        parent_layout.addWidget(slider)
        parent_layout.addWidget(value_label)
        return slider, value_label

    def _make_view(self, placeholder):
        label_widget = MagnifierLabel()
        label_widget.setMinimumSize(360, 260)
        label_widget.setText(placeholder)
        return label_widget

    def _view_panel(self, title, label_widget):
        panel = QWidget()
        layout = QVBoxLayout(panel)
        layout.setContentsMargins(0, 0, 0, 0)
        t = QLabel(title)
        t.setObjectName("viewTitle")
        layout.addWidget(t)
        layout.addWidget(label_widget)
        return panel

    def _connect_live_refresh(self):
        self.slider_alpha.valueChanged.connect(self.update_annotation_view)

    def run_async(self, title, func, *args):
        if self.worker is not None and self.worker.isRunning():
            QMessageBox.warning(self, "提示", "当前任务仍在运行，请稍后。")
            return
        self.progress_dialog = ProgressDialog(title, self)
        self.worker = Worker(func, *args)
        self.worker.progress.connect(self.progress_dialog.set_progress)
        self.worker.finished.connect(self.on_worker_finished)
        self.worker.error.connect(self.on_worker_error)
        self.worker.start()
        self.progress_dialog.show()

    def on_worker_finished(self, result):
        if self.progress_dialog:
            self.progress_dialog.close()
        if callable(result):
            result()
        self.worker = None

    def on_worker_error(self, text):
        if self.progress_dialog:
            self.progress_dialog.close()
        QMessageBox.critical(self, "处理失败", text)
        self.worker = None

    # ===================== Image loading =====================
    def load_image_dialog(self):
        path, _ = QFileDialog.getOpenFileName(
            self,
            "选择皮肤痣图片",
            "",
            "Images (*.png *.jpg *.jpeg *.bmp *.tif *.tiff);;All Files (*)",
        )
        if path:
            self.run_async("加载图片...", self._load_image_task, path)

    def load_folder_dialog(self):
        folder = QFileDialog.getExistingDirectory(self, "选择图片文件夹")
        if not folder:
            return
        exts = {".png", ".jpg", ".jpeg", ".bmp", ".tif", ".tiff"}
        files = [
            os.path.join(folder, name)
            for name in sorted(os.listdir(folder))
            if os.path.splitext(name.lower())[1] in exts
        ]
        if not files:
            QMessageBox.warning(self, "提示", "该文件夹中没有可读取的图片。")
            return
        self._image_files = files
        self.file_list.clear()
        for item in files:
            self.file_list.addItem(os.path.basename(item))
        self.file_list.show()
        self.file_list.setCurrentRow(0)

    def on_file_select(self, row):
        if row < 0 or row >= len(self._image_files):
            return
        self.run_async("加载图片...", self._load_image_task, self._image_files[row])

    def _load_image_task(self, report, path):
        report(10, "读取图片文件...")
        image = cv2.imdecode(np.fromfile(path, dtype=np.uint8), cv2.IMREAD_COLOR)
        if image is None:
            raise RuntimeError(f"无法读取图片: {path}")

        max_side = max(image.shape[:2])
        if max_side > 1800:
            scale = 1800.0 / max_side
            image = cv2.resize(image, None, fx=scale, fy=scale, interpolation=cv2.INTER_AREA)

        report(70, "生成预览...")

        def apply_result():
            self.image_path = path
            self.image_bgr = image
            self.image_rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
            self.mask_bool = None
            self.score_map = None
            self.skin_mask = None
            self.regions = []
            self.annotation_rgb = None
            self.stats_label.setText(f"已加载: {os.path.basename(path)}")
            self.refresh_views()

        report(100, "加载完成")
        return apply_result

    # ===================== Detection =====================
    def run_detection_async(self):
        if self.image_bgr is None:
            QMessageBox.warning(self, "提示", "请先加载一张皮肤痣图片。")
            return
        self.run_async("识别皮肤痣...", self._detection_task)

    def _detection_task(self, report):
        report(8, "去除毛发并估计皮肤区域...")
        local_radius = self.slider_radius.value()
        threshold_bias = self.slider_bias.value()
        min_area = self.slider_minarea.value()
        min_circ = self.slider_circularity.value() / 100.0
        min_solidity = self.slider_solidity.value() / 100.0
        close_r = self.slider_close.value()
        enable_hair = self.chk_hair.isChecked()
        enable_filter = self.chk_filter.isChecked()
        force_largest = self.chk_largest.isChecked()

        report(32, "生成痣概率热图...")
        mask, score, skin, regions, stats = segment_skin_moles(
            self.image_bgr,
            local_radius=local_radius,
            threshold_bias=threshold_bias,
            min_area=min_area,
            min_circularity=min_circ,
            min_solidity=min_solidity,
            morph_close_radius=close_r,
            morph_open_radius=1,
            enable_hair_removal=enable_hair,
            enable_component_filter=enable_filter,
            force_largest_only=force_largest,
        )

        report(78, "绘制轮廓和标注...")
        alpha = self.slider_alpha.value() / 100.0
        annot = build_annotation_image(self.image_bgr, mask, regions, alpha=alpha)

        removed = (
            stats["removed_small"]
            + stats["removed_large"]
            + stats["removed_shape"]
            + stats["removed_low_contrast"]
            + stats["removed_outside_skin"]
        )
        fallback_note = " (兜底保留最大连通块)" if stats["fallback_used"] else ""
        summary = (
            f"检测到 {stats['kept_moles']} 个疑似皮肤痣{fallback_note}；"
            f"候选区域 {stats['total_regions']} 个；"
            f"排除干扰 {removed} 个；阈值 {stats['threshold']}"
        )

        def apply_result():
            self.mask_bool = mask
            self.score_map = score
            self.skin_mask = skin
            self.regions = regions
            self.annotation_rgb = annot
            self.stats_label.setText(summary)
            self.refresh_views()

        report(100, summary)
        return apply_result

    def update_annotation_view(self):
        if self.image_bgr is None or self.mask_bool is None:
            return
        self.annotation_rgb = build_annotation_image(
            self.image_bgr,
            self.mask_bool,
            self.regions,
            alpha=self.slider_alpha.value() / 100.0,
        )
        self._display_rgb(self.label_annot, self.annotation_rgb)

    # ===================== Display =====================
    def refresh_views(self):
        if self.image_rgb is None:
            return
        self._display_rgb(self.label_original, self.image_rgb)

        if self.skin_mask is not None:
            self._display_rgb(self.label_skin, build_skin_mask_rgb(self.image_bgr, self.skin_mask))
        else:
            self.label_skin.clear()
            self.label_skin.setText("未生成皮肤区域")

        if self.score_map is not None:
            heat = cv2.applyColorMap(self.score_map, cv2.COLORMAP_JET)
            heat_rgb = cv2.cvtColor(heat, cv2.COLOR_BGR2RGB)
            self._display_rgb(self.label_score, heat_rgb)
        else:
            self.label_score.clear()
            self.label_score.setText("未生成热图")

        if self.annotation_rgb is not None:
            self._display_rgb(self.label_annot, self.annotation_rgb)
        else:
            self.label_annot.clear()
            self.label_annot.setText("未识别标注")

        if self.mask_bool is not None:
            self._display_rgb(self.label_mask, build_mask_rgb(self.mask_bool))
        else:
            self.label_mask.clear()
            self.label_mask.setText("未生成掩码")

    def _display_rgb(self, label_widget, rgb_img):
        if rgb_img is None:
            return
        rgb_img = np.ascontiguousarray(rgb_img)
        h, w = rgb_img.shape[:2]
        max_w = max(320, label_widget.width() - 20)
        max_h = max(230, label_widget.height() - 20)
        scale = min(max_w / max(w, 1), max_h / max(h, 1), 1.0)
        disp = cv2.resize(rgb_img, (int(w * scale), int(h * scale)), interpolation=cv2.INTER_AREA)
        disp = np.ascontiguousarray(disp)
        label_widget.set_original_image(disp)
        qimg = QImage(disp.data, disp.shape[1], disp.shape[0], 3 * disp.shape[1], QImage.Format_RGB888)
        label_widget.setPixmap(QPixmap.fromImage(qimg))

    # ===================== Save and clear =====================
    def save_mask(self):
        if self.mask_bool is None:
            QMessageBox.warning(self, "提示", "请先执行皮肤痣识别。")
            return
        path, _ = QFileDialog.getSaveFileName(
            self,
            "保存掩码",
            "skin_mole_mask.png",
            "PNG (*.png);;JPEG (*.jpg);;All Files (*)",
        )
        if not path:
            return
        mask = (self.mask_bool.astype(np.uint8) * 255)
        ok = cv2.imencode(os.path.splitext(path)[1] or ".png", mask)[1].tofile(path)
        QMessageBox.information(self, "完成", f"掩码已保存到:\n{path}")

    def save_annotation(self):
        if self.annotation_rgb is None:
            QMessageBox.warning(self, "提示", "请先执行皮肤痣识别。")
            return
        path, _ = QFileDialog.getSaveFileName(
            self,
            "保存标注图",
            "skin_mole_annotation.png",
            "PNG (*.png);;JPEG (*.jpg);;All Files (*)",
        )
        if not path:
            return
        bgr = cv2.cvtColor(self.annotation_rgb, cv2.COLOR_RGB2BGR)
        cv2.imencode(os.path.splitext(path)[1] or ".png", bgr)[1].tofile(path)
        QMessageBox.information(self, "完成", f"标注图已保存到:\n{path}")

    def clear_result(self):
        self.mask_bool = None
        self.score_map = None
        self.skin_mask = None
        self.regions = []
        self.annotation_rgb = None
        self.stats_label.setText("结果已清除")
        self.refresh_views()


# ===================== Main entry =====================
if __name__ == "__main__":
    app = QApplication(sys.argv)
    app.setStyleSheet(APP_STYLESHEET)
    window = SkinMoleDetectionWindow()
    window.show()
    sys.exit(app.exec())
