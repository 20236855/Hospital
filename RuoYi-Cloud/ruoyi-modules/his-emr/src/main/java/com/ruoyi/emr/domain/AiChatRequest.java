package com.ruoyi.emr.domain;

/**
 * AI问诊请求
 */
public class AiChatRequest
{
    private String message;

    private String sessionId;

    private Long patientId;

    /** 模式：patient=患者端, doctor=医生端（默认） */
    private String mode;

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
    }

    public Long getPatientId()
    {
        return patientId;
    }

    public void setPatientId(Long patientId)
    {
        this.patientId = patientId;
    }

    public String getMode()
    {
        return mode;
    }

    public void setMode(String mode)
    {
        this.mode = mode;
    }
}
