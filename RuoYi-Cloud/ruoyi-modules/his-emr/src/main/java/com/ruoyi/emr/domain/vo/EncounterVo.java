package com.ruoyi.emr.domain.vo;

import java.util.Date;
import com.ruoyi.emr.domain.Encounter;

/**
 * Encounter view object.
 */
public class EncounterVo extends Encounter
{
    private static final long serialVersionUID = 1L;

    /** Patient name. */
    private String patientName;

    /** Patient phone. */
    private String patientPhone;

    /** Register no. */
    private String registerNo;

    /** Queue no. */
    private Long queueNo;

    /** CheckIn time. */
    private Date checkInTime;

    public String getPatientName()
    {
        return patientName;
    }

    public void setPatientName(String patientName)
    {
        this.patientName = patientName;
    }

    public String getPatientPhone()
    {
        return patientPhone;
    }

    public void setPatientPhone(String patientPhone)
    {
        this.patientPhone = patientPhone;
    }

    public String getRegisterNo()
    {
        return registerNo;
    }

    public void setRegisterNo(String registerNo)
    {
        this.registerNo = registerNo;
    }

    public Long getQueueNo()
    {
        return queueNo;
    }

    public void setQueueNo(Long queueNo)
    {
        this.queueNo = queueNo;
    }

    public Date getCheckInTime()
    {
        return checkInTime;
    }

    public void setCheckInTime(Date checkInTime)
    {
        this.checkInTime = checkInTime;
    }
}
