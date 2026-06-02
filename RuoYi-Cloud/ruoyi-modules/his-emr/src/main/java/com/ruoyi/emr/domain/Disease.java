package com.ruoyi.emr.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 疾病字典对象 disease
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
public class Disease extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 疾病ID */
    private Long diseaseId;

    /** 疾病编码 */
    @Excel(name = "疾病编码")
    private String diseaseCode;

    /** 疾病名称 */
    @Excel(name = "疾病名称")
    private String diseaseName;

    /** ICD编码 */
    @Excel(name = "ICD编码")
    private String icdCode;

    /** 疾病描述 */
    @Excel(name = "疾病描述")
    private String description;

    /** 状态 */
    @Excel(name = "状态")
    private String status;

    public void setDiseaseId(Long diseaseId) 
    {
        this.diseaseId = diseaseId;
    }

    public Long getDiseaseId() 
    {
        return diseaseId;
    }

    public void setDiseaseCode(String diseaseCode) 
    {
        this.diseaseCode = diseaseCode;
    }

    public String getDiseaseCode() 
    {
        return diseaseCode;
    }

    public void setDiseaseName(String diseaseName) 
    {
        this.diseaseName = diseaseName;
    }

    public String getDiseaseName() 
    {
        return diseaseName;
    }

    public void setIcdCode(String icdCode) 
    {
        this.icdCode = icdCode;
    }

    public String getIcdCode() 
    {
        return icdCode;
    }

    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("diseaseId", getDiseaseId())
            .append("diseaseCode", getDiseaseCode())
            .append("diseaseName", getDiseaseName())
            .append("icdCode", getIcdCode())
            .append("description", getDescription())
            .append("status", getStatus())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
