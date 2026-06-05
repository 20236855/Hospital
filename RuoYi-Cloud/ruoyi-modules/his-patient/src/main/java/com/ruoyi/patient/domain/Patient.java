package com.ruoyi.patient.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 患者对象 patient
 *
 * @author ruoyi
 * @date 2026-05-29
 */
public class Patient extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 患者ID */
    private Long patientId;

    /** 病历号 */
    @Excel(name = "病历号")
    private String patientNo;

    /** 患者姓名 */
    @Excel(name = "患者姓名")
    private String name;

    /** 性别 */
    @Excel(name = "性别")
    private String gender;

    /** 出生日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "出生日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date birthday;

    /** 年龄 */
    @Excel(name = "年龄")
    private Long age;

    /** 手机号 */
    @Excel(name = "手机号")
    private String phone;

    /** 身份证号 */
    @Excel(name = "身份证号")
    private String idCard;

    /** 血型 */
    @Excel(name = "血型")
    private String bloodType;

    /** 婚姻状态 */
    @Excel(name = "婚姻状态")
    private String maritalStatus;

    /** 家庭住址 */
    @Excel(name = "家庭住址")
    private String address;

    /** 紧急联系人 */
    @Excel(name = "紧急联系人")
    private String emergencyContact;

    /** 紧急联系电话 */
    @Excel(name = "紧急联系电话")
    private String emergencyPhone;

    /** 过敏史 */
    @Excel(name = "过敏史")
    private String allergyHistory;

    /** 既往史 */
    @Excel(name = "既往史")
    private String pastHistory;

    /** 状态 */
    @Excel(name = "状态")
    private String status;

    /** 系统用户id */
    @Excel(name = "用户id")
    private Long userId;


    public void setPatientId(Long patientId)
    {
        this.patientId = patientId;
    }

    public Long getPatientId()
    {
        return patientId;
    }

    public void setPatientNo(String patientNo)
    {
        this.patientNo = patientNo;
    }

    public String getPatientNo()
    {
        return patientNo;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setGender(String gender)
    {
        this.gender = gender;
    }

    public String getGender()
    {
        return gender;
    }

    public void setBirthday(Date birthday)
    {
        this.birthday = birthday;
    }

    public Date getBirthday()
    {
        return birthday;
    }

    public void setAge(Long age)
    {
        this.age = age;
    }

    public Long getAge()
    {
        return age;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setIdCard(String idCard)
    {
        this.idCard = idCard;
    }

    public String getIdCard()
    {
        return idCard;
    }

    public void setBloodType(String bloodType)
    {
        this.bloodType = bloodType;
    }

    public String getBloodType()
    {
        return bloodType;
    }

    public void setMaritalStatus(String maritalStatus)
    {
        this.maritalStatus = maritalStatus;
    }

    public String getMaritalStatus()
    {
        return maritalStatus;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getAddress()
    {
        return address;
    }

    public void setEmergencyContact(String emergencyContact)
    {
        this.emergencyContact = emergencyContact;
    }

    public String getEmergencyContact()
    {
        return emergencyContact;
    }

    public void setEmergencyPhone(String emergencyPhone)
    {
        this.emergencyPhone = emergencyPhone;
    }

    public String getEmergencyPhone()
    {
        return emergencyPhone;
    }

    public void setAllergyHistory(String allergyHistory)
    {
        this.allergyHistory = allergyHistory;
    }

    public String getAllergyHistory()
    {
        return allergyHistory;
    }

    public void setPastHistory(String pastHistory)
    {
        this.pastHistory = pastHistory;
    }

    public String getPastHistory()
    {
        return pastHistory;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("patientId", getPatientId())
            .append("patientNo", getPatientNo())
            .append("name", getName())
            .append("gender", getGender())
            .append("birthday", getBirthday())
            .append("age", getAge())
            .append("phone", getPhone())
            .append("idCard", getIdCard())
            .append("bloodType", getBloodType())
            .append("maritalStatus", getMaritalStatus())
            .append("address", getAddress())
            .append("emergencyContact", getEmergencyContact())
            .append("emergencyPhone", getEmergencyPhone())
            .append("allergyHistory", getAllergyHistory())
            .append("pastHistory", getPastHistory())
            .append("status", getStatus())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("userId", getUserId())
            .toString();
    }
}
