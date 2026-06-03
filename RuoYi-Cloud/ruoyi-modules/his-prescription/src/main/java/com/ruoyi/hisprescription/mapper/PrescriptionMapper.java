package com.ruoyi.hisprescription.mapper;

import java.util.List;
import com.ruoyi.hisprescription.domain.Prescription;

/**
 * 处方主Mapper接口
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
public interface PrescriptionMapper 
{
    /**
     * 查询处方主
     * 
     * @param prescriptionId 处方主主键
     * @return 处方主
     */
    public Prescription selectPrescriptionByPrescriptionId(Long prescriptionId);

    /**
     * 查询处方主列表
     * 
     * @param prescription 处方主
     * @return 处方主集合
     */
    public List<Prescription> selectPrescriptionList(Prescription prescription);

    /**
     * 新增处方主
     * 
     * @param prescription 处方主
     * @return 结果
     */
    public int insertPrescription(Prescription prescription);

    /**
     * 修改处方主
     * 
     * @param prescription 处方主
     * @return 结果
     */
    public int updatePrescription(Prescription prescription);

    /**
     * 删除处方主
     * 
     * @param prescriptionId 处方主主键
     * @return 结果
     */
    public int deletePrescriptionByPrescriptionId(Long prescriptionId);

    /**
     * 批量删除处方主
     * 
     * @param prescriptionIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deletePrescriptionByPrescriptionIds(Long[] prescriptionIds);
}
