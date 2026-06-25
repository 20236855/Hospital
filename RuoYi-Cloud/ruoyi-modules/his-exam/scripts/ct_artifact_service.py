"""
CT金属伪影检测后端API服务
基于 CTAnnotationTool03.py 的核心算法，提供HTTP接口供Java后端调用。

启动方式: python ct_artifact_service.py --port 5001
依赖: pip install flask simpleitk numpy scipy scikit-image pillow
"""

import sys
import os
import io
import json
import base64
import tempfile
import traceback
import zipfile
import argparse
from pathlib import Path

import numpy as np
from flask import Flask, request, jsonify
from PIL import Image

# ===================== 算法依赖 =====================
try:
    import SimpleITK as sitk
except ImportError:
    print("警告: SimpleITK未安装，请运行: pip install SimpleITK")
    sitk = None

try:
    from scipy.ndimage import binary_erosion, binary_fill_holes, binary_dilation
except ImportError:
    print("警告: scipy未安装，请运行: pip install scipy")

try:
    from skimage.measure import regionprops, label
    from skimage.morphology import remove_small_objects
except ImportError:
    print("警告: scikit-image未安装，请运行: pip install scikit-image")

# ===================== Flask应用 =====================
app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = 500 * 1024 * 1024  # 500MB
UPLOAD_FOLDER = Path(tempfile.gettempdir()) / 'ct_artifact_uploads'
UPLOAD_FOLDER.mkdir(parents=True, exist_ok=True)


# ===================== 金属伪影检测核心算法 =====================
# （从 CTAnnotationTool03.py 提取）

def extract_skull_brain_mask(slice_img, bone_low=300, erode_radius=3):
    """提取颅骨和脑部掩码"""
    skull_mask = slice_img > bone_low
    skull_mask = binary_fill_holes(skull_mask)
    skull_mask = remove_small_objects(skull_mask, min_size=500)

    labeled_skull = label(skull_mask)
    num = np.max(labeled_skull) if np.sum(labeled_skull) > 0 else 0
    if num > 0:
        props = regionprops(labeled_skull)
        largest_idx = np.argmax([p.area for p in props])
        skull_mask = (labeled_skull == (largest_idx + 1))

    if erode_radius > 0:
        structure = np.ones((2 * erode_radius + 1, 2 * erode_radius + 1), dtype=bool)
        brain_mask = binary_erosion(skull_mask, structure=structure)
    else:
        brain_mask = skull_mask
    return skull_mask, brain_mask


def extract_metal_mask(slice_img, metal_low=2000):
    """检测金属植入物 (HU > metal_low)"""
    metal_mask = slice_img > metal_low
    metal_mask = remove_small_objects(metal_mask, min_size=10)
    return metal_mask


def detect_artifact_near_metal(slice_img, brain_mask, metal_mask,
                               metal_dilation=20, high_hu_threshold=500,
                               low_hu_threshold=-500):
    """检测金属附近的伪影候选区域（粗检测）"""
    if np.sum(metal_mask) == 0:
        return np.zeros_like(slice_img, dtype=bool)

    metal_region_dilated = binary_dilation(metal_mask, iterations=metal_dilation)
    artifact_mask1 = (slice_img > high_hu_threshold) & (~metal_mask)
    artifact_mask2 = (slice_img < low_hu_threshold)
    artifact_mask = (artifact_mask1 | artifact_mask2) & metal_region_dilated

    if brain_mask is not None and np.sum(brain_mask) > 0:
        artifact_mask = artifact_mask & brain_mask

    artifact_mask = remove_small_objects(artifact_mask, min_size=15)
    return artifact_mask


