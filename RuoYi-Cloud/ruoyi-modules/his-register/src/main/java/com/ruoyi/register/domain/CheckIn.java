package com.ruoyi.register.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 签到对象 check_in
 * 
 * @author ruoyi
 * @date 2026-05-29
 */
public class CheckIn extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 签到ID */
    private Long checkInId;

    /** 挂号ID */
    @Excel(name = "挂号ID")
    private Long registerId;

    /** 排队号 */
    @Excel(name = "排队号")
    private Long queueNo;

    /** 签到时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "签到时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date checkInTime;

    /** 签到状态 */
    @Excel(name = "签到状态")
    private String status;

    /** Patient ID for permission filtering. */
    private Long patientId;

    /** Department ID for nurse scope filtering. */
    private Long deptId;

    /** Doctor ID for doctor scope filtering. */
    private Long doctorId;

    public void setCheckInId(Long checkInId) 
    {
        this.checkInId = checkInId;
    }

    public Long getCheckInId() 
    {
        return checkInId;
    }

    public void setRegisterId(Long registerId) 
    {
        this.registerId = registerId;
    }

    public Long getRegisterId() 
    {
        return registerId;
    }

    public void setQueueNo(Long queueNo) 
    {
        this.queueNo = queueNo;
    }

    public Long getQueueNo() 
    {
        return queueNo;
    }

    public void setCheckInTime(Date checkInTime) 
    {
        this.checkInTime = checkInTime;
    }

    public Date getCheckInTime() 
    {
        return checkInTime;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setPatientId(Long patientId)
    {
        this.patientId = patientId;
    }

    public Long getPatientId()
    {
        return patientId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDoctorId(Long doctorId)
    {
        this.doctorId = doctorId;
    }

    public Long getDoctorId()
    {
        return doctorId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("checkInId", getCheckInId())
            .append("registerId", getRegisterId())
            .append("queueNo", getQueueNo())
            .append("checkInTime", getCheckInTime())
            .append("status", getStatus())
            .append("patientId", getPatientId())
            .append("deptId", getDeptId())
            .append("doctorId", getDoctorId())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
