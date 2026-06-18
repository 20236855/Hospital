package com.ruoyi.hisprescription.mapper;

import java.util.List;
import com.ruoyi.hisprescription.domain.PrescriptionItem;

/**
 * 处方明细Mapper接口
 * 
 * @author ruoyi
 * @date 2026-06-03
 */
public interface PrescriptionItemMapper 
{
    /**
     * 查询处方明细
     * 
     * @param itemId 处方明细主键
     * @return 处方明细
     */
    public PrescriptionItem selectPrescriptionItemByItemId(Long itemId);

    /**
     * 查询处方明细列表
     * 
     * @param prescriptionItem 处方明细
     * @return 处方明细集合
     */
    public List<PrescriptionItem> selectPrescriptionItemList(PrescriptionItem prescriptionItem);

    /**
     * 新增处方明细
     * 
     * @param prescriptionItem 处方明细
     * @return 结果
     */
    public int insertPrescriptionItem(PrescriptionItem prescriptionItem);

    /**
     * 修改处方明细
     * 
     * @param prescriptionItem 处方明细
     * @return 结果
     */
    public int updatePrescriptionItem(PrescriptionItem prescriptionItem);

    /**
     * 删除处方明细
     * 
     * @param itemId 处方明细主键
     * @return 结果
     */
    public int deletePrescriptionItemByItemId(Long itemId);

    /**
     * 批量删除处方明细
     * 
     * @param itemIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deletePrescriptionItemByItemIds(Long[] itemIds);

    /**
     * 根据处方ID删除所有明细
     */
    public int deletePrescriptionItemByPrescriptionId(Long prescriptionId);
}
