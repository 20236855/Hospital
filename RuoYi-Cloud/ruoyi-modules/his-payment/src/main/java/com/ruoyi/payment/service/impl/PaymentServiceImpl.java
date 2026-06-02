package com.ruoyi.payment.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
    public int insertPayment(Payment payment)
    {
        payment.setCreateTime(DateUtils.getNowDate());
        return paymentMapper.insertPayment(payment);
    }

    /**
     * 修改收费
     * 
     * @param payment 收费
     * @return 结果
     */
    @Override
    public int updatePayment(Payment payment)
    {
        payment.setUpdateTime(DateUtils.getNowDate());
        return paymentMapper.updatePayment(payment);
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
}
