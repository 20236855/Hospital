package com.ruoyi.emr.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 接诊对象 encounter
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
public class Encounter extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 接诊ID */
    private Long encounterId;

    /** 挂号ID */
    @Excel(name = "挂号ID")
    private Long registerId;

    /** 患者ID */
    @Excel(name = "患者ID")
    private Long patientId;

    /** 医生ID / Doctor ID for doctor scope filtering. */
    @Excel(name = "医生ID")
    private Long doctorId;

    /** 科室ID */
    @Excel(name = "科室ID")
    private Long deptId;

    /** 接诊类型 */
    @Excel(name = "接诊类型")
    private String encounterType;

    /** 接诊状态 */
    @Excel(name = "接诊状态")
    private String encounterStatus;

    /** 接诊时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "接诊时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date visitTime;

    /** 结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "结束时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date finishTime;

    public void setEncounterId(Long encounterId) 
    {
        this.encounterId = encounterId;
    }

    public Long getEncounterId() 
    {
        return encounterId;
    }

    public void setRegisterId(Long registerId) 
    {
        this.registerId = registerId;
    }

    public Long getRegisterId() 
    {
        return registerId;
    }

    public void setPatientId(Long patientId) 
    {
        this.patientId = patientId;
    }

    public Long getPatientId() 
    {
        return patientId;
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

    public void setEncounterType(String encounterType) 
    {
        this.encounterType = encounterType;
    }

    public String getEncounterType() 
    {
        return encounterType;
    }

    public void setEncounterStatus(String encounterStatus) 
    {
        this.encounterStatus = encounterStatus;
    }

    public String getEncounterStatus() 
    {
        return encounterStatus;
    }

    public void setVisitTime(Date visitTime) 
    {
        this.visitTime = visitTime;
    }

    public Date getVisitTime() 
    {
        return visitTime;
    }

    public void setFinishTime(Date finishTime) 
    {
        this.finishTime = finishTime;
    }

    public Date getFinishTime() 
    {
        return finishTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("encounterId", getEncounterId())
            .append("registerId", getRegisterId())
            .append("patientId", getPatientId())
            .append("doctorId", getDoctorId())
            .append("deptId", getDeptId())
            .append("encounterType", getEncounterType())
            .append("encounterStatus", getEncounterStatus())
            .append("visitTime", getVisitTime())
            .append("finishTime", getFinishTime())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
