package com.ruoyi.payment.service;

import java.util.List;
import com.ruoyi.payment.domain.Payment;

/**
 * 收费Service接口
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
public interface IPaymentService 
{
    /**
     * 查询收费
     * 
     * @param paymentId 收费主键
     * @return 收费
     */
    public Payment selectPaymentByPaymentId(Long paymentId);

    /**
     * 查询收费列表
     * 
     * @param payment 收费
     * @return 收费集合
     */
    public List<Payment> selectPaymentList(Payment payment);

    /**
     * 新增收费
     * 
     * @param payment 收费
     * @return 结果
     */
    public int insertPayment(Payment payment);

    /**
     * 修改收费
     * 
     * @param payment 收费
     * @return 结果
     */
    public int updatePayment(Payment payment);

    /**
     * 批量删除收费
     * 
     * @param paymentIds 需要删除的收费主键集合
     * @return 结果
     */
    public int deletePaymentByPaymentIds(Long[] paymentIds);

    /**
     * 删除收费信息
     * 
     * @param paymentId 收费主键
     * @return 结果
     */
    public int deletePaymentByPaymentId(Long paymentId);

    /**
     * 支付挂号费
     *
     * @param registerId 挂号ID
     * @param payType 支付方式
     * @return 收费单
     */
    public Payment payRegister(Long registerId, String payType);

    /**
     * 支付检查检验费用
     *
     * @param registerId 挂号ID
     * @param payType 支付方式
     * @return 收费单
     */
    public Payment payExamByRegister(Long registerId, String payType);
}
