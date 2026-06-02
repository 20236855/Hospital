package com.ruoyi.payment.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 收费明细对象 payment_item
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
public class PaymentItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID(主键) */
    private Long itemId;

    /** 收费单ID(外键) */
    @Excel(name = "收费单ID(外键)")
    private Long paymentId;

    /** 业务类型：REGISTER挂号/EXAM医技/PRESCRIPTION处方 */
    @Excel(name = "业务类型：REGISTER挂号/EXAM医技/PRESCRIPTION处方")
    private String bizType;

    /** 对应业务单据ID */
    @Excel(name = "对应业务单据ID")
    private Long bizId;

    /** 收费项目名称 */
    @Excel(name = "收费项目名称")
    private String itemName;

    /** 单价 */
    @Excel(name = "单价")
    private BigDecimal unitPrice;

    /** 数量 */
    @Excel(name = "数量")
    private Long quantity;

    /** 单项小计金额 */
    @Excel(name = "单项小计金额")
    private BigDecimal subtotal;

    public void setItemId(Long itemId) 
    {
        this.itemId = itemId;
    }

    public Long getItemId() 
    {
        return itemId;
    }

    public void setPaymentId(Long paymentId) 
    {
        this.paymentId = paymentId;
    }

    public Long getPaymentId() 
    {
        return paymentId;
    }

    public void setBizType(String bizType) 
    {
        this.bizType = bizType;
    }

    public String getBizType() 
    {
        return bizType;
    }

    public void setBizId(Long bizId) 
    {
        this.bizId = bizId;
    }

    public Long getBizId() 
    {
        return bizId;
    }

    public void setItemName(String itemName) 
    {
        this.itemName = itemName;
    }

    public String getItemName() 
    {
        return itemName;
    }

    public void setUnitPrice(BigDecimal unitPrice) 
    {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getUnitPrice() 
    {
        return unitPrice;
    }

    public void setQuantity(Long quantity) 
    {
        this.quantity = quantity;
    }

    public Long getQuantity() 
    {
        return quantity;
    }

    public void setSubtotal(BigDecimal subtotal) 
    {
        this.subtotal = subtotal;
    }

    public BigDecimal getSubtotal() 
    {
        return subtotal;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("itemId", getItemId())
            .append("paymentId", getPaymentId())
            .append("bizType", getBizType())
            .append("bizId", getBizId())
            .append("itemName", getItemName())
            .append("unitPrice", getUnitPrice())
            .append("quantity", getQuantity())
            .append("subtotal", getSubtotal())
            .append("createTime", getCreateTime())
            .toString();
    }
}
