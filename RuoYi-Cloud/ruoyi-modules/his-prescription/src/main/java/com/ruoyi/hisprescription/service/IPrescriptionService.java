package com.ruoyi.hisprescription.service;

import java.util.List;
import com.ruoyi.hisprescription.domain.Prescription;

/**
 * 处方主Service接口
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
public interface IPrescriptionService 
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
     * 批量删除处方主
     * 
     * @param prescriptionIds 需要删除的处方主主键集合
     * @return 结果
     */
    public int deletePrescriptionByPrescriptionIds(Long[] prescriptionIds);

    /**
     * 删除处方主信息
     * 
     * @param prescriptionId 处方主主键
     * @return 结果
     */
    public int deletePrescriptionByPrescriptionId(Long prescriptionId);
}
