package com.ruoyi.hisprescription.service;

import java.util.List;
import java.util.Map;
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
    /**
     * 查询患者端处方缴费项。
     *
     * @param patientId 患者ID
     * @return 处方缴费项集合
     */
    public List<Map<String, Object>> selectPatientPrescriptionPayments(Long patientId);

    /**
     * 查询某处方未缴费的支付信息。
     *
     * @param prescriptionId 处方ID
     * @return 支付信息
     */
    public Map<String, Object> selectPrescriptionPayInfo(Long prescriptionId);

    /**
     * 标记处方为已缴费。
     *
     * @param prescriptionId 处方ID
     * @return 是否成功
     */
    public boolean markPrescriptionPaid(Long prescriptionId);

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
