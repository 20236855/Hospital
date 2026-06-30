package com.ruoyi.hisdoctor.domain;

import java.util.Date;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 医生排班对象 schedule
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
public class Schedule extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 排班ID */
    private Long scheduleId;

    /** 医生ID */
    @Excel(name = "医生ID")
    private Long doctorId;

    /** 科室ID */
    private Long deptId;

    /** 医生姓名 */
    private String doctorName;

    /** 医生角色ID，仅用于查询过滤 */
    private Long roleId;

    /** 挂号级别ID */
    private Long levelId;

    /** 挂号级别名称 */
    private String levelName;

    /** 挂号费用 */
    private BigDecimal fee;

    /** 查询开始日期 */
    private String beginDate;

    /** 查询结束日期 */
    private String endDate;

    /** 排班日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Excel(name = "排班日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date scheduleDate;

    /** 午别（morning/afternoon/evening） */
    @Excel(name = "午别", readConverterExp = "m=orning/afternoon/evening")
    private String timeSlot;

    /** 最大挂号数 */
    @Excel(name = "最大挂号数")
    private Long maxNumber;

    /** 已预约人数 */
    @Excel(name = "已预约人数")
    private Long reservedNumber;

    /** 可预约人数 */
    @Excel(name = "可预约人数")
    private Long availableNumber;

    /** 状态 */
    @Excel(name = "状态")
    private String status;

    public void setScheduleId(Long scheduleId) 
    {
        this.scheduleId = scheduleId;
    }

    public Long getScheduleId() 
    {
        return scheduleId;
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

    public void setDoctorName(String doctorName)
    {
        this.doctorName = doctorName;
    }

    public String getDoctorName()
    {
        return doctorName;
    }

    public void setRoleId(Long roleId)
    {
        this.roleId = roleId;
    }

    public Long getRoleId()
    {
        return roleId;
    }

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

    public void setBeginDate(String beginDate)
    {
        this.beginDate = beginDate;
    }

    public String getBeginDate()
    {
        return beginDate;
    }

    public void setEndDate(String endDate)
    {
        this.endDate = endDate;
    }

    public String getEndDate()
    {
        return endDate;
    }

    public void setScheduleDate(Date scheduleDate) 
    {
        this.scheduleDate = scheduleDate;
    }

    public Date getScheduleDate() 
    {
        return scheduleDate;
    }

    public void setTimeSlot(String timeSlot) 
    {
        this.timeSlot = timeSlot;
    }

    public String getTimeSlot() 
    {
        return timeSlot;
    }

    public void setMaxNumber(Long maxNumber) 
    {
        this.maxNumber = maxNumber;
    }

    public Long getMaxNumber() 
    {
        return maxNumber;
    }

    public void setReservedNumber(Long reservedNumber) 
    {
        this.reservedNumber = reservedNumber;
    }

    public Long getReservedNumber() 
    {
        return reservedNumber;
    }

    public Long getAvailableNumber()
    {
        if (this.maxNumber == null || this.reservedNumber == null)
        {
            return null;
        }
        long available = this.maxNumber - this.reservedNumber;
        return available >= 0 ? available : 0;
    }

    public void setAvailableNumber(Long availableNumber)
    {
        this.availableNumber = availableNumber;
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
            .append("scheduleId", getScheduleId())
            .append("doctorId", getDoctorId())
            .append("deptId", getDeptId())
            .append("doctorName", getDoctorName())
            .append("roleId", getRoleId())
            .append("levelId", getLevelId())
            .append("levelName", getLevelName())
            .append("fee", getFee())
            .append("beginDate", getBeginDate())
            .append("endDate", getEndDate())
            .append("scheduleDate", getScheduleDate())
            .append("timeSlot", getTimeSlot())
            .append("maxNumber", getMaxNumber())
            .append("reservedNumber", getReservedNumber())
            .append("availableNumber", getAvailableNumber())
            .append("status", getStatus())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
