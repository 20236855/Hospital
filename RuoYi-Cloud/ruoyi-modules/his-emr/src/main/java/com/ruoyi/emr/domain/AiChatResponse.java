package com.ruoyi.emr.domain;

/**
 * AI问诊响应
 */
public class AiChatResponse
{
    private String sessionId;

    private String reply;

    private Long patientId;

    private Boolean hasMedicalRecord;

    public String getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
    }

    public String getReply()
    {
        return reply;
    }

    public void setReply(String reply)
    {
        this.reply = reply;
    }

    public Long getPatientId()
    {
        return patientId;
    }

    public void setPatientId(Long patientId)
    {
        this.patientId = patientId;
    }

    public Boolean getHasMedicalRecord()
    {
        return hasMedicalRecord;
    }

    public void setHasMedicalRecord(Boolean hasMedicalRecord)
    {
        this.hasMedicalRecord = hasMedicalRecord;
    }
}
