package com.ruoyi.hisprescription.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 处方明细对象 prescription_item
 * 
 * @author ruoyi
 * @date 2026-06-03
 */
public class PrescriptionItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID(主键) */
    private Long itemId;

    /** 处方ID */
    @Excel(name = "处方ID")
    private Long prescriptionId;

    /** 药品ID */
    @Excel(name = "药品ID")
    private Long drugId;

    /** 药品名称 */
    @Excel(name = "药品名称")
    private String drugName;

    /** 药品单价 */
    @Excel(name = "药品单价")
    private BigDecimal drugPrice;

    /** 药品数量 */
    @Excel(name = "药品数量")
    private Long quantity;

    /** 用法 */
    @Excel(name = "用法")
    private String drugUsage;

    /** 单次用量 */
    @Excel(name = "单次用量")
    private String dosage;

    /** 服用频次 */
    @Excel(name = "服用频次")
    private String frequency;

    /** 使用天数 */
    @Excel(name = "使用天数")
    private Long useDays;

    /** 单种药品嘱托 */
    @Excel(name = "单种药品嘱托")
    private String itemTip;

    /** 患者ID，仅用于数据权限过滤 */
    private Long patientId;

    /** 医生ID，仅用于数据权限过滤（通过处方表关联） */
    private Long doctorId;

    public void setItemId(Long itemId) 
    {
        this.itemId = itemId;
    }

    public Long getItemId() 
    {
        return itemId;
    }

    public void setPrescriptionId(Long prescriptionId) 
    {
        this.prescriptionId = prescriptionId;
    }

    public Long getPrescriptionId() 
    {
        return prescriptionId;
    }

    public void setDrugId(Long drugId) 
    {
        this.drugId = drugId;
    }

    public Long getDrugId() 
    {
        return drugId;
    }

    public void setDrugName(String drugName) 
    {
        this.drugName = drugName;
    }

    public String getDrugName() 
    {
        return drugName;
    }

    public void setDrugPrice(BigDecimal drugPrice) 
    {
        this.drugPrice = drugPrice;
    }

    public BigDecimal getDrugPrice() 
    {
        return drugPrice;
    }

    public void setQuantity(Long quantity) 
    {
        this.quantity = quantity;
    }

    public Long getQuantity() 
    {
        return quantity;
    }

    public void setDrugUsage(String drugUsage) 
    {
        this.drugUsage = drugUsage;
    }

    public String getDrugUsage() 
    {
        return drugUsage;
    }

    public void setDosage(String dosage) 
    {
        this.dosage = dosage;
    }

    public String getDosage() 
    {
        return dosage;
    }

    public void setFrequency(String frequency) 
    {
        this.frequency = frequency;
    }

    public String getFrequency() 
    {
        return frequency;
    }

    public void setUseDays(Long useDays) 
    {
        this.useDays = useDays;
    }

    public Long getUseDays() 
    {
        return useDays;
    }

    public void setItemTip(String itemTip) 
    {
        this.itemTip = itemTip;
    }

    public String getItemTip() 
    {
        return itemTip;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("itemId", getItemId())
            .append("prescriptionId", getPrescriptionId())
            .append("drugId", getDrugId())
            .append("drugName", getDrugName())
            .append("drugPrice", getDrugPrice())
            .append("quantity", getQuantity())
            .append("drugUsage", getDrugUsage())
            .append("dosage", getDosage())
            .append("frequency", getFrequency())
            .append("useDays", getUseDays())
            .append("itemTip", getItemTip())
            .append("patientId", getPatientId())
            .append("createTime", getCreateTime())
            .toString();
    }
}
