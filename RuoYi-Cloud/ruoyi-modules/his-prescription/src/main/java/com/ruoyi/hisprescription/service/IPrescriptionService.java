package com.ruoyi.hisprescription.service;

import java.util.List;
import com.ruoyi.hisprescription.domain.Prescription;
import com.ruoyi.hisprescription.domain.PrescriptionItem;

/**
 * 处方主Service接口
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
public interface IPrescriptionService 
{
    public Prescription selectPrescriptionByPrescriptionId(Long prescriptionId);

    public List<Prescription> selectPrescriptionList(Prescription prescription);

    public int insertPrescription(Prescription prescription);

    public int updatePrescription(Prescription prescription);

    public int deletePrescriptionByPrescriptionIds(Long[] prescriptionIds);

    public int deletePrescriptionByPrescriptionId(Long prescriptionId);

    /**
     * 保存处方主+明细（事务统一处理）
     */
    public Prescription savePrescriptionWithItems(Prescription prescription, List<PrescriptionItem> items);

    /**
     * 更新处方主+明细（先删旧明细再插新明细，事务统一处理）
     */
    public Prescription updatePrescriptionWithItems(Prescription prescription, List<PrescriptionItem> items);
}