def classify_candidate_regions(slice_img, candidate_mask):
    """连通域分类过滤：排除钙化/出血，保留真伪影"""
    if np.sum(candidate_mask) == 0:
        return candidate_mask, {
            'removed_calcification': 0, 'removed_hemorrhage': 0,
            'removed_normal_tissue': 0, 'kept_artifact': 0, 'total_regions': 0
        }

    labeled_regions = label(candidate_mask)
    filtered = np.zeros_like(candidate_mask, dtype=bool)
    stats = {'removed_calcification': 0, 'removed_hemorrhage': 0,
             'removed_normal_tissue': 0, 'kept_artifact': 0, 'total_regions': 0}

    regions = regionprops(labeled_regions, intensity_image=slice_img)
    stats['total_regions'] = len(regions)

    for region in regions:
        if region.perimeter < 1 or region.area < 5:
            continue

        area = region.area
        perimeter = region.perimeter
        circularity = 4 * np.pi * area / (perimeter * perimeter)

        if region.minor_axis_length > 1e-6:
            elongation = region.major_axis_length / region.minor_axis_length
        else:
            elongation = 999

        solidity = region.solidity
        region_mask = labeled_regions == region.label
        hu_vals = slice_img[region_mask]
        hu_mean = float(np.mean(hu_vals))
        hu_std = float(np.std(hu_vals))
        hu_min = float(np.min(hu_vals))
        hu_max = float(np.max(hu_vals))
        hu_range = hu_max - hu_min
        spans_pos_neg = (hu_min < -100 and hu_max > 200)

        # --- 钙化判定 ---
        calc_score = 0
        if circularity > 0.55: calc_score += 2
        elif circularity > 0.40: calc_score += 1
        if elongation < 3.0: calc_score += 2
        elif elongation < 4.5: calc_score += 1
        if 100 < hu_mean < 800: calc_score += 1
        if hu_std < 200: calc_score += 2
        elif hu_std < 300: calc_score += 1
        if hu_min > -100: calc_score += 1
        if solidity > 0.7: calc_score += 1
        is_calcification = (calc_score >= 7 and area >= 10)

        # --- 出血判定 ---
        heme_score = 0
        if circularity > 0.35: heme_score += 2
        if elongation < 5.0: heme_score += 1
        if 30 < hu_mean < 150: heme_score += 2
        if hu_std < 150: heme_score += 2
        if hu_range < 400: heme_score += 1
        if hu_min > -80: heme_score += 1
        if solidity > 0.5: heme_score += 1
        is_hemorrhage = (heme_score >= 7 and area >= 15)

        # --- 真伪影判定 ---
        art_score = 0
        if elongation > 4.0 and circularity < 0.4: art_score += 3
        elif elongation > 2.5: art_score += 1
        if hu_std > 300: art_score += 3
        elif hu_std > 200: art_score += 1
        if hu_range > 800: art_score += 3
        elif hu_range > 500: art_score += 1
        if spans_pos_neg: art_score += 2
        if hu_min < -300: art_score += 2
        if circularity < 0.3: art_score += 1
        is_true_artifact = (art_score >= 4)

        # --- 正常组织判定 ---
        is_normal_tissue = (
            area >= 20 and circularity > 0.5 and
            hu_mean < 100 and hu_std < 100 and hu_min > -100
        )

        # --- 最终决策 ---
        if is_calcification and not is_true_artifact:
            stats['removed_calcification'] += 1
            continue
        if is_hemorrhage and not is_true_artifact:
            stats['removed_hemorrhage'] += 1
            continue
        if is_normal_tissue and not is_true_artifact:
            stats['removed_normal_tissue'] += 1
            continue

        filtered[region_mask] = True
        stats['kept_artifact'] += 1

    return filtered, stats


