package com.ruoyi.emr.service;

import java.util.List;
import com.ruoyi.emr.domain.Disease;

/**
 * 疾病字典Service接口
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
public interface IDiseaseService 
{
    /**
     * 查询疾病字典
     * 
     * @param diseaseId 疾病字典主键
     * @return 疾病字典
     */
    public Disease selectDiseaseByDiseaseId(Long diseaseId);

    /**
     * 查询疾病字典列表
     * 
     * @param disease 疾病字典
     * @return 疾病字典集合
     */
    public List<Disease> selectDiseaseList(Disease disease);

    /**
     * 新增疾病字典
     * 
     * @param disease 疾病字典
     * @return 结果
     */
    public int insertDisease(Disease disease);

    /**
     * 修改疾病字典
     * 
     * @param disease 疾病字典
     * @return 结果
     */
    public int updateDisease(Disease disease);

    /**
     * 批量删除疾病字典
     * 
     * @param diseaseIds 需要删除的疾病字典主键集合
     * @return 结果
     */
    public int deleteDiseaseByDiseaseIds(Long[] diseaseIds);

    /**
     * 删除疾病字典信息
     * 
     * @param diseaseId 疾病字典主键
     * @return 结果
     */
    public int deleteDiseaseByDiseaseId(Long diseaseId);
}
