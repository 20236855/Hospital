import sys
import os
import SimpleITK as sitk
import numpy as np
from PySide6.QtWidgets import *
from PySide6.QtCore import *
from PySide6.QtGui import *

import vtk
from vtkmodules.qt.QVTKRenderWindowInteractor import QVTKRenderWindowInteractor

# 算法依赖库
from scipy.ndimage import binary_erosion, binary_fill_holes, binary_dilation
from skimage.measure import regionprops, label
from skimage.morphology import remove_small_objects

# ===================== 全局样式表 =====================
APP_STYLESHEET = """
/* 全局背景 */
QMainWindow, QWidget {
    background-color: #1a1a2e;
    color: #e0e0e0;
}

/* 左侧控制面板 */
QWidget#controlPanel {
    background-color: #16213e;
    border-right: 2px solid #0f3460;
}

/* 按钮样式 */
QPushButton {
    background: qlineargradient(x1:0, y1:0, x2:0, y2:1,
                stop:0 #0f3460, stop:1 #0a1a3a);
    color: #e0e0e0;
    border: 1px solid #0f3460;
    border-radius: 6px;
    padding: 8px 12px;
    font-size: 12px;
    font-weight: bold;
    min-height: 28px;
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

/* 生成掩码按钮 - 绿色 */
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

/* 保存按钮 - 橙色 */
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

/* 滑动条 */
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

/* 标签 */
QLabel {
    color: #c0c0c0;
    font-size: 11px;
}

/* 切片标签 */
QLabel#sliceLabel {
    color: #00d4ff;
    font-weight: bold;
    font-size: 12px;
    background-color: #0f3460;
    padding: 4px 8px;
    border-radius: 4px;
}

/* 视图标题 */
QLabel#viewTitle {
    color: #00d4ff;
    font-weight: bold;
    font-size: 13px;
    padding: 2px 6px;
}

/* 进度条 */
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

/* 对话框 */
QDialog {
    background-color: #16213e;
}
QDialog QLabel {
    color: #e0e0e0;
}

/* 消息框 */
QMessageBox {
    background-color: #16213e;
}
QMessageBox QLabel {
    color: #e0e0e0;
}

/* 滚动条 */
QScrollBar:vertical {
    background: #0f3460;
    width: 8px;
    border-radius: 4px;
}
QScrollBar::handle:vertical {
    background: #1a4a8a;
    border-radius: 4px;
    min-height: 20px;
}
QScrollBar::handle:vertical:hover {
    background: #00d4ff;
}
"""

# ===================== 金属伪影检测核心算法 =====================
def extract_skull_brain_mask(slice_img, bone_low=300, erode_radius=3):
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
    metal_mask = slice_img > metal_low
    metal_mask = remove_small_objects(metal_mask, min_size=20)
    return metal_mask


def detect_artifact_near_metal(slice_img, brain_mask, metal_mask,
                               metal_dilation=20,
                               high_hu_threshold=500,
                               low_hu_threshold=-500):
    if np.sum(metal_mask) == 0:
        return np.zeros_like(slice_img, dtype=bool)

    metal_region_dilated = binary_dilation(metal_mask, iterations=metal_dilation)
    artifact_mask1 = (slice_img > high_hu_threshold) & (~metal_mask)
    artifact_mask2 = (slice_img < low_hu_threshold)
    artifact_mask = (artifact_mask1 | artifact_mask2) & metal_region_dilated
    artifact_mask = remove_small_objects(artifact_mask, min_size=30)
    return artifact_mask


def run_full_artifact_detection(volume_np, metal_hu=2000, art_high=500, art_low=-500, dilate_r=20):
    z_num, h_num, w_num = volume_np.shape
    artifact_np = np.zeros_like(volume_np, dtype=bool)
    metal_np = np.zeros_like(volume_np, dtype=bool)

    for z in range(z_num):
        slice_img = volume_np[z, :, :]
        _, brain_mask = extract_skull_brain_mask(slice_img, bone_low=300, erode_radius=3)
        metal_mask = extract_metal_mask(slice_img, metal_low=metal_hu)

        if np.sum(metal_mask) > 0:
            artifact_mask = detect_artifact_near_metal(
                slice_img, brain_mask, metal_mask,
                metal_dilation=dilate_r,
                high_hu_threshold=art_high,
                low_hu_threshold=art_low
            )
            artifact_np[z, :, :] = artifact_mask
        metal_np[z, :, :] = metal_mask

    return artifact_np, metal_np

