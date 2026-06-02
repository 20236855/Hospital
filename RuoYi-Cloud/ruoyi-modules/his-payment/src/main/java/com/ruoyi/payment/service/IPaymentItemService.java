package com.ruoyi.payment.service;

import java.util.List;
import com.ruoyi.payment.domain.PaymentItem;

/**
 * 收费明细Service接口
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
public interface IPaymentItemService 
{
    /**
     * 查询收费明细
     * 
     * @param itemId 收费明细主键
     * @return 收费明细
     */
    public PaymentItem selectPaymentItemByItemId(Long itemId);

    /**
     * 查询收费明细列表
     * 
     * @param paymentItem 收费明细
     * @return 收费明细集合
     */
    public List<PaymentItem> selectPaymentItemList(PaymentItem paymentItem);

    /**
     * 新增收费明细
     * 
     * @param paymentItem 收费明细
     * @return 结果
     */
    public int insertPaymentItem(PaymentItem paymentItem);

    /**
     * 修改收费明细
     * 
     * @param paymentItem 收费明细
     * @return 结果
     */
    public int updatePaymentItem(PaymentItem paymentItem);

    /**
     * 批量删除收费明细
     * 
     * @param itemIds 需要删除的收费明细主键集合
     * @return 结果
     */
    public int deletePaymentItemByItemIds(Long[] itemIds);

    /**
     * 删除收费明细信息
     * 
     * @param itemId 收费明细主键
     * @return 结果
     */
    public int deletePaymentItemByItemId(Long itemId);
}
