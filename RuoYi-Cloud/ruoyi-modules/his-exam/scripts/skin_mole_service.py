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

# 将脚本所在目录加入 sys.path，以便直接 import 核心算法文件
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
TOOL_DIR = r"D:\大三下课程\实训\CQ500数据处理\VTK"
sys.path.insert(0, TOOL_DIR)

from SkinMoleDetectionTool002 import (
    segment_skin_moles,
    estimate_skin_mask,
    build_lesion_score,
    remove_hair_and_specular_noise,
)


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
