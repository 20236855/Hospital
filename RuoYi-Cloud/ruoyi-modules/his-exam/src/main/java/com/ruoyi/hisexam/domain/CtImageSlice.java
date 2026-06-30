package com.ruoyi.hisexam.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * CT影像切片存储对象 ct_image_slice
 * 图片存磁盘，表中仅存文件URL路径
 *
 * @author ruoyi
 * @date 2026-06-26
 */
public class CtImageSlice extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long sliceId;
    @Excel(name = "申请单ID") private Long applyId;
    @Excel(name = "检查类型") private String checkType;
    @Excel(name = "患者ID") private Long patientId;
    @Excel(name = "检验医生ID") private Long doctorId;
    @Excel(name = "切片层号") private Integer sliceIndex;
    @Excel(name = "Z层坐标") private Integer sliceZ;

    /** 标注切片文件路径/URL */
    private String imagePath;

    /** 原始切片文件路径/URL */
    private String rawImagePath;

    @Excel(name = "HU最小值") private Double huMin;
    @Excel(name = "HU最大值") private Double huMax;
    @Excel(name = "HU平均值") private Double huMean;
    @Excel(name = "层厚") private Double sliceThickness;
    @Excel(name = "像素间距X") private Double pixelSpacingX;
    @Excel(name = "像素间距Y") private Double pixelSpacingY;

    /** 分割掩码(JSON) */
    private String segmentationData;

    /** 检测结节(JSON) */
    private String detectionResult;

    @Excel(name = "原始文件名") private String fileName;

    // ============ getter/setter ============
    public Long getSliceId() { return sliceId; }
    public void setSliceId(Long sliceId) { this.sliceId = sliceId; }
    public Long getApplyId() { return applyId; }
    public void setApplyId(Long applyId) { this.applyId = applyId; }
    public String getCheckType() { return checkType; }
    public void setCheckType(String checkType) { this.checkType = checkType; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public Integer getSliceIndex() { return sliceIndex; }
    public void setSliceIndex(Integer sliceIndex) { this.sliceIndex = sliceIndex; }
    public Integer getSliceZ() { return sliceZ; }
    public void setSliceZ(Integer sliceZ) { this.sliceZ = sliceZ; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public String getRawImagePath() { return rawImagePath; }
    public void setRawImagePath(String rawImagePath) { this.rawImagePath = rawImagePath; }
    public Double getHuMin() { return huMin; }
    public void setHuMin(Double huMin) { this.huMin = huMin; }
    public Double getHuMax() { return huMax; }
    public void setHuMax(Double huMax) { this.huMax = huMax; }
    public Double getHuMean() { return huMean; }
    public void setHuMean(Double huMean) { this.huMean = huMean; }
    public Double getSliceThickness() { return sliceThickness; }
    public void setSliceThickness(Double sliceThickness) { this.sliceThickness = sliceThickness; }
    public Double getPixelSpacingX() { return pixelSpacingX; }
    public void setPixelSpacingX(Double pixelSpacingX) { this.pixelSpacingX = pixelSpacingX; }
    public Double getPixelSpacingY() { return pixelSpacingY; }
    public void setPixelSpacingY(Double pixelSpacingY) { this.pixelSpacingY = pixelSpacingY; }
    public String getSegmentationData() { return segmentationData; }
    public void setSegmentationData(String segmentationData) { this.segmentationData = segmentationData; }
    public String getDetectionResult() { return detectionResult; }
    public void setDetectionResult(String detectionResult) { this.detectionResult = detectionResult; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("sliceId", getSliceId())
            .append("applyId", getApplyId())
            .append("checkType", getCheckType())
            .append("sliceIndex", getSliceIndex())
            .append("sliceZ", getSliceZ())
            .append("imagePath", getImagePath())
            .append("huMin", getHuMin())
            .append("huMax", getHuMax())
            .append("fileName", getFileName())
            .toString();
    }
}
