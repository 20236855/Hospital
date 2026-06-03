package com.ruoyi.hisdoctor.domain;

import java.util.Date;
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

    /** 排班日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
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
            .append("scheduleDate", getScheduleDate())
            .append("timeSlot", getTimeSlot())
            .append("maxNumber", getMaxNumber())
            .append("reservedNumber", getReservedNumber())
            .append("status", getStatus())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
