package com.ruoyi.patient.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.patient.domain.Patient;

/**
 * 患者Mapper接口
 * 
 * @author ruoyi
 * @date 2026-05-29
 */
public interface PatientMapper 
{
    /**
     * 查询患者
     * 
     * @param patientId 患者主键
     * @return 患者
     */
    public Patient selectPatientByPatientId(Long patientId);

    /**
     * 查询患者列表
     * 
     * @param patient 患者
     * @return 患者集合
     */
    public List<Patient> selectPatientList(Patient patient);

    /**
     * 根据身份证号查询患者
     *
     * @param idCard 身份证号
     * @return 患者
     */
    public Patient selectPatientByIdCard(@Param("idCard") String idCard);

    /**
     * 新增患者
     * 
     * @param patient 患者
     * @return 结果
     */
    public int insertPatient(Patient patient);

    /**
     * 修改患者
     * 
     * @param patient 患者
     * @return 结果
     */
    public int updatePatient(Patient patient);

    /**
     * 删除患者
     * 
     * @param patientId 患者主键
     * @return 结果
     */
    public int deletePatientByPatientId(Long patientId);

    /**
     * 批量删除患者
     * 
     * @param patientIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deletePatientByPatientIds(Long[] patientIds);

    /**
     * 查询当天最大病历号
     * @param day 日期前缀（格式：yyyyMMdd）
     * @return 最大病历号
     */
    String selectMaxMedicalIdByDay(@Param("day") String day);

    /**
     * 根据用户ID查询患者
     *
     * @param userId 用户ID
     * @return 患者
     */
    public Patient selectPatientByUserId(Long userId);
}
