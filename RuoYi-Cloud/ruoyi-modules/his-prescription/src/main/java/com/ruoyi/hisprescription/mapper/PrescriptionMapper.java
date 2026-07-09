package com.ruoyi.hisprescription.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
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
     * 查询患者端处方缴费项。
     *
     * @param patientId 患者ID
     * @return 处方及金额信息
     */
    public List<Map<String, Object>> selectPatientPrescriptionPayments(@Param("patientId") Long patientId);

    /**
     * 查询某处方未缴费的药品明细。
     *
     * @param prescriptionId 处方ID
     * @return 处方明细
     */
    public List<Map<String, Object>> selectPrescriptionItemsById(@Param("prescriptionId") Long prescriptionId);

    /**
     * 标记处方为已缴费（同时更新为已发药状态）。
     *
     * @param prescriptionId 处方ID
     * @return 影响行数
     */
    public int markPrescriptionPaid(@Param("prescriptionId") Long prescriptionId);

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
