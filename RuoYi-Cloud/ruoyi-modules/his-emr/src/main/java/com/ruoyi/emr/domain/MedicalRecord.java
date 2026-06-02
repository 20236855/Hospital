package com.ruoyi.emr.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 电子病历对象 medical_record
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
public class MedicalRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 病历ID */
    private Long recordId;

    /** 接诊ID */
    @Excel(name = "接诊ID")
    private Long encounterId;

    /** 主诉 */
    @Excel(name = "主诉")
    private String chiefComplaint;

    /** 现病史 */
    @Excel(name = "现病史")
    private String presentIllness;

    /** 既往史 */
    @Excel(name = "既往史")
    private String pastHistory;

    /** 过敏史 */
    @Excel(name = "过敏史")
    private String allergyHistory;

    /** 体格检查 */
    @Excel(name = "体格检查")
    private String physicalExam;

    /** 诊断意见 */
    @Excel(name = "诊断意见")
    private String diagnosisOpinion;

    /** 治疗方案 */
    @Excel(name = "治疗方案")
    private String treatmentPlan;

    /** 医生建议 */
    @Excel(name = "医生建议")
    private String doctorAdvice;

    public void setRecordId(Long recordId) 
    {
        this.recordId = recordId;
    }

    public Long getRecordId() 
    {
        return recordId;
    }

    public void setEncounterId(Long encounterId) 
    {
        this.encounterId = encounterId;
    }

    public Long getEncounterId() 
    {
        return encounterId;
    }

    public void setChiefComplaint(String chiefComplaint) 
    {
        this.chiefComplaint = chiefComplaint;
    }

    public String getChiefComplaint() 
    {
        return chiefComplaint;
    }

    public void setPresentIllness(String presentIllness) 
    {
        this.presentIllness = presentIllness;
    }

    public String getPresentIllness() 
    {
        return presentIllness;
    }

    public void setPastHistory(String pastHistory) 
    {
        this.pastHistory = pastHistory;
    }

    public String getPastHistory() 
    {
        return pastHistory;
    }

    public void setAllergyHistory(String allergyHistory) 
    {
        this.allergyHistory = allergyHistory;
    }

    public String getAllergyHistory() 
    {
        return allergyHistory;
    }

    public void setPhysicalExam(String physicalExam) 
    {
        this.physicalExam = physicalExam;
    }

    public String getPhysicalExam() 
    {
        return physicalExam;
    }

    public void setDiagnosisOpinion(String diagnosisOpinion) 
    {
        this.diagnosisOpinion = diagnosisOpinion;
    }

    public String getDiagnosisOpinion() 
    {
        return diagnosisOpinion;
    }

    public void setTreatmentPlan(String treatmentPlan) 
    {
        this.treatmentPlan = treatmentPlan;
    }

    public String getTreatmentPlan() 
    {
        return treatmentPlan;
    }

    public void setDoctorAdvice(String doctorAdvice) 
    {
        this.doctorAdvice = doctorAdvice;
    }

    public String getDoctorAdvice() 
    {
        return doctorAdvice;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("recordId", getRecordId())
            .append("encounterId", getEncounterId())
            .append("chiefComplaint", getChiefComplaint())
            .append("presentIllness", getPresentIllness())
            .append("pastHistory", getPastHistory())
            .append("allergyHistory", getAllergyHistory())
            .append("physicalExam", getPhysicalExam())
            .append("diagnosisOpinion", getDiagnosisOpinion())
            .append("treatmentPlan", getTreatmentPlan())
            .append("doctorAdvice", getDoctorAdvice())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
