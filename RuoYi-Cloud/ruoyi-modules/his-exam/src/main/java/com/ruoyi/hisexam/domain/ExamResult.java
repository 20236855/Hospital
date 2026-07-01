package com.ruoyi.hisexam.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 检查检验结果对象 exam_result
 *
 * @author ruoyi
 * @date 2026-06-30
 */
public class ExamResult extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 结果ID */
    private Long resultId;

    /** 关联exam_apply主键 */
    @Excel(name = "申请单ID")
    private Long applyId;

    /** 结果类型：CHECK检查 INSPEC检验 DISPOSAL处置 */
    @Excel(name = "结果类型")
    private String resultType;

    /** 明细类型：IMAGE图片 ITEM指标 TEXT文本 */
    @Excel(name = "明细类型")
    private String itemType;

    /** 项目编码，如WBC/RBC/HGB */
    @Excel(name = "项目编码")
    private String itemCode;

    /** 项目名称，如白细胞计数、胸部CT图片 */
    @Excel(name = "项目名称")
    private String itemName;

    /** 结果值 */
    @Excel(name = "结果值")
    private String resultValue;

    /** 单位 */
    @Excel(name = "单位")
    private String resultUnit;

    /** 参考范围 */
    @Excel(name = "参考范围")
    private String referenceRange;

    /** 异常标识：0正常 1偏高 2偏低 3阳性 4阴性 */
    @Excel(name = "异常标识")
    private String abnormalFlag;

    /** 图片访问地址 */
    @Excel(name = "图片地址")
    private String imageUrl;

    /** 影像所见/检查所见 */
    @Excel(name = "影像所见")
    private String imageFind;

    /** 诊断意见/检验意见 */
    @Excel(name = "诊断意见")
    private String diagnosisOpinion;

    /** 结论分类 */
    @Excel(name = "结论分类")
    private String diagnosisResult;

    /** 诊疗建议 */
    @Excel(name = "诊疗建议")
    private String suggestion;

    /** 排序号 */
    @Excel(name = "排序号")
    private Long sort;

    /** 状态：0草稿 1已发布 */
    @Excel(name = "状态")
    private String status;

    /** 检查/诊断医生ID */
    @Excel(name = "医生ID")
    private Long doctorId;

    /** 报告时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "报告时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date reportTime;

    public void setResultId(Long resultId)
    {
        this.resultId = resultId;
    }

    public Long getResultId()
    {
        return resultId;
    }

    public void setApplyId(Long applyId)
    {
        this.applyId = applyId;
    }

    public Long getApplyId()
    {
        return applyId;
    }

    public void setResultType(String resultType)
    {
        this.resultType = resultType;
    }

    public String getResultType()
    {
        return resultType;
    }

    public void setItemType(String itemType)
    {
        this.itemType = itemType;
    }

    public String getItemType()
    {
        return itemType;
    }

    public void setItemCode(String itemCode)
    {
        this.itemCode = itemCode;
    }

    public String getItemCode()
    {
        return itemCode;
    }

    public void setItemName(String itemName)
    {
        this.itemName = itemName;
    }

    public String getItemName()
    {
        return itemName;
    }

    public void setResultValue(String resultValue)
    {
        this.resultValue = resultValue;
    }

    public String getResultValue()
    {
        return resultValue;
    }

    public void setResultUnit(String resultUnit)
    {
        this.resultUnit = resultUnit;
    }

    public String getResultUnit()
    {
        return resultUnit;
    }

    public void setReferenceRange(String referenceRange)
    {
        this.referenceRange = referenceRange;
    }

    public String getReferenceRange()
    {
        return referenceRange;
    }

    public void setAbnormalFlag(String abnormalFlag)
    {
        this.abnormalFlag = abnormalFlag;
    }

    public String getAbnormalFlag()
    {
        return abnormalFlag;
    }

    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl()
    {
        return imageUrl;
    }

    public void setImageFind(String imageFind)
    {
        this.imageFind = imageFind;
    }

    public String getImageFind()
    {
        return imageFind;
    }

    public void setDiagnosisOpinion(String diagnosisOpinion)
    {
        this.diagnosisOpinion = diagnosisOpinion;
    }

    public String getDiagnosisOpinion()
    {
        return diagnosisOpinion;
    }

    public void setDiagnosisResult(String diagnosisResult)
    {
        this.diagnosisResult = diagnosisResult;
    }

    public String getDiagnosisResult()
    {
        return diagnosisResult;
    }

    public void setSuggestion(String suggestion)
    {
        this.suggestion = suggestion;
    }

    public String getSuggestion()
    {
        return suggestion;
    }

    public void setSort(Long sort)
    {
        this.sort = sort;
    }

    public Long getSort()
    {
        return sort;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    public void setDoctorId(Long doctorId)
    {
        this.doctorId = doctorId;
    }

    public Long getDoctorId()
    {
        return doctorId;
    }

    public void setReportTime(Date reportTime)
    {
        this.reportTime = reportTime;
    }

    public Date getReportTime()
    {
        return reportTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("resultId", getResultId())
            .append("applyId", getApplyId())
            .append("resultType", getResultType())
            .append("itemType", getItemType())
            .append("itemCode", getItemCode())
            .append("itemName", getItemName())
            .append("resultValue", getResultValue())
            .append("resultUnit", getResultUnit())
            .append("referenceRange", getReferenceRange())
            .append("abnormalFlag", getAbnormalFlag())
            .append("imageUrl", getImageUrl())
            .append("imageFind", getImageFind())
            .append("diagnosisOpinion", getDiagnosisOpinion())
            .append("diagnosisResult", getDiagnosisResult())
            .append("suggestion", getSuggestion())
            .append("sort", getSort())
            .append("status", getStatus())
            .append("doctorId", getDoctorId())
            .append("reportTime", getReportTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