def run_full_artifact_detection(volume_np, metal_hu=2000, art_high=500,
                                art_low=-500, dilate_r=20,
                                enable_classification=True, enable_3d_check=False):
    """完整的金属伪影检测流程"""
    z_num = volume_np.shape[0]
    artifact_np = np.zeros_like(volume_np, dtype=bool)
    metal_np = np.zeros_like(volume_np, dtype=bool)

    total_removed_calc = 0
    total_removed_heme = 0
    total_removed_normal = 0
    total_kept = 0
    total_regions = 0

    for z in range(z_num):
        slice_img = volume_np[z, :, :]
        _, brain_mask = extract_skull_brain_mask(slice_img, bone_low=300, erode_radius=3)
        metal_mask = extract_metal_mask(slice_img, metal_low=metal_hu)

        if np.sum(metal_mask) > 0:
            artifact_candidate = detect_artifact_near_metal(
                slice_img, brain_mask, metal_mask,
                metal_dilation=dilate_r,
                high_hu_threshold=art_high,
                low_hu_threshold=art_low
            )

            if enable_classification and np.sum(artifact_candidate) > 0:
                artifact_mask, stats = classify_candidate_regions(slice_img, artifact_candidate)
                total_removed_calc += stats['removed_calcification']
                total_removed_heme += stats['removed_hemorrhage']
                total_removed_normal += stats['removed_normal_tissue']
                total_kept += stats['kept_artifact']
                total_regions += stats['total_regions']
            else:
                artifact_mask = artifact_candidate

            artifact_np[z, :, :] = artifact_mask
        metal_np[z, :, :] = metal_mask

    print(f"\n[伪影检测统计] 总切片: {z_num} | 分析连通域: {total_regions} | "
          f"排除钙化: {total_removed_calc} | 排除出血: {total_removed_heme} | "
          f"保留真伪影: {total_kept} | 伪影像素: {int(np.sum(artifact_np))}")

    return artifact_np, metal_np



# ===================== 急性期脑实质内血肿检测 =====================

def extract_brain_parenchyma_mask(slice_img, soft_low=20, soft_high=80,
                                   bone_low=400, min_brain_size=8000,
                                   open_radius=3):
    """
    提取真正的脑实质掩膜（两步法：颅骨环定位 → 内部软组织提取）
    
    步骤：
      1. 颅骨环定位：HU > 400 提取颅骨 → 填孔得到颅腔轮廓 → 腐蚀去骨壁
      2. 脑组织提取：颅腔内 HU 20~80 软组织 → 保留最大连通域
    
    这样颅外肌肉（颞肌/咬肌/颈部肌群）全部被颅骨环排除在外。
    """
    # ---- 步骤1: 颅骨环定位 ----
    bone_mask = slice_img > bone_low
    bone_mask = remove_small_objects(bone_mask, min_size=100)
    
    # 填孔得到完整颅腔轮廓（含颅骨+内部一切）
    skull_filled = binary_fill_holes(bone_mask)
    # 保留最大连通域（颅腔主体，排除扫描床/线缆等外部物体）
    labeled_skull = label(skull_filled)
    if np.max(labeled_skull) > 0:
        props = regionprops(labeled_skull)
        largest_idx = np.argmax([p.area for p in props])
        skull_filled = (labeled_skull == (largest_idx + 1))
    
    # 腐蚀掉颅骨壁，得到纯颅腔内部区域
    if open_radius > 0:
        structure = np.ones((2*open_radius+1, 2*open_radius+1), dtype=bool)
        cranial_cavity = binary_erosion(skull_filled, structure=structure)
    else:
        cranial_cavity = skull_filled & (~bone_mask)
    
    # ---- 步骤2: 颅腔内脑组织提取 ----
    soft_mask = (slice_img >= soft_low) & (slice_img <= soft_high)
    # 仅保留颅腔内的软组织（颅外肌肉被排除）
    brain_soft = soft_mask & cranial_cavity
    brain_soft = remove_small_objects(brain_soft, min_size=200)
    
    if np.sum(brain_soft) == 0:
        return np.zeros_like(slice_img, dtype=bool)
    
    # 填充脑沟脑室等内部低密度空隙
    brain_soft = binary_fill_holes(brain_soft)
    
    # 保留最大连通域（脑实质主体）
    labeled = label(brain_soft)
    props = regionprops(labeled)
    largest_idx = np.argmax([p.area for p in props])
    brain_soft = (labeled == (largest_idx + 1))
    
    brain_soft = remove_small_objects(brain_soft, min_size=min_brain_size)
    
    return brain_soft


