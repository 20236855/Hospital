package com.ruoyi.payment.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 收费对象 payment
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
public class Payment extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 收费单ID(主键) */
    private Long paymentId;

    /** 收费单号(唯一) */
    @Excel(name = "收费单号(唯一)")
    private String payNo;

    /** 患者ID */
    @Excel(name = "患者ID")
    private Long patientId;

    /** 关联挂号ID */
    @Excel(name = "关联挂号ID")
    private Long registerId;

    /** 缴费总金额 */
    @Excel(name = "缴费总金额")
    private BigDecimal totalAmount;

    /** 支付方式：现金/微信/支付宝/医保 */
    @Excel(name = "支付方式：现金/微信/支付宝/医保")
    private String payType;

    /** 缴费状态：UNPAID待支付/PAID已支付/REFUND已退费 */
    @Excel(name = "缴费状态：UNPAID待支付/PAID已支付/REFUND已退费")
    private String payStatus;

    /** 收费员ID */
    @Excel(name = "收费员ID")
    private Long operatorId;

    /** 缴费时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "缴费时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date payTime;

    public void setPaymentId(Long paymentId) 
    {
        this.paymentId = paymentId;
    }

    public Long getPaymentId() 
    {
        return paymentId;
    }

    public void setPayNo(String payNo) 
    {
        this.payNo = payNo;
    }

    public String getPayNo() 
    {
        return payNo;
    }

    public void setPatientId(Long patientId) 
    {
        this.patientId = patientId;
    }

    public Long getPatientId() 
    {
        return patientId;
    }

    public void setRegisterId(Long registerId) 
    {
        this.registerId = registerId;
    }

    public Long getRegisterId() 
    {
        return registerId;
    }

    public void setTotalAmount(BigDecimal totalAmount) 
    {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getTotalAmount() 
    {
        return totalAmount;
    }

    public void setPayType(String payType) 
    {
        this.payType = payType;
    }

    public String getPayType() 
    {
        return payType;
    }

    public void setPayStatus(String payStatus) 
    {
        this.payStatus = payStatus;
    }

    public String getPayStatus() 
    {
        return payStatus;
    }

    public void setOperatorId(Long operatorId) 
    {
        this.operatorId = operatorId;
    }

    public Long getOperatorId() 
    {
        return operatorId;
    }

    public void setPayTime(Date payTime) 
    {
        this.payTime = payTime;
    }

    public Date getPayTime() 
    {
        return payTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("paymentId", getPaymentId())
            .append("payNo", getPayNo())
            .append("patientId", getPatientId())
            .append("registerId", getRegisterId())
            .append("totalAmount", getTotalAmount())
            .append("payType", getPayType())
            .append("payStatus", getPayStatus())
            .append("operatorId", getOperatorId())
            .append("payTime", getPayTime())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
