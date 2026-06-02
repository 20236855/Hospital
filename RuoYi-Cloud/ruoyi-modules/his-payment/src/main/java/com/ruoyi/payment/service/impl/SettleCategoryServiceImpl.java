package com.ruoyi.payment.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.payment.mapper.SettleCategoryMapper;
import com.ruoyi.payment.domain.SettleCategory;
import com.ruoyi.payment.service.ISettleCategoryService;

/**
 * 结算类别Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
@Service
public class SettleCategoryServiceImpl implements ISettleCategoryService 
{
    @Autowired
    private SettleCategoryMapper settleCategoryMapper;

    /**
     * 查询结算类别
     * 
     * @param categoryId 结算类别主键
     * @return 结算类别
     */
    @Override
    public SettleCategory selectSettleCategoryByCategoryId(Long categoryId)
    {
        return settleCategoryMapper.selectSettleCategoryByCategoryId(categoryId);
    }

    /**
     * 查询结算类别列表
     * 
     * @param settleCategory 结算类别
     * @return 结算类别
     */
    @Override
    public List<SettleCategory> selectSettleCategoryList(SettleCategory settleCategory)
    {
        return settleCategoryMapper.selectSettleCategoryList(settleCategory);
    }

    /**
     * 新增结算类别
     * 
     * @param settleCategory 结算类别
     * @return 结果
     */
    @Override
    public int insertSettleCategory(SettleCategory settleCategory)
    {
        settleCategory.setCreateTime(DateUtils.getNowDate());
        return settleCategoryMapper.insertSettleCategory(settleCategory);
    }

    /**
     * 修改结算类别
     * 
     * @param settleCategory 结算类别
     * @return 结果
     */
    @Override
    public int updateSettleCategory(SettleCategory settleCategory)
    {
        settleCategory.setUpdateTime(DateUtils.getNowDate());
        return settleCategoryMapper.updateSettleCategory(settleCategory);
    }

    /**
     * 批量删除结算类别
     * 
     * @param categoryIds 需要删除的结算类别主键
     * @return 结果
     */
    @Override
    public int deleteSettleCategoryByCategoryIds(Long[] categoryIds)
    {
        return settleCategoryMapper.deleteSettleCategoryByCategoryIds(categoryIds);
    }

    /**
     * 删除结算类别信息
     * 
     * @param categoryId 结算类别主键
     * @return 结果
     */
    @Override
    public int deleteSettleCategoryByCategoryId(Long categoryId)
    {
        return settleCategoryMapper.deleteSettleCategoryByCategoryId(categoryId);
    }
}
