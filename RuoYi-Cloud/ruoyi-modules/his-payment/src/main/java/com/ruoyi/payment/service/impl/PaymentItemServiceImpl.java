package com.ruoyi.payment.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.payment.mapper.PaymentItemMapper;
import com.ruoyi.payment.domain.PaymentItem;
import com.ruoyi.payment.service.IPaymentItemService;

/**
 * 收费明细Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
@Service
public class PaymentItemServiceImpl implements IPaymentItemService 
{
    @Autowired
    private PaymentItemMapper paymentItemMapper;

    /**
     * 查询收费明细
     * 
     * @param itemId 收费明细主键
     * @return 收费明细
     */
    @Override
    public PaymentItem selectPaymentItemByItemId(Long itemId)
    {
        return paymentItemMapper.selectPaymentItemByItemId(itemId);
    }

    /**
     * 查询收费明细列表
     * 
     * @param paymentItem 收费明细
     * @return 收费明细
     */
    @Override
    public List<PaymentItem> selectPaymentItemList(PaymentItem paymentItem)
    {
        return paymentItemMapper.selectPaymentItemList(paymentItem);
    }

    /**
     * 新增收费明细
     * 
     * @param paymentItem 收费明细
     * @return 结果
     */
    @Override
    public int insertPaymentItem(PaymentItem paymentItem)
    {
        paymentItem.setCreateTime(DateUtils.getNowDate());
        return paymentItemMapper.insertPaymentItem(paymentItem);
    }

    /**
     * 修改收费明细
     * 
     * @param paymentItem 收费明细
     * @return 结果
     */
    @Override
    public int updatePaymentItem(PaymentItem paymentItem)
    {
        return paymentItemMapper.updatePaymentItem(paymentItem);
    }

    /**
     * 批量删除收费明细
     * 
     * @param itemIds 需要删除的收费明细主键
     * @return 结果
     */
    @Override
    public int deletePaymentItemByItemIds(Long[] itemIds)
    {
        return paymentItemMapper.deletePaymentItemByItemIds(itemIds);
    }

    /**
     * 删除收费明细信息
     * 
     * @param itemId 收费明细主键
     * @return 结果
     */
    @Override
    public int deletePaymentItemByItemId(Long itemId)
    {
        return paymentItemMapper.deletePaymentItemByItemId(itemId);
    }
}
