package com.ruoyi.payment.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.payment.domain.Payment;

/**
 * 收费Mapper接口
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
public interface PaymentMapper 
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
     * 删除收费
     * 
     * @param paymentId 收费主键
     * @return 结果
     */
    public int deletePaymentByPaymentId(Long paymentId);

    /**
     * 批量删除收费
     * 
     * @param paymentIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deletePaymentByPaymentIds(Long[] paymentIds);

    /**
     * 查询挂号单已支付收费记录
     *
     * @param registerId 挂号ID
     * @return 收费记录
     */
    public Payment selectPaidPaymentByRegisterId(@Param("registerId") Long registerId);

    /**
     * 查询当天最大收费单号
     *
     * @param day 日期前缀
     * @return 最大收费单号
     */
    public String selectMaxPayNo(@Param("day") String day);
}
