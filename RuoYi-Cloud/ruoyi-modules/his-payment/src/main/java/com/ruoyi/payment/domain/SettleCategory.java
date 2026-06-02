package com.ruoyi.payment.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 结算类别对象 settle_category
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
public class SettleCategory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 结算类别ID(主键) */
    private Long categoryId;

    /** 结算名称：自费/医保/新农合 */
    @Excel(name = "结算名称：自费/医保/新农合")
    private String categoryName;

    /** 排序号 */
    @Excel(name = "排序号")
    private Long sort;

    /** 状态 0正常 1停用 */
    @Excel(name = "状态 0正常 1停用")
    private String status;

    public void setCategoryId(Long categoryId) 
    {
        this.categoryId = categoryId;
    }

    public Long getCategoryId() 
    {
        return categoryId;
    }

    public void setCategoryName(String categoryName) 
    {
        this.categoryName = categoryName;
    }

    public String getCategoryName() 
    {
        return categoryName;
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
            .append("categoryId", getCategoryId())
            .append("categoryName", getCategoryName())
            .append("sort", getSort())
            .append("status", getStatus())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
