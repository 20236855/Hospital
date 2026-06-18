package com.ruoyi.emr.mapper;

import java.util.List;
import com.ruoyi.emr.domain.MedicalRecord;

/**
 * 电子病历Mapper接口
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
public interface MedicalRecordMapper 
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
     * 删除电子病历
     * 
     * @param recordId 电子病历主键
     * @return 结果
     */
    public int deleteMedicalRecordByRecordId(Long recordId);

    /**
     * 批量删除电子病历
     * 
     * @param recordIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMedicalRecordByRecordIds(Long[] recordIds);

    /**
     * 通过接诊ID查询关联的医生ID
     * 
     * @param encounterId 接诊ID
     * @return 医生ID
     */
    public Long selectDoctorIdByEncounterId(Long encounterId);

    /**
     * 根据接诊ID查询电子病历（用于判断是否已存在）
     * 
     * @param encounterId 接诊ID
     * @return 电子病历
     */
    public MedicalRecord selectMedicalRecordByEncounterId(Long encounterId);

    /**
     * 根据接诊ID更新电子病历
     * 
     * @param medicalRecord 电子病历
     * @return 结果
     */
    public int updateMedicalRecordByEncounterId(MedicalRecord medicalRecord);
}
