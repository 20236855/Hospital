package com.ruoyi.register.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 挂号对象 register
 * 
 * @author ruoyi
 * @date 2026-05-29
 */
public class Register extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 挂号ID */
    private Long registerId;

    /** 挂号单号 */
    @Excel(name = "挂号单号")
    private String registerNo;

    /** 患者ID */
    @Excel(name = "患者ID")
    private Long patientId;

    /** 排班ID */
    @Excel(name = "排班ID")
    private Long scheduleId;

    /** 医生ID */
    @Excel(name = "医生ID")
    private Long doctorId;

    /** 科室ID */
    @Excel(name = "科室ID")
    private Long deptId;

    /** 挂号级别ID */
    @Excel(name = "挂号级别ID")
    private Long levelId;

    /** 挂号类型（online/offline） */
    @Excel(name = "挂号类型", readConverterExp = "o=nline/offline")
    private String registerType;

    /** 挂号状态 */
    @Excel(name = "挂号状态")
    private String registerStatus;

    /** 支付状态 */
    @Excel(name = "支付状态")
    private String payStatus;

    /** 挂号费用 */
    @Excel(name = "挂号费用")
    private BigDecimal registerFee;

    /** 挂号时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "挂号时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date registerTime;

    /** 身份证号（查询/入参用，不入挂号表） */
    private String idCard;

    /** 患者姓名（查询/展示用，不入挂号表） */
    private String patientName;

    /** 医生姓名（查询/展示用，不入挂号表） */
    private String doctorName;

    /** 科室名称（查询/展示用，不入挂号表） */
    private String deptName;

    /** 挂号级别名称（查询/展示用，不入挂号表） */
    private String levelName;

    public void setRegisterId(Long registerId) 
    {
        this.registerId = registerId;
    }

    public Long getRegisterId() 
    {
        return registerId;
    }

    public void setRegisterNo(String registerNo) 
    {
        this.registerNo = registerNo;
    }

    public String getRegisterNo() 
    {
        return registerNo;
    }

    public void setPatientId(Long patientId) 
    {
        this.patientId = patientId;
    }

    public Long getPatientId() 
    {
        return patientId;
    }

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

    public void setDeptId(Long deptId) 
    {
        this.deptId = deptId;
    }

    public Long getDeptId() 
    {
        return deptId;
    }

    public void setLevelId(Long levelId) 
    {
        this.levelId = levelId;
    }

    public Long getLevelId() 
    {
        return levelId;
    }

    public void setRegisterType(String registerType) 
    {
        this.registerType = registerType;
    }

    public String getRegisterType() 
    {
        return registerType;
    }

    public void setRegisterStatus(String registerStatus) 
    {
        this.registerStatus = registerStatus;
    }

    public String getRegisterStatus() 
    {
        return registerStatus;
    }

    public void setPayStatus(String payStatus) 
    {
        this.payStatus = payStatus;
    }

    public String getPayStatus() 
    {
        return payStatus;
    }

    public void setRegisterFee(BigDecimal registerFee) 
    {
        this.registerFee = registerFee;
    }

    public BigDecimal getRegisterFee() 
    {
        return registerFee;
    }

    public void setRegisterTime(Date registerTime) 
    {
        this.registerTime = registerTime;
    }

    public Date getRegisterTime() 
    {
        return registerTime;
    }

    public void setIdCard(String idCard)
    {
        this.idCard = idCard;
    }

    public String getIdCard()
    {
        return idCard;
    }

    public void setPatientName(String patientName)
    {
        this.patientName = patientName;
    }

    public String getPatientName()
    {
        return patientName;
    }

    public void setDoctorName(String doctorName)
    {
        this.doctorName = doctorName;
    }

    public String getDoctorName()
    {
        return doctorName;
    }

    public void setDeptName(String deptName)
    {
        this.deptName = deptName;
    }

    public String getDeptName()
    {
        return deptName;
    }

    public void setLevelName(String levelName)
    {
        this.levelName = levelName;
    }

    public String getLevelName()
    {
        return levelName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("registerId", getRegisterId())
            .append("registerNo", getRegisterNo())
            .append("patientId", getPatientId())
            .append("scheduleId", getScheduleId())
            .append("doctorId", getDoctorId())
            .append("deptId", getDeptId())
            .append("levelId", getLevelId())
            .append("registerType", getRegisterType())
            .append("registerStatus", getRegisterStatus())
            .append("payStatus", getPayStatus())
            .append("registerFee", getRegisterFee())
            .append("registerTime", getRegisterTime())
            .append("idCard", getIdCard())
            .append("patientName", getPatientName())
            .append("doctorName", getDoctorName())
            .append("deptName", getDeptName())
            .append("levelName", getLevelName())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
