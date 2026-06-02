package com.ruoyi.payment.service;

import java.util.List;
import com.ruoyi.payment.domain.SettleCategory;

/**
 * 结算类别Service接口
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
public interface ISettleCategoryService 
{
    /**
     * 查询结算类别
     * 
     * @param categoryId 结算类别主键
     * @return 结算类别
     */
    public SettleCategory selectSettleCategoryByCategoryId(Long categoryId);

    /**
     * 查询结算类别列表
     * 
     * @param settleCategory 结算类别
     * @return 结算类别集合
     */
    public List<SettleCategory> selectSettleCategoryList(SettleCategory settleCategory);

    /**
     * 新增结算类别
     * 
     * @param settleCategory 结算类别
     * @return 结果
     */
    public int insertSettleCategory(SettleCategory settleCategory);

    /**
     * 修改结算类别
     * 
     * @param settleCategory 结算类别
     * @return 结果
     */
    public int updateSettleCategory(SettleCategory settleCategory);

    /**
     * 批量删除结算类别
     * 
     * @param categoryIds 需要删除的结算类别主键集合
     * @return 结果
     */
    public int deleteSettleCategoryByCategoryIds(Long[] categoryIds);

    /**
     * 删除结算类别信息
     * 
     * @param categoryId 结算类别主键
     * @return 结果
     */
    public int deleteSettleCategoryByCategoryId(Long categoryId);
}
