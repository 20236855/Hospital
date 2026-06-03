package com.ruoyi.hisprescription.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hisprescription.mapper.PrescriptionItemMapper;
import com.ruoyi.hisprescription.domain.PrescriptionItem;
import com.ruoyi.hisprescription.service.IPrescriptionItemService;

/**
 * 处方明细Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-06-03
 */
@Service
public class PrescriptionItemServiceImpl implements IPrescriptionItemService 
{
    @Autowired
    private PrescriptionItemMapper prescriptionItemMapper;

    /**
     * 查询处方明细
     * 
     * @param itemId 处方明细主键
     * @return 处方明细
     */
    @Override
    public PrescriptionItem selectPrescriptionItemByItemId(Long itemId)
    {
        return prescriptionItemMapper.selectPrescriptionItemByItemId(itemId);
    }

    /**
     * 查询处方明细列表
     * 
     * @param prescriptionItem 处方明细
     * @return 处方明细
     */
    @Override
    public List<PrescriptionItem> selectPrescriptionItemList(PrescriptionItem prescriptionItem)
    {
        return prescriptionItemMapper.selectPrescriptionItemList(prescriptionItem);
    }

    /**
     * 新增处方明细
     * 
     * @param prescriptionItem 处方明细
     * @return 结果
     */
    @Override
    public int insertPrescriptionItem(PrescriptionItem prescriptionItem)
    {
        prescriptionItem.setCreateTime(DateUtils.getNowDate());
        return prescriptionItemMapper.insertPrescriptionItem(prescriptionItem);
    }

    /**
     * 修改处方明细
     * 
     * @param prescriptionItem 处方明细
     * @return 结果
     */
    @Override
    public int updatePrescriptionItem(PrescriptionItem prescriptionItem)
    {
        return prescriptionItemMapper.updatePrescriptionItem(prescriptionItem);
    }

    /**
     * 批量删除处方明细
     * 
     * @param itemIds 需要删除的处方明细主键
     * @return 结果
     */
    @Override
    public int deletePrescriptionItemByItemIds(Long[] itemIds)
    {
        return prescriptionItemMapper.deletePrescriptionItemByItemIds(itemIds);
    }

    /**
     * 删除处方明细信息
     * 
     * @param itemId 处方明细主键
     * @return 结果
     */
    @Override
    public int deletePrescriptionItemByItemId(Long itemId)
    {
        return prescriptionItemMapper.deletePrescriptionItemByItemId(itemId);
    }
}
