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
        MedicalRecord record = medicalRecordMapper.selectMedicalRecordByRecordId(recordId);
        // 调试日志
        if (record != null) {
            System.out.println("查询病历详情 - recordId: " + recordId + 
                ", encounterId: " + record.getEncounterId() + 
                ", doctorName: " + record.getDoctorName() + 
                ", deptName: " + record.getDeptName());
        }
        return record;
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
        List<MedicalRecord> list = medicalRecordMapper.selectMedicalRecordList(medicalRecord);
        // 调试日志
        if (list != null && !list.isEmpty()) {
            System.out.println("查询病历列表 - 数量: " + list.size());
            for (int i = 0; i < Math.min(list.size(), 3); i++) {
                MedicalRecord r = list.get(i);
                System.out.println("  recordId: " + r.getRecordId() + 
                    ", encounterId: " + r.getEncounterId() + 
                    ", doctorName: " + r.getDoctorName() + 
                    ", deptName: " + r.getDeptName());
            }
        }
        return list;
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

    @Override
    public Long selectDoctorIdByEncounterId(Long encounterId)
    {
        return medicalRecordMapper.selectDoctorIdByEncounterId(encounterId);
    }

    @Override
    public int saveMedicalRecord(MedicalRecord medicalRecord)
    {
        if (medicalRecord.getEncounterId() == null)
        {
            return 0;
        }
        // 检查该接诊是否已有病历
        MedicalRecord existing = medicalRecordMapper.selectMedicalRecordByEncounterId(medicalRecord.getEncounterId());
        if (existing != null)
        {
            // 同一次接诊只更新本接诊病历，不使用前端可能携带的旧recordId
            medicalRecord.setUpdateTime(DateUtils.getNowDate());
            return medicalRecordMapper.updateMedicalRecordByEncounterId(medicalRecord);
        }
        else
        {
            // 不同接诊必须新增独立病历
            medicalRecord.setRecordId(null);
            medicalRecord.setCreateTime(DateUtils.getNowDate());
            return medicalRecordMapper.insertMedicalRecord(medicalRecord);
        }
    }
}
