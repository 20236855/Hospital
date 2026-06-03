package com.ruoyi.hisprescription.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 药品信息对象 drug
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
public class Drug extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 药品ID(主键) */
    private Long id;

    /** 药品编码 */
    @Excel(name = "药品编码")
    private String drugCode;

    /** 药品名称 */
    @Excel(name = "药品名称")
    private String drugName;

    /** 拼音码(检索用) */
    @Excel(name = "拼音码(检索用)")
    private String pyCode;

    /** 药品规格 */
    @Excel(name = "药品规格")
    private String spec;

    /** 包装单位 */
    @Excel(name = "包装单位")
    private String unit;

    /** 单价 */
    @Excel(name = "单价")
    private BigDecimal drugPrice;

    /** 生产厂家 */
    @Excel(name = "生产厂家")
    private String manufacturer;

    /** 药剂类型 */
    @Excel(name = "药剂类型")
    private String drugType;

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

    public void setDrugCode(String drugCode) 
    {
        this.drugCode = drugCode;
    }

    public String getDrugCode() 
    {
        return drugCode;
    }

    public void setDrugName(String drugName) 
    {
        this.drugName = drugName;
    }

    public String getDrugName() 
    {
        return drugName;
    }

    public void setPyCode(String pyCode) 
    {
        this.pyCode = pyCode;
    }

    public String getPyCode() 
    {
        return pyCode;
    }

    public void setSpec(String spec) 
    {
        this.spec = spec;
    }

    public String getSpec() 
    {
        return spec;
    }

    public void setUnit(String unit) 
    {
        this.unit = unit;
    }

    public String getUnit() 
    {
        return unit;
    }

    public void setDrugPrice(BigDecimal drugPrice) 
    {
        this.drugPrice = drugPrice;
    }

    public BigDecimal getDrugPrice() 
    {
        return drugPrice;
    }

    public void setManufacturer(String manufacturer) 
    {
        this.manufacturer = manufacturer;
    }

    public String getManufacturer() 
    {
        return manufacturer;
    }

    public void setDrugType(String drugType) 
    {
        this.drugType = drugType;
    }

    public String getDrugType() 
    {
        return drugType;
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
            .append("drugCode", getDrugCode())
            .append("drugName", getDrugName())
            .append("pyCode", getPyCode())
            .append("spec", getSpec())
            .append("unit", getUnit())
            .append("drugPrice", getDrugPrice())
            .append("manufacturer", getManufacturer())
            .append("drugType", getDrugType())
            .append("sort", getSort())
            .append("status", getStatus())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
