package com.ruoyi.hisdoctor.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 智能排班运行记录。
 */
public class AgentScheduleRun
{
    private Long runId;
    private String runNo;
    private String triggerType;
    private String stage;
    private String status;
    private String modelProvider;
    private String modelName;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date weekStart;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date weekEnd;

    private Integer perceivedDoctorCount;
    private Integer perceivedDeptCount;
    private Integer generatedCount;
    private Integer insertedCount;
    private Integer fallbackCount;
    private Integer warningCount;
    private Long durationMs;
    private String perceptionSummary;
    private String reasoningSummary;
    private String actionSummary;
    private String warningSummary;
    private String errorMessage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    public Long getRunId()
    {
        return runId;
    }

    public void setRunId(Long runId)
    {
        this.runId = runId;
    }

    public String getRunNo()
    {
        return runNo;
    }

    public void setRunNo(String runNo)
    {
        this.runNo = runNo;
    }

    public String getTriggerType()
    {
        return triggerType;
    }

    public void setTriggerType(String triggerType)
    {
        this.triggerType = triggerType;
    }

    public String getStage()
    {
        return stage;
    }

    public void setStage(String stage)
    {
        this.stage = stage;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getModelProvider()
    {
        return modelProvider;
    }

    public void setModelProvider(String modelProvider)
    {
        this.modelProvider = modelProvider;
    }

    public String getModelName()
    {
        return modelName;
    }

    public void setModelName(String modelName)
    {
        this.modelName = modelName;
    }

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

    public Integer getPerceivedDoctorCount()
    {
        return perceivedDoctorCount;
    }

    public void setPerceivedDoctorCount(Integer perceivedDoctorCount)
    {
        this.perceivedDoctorCount = perceivedDoctorCount;
    }

    public Integer getPerceivedDeptCount()
    {
        return perceivedDeptCount;
    }

    public void setPerceivedDeptCount(Integer perceivedDeptCount)
    {
        this.perceivedDeptCount = perceivedDeptCount;
    }

    public Integer getGeneratedCount()
    {
        return generatedCount;
    }

    public void setGeneratedCount(Integer generatedCount)
    {
        this.generatedCount = generatedCount;
    }

    public Integer getInsertedCount()
    {
        return insertedCount;
    }

    public void setInsertedCount(Integer insertedCount)
    {
        this.insertedCount = insertedCount;
    }

    public Integer getFallbackCount()
    {
        return fallbackCount;
    }

    public void setFallbackCount(Integer fallbackCount)
    {
        this.fallbackCount = fallbackCount;
    }

    public Integer getWarningCount()
    {
        return warningCount;
    }

    public void setWarningCount(Integer warningCount)
    {
        this.warningCount = warningCount;
    }

    public Long getDurationMs()
    {
        return durationMs;
    }

    public void setDurationMs(Long durationMs)
    {
        this.durationMs = durationMs;
    }

    public String getPerceptionSummary()
    {
        return perceptionSummary;
    }

    public void setPerceptionSummary(String perceptionSummary)
    {
        this.perceptionSummary = perceptionSummary;
    }

    public String getReasoningSummary()
    {
        return reasoningSummary;
    }

    public void setReasoningSummary(String reasoningSummary)
    {
        this.reasoningSummary = reasoningSummary;
    }

    public String getActionSummary()
    {
        return actionSummary;
    }

    public void setActionSummary(String actionSummary)
    {
        this.actionSummary = actionSummary;
    }

    public String getWarningSummary()
    {
        return warningSummary;
    }

    public void setWarningSummary(String warningSummary)
    {
        this.warningSummary = warningSummary;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    public Date getStartTime()
    {
        return startTime;
    }

    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }

    public Date getEndTime()
    {
        return endTime;
    }

    public void setEndTime(Date endTime)
    {
        this.endTime = endTime;
    }
}
