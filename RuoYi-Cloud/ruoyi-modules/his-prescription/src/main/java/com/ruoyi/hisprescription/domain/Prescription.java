package com.ruoyi.hisprescription.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 处方主对象 prescription
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
public class Prescription extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 处方ID(主键) */
    private Long prescriptionId;

    /** 挂号ID */
    @Excel(name = "挂号ID")
    private Long registerId;

    /** 接诊ID */
    @Excel(name = "接诊ID")
    private Long encounterId;

    /** 患者ID */
    @Excel(name = "患者ID")
    private Long patientId;

    /** 开方医生ID */
    @Excel(name = "开方医生ID")
    private Long doctorId;

    /** 开方科室ID */
    @Excel(name = "开方科室ID")
    private Long deptId;

    /** 处方单号 */
    @Excel(name = "处方单号")
    private String prescriptionNo;

    /** 处方总金额 */
    @Excel(name = "处方总金额")
    private BigDecimal totalAmount;

    /** 状态：待缴费/已缴费/已发药/已退方 */
    @Excel(name = "状态：待缴费/已缴费/已发药/已退方")
    private String prescriptionStatus;

    /** 支付状态 */
    @Excel(name = "支付状态")
    private String payStatus;

    /** 整体用药嘱托 */
    @Excel(name = "整体用药嘱托")
    private String drugTip;

    public void setPrescriptionId(Long prescriptionId) 
    {
        this.prescriptionId = prescriptionId;
    }

    public Long getPrescriptionId() 
    {
        return prescriptionId;
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

    public void setPrescriptionNo(String prescriptionNo) 
    {
        this.prescriptionNo = prescriptionNo;
    }

    public String getPrescriptionNo() 
    {
        return prescriptionNo;
    }

    public void setTotalAmount(BigDecimal totalAmount) 
    {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getTotalAmount() 
    {
        return totalAmount;
    }

    public void setPrescriptionStatus(String prescriptionStatus) 
    {
        this.prescriptionStatus = prescriptionStatus;
    }

    public String getPrescriptionStatus() 
    {
        return prescriptionStatus;
    }

    public void setPayStatus(String payStatus) 
    {
        this.payStatus = payStatus;
    }

    public String getPayStatus() 
    {
        return payStatus;
    }

    public void setDrugTip(String drugTip) 
    {
        this.drugTip = drugTip;
    }

    public String getDrugTip() 
    {
        return drugTip;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("prescriptionId", getPrescriptionId())
            .append("registerId", getRegisterId())
            .append("encounterId", getEncounterId())
            .append("patientId", getPatientId())
            .append("doctorId", getDoctorId())
            .append("deptId", getDeptId())
            .append("prescriptionNo", getPrescriptionNo())
            .append("totalAmount", getTotalAmount())
            .append("prescriptionStatus", getPrescriptionStatus())
            .append("payStatus", getPayStatus())
            .append("drugTip", getDrugTip())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
