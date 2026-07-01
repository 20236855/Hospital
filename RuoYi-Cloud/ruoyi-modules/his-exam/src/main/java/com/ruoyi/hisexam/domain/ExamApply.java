package com.ruoyi.hisexam.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 检查/检验/处置申请单对象 exam_apply
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
public class ExamApply extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 申请ID(主键) */
    private Long applyId;

    /** 挂号ID */
    @Excel(name = "挂号ID")
    private Long registerId;

    /** 接诊ID */
    @Excel(name = "接诊ID")
    private Long encounterId;

    /** 患者ID */
    @Excel(name = "患者ID")
    private Long patientId;

    /** 开单医生ID */
    @Excel(name = "开单医生ID")
    private Long doctorId;

    /** 开单科室ID */
    @Excel(name = "开单科室ID")
    private Long deptId;

    /** 类型：CHECK检查/LAB检验/DISPOSAL处置 */
    @Excel(name = "类型：CHECK检查/LAB检验/DISPOSAL处置")
    private String applyType;

    /** 医技项目ID */
    @Excel(name = "医技项目ID")
    private Long techId;

    /** 目的要求 */
    @Excel(name = "目的要求")
    private String applyInfo;

    /** 检查/检验/处置部位 */
    @Excel(name = "检查/检验/处置部位")
    private String applyPosition;

    /** 执行人员ID */
    @Excel(name = "执行人员ID")
    private Long operatorId;

    /** 结果录入人员ID */
    @Excel(name = "结果录入人员ID")
    private Long inputerId;

    /** 开立时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "开立时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date applyTime;

    /** 执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "执行时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date examTime;

    /** 执行结果 */
    @Excel(name = "执行结果")
    private String examResult;

    /** CT/影像文件访问地址，检验、处置类可留空 */
    @Excel(name = "CT/影像文件访问地址，检验、处置类可留空")
    private String imageUrl;

    /** 状态：待缴费/待执行/已完成 */
    @Excel(name = "状态：待缴费/待执行/已完成")
    private String applyStatus;

    /** 支付状态 */
    @Excel(name = "支付状态")
    private String payStatus;

    public void setApplyId(Long applyId) 
    {
        this.applyId = applyId;
    }

    public Long getApplyId() 
    {
        return applyId;
    }

    public void setRegisterId(Long registerId) 
    {
        this.registerId = registerId;
    }

    public Long getRegisterId() 
    {
        return registerId;
    }

    public void setEncounterId(Long encounterId) 
    {
        this.encounterId = encounterId;
    }

    public Long getEncounterId() 
    {
        return encounterId;
    }

    public void setPatientId(Long patientId) 
    {
        this.patientId = patientId;
    }

    public Long getPatientId() 
    {
        return patientId;
    }

    public void setDoctorId(Long doctorId) 
    {
        this.doctorId = doctorId;
    }

    public Long getDoctorId() 
    {
        return doctorId;
    }

    public void setDeptId(Long deptId) 
    {
        this.deptId = deptId;
    }

    public Long getDeptId() 
    {
        return deptId;
    }

    public void setApplyType(String applyType) 
    {
        this.applyType = applyType;
    }

    public String getApplyType() 
    {
        return applyType;
    }

    public void setTechId(Long techId) 
    {
        this.techId = techId;
    }

    public Long getTechId() 
    {
        return techId;
    }

    public void setApplyInfo(String applyInfo) 
    {
        this.applyInfo = applyInfo;
    }

    public String getApplyInfo() 
    {
        return applyInfo;
    }

    public void setApplyPosition(String applyPosition) 
    {
        this.applyPosition = applyPosition;
    }

    public String getApplyPosition() 
    {
        return applyPosition;
    }

    public void setOperatorId(Long operatorId) 
    {
        this.operatorId = operatorId;
    }

    public Long getOperatorId() 
    {
        return operatorId;
    }

    public void setInputerId(Long inputerId) 
    {
        this.inputerId = inputerId;
    }

    public Long getInputerId() 
    {
        return inputerId;
    }

    public void setApplyTime(Date applyTime) 
    {
        this.applyTime = applyTime;
    }

    public Date getApplyTime() 
    {
        return applyTime;
    }

    public void setExamTime(Date examTime) 
    {
        this.examTime = examTime;
    }

    public Date getExamTime() 
    {
        return examTime;
    }

    public void setExamResult(String examResult) 
    {
        this.examResult = examResult;
    }

    public String getExamResult() 
    {
        return examResult;
    }

    public void setImageUrl(String imageUrl) 
    {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() 
    {
        return imageUrl;
    }

    public void setApplyStatus(String applyStatus) 
    {
        this.applyStatus = applyStatus;
    }

    public String getApplyStatus() 
    {
        return applyStatus;
    }

    public void setPayStatus(String payStatus) 
    {
        this.payStatus = payStatus;
    }

    public String getPayStatus() 
    {
        return payStatus;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("applyId", getApplyId())
            .append("registerId", getRegisterId())
            .append("registerNo", getRegisterNo())
            .append("encounterId", getEncounterId())
            .append("patientId", getPatientId())
            .append("patientName", getPatientName())
            .append("doctorId", getDoctorId())
            .append("doctorName", getDoctorName())
            .append("deptId", getDeptId())
            .append("deptName", getDeptName())
            .append("techId", getTechId())
            .append("techName", getTechName())
            .append("applyType", getApplyType())
            .append("applyInfo", getApplyInfo())
            .append("applyPosition", getApplyPosition())
            .append("operatorId", getOperatorId())
            .append("inputerId", getInputerId())
            .append("applyTime", getApplyTime())
            .append("examTime", getExamTime())
            .append("examResult", getExamResult())
            .append("imageUrl", getImageUrl())
            .append("applyStatus", getApplyStatus())
            .append("payStatus", getPayStatus())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }

    // ============ 展示用扩展字段（不入库） ============
    private String registerNo;
    private String patientName;
    private String doctorName;
    private String deptName;
    private String techName;

    public String getRegisterNo() { return registerNo; }
    public void setRegisterNo(String registerNo) { this.registerNo = registerNo; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getTechName() { return techName; }
    public void setTechName(String techName) { this.techName = techName; }
}