# ===================== 放大镜 QLabel =====================
class MagnifierLabel(QLabel):
    """支持鼠标悬浮局部放大的 QLabel"""
    def __init__(self, parent=None):
        super().__init__(parent)
        self.setMouseTracking(True)
        self.original_image = None
        self.zoom_factor = 3
        self.magnifier_size = 120
        self.is_hovering = False
        self.hover_pos = QPoint()

    def set_original_image(self, arr):
        """保存原始 numpy 图像数据"""
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

        pos = self.hover_pos
        w, h = self.width(), self.height()
        if pos.x() < 0 or pos.y() < 0 or pos.x() >= w or pos.y() >= h:
            return

        pix = self.pixmap()
        if pix is None:
            return

        pw, ph = pix.width(), pix.height()
        offset_x = (w - pw) // 2
        offset_y = (h - ph) // 2

        ix = pos.x() - offset_x
        iy = pos.y() - offset_y

        if ix < 0 or iy < 0 or ix >= pw or iy >= ph:
            return

        img_h, img_w = self.original_image.shape[:2]
        orig_x = int(ix / pw * img_w)
        orig_y = int(iy / ph * img_h)

        half = self.magnifier_size // (2 * self.zoom_factor)
        x1 = max(0, orig_x - half)
        y1 = max(0, orig_y - half)
        x2 = min(img_w, orig_x + half)
        y2 = min(img_h, orig_y + half)

        region = np.ascontiguousarray(self.original_image[y1:y2, x1:x2])
        if region.size == 0:
            return

        zoomed = QImage(region.data, region.shape[1], region.shape[0],
                        3 * region.shape[1], QImage.Format_RGB888)
        zoomed_pixmap = QPixmap.fromImage(zoomed).scaled(
            self.magnifier_size, self.magnifier_size,
            Qt.KeepAspectRatio, Qt.SmoothTransformation)

        painter = QPainter(self)
        painter.setRenderHint(QPainter.Antialiasing)

        mag_x = pos.x() + 15
        mag_y = pos.y() + 15
        if mag_x + self.magnifier_size > w:
            mag_x = pos.x() - self.magnifier_size - 15
        if mag_y + self.magnifier_size > h:
            mag_y = pos.y() - self.magnifier_size - 15

        path = QPainterPath()
        path.addEllipse(mag_x, mag_y, self.magnifier_size, self.magnifier_size)
        painter.setClipPath(path)

        painter.drawPixmap(mag_x, mag_y, zoomed_pixmap)

        painter.setClipping(False)
        pen = QPen(QColor("#00d4ff"), 2)
        painter.setPen(pen)
        painter.drawEllipse(mag_x, mag_y, self.magnifier_size, self.magnifier_size)

        pen2 = QPen(QColor("#ff4444"), 1, Qt.DashLine)
        painter.setPen(pen2)
        cx = mag_x + self.magnifier_size // 2
        cy = mag_y + self.magnifier_size // 2
        painter.drawLine(cx - 10, cy, cx + 10, cy)
        painter.drawLine(cx, cy - 10, cx, cy + 10)

# ===================== 异步进度条弹窗 =====================
class ProgressDialog(QDialog):
    def __init__(self, title="处理中...", parent=None):
        super().__init__(parent)
        self.setWindowTitle(title)
        self.setFixedSize(450, 120)
        self.setModal(True)

        layout = QVBoxLayout(self)
        layout.setContentsMargins(30, 20, 30, 20)
        layout.setSpacing(12)

        self.label = QLabel("初始化处理...")
        self.label.setAlignment(Qt.AlignCenter)
        layout.addWidget(self.label)

        self.progress = QProgressBar()
        self.progress.setRange(0, 100)
        layout.addWidget(self.progress)

    def set_progress(self, value, text=""):
        self.progress.setValue(value)
        if text:
            self.label.setText(text)

# ===================== 工作线程（异步执行） =====================
class Worker(QThread):
    progress = Signal(int, str)
    finished = Signal()
    error = Signal(str)
    success_msg = Signal(str)

    def __init__(self, func, *args, parent=None):
        super().__init__(parent)
        self.func = func
        self.args = args

    def run(self):
        try:
            self.func(self.report_progress, *self.args)
            self.finished.emit()
        except Exception as e:
            self.error.emit(str(e))

    def report_progress(self, value, text=""):
        self.progress.emit(value, text)