def detect_brain_hemorrhage(slice_img, skull_bone_mask,
                             hu_low=60, hu_high=90,
                             min_area=100, min_circularity=0.35,
                             max_elongation=6.0):
    """
    急性期大块脑实质内血肿检测（改进版）
    
    关键改进：
      1. 使用脑实质掩膜（而非颅骨内腔）限定搜索范围
      2. 增加"周围必须有脑组织"验证，排除孤立骨内高密度
      3. 排除紧贴骨壁的窄条状钙化
    
    参数:
        hu_low/hu_high: 血肿 HU 窗口
        min_area: 最小面积(像素)，排除微小出血点
        min_circularity: 最小圆形度，类圆形团块特征
        max_elongation: 最大延伸率，排除条状伪影
    
    返回:
        hemorrhage_mask: 血肿区域掩码 (bool)
        stats: 统计信息
    """
    # 步骤1: 提取真正的脑实质掩膜
    brain_parenchyma = extract_brain_parenchyma_mask(slice_img)
    
    if np.sum(brain_parenchyma) == 0:
        return np.zeros_like(slice_img, dtype=bool), {
            'total_regions': 0, 'hemorrhage_count': 0, 'total_area': 0
        }
    
    # 步骤2: 软组织窗 HU 高密度候选（仅限脑实质内）
    hu_mask = (slice_img >= hu_low) & (slice_img <= hu_high)
    candidate_mask = hu_mask & brain_parenchyma
    candidate_mask = remove_small_objects(candidate_mask, min_size=30)
    
    if np.sum(candidate_mask) == 0:
        return np.zeros_like(slice_img, dtype=bool), {
            'total_regions': 0, 'hemorrhage_count': 0, 'total_area': 0
        }
    
    # 步骤3: 排除紧贴颅骨/骨壁的伪钙化/金属碎屑
    # 颅骨向外膨胀 3 像素 → 候选区必须在膨胀区之外（完全在脑实质内）
    bone_dilated = binary_dilation(skull_bone_mask, iterations=3) if skull_bone_mask is not None else np.zeros_like(slice_img, dtype=bool)
    candidate_mask = candidate_mask & (~bone_dilated)
    candidate_mask = remove_small_objects(candidate_mask, min_size=30)
    
    if np.sum(candidate_mask) == 0:
        return np.zeros_like(slice_img, dtype=bool), {
            'total_regions': 0, 'hemorrhage_count': 0, 'total_area': 0
        }
    
    # 步骤4: 连通域形态学分析（类圆形 + 均匀高密度 = 血肿）
    labeled_regions = label(candidate_mask)
    regions = regionprops(labeled_regions, intensity_image=slice_img)
    
    hemorrhage_mask = np.zeros_like(slice_img, dtype=bool)
    heme_count = 0
    total_area = 0
    
    for region in regions:
        if region.area < min_area:
            continue
        
        # 圆形度
        circularity = 4 * np.pi * region.area / max(region.perimeter ** 2, 1e-6)
        # 延伸率
        elongation = region.major_axis_length / max(region.minor_axis_length, 1e-6)
        
        # HU 统计
        hu_vals = slice_img[labeled_regions == region.label]
        hu_mean = float(np.mean(hu_vals))
        hu_std = float(np.std(hu_vals))
        
        # 步骤5: "周围必须有脑组织"验证
        # 血肿周边膨胀环内必须有足够比例的脑组织像素
        region_mask = labeled_regions == region.label
        ring = binary_dilation(region_mask, iterations=4) & (~region_mask)
        ring_pixels = np.sum(ring)
        if ring_pixels > 0:
            ring_in_brain = np.sum(ring & brain_parenchyma)
            brain_ratio = ring_in_brain / ring_pixels
        else:
            brain_ratio = 0
        
        # 血肿判定:
        # 1. 类圆形团块状
        # 2. HU 60~90 范围内且均匀
        # 3. 周围被脑组织包裹（排除孤立骨内高密度）
        is_hemorrhage = (
            circularity > min_circularity and
            elongation < max_elongation and
            hu_low <= hu_mean <= hu_high and
            hu_std < 80 and                        # 密度均匀
            brain_ratio > 0.5                      # 周围>50%是脑组织
        )
        
        if is_hemorrhage:
            hemorrhage_mask[region_mask] = True
            heme_count += 1
            total_area += region.area
    
    stats = {
        'total_regions': len(regions),
        'hemorrhage_count': heme_count,
        'total_area': total_area
    }
    
    return hemorrhage_mask, stats


