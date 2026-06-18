package com.ruoyi.emr.service;

import java.util.List;
import com.ruoyi.emr.domain.MedicalRecord;

/**
 * 电子病历Service接口
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
public interface IMedicalRecordService 
{
    /**
     * 查询电子病历
     * 
     * @param recordId 电子病历主键
     * @return 电子病历
     */
    public MedicalRecord selectMedicalRecordByRecordId(Long recordId);

    /**
     * 查询电子病历列表
     * 
     * @param medicalRecord 电子病历
     * @return 电子病历集合
     */
    public List<MedicalRecord> selectMedicalRecordList(MedicalRecord medicalRecord);

    /**
     * 新增电子病历
     * 
     * @param medicalRecord 电子病历
     * @return 结果
     */
    public int insertMedicalRecord(MedicalRecord medicalRecord);

    /**
     * 修改电子病历
     * 
     * @param medicalRecord 电子病历
     * @return 结果
     */
    public int updateMedicalRecord(MedicalRecord medicalRecord);

    /**
     * 批量删除电子病历
     * 
     * @param recordIds 需要删除的电子病历主键集合
     * @return 结果
     */
    public int deleteMedicalRecordByRecordIds(Long[] recordIds);

    /**
     * 删除电子病历信息
     * 
     * @param recordId 电子病历主键
     * @return 结果
     */
    public int deleteMedicalRecordByRecordId(Long recordId);

    /**
     * 通过接诊ID查询关联的医生ID
     * 
     * @param encounterId 接诊ID
     * @return 医生ID
     */
    public Long selectDoctorIdByEncounterId(Long encounterId);

    /**
     * 保存或更新电子病历（按encounterId判断：存在则更新，不存在则新增）
     * 
     * @param medicalRecord 电子病历
     * @return 结果
     */
    public int saveMedicalRecord(MedicalRecord medicalRecord);
}
