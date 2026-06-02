package com.ruoyi.emr.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.emr.mapper.DiseaseMapper;
import com.ruoyi.emr.domain.Disease;
import com.ruoyi.emr.service.IDiseaseService;

/**
 * 疾病字典Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
@Service
public class DiseaseServiceImpl implements IDiseaseService 
{
    @Autowired
    private DiseaseMapper diseaseMapper;

    /**
     * 查询疾病字典
     * 
     * @param diseaseId 疾病字典主键
     * @return 疾病字典
     */
    @Override
    public Disease selectDiseaseByDiseaseId(Long diseaseId)
    {
        return diseaseMapper.selectDiseaseByDiseaseId(diseaseId);
    }

    /**
     * 查询疾病字典列表
     * 
     * @param disease 疾病字典
     * @return 疾病字典
     */
    @Override
    public List<Disease> selectDiseaseList(Disease disease)
    {
        return diseaseMapper.selectDiseaseList(disease);
    }

    /**
     * 新增疾病字典
     * 
     * @param disease 疾病字典
     * @return 结果
     */
    @Override
    public int insertDisease(Disease disease)
    {
        disease.setCreateTime(DateUtils.getNowDate());
        return diseaseMapper.insertDisease(disease);
    }

    /**
     * 修改疾病字典
     * 
     * @param disease 疾病字典
     * @return 结果
     */
    @Override
    public int updateDisease(Disease disease)
    {
        disease.setUpdateTime(DateUtils.getNowDate());
        return diseaseMapper.updateDisease(disease);
    }

    /**
     * 批量删除疾病字典
     * 
     * @param diseaseIds 需要删除的疾病字典主键
     * @return 结果
     */
    @Override
    public int deleteDiseaseByDiseaseIds(Long[] diseaseIds)
    {
        return diseaseMapper.deleteDiseaseByDiseaseIds(diseaseIds);
    }

    /**
     * 删除疾病字典信息
     * 
     * @param diseaseId 疾病字典主键
     * @return 结果
     */
    @Override
    public int deleteDiseaseByDiseaseId(Long diseaseId)
    {
        return diseaseMapper.deleteDiseaseByDiseaseId(diseaseId);
    }
}
