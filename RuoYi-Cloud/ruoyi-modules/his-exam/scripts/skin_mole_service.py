"""
==================== 皮肤痣检测 Flask 服务 ====================
接收皮肤图片，调用 SkinMoleDetectionTool002 核心算法，
返回检测结果（是否发现、数量、置信度、标注图 base64）。

启动方式：
    python skin_mole_service.py --port 5002

API:
    GET  /api/health     — 健康检查
    POST /api/analyze    — 上传图片进行分析（multipart: file）
============================================================
"""

import argparse
import base64
import io
import sys
import os
import traceback

import cv2
import numpy as np
from flask import Flask, jsonify, request, send_file

# 将脚本所在目录加入 sys.path，以便用相对路径 import 核心算法文件
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
TOOL_DIR = os.path.join(SCRIPT_DIR)
sys.path.insert(0, SCRIPT_DIR)
sys.path.insert(0, TOOL_DIR)

try:
    from SkinMoleDetectionTool002 import (
        segment_skin_moles,
        estimate_skin_mask,
        build_lesion_score,
        remove_hair_and_specular_noise,
    )
    DETECTOR_BACKEND = "SkinMoleDetectionTool002"
except ModuleNotFoundError:
    DETECTOR_BACKEND = "builtin-opencv"

    def estimate_skin_mask(image_bgr):
        """估计皮肤区域，返回 bool mask。"""
        hsv = cv2.cvtColor(image_bgr, cv2.COLOR_BGR2HSV)
        ycrcb = cv2.cvtColor(image_bgr, cv2.COLOR_BGR2YCrCb)

        h, s, v = cv2.split(hsv)
        y, cr, cb = cv2.split(ycrcb)

        hsv_mask = (h < 30) & (s > 20) & (v > 60)
        ycrcb_mask = (cr > 130) & (cr < 180) & (cb > 75) & (cb < 145) & (y > 40)
        mask = (hsv_mask | ycrcb_mask).astype(np.uint8)

        kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (9, 9))
        mask = cv2.morphologyEx(mask, cv2.MORPH_CLOSE, kernel, iterations=2)
        mask = cv2.morphologyEx(mask, cv2.MORPH_OPEN, kernel, iterations=1)
        return mask.astype(bool)

    def remove_hair_and_specular_noise(image_bgr):
        """简单去除黑色毛发和高光噪声。"""
        gray = cv2.cvtColor(image_bgr, cv2.COLOR_BGR2GRAY)
        hair_kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (17, 17))
        blackhat = cv2.morphologyEx(gray, cv2.MORPH_BLACKHAT, hair_kernel)
        hair_mask = blackhat > 18

        hsv = cv2.cvtColor(image_bgr, cv2.COLOR_BGR2HSV)
        highlight_mask = (hsv[:, :, 2] > 245) & (hsv[:, :, 1] < 45)
        noise_mask = (hair_mask | highlight_mask).astype(np.uint8) * 255
        if noise_mask.any():
            return cv2.inpaint(image_bgr, noise_mask, 3, cv2.INPAINT_TELEA)
        return image_bgr

    def build_lesion_score(image_bgr, skin_mask=None, local_radius=41):
        """生成疑似皮损评分图，暗色、边界清晰区域得分更高。"""
        if skin_mask is None:
            skin_mask = estimate_skin_mask(image_bgr)

        lab = cv2.cvtColor(image_bgr, cv2.COLOR_BGR2LAB)
        l_channel = lab[:, :, 0]
        gray = cv2.cvtColor(image_bgr, cv2.COLOR_BGR2GRAY)

        radius = max(15, int(local_radius))
        if radius % 2 == 0:
            radius += 1
        local_bg = cv2.GaussianBlur(l_channel, (radius, radius), 0)
        dark_score = cv2.subtract(local_bg, l_channel)

        edges = cv2.Laplacian(gray, cv2.CV_16S, ksize=3)
        edge_score = cv2.convertScaleAbs(edges)

        score = cv2.normalize(dark_score, None, 0, 255, cv2.NORM_MINMAX)
        edge_score = cv2.normalize(edge_score, None, 0, 80, cv2.NORM_MINMAX)
        score = cv2.addWeighted(score.astype(np.uint8), 0.85, edge_score.astype(np.uint8), 0.15, 0)
        score[~skin_mask] = 0
        return score

    def segment_skin_moles(
        image_bgr,
        local_radius=41,
        threshold_bias=0,
        min_area=200,
        enable_hair_removal=True,
        force_largest_only=True,
    ):
        """内置皮肤痣分割算法，兼容外部工具函数返回格式。"""
        work_img = remove_hair_and_specular_noise(image_bgr) if enable_hair_removal else image_bgr
        skin_mask = estimate_skin_mask(work_img)
        score_map = build_lesion_score(work_img, skin_mask, local_radius=local_radius)

        skin_scores = score_map[skin_mask]
        if skin_scores.size == 0:
            empty = np.zeros(score_map.shape, dtype=bool)
            return empty, score_map, skin_mask, [], {
                "threshold": 0,
                "kept_moles": 0,
                "removed_small": 0,
                "removed_shape": 0,
            }

        threshold = int(max(35, np.percentile(skin_scores, 92) + threshold_bias))
        raw_mask = (score_map >= threshold) & skin_mask
        raw_mask = raw_mask.astype(np.uint8)

        kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (5, 5))
        raw_mask = cv2.morphologyEx(raw_mask, cv2.MORPH_OPEN, kernel, iterations=1)
        raw_mask = cv2.morphologyEx(raw_mask, cv2.MORPH_CLOSE, kernel, iterations=2)

        contours, _ = cv2.findContours(raw_mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
        candidates = []
        removed_small = 0
        removed_shape = 0

        for cnt in contours:
            area = cv2.contourArea(cnt)
            if area < min_area:
                removed_small += 1
                continue
            perimeter = cv2.arcLength(cnt, True)
            if perimeter <= 0:
                removed_shape += 1
                continue
            circularity = 4 * np.pi * area / (perimeter * perimeter)
            x, y, w, h = cv2.boundingRect(cnt)
            aspect = w / max(h, 1)
            if circularity < 0.18 or aspect > 5 or aspect < 0.2:
                removed_shape += 1
                continue
            candidates.append({
                "contour": cnt,
                "area": area,
                "circularity": circularity,
                "bbox": (x, y, w, h),
            })

        if force_largest_only and candidates:
            candidates = [max(candidates, key=lambda item: item["area"])]

        mask = np.zeros(score_map.shape, dtype=np.uint8)
        for item in candidates:
            cv2.drawContours(mask, [item["contour"]], -1, 1, thickness=-1)

        return mask.astype(bool), score_map, skin_mask, candidates, {
            "threshold": threshold,
            "kept_moles": len(candidates),
            "removed_small": removed_small,
            "removed_shape": removed_shape,
        }


app = Flask(__name__)


def numpy_to_base64(img, ext=".png"):
    """将 OpenCV 图像编码为 base64 字符串"""
    _, buf = cv2.imencode(ext, img)
    return base64.b64encode(buf).decode("utf-8")


def draw_annotations(image_bgr, mask):
    """在原始图像上绘制检测框和轮廓"""
    annotated = image_bgr.copy()
    contours, _ = cv2.findContours(
        mask.astype(np.uint8), cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

    for cnt in contours:
        if len(cnt) < 5:
            continue
        x, y, w, h = cv2.boundingRect(cnt)
        area = cv2.contourArea(cnt)
        if area < 100:
            continue
        cv2.rectangle(annotated, (x, y), (x + w, y + h), (0, 255, 0), 2)
        cv2.drawContours(annotated, [cnt], -1, (0, 200, 0), 1)
        cv2.putText(annotated, f"Area:{int(area)}px",
                    (x, max(y - 8, 10)), cv2.FONT_HERSHEY_SIMPLEX,
                    0.45, (0, 255, 0), 1)

    return annotated


@app.route("/api/health", methods=["GET"])
def health():
    return jsonify({"status": "ok", "service": "skin_mole_detection"})


@app.route("/api/analyze", methods=["POST"])
def analyze():
    try:
        if "file" not in request.files:
            return jsonify({"error": "没有上传文件"}), 400

        file = request.files["file"]
        img_bytes = file.read()
        nparr = np.frombuffer(img_bytes, np.uint8)
        image_bgr = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

        if image_bgr is None:
            return jsonify({"error": "无法解析图片"}), 400

        h, w = image_bgr.shape[:2]

        # 调整过大图片以提高速度
        max_dim = 1200
        if max(h, w) > max_dim:
            scale = max_dim / max(h, w)
            image_bgr = cv2.resize(image_bgr, (int(w * scale), int(h * scale)))

        # 运行核心算法
        mask, score_map, skin_mask, regions, stats = segment_skin_moles(
            image_bgr,
            local_radius=41,
            threshold_bias=0,
            min_area=200,
            enable_hair_removal=True,
            force_largest_only=True,
        )

        # 计算检测结果
        mole_region_count = stats.get("kept_moles", 0)
        has_mole = mole_region_count > 0
        confidence = 0
        if has_mole and mask.any():
            skin_scores = score_map[mask]
            confidence = min(99, int(np.percentile(skin_scores, 90) / 255.0 * 100))

        # 生成标注图
        annotated_image = draw_annotations(image_bgr, mask)

        # 构建返回结果
        result = {
            "hasMole": has_mole,
            "moleCount": mole_region_count,
            "confidence": confidence,
            "threshold": stats.get("threshold", 0),
            "removedSmall": stats.get("removed_small", 0),
            "removedShape": stats.get("removed_shape", 0),
            "annotatedImage": numpy_to_base64(annotated_image),
            "maskImage": numpy_to_base64(cv2.cvtColor(
                (mask.astype(np.uint8) * 255), cv2.COLOR_GRAY2BGR)),
            "scoreMapImage": numpy_to_base64(cv2.applyColorMap(
                (score_map).astype(np.uint8), cv2.COLORMAP_JET)),
            "imageWidth": image_bgr.shape[1],
            "imageHeight": image_bgr.shape[0],
        }

        return jsonify(result)

    except Exception as e:
        traceback.print_exc()
        return jsonify({"error": str(e)}), 500


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="皮肤痣检测 Flask 服务")
    parser.add_argument("--port", type=int, default=5002, help="服务端口")
    parser.add_argument("--host", type=str, default="127.0.0.1", help="绑定地址")
    args = parser.parse_args()

    print(f"[皮肤检测服务] 启动于 http://{args.host}:{args.port}")
    app.run(host=args.host, port=args.port, debug=False)
