package com.ruoyi.hisdoctor.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 挂号级别对象 regist_level
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
public class RegistLevel extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 级别ID(主键) */
    private Long levelId;

    /** 级别名称：普通号/专家号/主任号 */
    @Excel(name = "级别名称：普通号/专家号/主任号")
    private String levelName;

    /** 对应挂号费用 */
    @Excel(name = "对应挂号费用")
    private BigDecimal fee;

    /** 排序号 */
    @Excel(name = "排序号")
    private Long sort;

    /** 状态 0正常 1停用 */
    @Excel(name = "状态 0正常 1停用")
    private String status;

    public void setLevelId(Long levelId) 
    {
        this.levelId = levelId;
    }

    public Long getLevelId() 
    {
        return levelId;
    }

    public void setLevelName(String levelName) 
    {
        this.levelName = levelName;
    }

    public String getLevelName() 
    {
        return levelName;
    }

    public void setFee(BigDecimal fee) 
    {
        this.fee = fee;
    }

    public BigDecimal getFee() 
    {
        return fee;
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
            .append("levelId", getLevelId())
            .append("levelName", getLevelName())
            .append("fee", getFee())
            .append("sort", getSort())
            .append("status", getStatus())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
