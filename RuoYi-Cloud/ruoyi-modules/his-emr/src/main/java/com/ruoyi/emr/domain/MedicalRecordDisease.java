package com.ruoyi.emr.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 病历疾病关联对象 medical_record_disease
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
public class MedicalRecordDisease extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 病历ID */
    @Excel(name = "病历ID")
    private Long recordId;

    /** 疾病ID */
    @Excel(name = "疾病ID")
    private Long diseaseId;

    /** 诊断类型 */
    @Excel(name = "诊断类型")
    private String diagnosisType;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setRecordId(Long recordId) 
    {
        this.recordId = recordId;
    }

    public Long getRecordId() 
    {
        return recordId;
    }

    public void setDiseaseId(Long diseaseId) 
    {
        this.diseaseId = diseaseId;
    }

    public Long getDiseaseId() 
    {
        return diseaseId;
    }

    public void setDiagnosisType(String diagnosisType) 
    {
        this.diagnosisType = diagnosisType;
    }

    public String getDiagnosisType() 
    {
        return diagnosisType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("recordId", getRecordId())
            .append("diseaseId", getDiseaseId())
            .append("diagnosisType", getDiagnosisType())
            .append("createTime", getCreateTime())
            .toString();
    }
}