# ===================== 主界面 =====================
class ArtifactAnnotationWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("CT金属伪影掩码标注系统 - 支持DICOM/MHD + VTK 3D视图")
        self.resize(1600, 900)

        self.ct_image = None
        self.artifact_mask = None
        self.volume_np = None
        self.mask_np = None

        self.slice_axial = 0
        self.slice_coronal = 0
        self.slice_sagittal = 0
        self.win_center = 40
        self.win_width = 400

        self.init_ui()
        self.init_vtk_3d_view()

    def init_ui(self):
        central = QWidget()
        self.setCentralWidget(central)
        main_layout = QHBoxLayout(central)

        # ========== 左侧控制面板 ==========
        control_widget = QWidget()
        control_widget.setObjectName("controlPanel")
        control_layout = QVBoxLayout(control_widget)
        control_widget.setFixedWidth(240)

        self.btn_load = QPushButton("加载 DICOM 序列文件夹")
        self.btn_load.clicked.connect(self.load_dicom_async)
        control_layout.addWidget(self.btn_load)

        self.btn_load_mhd = QPushButton("加载 MHD/NII 三维影像")
        self.btn_load_mhd.clicked.connect(self.load_mhd_async)
        control_layout.addWidget(self.btn_load_mhd)

        self.btn_load_single = QPushButton("加载 单张图像(DCM/PNG)")
        self.btn_load_single.clicked.connect(self.load_single_async)
        control_layout.addWidget(self.btn_load_single)

        # 分隔线
        line1 = QFrame()
        line1.setFrameShape(QFrame.HLine)
        line1.setStyleSheet("background-color: #0f3460;")
        control_layout.addWidget(line1)

        # ========== 算法参数滑块 ==========
        control_layout.addWidget(QLabel("金属HU阈值 (默认2000)"))
        self.metal_hu_th = QSlider(Qt.Horizontal)
        self.metal_hu_th.setRange(1500, 3000)
        self.metal_hu_th.setValue(2000)
        self.metal_hu_th_label = QLabel("2000")
        self.metal_hu_th_label.setStyleSheet("color: #00d4ff; font-weight: bold;")
        control_layout.addWidget(self.metal_hu_th)
        control_layout.addWidget(self.metal_hu_th_label)

        control_layout.addWidget(QLabel("伪影高HU阈值 (默认500)"))
        self.artifact_high_th = QSlider(Qt.Horizontal)
        self.artifact_high_th.setRange(200, 1000)
        self.artifact_high_th.setValue(500)
        self.artifact_high_th_label = QLabel("500")
        self.artifact_high_th_label.setStyleSheet("color: #00d4ff; font-weight: bold;")
        control_layout.addWidget(self.artifact_high_th)
        control_layout.addWidget(self.artifact_high_th_label)

        control_layout.addWidget(QLabel("伪影低HU阈值 (默认-500)"))
        self.artifact_low_th = QSlider(Qt.Horizontal)
        self.artifact_low_th.setRange(-1000, 0)
        self.artifact_low_th.setValue(-500)
        self.artifact_low_th_label = QLabel("-500")
        self.artifact_low_th_label.setStyleSheet("color: #00d4ff; font-weight: bold;")
        control_layout.addWidget(self.artifact_low_th)
        control_layout.addWidget(self.artifact_low_th_label)

        control_layout.addWidget(QLabel("金属膨胀半径 (默认20)"))
        self.metal_dilate_r = QSlider(Qt.Horizontal)
        self.metal_dilate_r.setRange(5, 40)
        self.metal_dilate_r.setValue(20)
        self.metal_dilate_r_label = QLabel("20")
        self.metal_dilate_r_label.setStyleSheet("color: #00d4ff; font-weight: bold;")
        control_layout.addWidget(self.metal_dilate_r)
        control_layout.addWidget(self.metal_dilate_r_label)

        # 分隔线
        line2 = QFrame()
        line2.setFrameShape(QFrame.HLine)
        line2.setStyleSheet("background-color: #0f3460;")
        control_layout.addWidget(line2)

        # CT窗宽窗位
        control_layout.addWidget(QLabel("窗位 (HU)｜CT 显示"))
        self.win_center_slider = QSlider(Qt.Horizontal)
        self.win_center_slider.setRange(-1000, 2000)
        self.win_center_slider.setValue(40)
        self.win_center_label = QLabel("40")
        self.win_center_label.setStyleSheet("color: #00d4ff; font-weight: bold;")
        control_layout.addWidget(self.win_center_slider)
        control_layout.addWidget(self.win_center_label)

        control_layout.addWidget(QLabel("窗宽 (HU)｜CT 显示"))
        self.win_width_slider = QSlider(Qt.Horizontal)
        self.win_width_slider.setRange(50, 4000)
        self.win_width_slider.setValue(400)
        self.win_width_label = QLabel("400")
        self.win_width_label.setStyleSheet("color: #00d4ff; font-weight: bold;")
        control_layout.addWidget(self.win_width_slider)
        control_layout.addWidget(self.win_width_label)

        # 分隔线
        line3 = QFrame()
        line3.setFrameShape(QFrame.HLine)
        line3.setStyleSheet("background-color: #0f3460;")
        control_layout.addWidget(line3)

        # 功能按钮
        self.btn_run = QPushButton("生成伪影掩码")
        self.btn_run.setObjectName("btnRun")
        self.btn_run.clicked.connect(self.generate_mask_async)
        control_layout.addWidget(self.btn_run)

        self.btn_save = QPushButton("保存掩码(NIfTI)")
        self.btn_save.setObjectName("btnSave")
        self.btn_save.clicked.connect(self.save_mask_async)
        control_layout.addWidget(self.btn_save)

        control_layout.addStretch()
        main_layout.addWidget(control_widget)

        # ========== 右侧整体布局 ==========
        right_container = QWidget()
        right_layout = QHBoxLayout(right_container)

        # ========== 2D 切片区域 ==========
        left_2d_container = QWidget()
        left_2d_container.setFixedWidth(580)
        self._max_axial_w = 540
        self._max_mpr_w = 265
        left_2d_layout = QVBoxLayout(left_2d_container)

        # 轴位
        axial_layout = QVBoxLayout()
        axial_title_layout = QHBoxLayout()
        axial_title_layout.addWidget(QLabel("【轴位 Axial】"))
        axial_title_layout.addWidget(QLabel("  (鼠标悬浮放大)"))
        axial_title_layout.addStretch()
        self.axial_slice_label = QLabel("切片：0 / 0")
        self.axial_slice_label.setObjectName("sliceLabel")
        axial_title_layout.addWidget(self.axial_slice_label)
        axial_layout.addLayout(axial_title_layout)

        self.axial_label = MagnifierLabel()
        self.axial_label.setStyleSheet("background-color:#111; min-height:280px; border: 1px solid #0f3460; border-radius: 4px;")
        self.axial_label.setAlignment(Qt.AlignCenter)
        self.slider_axial = QSlider(Qt.Horizontal)
        self.slider_axial.valueChanged.connect(lambda v: self.on_slice_change("axial", v))
        axial_layout.addWidget(self.axial_label)
        axial_layout.addWidget(self.slider_axial)
        left_2d_layout.addLayout(axial_layout)

        # 冠状 + 矢状
        cor_sag_layout = QHBoxLayout()

        coronal_layout = QVBoxLayout()
        coronal_title_layout = QHBoxLayout()
        coronal_title_layout.addWidget(QLabel("【冠状 Coronal】"))
        coronal_title_layout.addWidget(QLabel("  (鼠标悬浮放大)"))
        coronal_title_layout.addStretch()
        self.coronal_slice_label = QLabel("切片：0 / 0")
        self.coronal_slice_label.setObjectName("sliceLabel")
        coronal_title_layout.addWidget(self.coronal_slice_label)
        coronal_layout.addLayout(coronal_title_layout)

        self.coronal_label = MagnifierLabel()
        self.coronal_label.setStyleSheet("background-color:#111; min-height:280px; border: 1px solid #0f3460; border-radius: 4px;")
        self.coronal_label.setAlignment(Qt.AlignCenter)
        self.slider_coronal = QSlider(Qt.Horizontal)
        self.slider_coronal.valueChanged.connect(lambda v: self.on_slice_change("coronal", v))
        coronal_layout.addWidget(self.coronal_label)
        coronal_layout.addWidget(self.slider_coronal)
        cor_sag_layout.addLayout(coronal_layout)

        sagittal_layout = QVBoxLayout()
        sagittal_title_layout = QHBoxLayout()
        sagittal_title_layout.addWidget(QLabel("【矢状 Sagittal】"))
        sagittal_title_layout.addWidget(QLabel("  (鼠标悬浮放大)"))
        sagittal_title_layout.addStretch()
        self.sagittal_slice_label = QLabel("切片：0 / 0")
        self.sagittal_slice_label.setObjectName("sliceLabel")
        sagittal_title_layout.addWidget(self.sagittal_slice_label)
        sagittal_layout.addLayout(sagittal_title_layout)

        self.sagittal_label = MagnifierLabel()
        self.sagittal_label.setStyleSheet("background-color:#111; min-height:280px; border: 1px solid #0f3460; border-radius: 4px;")
        self.sagittal_label.setAlignment(Qt.AlignCenter)
        self.slider_sagittal = QSlider(Qt.Horizontal)
        self.slider_sagittal.valueChanged.connect(lambda v: self.on_slice_change("sagittal", v))
        sagittal_layout.addWidget(self.sagittal_label)
        sagittal_layout.addWidget(self.slider_sagittal)
        cor_sag_layout.addLayout(sagittal_layout)

        left_2d_layout.addLayout(cor_sag_layout)
        right_layout.addWidget(left_2d_container)

        # ========== VTK 3D 视图 ==========
        vtk_container = QWidget()
        vtk_layout = QVBoxLayout(vtk_container)
        vtk_layout.addWidget(QLabel("【VTK 3D CT 重建】"))
        self.vtk_widget = QWidget()
        self.vtk_widget.setStyleSheet("background-color:#000; border: 1px solid #0f3460; border-radius: 4px;")
        vtk_layout.addWidget(self.vtk_widget)

        right_layout.addWidget(vtk_container)
        right_layout.setStretch(0, 0)
        right_layout.setStretch(1, 100)

        main_layout.addWidget(right_container)
        self.connect_all_sliders()

    def init_vtk_3d_view(self):
        self.vtk_view = QVTKRenderWindowInteractor(self.vtk_widget)
        self.vtk_renderer = vtk.vtkRenderer()
        self.vtk_renderer.SetBackground(0.1, 0.1, 0.1)
        self.vtk_view.GetRenderWindow().AddRenderer(self.vtk_renderer)
        self.iren = self.vtk_view.GetRenderWindow().GetInteractor()
        self.iren.Initialize()

        layout = QVBoxLayout(self.vtk_widget)
        layout.addWidget(self.vtk_view)
        layout.setContentsMargins(0, 0, 0, 0)

    def render_3d_ct(self):
        if self.ct_image is None:
            return

        from vtkmodules.util import numpy_support
        self.vtk_renderer.RemoveAllViewProps()

        vol = self.volume_np.astype(np.float32)
        depth, height, width = vol.shape

        vtk_data = numpy_support.numpy_to_vtk(
            vol.ravel(), deep=True, array_type=vtk.VTK_FLOAT
        )

        vtk_image = vtk.vtkImageData()
        vtk_image.SetDimensions(width, height, depth)

        spacing = self.ct_image.GetSpacing()
        origin = self.ct_image.GetOrigin()
        vtk_image.SetSpacing(spacing)
        vtk_image.SetOrigin(origin)
        vtk_image.GetPointData().SetScalars(vtk_data)

        mapper = vtk.vtkSmartVolumeMapper()
        mapper.SetInputData(vtk_image)
        mapper.SetBlendModeToComposite()

        color = vtk.vtkColorTransferFunction()
        color.AddRGBPoint(-1000, 0.0, 0.0, 0.0)
        color.AddRGBPoint(-200, 0.1, 0.1, 0.1)
        color.AddRGBPoint(0, 0.5, 0.5, 0.5)
        color.AddRGBPoint(100, 0.8, 0.7, 0.6)
        color.AddRGBPoint(300, 1.0, 0.8, 0.6)
        color.AddRGBPoint(500, 1.0, 0.9, 0.8)
        color.AddRGBPoint(1000, 1.0, 1.0, 1.0)
        color.AddRGBPoint(2000, 1.0, 1.0, 1.0)
        color.AddRGBPoint(3000, 1.0, 1.0, 1.0)

        opacity = vtk.vtkPiecewiseFunction()
        opacity.AddPoint(-1000, 0.0)
        opacity.AddPoint(-200, 0.0)
        opacity.AddPoint(-100, 0.05)
        opacity.AddPoint(0, 0.1)
        opacity.AddPoint(50, 0.2)
        opacity.AddPoint(100, 0.4)
        opacity.AddPoint(200, 0.6)
        opacity.AddPoint(300, 0.8)
        opacity.AddPoint(500, 0.9)
        opacity.AddPoint(1000, 0.95)
        opacity.AddPoint(2000, 0.95)
        opacity.AddPoint(3000, 0.95)

        gradientOpacity = vtk.vtkPiecewiseFunction()
        gradientOpacity.AddPoint(0, 0.0)
        gradientOpacity.AddPoint(50, 0.1)
        gradientOpacity.AddPoint(100, 0.3)
        gradientOpacity.AddPoint(200, 0.5)
        gradientOpacity.AddPoint(500, 0.8)
        gradientOpacity.AddPoint(1000, 1.0)

        prop = vtk.vtkVolumeProperty()
        prop.SetColor(color)
        prop.SetScalarOpacity(opacity)
        prop.SetGradientOpacity(gradientOpacity)
        prop.ShadeOn()
        prop.SetInterpolationTypeToLinear()
        prop.SetAmbient(0.2)
        prop.SetDiffuse(0.8)
        prop.SetSpecular(0.3)
        prop.SetSpecularPower(20)

        volume = vtk.vtkVolume()
        volume.SetMapper(mapper)
        volume.SetProperty(prop)

        self.vtk_renderer.AddVolume(volume)
        self.vtk_renderer.ResetCamera()
        self.vtk_view.GetRenderWindow().Render()

    def connect_all_sliders(self):
        # 算法参数显示绑定
        self.metal_hu_th.valueChanged.connect(lambda v: self.metal_hu_th_label.setText(str(v)))
        self.artifact_high_th.valueChanged.connect(lambda v: self.artifact_high_th_label.setText(str(v)))
        self.artifact_low_th.valueChanged.connect(lambda v: self.artifact_low_th_label.setText(str(v)))
        self.metal_dilate_r.valueChanged.connect(lambda v: self.metal_dilate_r_label.setText(str(v)))

        # 窗宽窗位
        self.win_center_slider.valueChanged.connect(self._on_window_change)
        self.win_width_slider.valueChanged.connect(self._on_window_change)
        self.win_center_slider.valueChanged.connect(lambda v: self.win_center_label.setText(str(v)))
        self.win_width_slider.valueChanged.connect(lambda v: self.win_width_label.setText(str(v)))

        # 参数变化延时重新计算掩码
        self.update_timer = QTimer()
        self.update_timer.setSingleShot(True)
        self.update_timer.timeout.connect(self.generate_mask_async)

        self.metal_hu_th.valueChanged.connect(lambda: self.update_timer.start(300))
        self.artifact_high_th.valueChanged.connect(lambda: self.update_timer.start(300))
        self.artifact_low_th.valueChanged.connect(lambda: self.update_timer.start(300))
        self.metal_dilate_r.valueChanged.connect(lambda: self.update_timer.start(300))

    def run_async(self, title, func, *args):
        self.dialog = ProgressDialog(title, self)
        self.worker = Worker(func, *args)
        self.worker.progress.connect(self.dialog.set_progress)
        self.worker.finished.connect(self.dialog.close)
        self.worker.finished.connect(self.on_task_done)
        self.worker.error.connect(lambda e: (self.dialog.close(), QMessageBox.critical(self, "错误", e)))
        self.worker.success_msg.connect(self.on_success_msg)
        self.dialog.show()
        self.worker.start()

    def on_task_done(self):
        self.refresh_all_views()
        self.update_all_slice_labels()
        self.render_3d_ct()

    def on_success_msg(self, msg):
        QMessageBox.information(self, "成功", msg)

    def load_mhd_async(self):
        path, _ = QFileDialog.getOpenFileName(self, "选择影像文件", "", "MHD/NIfTI Image (*.mhd *.MHD *.nii *.NII *.nii.gz)")
        if not path: return
        self.run_async("加载影像中...", self._load_mhd_task, path)

    def _load_mhd_task(self, report, path):
        report(20, "读取图像文件...")
        img = sitk.ReadImage(path)
        report(60, "解析三维数据...")
        vol = sitk.GetArrayFromImage(img)
        self.ct_image = img
        self.volume_np = vol
        self.mask_np = None
        self.artifact_mask = None
        self.init_slice_range()
        report(100, "完成")

    def load_dicom_async(self):
        folder = QFileDialog.getExistingDirectory(self, "选择 DICOM 序列文件夹")
        if not folder: return
        self.run_async("加载 DICOM 中...", self._load_dicom_task, folder)

    def _load_dicom_task(self, report, folder):
        report(10, "扫描序列文件...")
        reader = sitk.ImageSeriesReader()
        fns = reader.GetGDCMSeriesFileNames(folder)
        reader.SetFileNames(fns)
        report(40, "读取图像...")
        img = reader.Execute()
        report(70, "构建三维体...")
        vol = sitk.GetArrayFromImage(img)
        self.ct_image = img
        self.volume_np = vol
        self.mask_np = None
        self.artifact_mask = None
        self.init_slice_range()
        report(100, "完成")

    def load_single_async(self):
        path, _ = QFileDialog.getOpenFileName(self, "选择图像", "", "所有图像 (*.dcm *.png *.jpg *.nii *.mhd)")
        if not path: return
        self.run_async("加载单张图像...", self._load_single_task, path)

    def _load_single_task(self, report, path):
        img = sitk.ReadImage(path)
        vol = sitk.GetArrayFromImage(img)
        if vol.ndim == 2:
            vol = vol[None]
            img = sitk.GetImageFromArray(vol)
        self.ct_image = img
        self.volume_np = vol
        self.mask_np = None
        self.artifact_mask = None
        self.init_slice_range()
        report(100)

    def generate_mask_async(self):
        if self.ct_image is None:
            QMessageBox.warning(self, "提示", "请先加载CT影像！")
            return
        self.run_async("生成伪影掩码...", self._generate_mask_task)

    def _generate_mask_task(self, report):
        if self.volume_np is None:
            return

        # 读取界面参数
        metal_hu = self.metal_hu_th.value()
        art_high = self.artifact_high_th.value()
        art_low = self.artifact_low_th.value()
        dilate_r = self.metal_dilate_r.value()

        report(10, "逐切片检测金属与伪影...")
        artifact_bool, metal_bool = run_full_artifact_detection(
            self.volume_np, metal_hu, art_high, art_low, dilate_r
        )

        report(70, "合并掩码：1=伪影  2=金属")
        combined_mask = np.zeros_like(artifact_bool, dtype=np.uint8)
        combined_mask[artifact_bool] = 1
        combined_mask[metal_bool] = 2

        report(85, "转换为NIfTI图像...")
        sitk_mask = sitk.GetImageFromArray(combined_mask)
        sitk_mask.CopyInformation(self.ct_image)

        self.artifact_mask = sitk_mask
        self.mask_np = combined_mask
        report(100, "掩码生成完成")

    def save_mask_async(self):
        if self.artifact_mask is None:
            QMessageBox.warning(self, "提示", "请先生成掩码！")
            return
        path, _ = QFileDialog.getSaveFileName(self, "保存掩码", "artifact_mask.nii.gz", "NIfTI 掩码 (*.nii.gz *.nii)")
        if not path: return
        if os.path.exists(path):
            ret = QMessageBox.question(self, "文件已存在", f"文件已存在，是否覆盖？")
            if ret != QMessageBox.Yes:
                return
        self.run_async("保存掩码中...", self._save_mask_task, path)

    def _save_mask_task(self, report, path):
        sitk.WriteImage(self.artifact_mask, path)
        report(100, "保存完成")
        self.worker.success_msg.emit(f"掩码已保存到：\n{path}")

    def update_all_slice_labels(self):
        if self.volume_np is None:
            return
        d, h, w = self.volume_np.shape
        self.axial_slice_label.setText(f"Z={self.slice_axial + 1}/{d}")
        self.coronal_slice_label.setText(f"Y={self.slice_coronal + 1}/{h}")
        self.sagittal_slice_label.setText(f"X={self.slice_sagittal + 1}/{w}")

    def init_slice_range(self):
        d, h, w = self.volume_np.shape
        self.slider_axial.setRange(0, d-1)
        self.slider_coronal.setRange(0, h-1)
        self.slider_sagittal.setRange(0, w-1)
        sa, sc, ss = d//2, h//2, w//2
        self.slice_axial, self.slice_coronal, self.slice_sagittal = sa, sc, ss
        self.slider_axial.setValue(sa)
        self.slider_coronal.setValue(sc)
        self.slider_sagittal.setValue(ss)
        self.update_all_slice_labels()

    def on_slice_change(self, vt, v):
        if self.volume_np is None:
            return
        if vt == "axial":
            self.slice_axial = v
        elif vt == "coronal":
            self.slice_coronal = v
        elif vt == "sagittal":
            self.slice_sagittal = v
        self.refresh_all_views()
        self.update_all_slice_labels()

    def spacing_zyx(self):
        if self.ct_image is None:
            return 1.0, 1.0, 1.0
        sx, sy, sz = self.ct_image.GetSpacing()
        return float(sz), float(sy), float(sx)

    def _on_window_change(self):
        self.win_center = self.win_center_slider.value()
        self.win_width = self.win_width_slider.value()
        self.refresh_all_views()

    def window_limits(self):
        low = self.win_center - self.win_width / 2
        high = self.win_center + self.win_width / 2
        return low, high

    def draw_slice(self, im, mk):
        """
        双色显示：
        伪影(1) → 红色半透明
        金属(2) → 青色半透明
        """
        low, high = self.window_limits()
        gray = np.clip(im.astype(np.float32), low, high)
        gray = ((gray - low) / (high - low + 1e-8) * 255).astype(np.uint8)
        rgb = np.stack([gray, gray, gray], axis=-1).astype(np.float32)

        if mk is not None and mk.shape == im.shape and mk.sum() > 0:
            # 伪影 红色
            art_mask = mk == 1
            rgb[art_mask] = 0.5 * rgb[art_mask] + 0.5 * np.array([255, 0, 0], dtype=np.float32)
            # 金属 青色
            metal_mask = mk == 2
            rgb[metal_mask] = 0.5 * rgb[metal_mask] + 0.5 * np.array([0, 255, 255], dtype=np.float32)

        return rgb.astype(np.uint8)

    def mpr_panel_pixels(self, n_rows, n_cols, row_sp, col_sp, dpi=100):
        fig_h = max(3.0, n_rows * row_sp / 35.0)
        fig_w = max(5.0, n_cols * col_sp / 35.0)
        return int(fig_w * dpi), int(fig_h * dpi)

    def axial_panel_pixels(self, dpi=100):
        sz, sy, sx = self.spacing_zyx()
        _, ny, nx = self.volume_np.shape
        panel_h = 520
        fig_h = panel_h / dpi
        fig_w = fig_h * (nx * sx) / (ny * sy)
        return int(fig_w * dpi), panel_h

    def fit_panel_size(self, disp_w, disp_h, max_w):
        if disp_w <= max_w:
            return disp_w, disp_h
        scale = max_w / disp_w
        return max_w, max(1, int(round(disp_h * scale)))

    def to_qpix(self, arr, disp_w, disp_h):
        h, w = arr.shape[:2]
        arr = np.ascontiguousarray(arr)
        qim = QImage(arr.data, w, h, 3 * w, QImage.Format_RGB888)
        return QPixmap.fromImage(qim).scaled(
            disp_w, disp_h, Qt.IgnoreAspectRatio, Qt.SmoothTransformation
        )

    def update_view_axial(self):
        if self.volume_np is None:
            return
        im = self.volume_np[self.slice_axial]
        mk = self.mask_np[self.slice_axial] if self.mask_np is not None else None
        rendered = np.ascontiguousarray(self.draw_slice(im, mk))
        disp_w, disp_h = self.axial_panel_pixels()
        disp_w, disp_h = self.fit_panel_size(disp_w, disp_h, self._max_axial_w)
        self.axial_label.set_original_image(rendered)
        self.axial_label.setPixmap(self.to_qpix(rendered, disp_w, disp_h))

    def update_view_coronal(self):
        if self.volume_np is None:
            return
        sz, _, sx = self.spacing_zyx()
        nz, _, nx = self.volume_np.shape
        im = self.volume_np[:, self.slice_coronal, :]
        mk = self.mask_np[:, self.slice_coronal, :] if self.mask_np is not None else None
        rendered = np.ascontiguousarray(self.draw_slice(im, mk))
        disp_w, disp_h = self.mpr_panel_pixels(nz, nx, sz, sx)
        disp_w, disp_h = self.fit_panel_size(disp_w, disp_h, self._max_mpr_w)
        self.coronal_label.set_original_image(rendered)
        self.coronal_label.setPixmap(self.to_qpix(rendered, disp_w, disp_h))

    def update_view_sagittal(self):
        if self.volume_np is None:
            return
        sz, sy, _ = self.spacing_zyx()
        nz, ny, _ = self.volume_np.shape
        im = self.volume_np[:, :, self.slice_sagittal]
        mk = self.mask_np[:, :, self.slice_sagittal] if self.mask_np is not None else None
        rendered = np.ascontiguousarray(self.draw_slice(im, mk))
        disp_w, disp_h = self.mpr_panel_pixels(nz, ny, sz, sy)
        disp_w, disp_h = self.fit_panel_size(disp_w, disp_h, self._max_mpr_w)
        self.sagittal_label.set_original_image(rendered)
        self.sagittal_label.setPixmap(self.to_qpix(rendered, disp_w, disp_h))

    def refresh_all_views(self):
        self.update_view_axial()
        self.update_view_coronal()
        self.update_view_sagittal()

# -------------------------------------------------------------------------
if __name__ == "__main__":
    app = QApplication(sys.argv)
    app.setStyleSheet(APP_STYLESHEET)
    win = ArtifactAnnotationWindow()
    win.show()

    # 如需自动加载路径，可在这里修改
    # dicom_folder = r"D:\Tool\pycharm\Py_Projects\cifar10_20237087\itkdata\CT Plain"
    # if os.path.exists(dicom_folder):
    #     print(f"自动加载 DICOM: {dicom_folder}")
    #     win.run_async("加载 DICOM 中...", win._load_dicom_task, dicom_folder)

    sys.exit(app.exec())