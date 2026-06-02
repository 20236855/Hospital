package com.ruoyi.emr.service;

import java.util.List;
import com.ruoyi.emr.domain.MedicalRecordDisease;

/**
 * 病历疾病关联Service接口
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
public interface IMedicalRecordDiseaseService 
{
    /**
     * 查询病历疾病关联
     * 
     * @param id 病历疾病关联主键
     * @return 病历疾病关联
     */
    public MedicalRecordDisease selectMedicalRecordDiseaseById(Long id);

    /**
     * 查询病历疾病关联列表
     * 
     * @param medicalRecordDisease 病历疾病关联
     * @return 病历疾病关联集合
     */
    public List<MedicalRecordDisease> selectMedicalRecordDiseaseList(MedicalRecordDisease medicalRecordDisease);

    /**
     * 新增病历疾病关联
     * 
     * @param medicalRecordDisease 病历疾病关联
     * @return 结果
     */
    public int insertMedicalRecordDisease(MedicalRecordDisease medicalRecordDisease);

    /**
     * 修改病历疾病关联
     * 
     * @param medicalRecordDisease 病历疾病关联
     * @return 结果
     */
    public int updateMedicalRecordDisease(MedicalRecordDisease medicalRecordDisease);

    /**
     * 批量删除病历疾病关联
     * 
     * @param ids 需要删除的病历疾病关联主键集合
     * @return 结果
     */
    public int deleteMedicalRecordDiseaseByIds(Long[] ids);

    /**
     * 删除病历疾病关联信息
     * 
     * @param id 病历疾病关联主键
     * @return 结果
     */
    public int deleteMedicalRecordDiseaseById(Long id);
}