def run_full_hemorrhage_detection(volume_np, hu_low=60, hu_high=90,
                                   min_area=100, min_circularity=0.35):
    """完整的三维脑出血检测（改进版）"""
    z_num = volume_np.shape[0]
    heme_np = np.zeros_like(volume_np, dtype=bool)
    
    total_heme_count = 0
    total_heme_area = 0
    slices_with_heme = 0
    
    for z in range(z_num):
        slice_img = volume_np[z]
        # 提取颅骨掩膜（用于骨壁排除）
        skull_bone = slice_img > 300
        skull_bone = remove_small_objects(skull_bone, min_size=200)
        
        heme_mask, stats = detect_brain_hemorrhage(
            slice_img, skull_bone,
            hu_low=hu_low, hu_high=hu_high,
            min_area=min_area, min_circularity=min_circularity
        )
        
        heme_np[z] = heme_mask
        total_heme_count += stats['hemorrhage_count']
        total_heme_area += stats['total_area']
        if stats['hemorrhage_count'] > 0:
            slices_with_heme += 1
    
    print(f"\n[脑出血检测统计] 总切片: {z_num} | "
          f"检出出血灶: {total_heme_count} | "
          f"累及切片: {slices_with_heme} | "
          f"总出血面积: {total_heme_area} px")
    
    return heme_np, {
        'totalHemorrhages': total_heme_count,
        'slicesWithHemorrhage': slices_with_heme,
        'totalArea': total_heme_area
    }


# ===================== CT影像加载 =====================

def load_ct_volume(file_path):
    """加载CT体数据，支持DICOM文件夹(zip) / MHD / NIfTI / 单张图片"""
    path = Path(file_path)

    if path.is_dir():
        # DICOM 序列文件夹
        reader = sitk.ImageSeriesReader()
        fns = reader.GetGDCMSeriesFileNames(str(path))
        if not fns:
            raise ValueError("未找到DICOM序列文件")
        reader.SetFileNames(fns)
        img = reader.Execute()
    elif path.suffix.lower() in ['.mhd']:
        img = sitk.ReadImage(str(path))
    elif path.suffix.lower() in ['.nii', '.gz']:
        img = sitk.ReadImage(str(path))
    else:
        img = sitk.ReadImage(str(path))
        vol = sitk.GetArrayFromImage(img)
        if vol.ndim == 2:
            vol = vol[None, :, :]
            img = sitk.GetImageFromArray(vol)

    vol = sitk.GetArrayFromImage(img)
    return img, vol.astype(np.float32)


# ===================== 切片图像生成 =====================

def generate_raw_slice_image(slice_data, window_center=40, window_width=400):
    """生成原始CT切片图像（无任何标注）"""
    low = window_center - window_width / 2
    high = window_center + window_width / 2
    gray = np.clip(slice_data.astype(np.float32), low, high)
    gray = ((gray - low) / (high - low + 1e-8) * 255).astype(np.uint8)
    rgb = np.stack([gray, gray, gray], axis=-1)
    return rgb.astype(np.uint8)


