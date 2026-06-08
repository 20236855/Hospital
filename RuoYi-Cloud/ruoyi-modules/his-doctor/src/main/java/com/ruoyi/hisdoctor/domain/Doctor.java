package com.ruoyi.hisdoctor.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 医生信息对象 doctor
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
public class Doctor extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 医生ID */
    private Long doctorId;

    /** 系统用户ID（关联若依sys_user） */
    @Excel(name = "系统用户ID", readConverterExp = "关=联若依sys_user")
    private Long userId;

    /** 所属科室（关联若依sys_dept） */
    @Excel(name = "所属科室", readConverterExp = "关=联若依sys_dept")
    private Long deptId;

    /** 医生工号 */
    @Excel(name = "医生工号")
    private String doctorNo;

    /** 医生姓名 */
    @Excel(name = "医生姓名")
    private String doctorName;

    /** 性别 */
    @Excel(name = "性别")
    private String gender;

    /** 手机号 */
    @Excel(name = "手机号")
    private String phone;

    /** 挂号级别ID */
    @Excel(name = "挂号级别ID")
    private Long levelId;

    /** 挂号级别名称（查询展示用，不存数据库） */
    private String levelName;

    /** 擅长领域 */
    @Excel(name = "擅长领域")
    private String specialty;

    /** 医生简介 */
    @Excel(name = "医生简介")
    private String introduction;

    /** 头像 */
    @Excel(name = "头像")
    private String avatar;

    /** 状态 */
    @Excel(name = "状态")
    private String status;

    public void setDoctorId(Long doctorId) 
    {
        this.doctorId = doctorId;
    }

    public Long getDoctorId() 
    {
        return doctorId;
    }

    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }

    public void setDeptId(Long deptId) 
    {
        this.deptId = deptId;
    }

    public Long getDeptId() 
    {
        return deptId;
    }

    public void setDoctorNo(String doctorNo) 
    {
        this.doctorNo = doctorNo;
    }

    public String getDoctorNo() 
    {
        return doctorNo;
    }

    public void setDoctorName(String doctorName) 
    {
        this.doctorName = doctorName;
    }

    public String getDoctorName() 
    {
        return doctorName;
    }

    public void setGender(String gender) 
    {
        this.gender = gender;
    }

    public String getGender() 
    {
        return gender;
    }

    public void setPhone(String phone) 
    {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setLevelId(Long levelId) {
        this.levelId = levelId;
    }

    public Long getLevelId() {
        return levelId;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getSpecialty() 
    {
        return specialty;
    }

    public void setIntroduction(String introduction) 
    {
        this.introduction = introduction;
    }

    public String getIntroduction() 
    {
        return introduction;
    }

    public void setAvatar(String avatar) 
    {
        this.avatar = avatar;
    }

    public String getAvatar() 
    {
        return avatar;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("doctorId", getDoctorId())
            .append("userId", getUserId())
            .append("deptId", getDeptId())
            .append("doctorNo", getDoctorNo())
            .append("doctorName", getDoctorName())
            .append("gender", getGender())
            .append("phone", getPhone())
            .append("levelId", getLevelId())
            .append("levelName", getLevelName())
            .append("specialty", getSpecialty())
            .append("introduction", getIntroduction())
            .append("avatar", getAvatar())
            .append("status", getStatus())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
