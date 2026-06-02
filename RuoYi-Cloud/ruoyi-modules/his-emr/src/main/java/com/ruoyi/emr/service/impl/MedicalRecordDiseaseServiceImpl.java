package com.ruoyi.emr.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.emr.mapper.MedicalRecordDiseaseMapper;
import com.ruoyi.emr.domain.MedicalRecordDisease;
import com.ruoyi.emr.service.IMedicalRecordDiseaseService;

/**
 * 病历疾病关联Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
@Service
public class MedicalRecordDiseaseServiceImpl implements IMedicalRecordDiseaseService 
{
    @Autowired
    private MedicalRecordDiseaseMapper medicalRecordDiseaseMapper;

    /**
     * 查询病历疾病关联
     * 
     * @param id 病历疾病关联主键
     * @return 病历疾病关联
     */
    @Override
    public MedicalRecordDisease selectMedicalRecordDiseaseById(Long id)
    {
        return medicalRecordDiseaseMapper.selectMedicalRecordDiseaseById(id);
    }

    /**
     * 查询病历疾病关联列表
     * 
     * @param medicalRecordDisease 病历疾病关联
     * @return 病历疾病关联
     */
    @Override
    public List<MedicalRecordDisease> selectMedicalRecordDiseaseList(MedicalRecordDisease medicalRecordDisease)
    {
        return medicalRecordDiseaseMapper.selectMedicalRecordDiseaseList(medicalRecordDisease);
    }

    /**
     * 新增病历疾病关联
     * 
     * @param medicalRecordDisease 病历疾病关联
     * @return 结果
     */
    @Override
    public int insertMedicalRecordDisease(MedicalRecordDisease medicalRecordDisease)
    {
        medicalRecordDisease.setCreateTime(DateUtils.getNowDate());
        return medicalRecordDiseaseMapper.insertMedicalRecordDisease(medicalRecordDisease);
    }

    /**
     * 修改病历疾病关联
     * 
     * @param medicalRecordDisease 病历疾病关联
     * @return 结果
     */
    @Override
    public int updateMedicalRecordDisease(MedicalRecordDisease medicalRecordDisease)
    {
        return medicalRecordDiseaseMapper.updateMedicalRecordDisease(medicalRecordDisease);
    }

    /**
     * 批量删除病历疾病关联
     * 
     * @param ids 需要删除的病历疾病关联主键
     * @return 结果
     */
    @Override
    public int deleteMedicalRecordDiseaseByIds(Long[] ids)
    {
        return medicalRecordDiseaseMapper.deleteMedicalRecordDiseaseByIds(ids);
    }

    /**
     * 删除病历疾病关联信息
     * 
     * @param id 病历疾病关联主键
     * @return 结果
     */
    @Override
    public int deleteMedicalRecordDiseaseById(Long id)
    {
        return medicalRecordDiseaseMapper.deleteMedicalRecordDiseaseById(id);
    }
}