def generate_slice_image(slice_data, mask_data, window_center=40, window_width=400):
    """
    生成带伪影标注的切片图像
    mask_data: 0=无, 1=伪影(红色), 2=金属(青色)
    """
    low = window_center - window_width / 2
    high = window_center + window_width / 2
    gray = np.clip(slice_data.astype(np.float32), low, high)
    gray = ((gray - low) / (high - low + 1e-8) * 255).astype(np.uint8)
    rgb = np.stack([gray, gray, gray], axis=-1).astype(np.float32)

    if mask_data is not None and mask_data.shape == slice_data.shape and np.sum(mask_data) > 0:
        # 伪影 -> 红色半透明
        art_mask = (mask_data == 1)
        rgb[art_mask] = 0.5 * rgb[art_mask] + 0.5 * np.array([255, 0, 0], dtype=np.float32)
        # 金属 -> 青色半透明
        metal_mask = (mask_data == 2)
        rgb[metal_mask] = 0.5 * rgb[metal_mask] + 0.5 * np.array([0, 255, 255], dtype=np.float32)

    return np.clip(rgb, 0, 255).astype(np.uint8)


def generate_hemorrhage_slice_image(slice_data, heme_mask, window_center=50, window_width=100):
    """
    生成带脑出血标注的切片图像
    heme_mask: bool数组, True=出血区域（绿色高亮）
    """
    low = window_center - window_width / 2
    high = window_center + window_width / 2
    gray = np.clip(slice_data.astype(np.float32), low, high)
    gray = ((gray - low) / (high - low + 1e-8) * 255).astype(np.uint8)
    rgb = np.stack([gray, gray, gray], axis=-1).astype(np.float32)

    if heme_mask is not None and np.sum(heme_mask) > 0:
        # 出血 -> 绿色半透明高亮
        rgb[heme_mask] = 0.5 * rgb[heme_mask] + 0.5 * np.array([0, 255, 0], dtype=np.float32)

    return np.clip(rgb, 0, 255).astype(np.uint8)


def generate_all_slice_images(volume_np, mask_np):
    """生成所有轴位切片的标注图像，返回base64列表"""
    z_num = volume_np.shape[0]
    slices = []

    for z in range(z_num):
        slice_data = volume_np[z]
        mask_data = mask_np[z] if mask_np is not None else None
        rgb = generate_slice_image(slice_data, mask_data)

        # 转为 PNG base64
        pil_img = Image.fromarray(rgb)
        buf = io.BytesIO()
        pil_img.save(buf, format='PNG', optimize=True)
        b64 = base64.b64encode(buf.getvalue()).decode('utf-8')
        slices.append(b64)

    return slices


# ===================== API路由 =====================

@app.route('/api/health', methods=['GET'])
def health():
    """健康检查"""
    return jsonify({
        'status': 'ok',
        'simpleitk': sitk is not None,
        'message': 'CT金属伪影检测服务运行中'
    })


