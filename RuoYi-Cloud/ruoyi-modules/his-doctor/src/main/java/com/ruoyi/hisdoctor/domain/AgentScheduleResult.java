package com.ruoyi.hisdoctor.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 智能排班执行结果。
 */
public class AgentScheduleResult
{
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date weekStart;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date weekEnd;

    private Boolean preview;
    private Integer doctorCount = 0;
    private Integer deptCount = 0;
    private Integer insertedCount = 0;
    private Integer retainedCount = 0;
    private Integer fallbackCount = 0;
    private String publishStatus;
    private String message;
    private List<String> reports = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    private List<AgentScheduleItem> schedules = new ArrayList<>();

    public Date getWeekStart()
    {
        return weekStart;
    }

    public void setWeekStart(Date weekStart)
    {
        this.weekStart = weekStart;
    }

    public Date getWeekEnd()
    {
        return weekEnd;
    }

    public void setWeekEnd(Date weekEnd)
    {
        this.weekEnd = weekEnd;
    }

    public Boolean getPreview()
    {
        return preview;
    }

    public void setPreview(Boolean preview)
    {
        this.preview = preview;
    }

    public Integer getDoctorCount()
    {
        return doctorCount;
    }

    public void setDoctorCount(Integer doctorCount)
    {
        this.doctorCount = doctorCount;
    }

    public Integer getDeptCount()
    {
        return deptCount;
    }

    public void setDeptCount(Integer deptCount)
    {
        this.deptCount = deptCount;
    }

    public Integer getInsertedCount()
    {
        return insertedCount;
    }

    public void setInsertedCount(Integer insertedCount)
    {
        this.insertedCount = insertedCount;
    }

    public Integer getRetainedCount()
    {
        return retainedCount;
    }

    public void setRetainedCount(Integer retainedCount)
    {
        this.retainedCount = retainedCount;
    }

    public Integer getFallbackCount()
    {
        return fallbackCount;
    }

    public void setFallbackCount(Integer fallbackCount)
    {
        this.fallbackCount = fallbackCount;
    }

    public String getPublishStatus()
    {
        return publishStatus;
    }

    public void setPublishStatus(String publishStatus)
    {
        this.publishStatus = publishStatus;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public List<String> getReports()
    {
        return reports;
    }

    public void setReports(List<String> reports)
    {
        this.reports = reports;
    }

    public List<String> getWarnings()
    {
        return warnings;
    }

    public void setWarnings(List<String> warnings)
    {
        this.warnings = warnings;
    }

    public List<AgentScheduleItem> getSchedules()
    {
        return schedules;
    }

    public void setSchedules(List<AgentScheduleItem> schedules)
    {
        this.schedules = schedules;
    }
}
