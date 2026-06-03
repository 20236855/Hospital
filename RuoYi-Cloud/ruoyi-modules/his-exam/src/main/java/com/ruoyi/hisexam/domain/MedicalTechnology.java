package com.ruoyi.hisexam.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 医技项目对象 medical_technology
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
public class MedicalTechnology extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 项目编码 */
    @Excel(name = "项目编码")
    private String techCode;

    /** 项目名称 */
    @Excel(name = "项目名称")
    private String techName;

    /** 规格 */
    @Excel(name = "规格")
    private String techFormat;

    /** 单价 */
    @Excel(name = "单价")
    private BigDecimal techPrice;

    /** 项目类型：CHECK检查/INSPEC检验/DISPOSAL处置 */
    @Excel(name = "项目类型：CHECK检查/INSPEC检验/DISPOSAL处置")
    private String techType;

    /** 费用分类 */
    @Excel(name = "费用分类")
    private String priceType;

    /** 执行科室ID */
    @Excel(name = "执行科室ID")
    private Long deptmentId;

    /** 拼音码(检索用) */
    @Excel(name = "拼音码(检索用)")
    private String pyCode;

    /** 排序号 */
    @Excel(name = "排序号")
    private Long sort;

    /** 状态 0正常 1停用 */
    @Excel(name = "状态 0正常 1停用")
    private String status;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setTechCode(String techCode) 
    {
        this.techCode = techCode;
    }

    public String getTechCode() 
    {
        return techCode;
    }

    public void setTechName(String techName) 
    {
        this.techName = techName;
    }

    public String getTechName() 
    {
        return techName;
    }

    public void setTechFormat(String techFormat) 
    {
        this.techFormat = techFormat;
    }

    public String getTechFormat() 
    {
        return techFormat;
    }

    public void setTechPrice(BigDecimal techPrice) 
    {
        this.techPrice = techPrice;
    }

    public BigDecimal getTechPrice() 
    {
        return techPrice;
    }

    public void setTechType(String techType) 
    {
        this.techType = techType;
    }

    public String getTechType() 
    {
        return techType;
    }

    public void setPriceType(String priceType) 
    {
        this.priceType = priceType;
    }

    public String getPriceType() 
    {
        return priceType;
    }

    public void setDeptmentId(Long deptmentId) 
    {
        this.deptmentId = deptmentId;
    }

    public Long getDeptmentId() 
    {
        return deptmentId;
    }

    public void setPyCode(String pyCode) 
    {
        this.pyCode = pyCode;
    }

    public String getPyCode() 
    {
        return pyCode;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("techCode", getTechCode())
            .append("techName", getTechName())
            .append("techFormat", getTechFormat())
            .append("techPrice", getTechPrice())
            .append("techType", getTechType())
            .append("priceType", getPriceType())
            .append("deptmentId", getDeptmentId())
            .append("pyCode", getPyCode())
            .append("sort", getSort())
            .append("status", getStatus())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