@app.route('/api/analyze', methods=['POST'])
def analyze_ct():
    """
    CT金属伪影检测接口
    
    接收: multipart/form-data
      - file: CT文件 (DICOM zip / MHD+RAW tar.gz / NIfTI)
      - params: JSON字符串，可选算法参数
        {
          "metal_hu": 2000,      // 金属HU阈值
          "art_high": 500,       // 伪影高HU阈值
          "art_low": -500,       // 伪影低HU阈值
          "dilate_r": 20,        // 金属膨胀半径
          "enable_classify": true,// 启用形态学分类
          "enable_3d": false,    // 启用3D一致性
          "return_all_slices": false  // 是否返回全部切片（默认采样）
        }
    
    返回: JSON
      {
        "success": true,
        "data": {
          "totalSlices": 256,
          "sliceCount": 10,           // 实际返回的切片数
          "slices": ["base64...", ...],
          "sliceIndices": [0, 25, 50, ...],
          "volumeShape": [256, 512, 512],
          "stats": {
            "artifactPixels": 12345,
            "metalPixels": 678,
            ...
          }
        }
      }
    """
    if 'file' not in request.files:
        return jsonify({'success': False, 'error': '未找到上传文件'}), 400

    file = request.files['file']
    if file.filename == '':
        return jsonify({'success': False, 'error': '文件名为空'}), 400

    # 解析参数
    params_str = request.form.get('params', '{}')
    try:
        params = json.loads(params_str)
    except json.JSONDecodeError:
        params = {}

    metal_hu = params.get('metal_hu', 2000)
    art_high = params.get('art_high', 500)
    art_low = params.get('art_low', -500)
    dilate_r = params.get('dilate_r', 20)
    enable_classify = params.get('enable_classify', True)
    enable_3d = params.get('enable_3d', False)
    return_all = params.get('return_all_slices', False)
    max_slices = params.get('max_slices', 50)

    # 保存上传文件
    temp_dir = UPLOAD_FOLDER / f"ct_{os.urandom(8).hex()}"
    temp_dir.mkdir(parents=True, exist_ok=True)

    try:
        file_path = temp_dir / file.filename
        file.save(str(file_path))

        # 如果是zip文件，解压
        if file.filename.lower().endswith('.zip'):
            extract_dir = temp_dir / 'dicom'
            extract_dir.mkdir(exist_ok=True)
            with zipfile.ZipFile(str(file_path), 'r') as zf:
                zf.extractall(str(extract_dir))
            load_path = str(extract_dir)
        else:
            load_path = str(file_path)

        # 加载CT体数据
        print(f"加载CT数据: {load_path}")
        ct_image, volume_np = load_ct_volume(load_path)
        print(f"CT体数据形状: {volume_np.shape}")

        # 执行金属伪影检测
        print("开始金属伪影检测...")
        artifact_np, metal_np = run_full_artifact_detection(
            volume_np,
            metal_hu=metal_hu,
            art_high=art_high,
            art_low=art_low,
            dilate_r=dilate_r,
            enable_classification=enable_classify,
            enable_3d_check=enable_3d
        )

        # 合并掩码: 1=伪影, 2=金属
        mask_np = np.zeros_like(artifact_np, dtype=np.uint8)
        mask_np[artifact_np] = 1
        mask_np[metal_np] = 2

        # 执行急性期脑出血检测
        print("开始脑出血检测...")
        heme_np, heme_stats = run_full_hemorrhage_detection(
            volume_np, hu_low=60, hu_high=90, min_area=100, min_circularity=0.35
        )

        # 生成切片图像
        z_num = volume_np.shape[0]

        if return_all or z_num <= max_slices:
            slice_indices = list(range(z_num))
        else:
            # 采样：均匀取指定数量的切片
            step = max(1, z_num // max_slices)
            slice_indices = list(range(0, z_num, step))[:max_slices]

        print(f"生成 {len(slice_indices)} 张切片图像（原始+伪影+出血）...")
        raw_slices_b64 = []        # 原始CT（无标注）
        artifact_slices_b64 = []   # 金属伪影标注
        heme_slices_b64 = []       # 脑出血标注
        
        for z in slice_indices:
            # 原始图像
            raw_rgb = generate_raw_slice_image(volume_np[z])
            pil = Image.fromarray(raw_rgb)
            buf = io.BytesIO()
            pil.save(buf, format='PNG', optimize=True)
            raw_slices_b64.append(base64.b64encode(buf.getvalue()).decode('utf-8'))
            
            # 伪影标注图像（红色=伪影，青色=金属）
            art_rgb = generate_slice_image(volume_np[z], mask_np[z])
            pil = Image.fromarray(art_rgb)
            buf = io.BytesIO()
            pil.save(buf, format='PNG', optimize=True)
            artifact_slices_b64.append(base64.b64encode(buf.getvalue()).decode('utf-8'))
            
            # 出血标注图像（绿色=血肿）
            heme_rgb = generate_hemorrhage_slice_image(volume_np[z], heme_np[z])
            pil = Image.fromarray(heme_rgb)
            buf = io.BytesIO()
            pil.save(buf, format='PNG', optimize=True)
            heme_slices_b64.append(base64.b64encode(buf.getvalue()).decode('utf-8'))

        # 统计信息
        stats = {
            'totalSlices': z_num,
            'volumeShape': list(volume_np.shape),
            'artifactPixels': int(np.sum(artifact_np)),
            'metalPixels': int(np.sum(metal_np)),
            'slicesWithMetal': int(np.sum([1 for z in range(z_num) if np.sum(metal_np[z]) > 0])),
            'slicesWithArtifact': int(np.sum([1 for z in range(z_num) if np.sum(artifact_np[z]) > 0])),
            'hemorrhageCount': heme_stats['totalHemorrhages'],
            'hemorrhageTotalArea': heme_stats['totalArea'],
            'slicesWithHemorrhage': heme_stats['slicesWithHemorrhage'],
        }

        result = {
            'success': True,
            'data': {
                'totalSlices': z_num,
                'sliceCount': len(slice_indices),
                'rawSlices': raw_slices_b64,          # 原始CT（无标注，CT影像浏览用）
                'slices': artifact_slices_b64,         # 金属伪影标注（红色+青色）
                'hemorrhageSlices': heme_slices_b64,   # 脑出血标注（绿色）
                'sliceIndices': slice_indices,
                'volumeShape': list(volume_np.shape),
                'stats': stats
            }
        }

        print(f"分析完成: {stats}")
        return jsonify(result)

    except Exception as e:
        traceback.print_exc()
        return jsonify({'success': False, 'error': str(e)}), 500

    finally:
        # 清理临时文件
        import shutil
        try:
            shutil.rmtree(str(temp_dir))
        except Exception:
            pass


@app.route('/api/analyze-slice', methods=['POST'])
def analyze_single_slice():
    """
    分析单个切片图像（用于ZIP包上传后逐层查看）
    返回该层金属伪影标注的图像
    """
    if 'file' not in request.files:
        return jsonify({'success': False, 'error': '未找到上传文件'}), 400

    files = request.files.getlist('file')
    slice_index = int(request.form.get('sliceIndex', 0))

    try:
        temp_dir = UPLOAD_FOLDER / f"ct_slice_{os.urandom(4).hex()}"
        temp_dir.mkdir(parents=True, exist_ok=True)

        # 保存所有切片
        slice_files = []
        for f in files:
            fpath = temp_dir / f.filename
            f.save(str(fpath))
            slice_files.append(str(fpath))

        # 加载
        reader = sitk.ImageSeriesReader()
        reader.SetFileNames(slice_files)
        img = reader.Execute()
        volume_np = sitk.GetArrayFromImage(img).astype(np.float32)

        # 检测
        artifact_np, metal_np = run_full_artifact_detection(
            volume_np, enable_classification=True, enable_3d_check=False
        )

        mask_np = np.zeros_like(artifact_np, dtype=np.uint8)
        mask_np[artifact_np] = 1
        mask_np[metal_np] = 2

        # 生成指定切片
        z = min(slice_index, volume_np.shape[0] - 1)
        rgb = generate_slice_image(volume_np[z], mask_np[z])

        pil_img = Image.fromarray(rgb)
        buf = io.BytesIO()
        pil_img.save(buf, format='PNG', optimize=True)
        b64 = base64.b64encode(buf.getvalue()).decode('utf-8')

        return jsonify({
            'success': True,
            'data': {
                'sliceIndex': z,
                'totalSlices': volume_np.shape[0],
                'image': b64,
                'artifactPixels': int(np.sum(artifact_np[z])),
                'metalPixels': int(np.sum(metal_np[z])),
            }
        })

    except Exception as e:
        traceback.print_exc()
        return jsonify({'success': False, 'error': str(e)}), 500
    finally:
        import shutil
        try:
            shutil.rmtree(str(temp_dir))
        except Exception:
            pass


# ===================== 启动 =====================

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='CT金属伪影检测服务')
    parser.add_argument('--port', type=int, default=5001, help='服务端口 (默认5001)')
    parser.add_argument('--host', type=str, default='127.0.0.1', help='绑定地址')
    args = parser.parse_args()

    print(f"{'='*60}")
    print(f"  CT金属伪影检测服务 v1.0")
    print(f"  地址: http://{args.host}:{args.port}")
    print(f"  健康检查: http://{args.host}:{args.port}/api/health")
    print(f"{'='*60}")

    app.run(host=args.host, port=args.port, debug=False)
