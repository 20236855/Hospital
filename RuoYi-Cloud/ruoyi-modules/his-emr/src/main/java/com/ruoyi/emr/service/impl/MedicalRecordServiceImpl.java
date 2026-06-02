package com.ruoyi.emr.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.emr.mapper.MedicalRecordMapper;
import com.ruoyi.emr.domain.MedicalRecord;
import com.ruoyi.emr.service.IMedicalRecordService;

/**
 * 电子病历Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
@Service
public class MedicalRecordServiceImpl implements IMedicalRecordService 
{
    @Autowired
    private MedicalRecordMapper medicalRecordMapper;

    /**
     * 查询电子病历
     * 
     * @param recordId 电子病历主键
     * @return 电子病历
     */
    @Override
    public MedicalRecord selectMedicalRecordByRecordId(Long recordId)
    {
        return medicalRecordMapper.selectMedicalRecordByRecordId(recordId);
    }

    /**
     * 查询电子病历列表
     * 
     * @param medicalRecord 电子病历
     * @return 电子病历
     */
    @Override
    public List<MedicalRecord> selectMedicalRecordList(MedicalRecord medicalRecord)
    {
        return medicalRecordMapper.selectMedicalRecordList(medicalRecord);
    }

    /**
     * 新增电子病历
     * 
     * @param medicalRecord 电子病历
     * @return 结果
     */
    @Override
    public int insertMedicalRecord(MedicalRecord medicalRecord)
    {
        medicalRecord.setCreateTime(DateUtils.getNowDate());
        return medicalRecordMapper.insertMedicalRecord(medicalRecord);
    }

    /**
     * 修改电子病历
     * 
     * @param medicalRecord 电子病历
     * @return 结果
     */
    @Override
    public int updateMedicalRecord(MedicalRecord medicalRecord)
    {
        medicalRecord.setUpdateTime(DateUtils.getNowDate());
        return medicalRecordMapper.updateMedicalRecord(medicalRecord);
    }

    /**
     * 批量删除电子病历
     * 
     * @param recordIds 需要删除的电子病历主键
     * @return 结果
     */
    @Override
    public int deleteMedicalRecordByRecordIds(Long[] recordIds)
    {
        return medicalRecordMapper.deleteMedicalRecordByRecordIds(recordIds);
    }

    /**
     * 删除电子病历信息
     * 
     * @param recordId 电子病历主键
     * @return 结果
     */
    @Override
    public int deleteMedicalRecordByRecordId(Long recordId)
    {
        return medicalRecordMapper.deleteMedicalRecordByRecordId(recordId);
    }
}
