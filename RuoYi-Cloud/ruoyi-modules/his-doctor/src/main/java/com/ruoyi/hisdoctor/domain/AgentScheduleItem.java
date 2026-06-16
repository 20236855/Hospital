package com.ruoyi.hisdoctor.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 智能排班周视图明细。
 */
public class AgentScheduleItem
{
    private Long scheduleId;
    private Long doctorId;
    private String doctorName;
    private Long deptId;
    private String deptName;
    private Long levelId;
    private String levelName;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date scheduleDate;

    private String timeSlot;
    private Long maxNumber;
    private Long reservedNumber;
    private Long availableNumber;
    private String status;
    private Boolean generated;
    private String reason;

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

    public String getDoctorName()
    {
        return doctorName;
    }

    public void setDoctorName(String doctorName)
    {
        this.doctorName = doctorName;
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

    public Date getScheduleDate()
    {
        return scheduleDate;
    }

    public void setScheduleDate(Date scheduleDate)
    {
        this.scheduleDate = scheduleDate;
    }

    public String getTimeSlot()
    {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot)
    {
        this.timeSlot = timeSlot;
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
        if (availableNumber != null)
        {
            return availableNumber;
        }
        if (maxNumber == null || reservedNumber == null)
        {
            return null;
        }
        long available = maxNumber - reservedNumber;
        return available > 0 ? available : 0;
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

    public Boolean getGenerated()
    {
        return generated;
    }

    public void setGenerated(Boolean generated)
    {
        this.generated = generated;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }
}
