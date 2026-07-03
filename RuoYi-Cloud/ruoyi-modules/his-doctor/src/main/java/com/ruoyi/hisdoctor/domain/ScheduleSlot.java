package com.ruoyi.hisdoctor.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 医生排班时间片对象 schedule_slot
 */
public class ScheduleSlot extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long slotId;

    private Long scheduleId;

    private Long doctorId;

    private Long deptId;

    private String deptName;

    private String doctorName;

    private String avatar;

    private Long levelId;

    private String levelName;

    private BigDecimal fee;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date scheduleDate;

    private String startTime;

    private String endTime;

    private Long maxNumber;

    private Long reservedNumber;

    private Long availableNumber;

    private String status;

    private Long roleId;

    private String beginDate;

    private String endDate;

    public Long getSlotId()
    {
        return slotId;
    }

    public void setSlotId(Long slotId)
    {
        this.slotId = slotId;
    }

    public Long getScheduleId()
    {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId)
    {
        this.scheduleId = scheduleId;
    }

    public Long getDoctorId()
    {
        return doctorId;
    }

    public void setDoctorId(Long doctorId)
    {
        this.doctorId = doctorId;
    }

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    public String getDeptName()
    {
        return deptName;
    }

    public void setDeptName(String deptName)
    {
        this.deptName = deptName;
    }

    public String getDoctorName()
    {
        return doctorName;
    }

    public void setDoctorName(String doctorName)
    {
        this.doctorName = doctorName;
    }

    public String getAvatar()
    {
        return avatar;
    }

    public void setAvatar(String avatar)
    {
        this.avatar = avatar;
    }

    public Long getLevelId()
    {
        return levelId;
    }

    public void setLevelId(Long levelId)
    {
        this.levelId = levelId;
    }

    public String getLevelName()
    {
        return levelName;
    }

    public void setLevelName(String levelName)
    {
        this.levelName = levelName;
    }

    public BigDecimal getFee()
    {
        return fee;
    }

    public void setFee(BigDecimal fee)
    {
        this.fee = fee;
    }

    public Date getScheduleDate()
    {
        return scheduleDate;
    }

    public void setScheduleDate(Date scheduleDate)
    {
        this.scheduleDate = scheduleDate;
    }

    public String getStartTime()
    {
        return startTime;
    }

    public void setStartTime(String startTime)
    {
        this.startTime = startTime;
    }

    public String getEndTime()
    {
        return endTime;
    }

    public void setEndTime(String endTime)
    {
        this.endTime = endTime;
    }

    public Long getMaxNumber()
    {
        return maxNumber;
    }

    public void setMaxNumber(Long maxNumber)
    {
        this.maxNumber = maxNumber;
    }

    public Long getReservedNumber()
    {
        return reservedNumber;
    }

    public void setReservedNumber(Long reservedNumber)
    {
        this.reservedNumber = reservedNumber;
    }

    public Long getAvailableNumber()
    {
        if (maxNumber == null || reservedNumber == null)
        {
            return availableNumber;
        }
        long available = maxNumber - reservedNumber;
        return available >= 0 ? available : 0;
    }

    public void setAvailableNumber(Long availableNumber)
    {
        this.availableNumber = availableNumber;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Long getRoleId()
    {
        return roleId;
    }

    public void setRoleId(Long roleId)
    {
        this.roleId = roleId;
    }

    public String getBeginDate()
    {
        return beginDate;
    }

    public void setBeginDate(String beginDate)
    {
        this.beginDate = beginDate;
    }

    public String getEndDate()
    {
        return endDate;
    }

    public void setEndDate(String endDate)
    {
        this.endDate = endDate;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("slotId", getSlotId())
            .append("scheduleId", getScheduleId())
            .append("doctorId", getDoctorId())
            .append("deptId", getDeptId())
            .append("doctorName", getDoctorName())
            .append("avatar", getAvatar())
            .append("scheduleDate", getScheduleDate())
            .append("startTime", getStartTime())
            .append("endTime", getEndTime())
            .append("maxNumber", getMaxNumber())
            .append("reservedNumber", getReservedNumber())
            .append("status", getStatus())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
