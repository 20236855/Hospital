package com.ruoyi.payment.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.his.api.RemoteRegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.payment.mapper.PaymentMapper;
import com.ruoyi.payment.domain.Payment;
import com.ruoyi.payment.service.IPaymentService;

/**
 * 收费Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
@Service
public class PaymentServiceImpl implements IPaymentService 
{
    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private RemoteRegisterService remoteRegisterService;

    /**
     * 查询收费
     * 
     * @param paymentId 收费主键
     * @return 收费
     */
    @Override
    public Payment selectPaymentByPaymentId(Long paymentId)
    {
        return paymentMapper.selectPaymentByPaymentId(paymentId);
    }

    /**
     * 查询收费列表
     * 
     * @param payment 收费
     * @return 收费
     */
    @Override
    public List<Payment> selectPaymentList(Payment payment)
    {
        return paymentMapper.selectPaymentList(payment);
    }

    /**
     * 新增收费
     * 
     * @param payment 收费
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertPayment(Payment payment)
    {
        payment.setCreateTime(DateUtils.getNowDate());
        int rows = paymentMapper.insertPayment(payment);
        syncPaidRegister(payment);
        return rows;
    }

    /**
     * 修改收费
     * 
     * @param payment 收费
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePayment(Payment payment)
    {
        payment.setUpdateTime(DateUtils.getNowDate());
        int rows = paymentMapper.updatePayment(payment);
        syncPaidRegister(payment);
        return rows;
    }

    /**
     * 批量删除收费
     * 
     * @param paymentIds 需要删除的收费主键
     * @return 结果
     */
    @Override
    public int deletePaymentByPaymentIds(Long[] paymentIds)
    {
        return paymentMapper.deletePaymentByPaymentIds(paymentIds);
    }

    /**
     * 删除收费信息
     * 
     * @param paymentId 收费主键
     * @return 结果
     */
    @Override
    public int deletePaymentByPaymentId(Long paymentId)
    {
        return paymentMapper.deletePaymentByPaymentId(paymentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Payment payRegister(Long registerId, String payType)
    {
        if (registerId == null)
        {
            throw new ServiceException("挂号单不能为空");
        }
        Payment paidPayment = paymentMapper.selectPaidPaymentByRegisterId(registerId);
        if (paidPayment != null)
        {
            return paidPayment;
        }

        R<Map<String, Object>> registerResult = remoteRegisterService.getPayInfo(registerId, SecurityConstants.INNER);
        if (registerResult == null || !R.isSuccess(registerResult) || registerResult.getData() == null)
        {
            throw new ServiceException(registerResult != null && registerResult.getMsg() != null ? registerResult.getMsg() : "获取挂号单失败");
        }

        Map<String, Object> register = registerResult.getData();
        String registerStatus = stringValue(register.get("registerStatus"));
        String registerPayStatus = stringValue(register.get("payStatus"));
        if (!"registered".equals(registerStatus))
        {
            throw new ServiceException("当前挂号状态不可支付");
        }
        if ("paid".equalsIgnoreCase(registerPayStatus))
        {
            throw new ServiceException("该挂号单已支付");
        }

        Payment payment = new Payment();
        payment.setPayNo(buildPayNo());
        payment.setRegisterId(registerId);
        payment.setPatientId(longValue(register.get("patientId")));
        payment.setTotalAmount(decimalValue(register.get("registerFee")));
        payment.setPayType(StringUtils.isNotEmpty(payType) ? payType : "微信");
        payment.setPayStatus("PAID");
        payment.setPayTime(DateUtils.getNowDate());
        payment.setCreateTime(DateUtils.getNowDate());
        payment.setRemark("患者端支付挂号费");
        paymentMapper.insertPayment(payment);

        R<Boolean> paidResult = remoteRegisterService.markPaid(registerId, SecurityConstants.INNER);
        if (paidResult == null || !R.isSuccess(paidResult) || Boolean.FALSE.equals(paidResult.getData()))
        {
            throw new ServiceException(paidResult != null && paidResult.getMsg() != null ? paidResult.getMsg() : "更新挂号支付状态失败");
        }
        return payment;
    }

    private String buildPayNo()
    {
        String day = "PAY" + new SimpleDateFormat("yyyyMMdd").format(new Date());
        String maxNo = paymentMapper.selectMaxPayNo(day);
        if (StringUtils.isEmpty(maxNo))
        {
            return day + "0001";
        }
        int num = Integer.parseInt(maxNo.substring(day.length())) + 1;
        return day + String.format("%04d", num);
    }

    private String stringValue(Object value)
    {
        return value == null ? null : value.toString();
    }

    private void syncPaidRegister(Payment payment)
    {
        if (payment == null || payment.getRegisterId() == null || !isPaidStatus(payment.getPayStatus()))
        {
            return;
        }
        R<Boolean> paidResult = remoteRegisterService.markPaid(payment.getRegisterId(), SecurityConstants.INNER);
        if (paidResult == null || !R.isSuccess(paidResult) || Boolean.FALSE.equals(paidResult.getData()))
        {
            throw new ServiceException(paidResult != null && paidResult.getMsg() != null ? paidResult.getMsg() : "Update register payment status failed");
        }
    }

    private boolean isPaidStatus(String payStatus)
    {
        return StringUtils.isNotEmpty(payStatus) && "PAID".equalsIgnoreCase(payStatus);
    }

    private Long longValue(Object value)
    {
        return value == null ? null : Long.valueOf(value.toString());
    }

    private BigDecimal decimalValue(Object value)
    {
        if (value == null)
        {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.toString());
    }
}
