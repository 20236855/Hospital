package com.ruoyi.hisexam.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hisexam.mapper.MedicalTechnologyMapper;
import com.ruoyi.hisexam.domain.MedicalTechnology;
import com.ruoyi.hisexam.service.IMedicalTechnologyService;

/**
 * 医技项目Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
@Service
public class MedicalTechnologyServiceImpl implements IMedicalTechnologyService 
{
    @Autowired
    private MedicalTechnologyMapper medicalTechnologyMapper;

    /**
     * 查询医技项目
     * 
     * @param id 医技项目主键
     * @return 医技项目
     */
    @Override
    public MedicalTechnology selectMedicalTechnologyById(Long id)
    {
        return medicalTechnologyMapper.selectMedicalTechnologyById(id);
    }

    /**
     * 查询医技项目列表
     * 
     * @param medicalTechnology 医技项目
     * @return 医技项目
     */
    @Override
    public List<MedicalTechnology> selectMedicalTechnologyList(MedicalTechnology medicalTechnology)
    {
        return medicalTechnologyMapper.selectMedicalTechnologyList(medicalTechnology);
    }

    /**
     * 新增医技项目
     * 
     * @param medicalTechnology 医技项目
     * @return 结果
     */
    @Override
    public int insertMedicalTechnology(MedicalTechnology medicalTechnology)
    {
        medicalTechnology.setCreateTime(DateUtils.getNowDate());
        return medicalTechnologyMapper.insertMedicalTechnology(medicalTechnology);
    }

    /**
     * 修改医技项目
     * 
     * @param medicalTechnology 医技项目
     * @return 结果
     */
    @Override
    public int updateMedicalTechnology(MedicalTechnology medicalTechnology)
    {
        medicalTechnology.setUpdateTime(DateUtils.getNowDate());
        return medicalTechnologyMapper.updateMedicalTechnology(medicalTechnology);
    }

    /**
     * 批量删除医技项目
     * 
     * @param ids 需要删除的医技项目主键
     * @return 结果
     */
    @Override
    public int deleteMedicalTechnologyByIds(Long[] ids)
    {
        return medicalTechnologyMapper.deleteMedicalTechnologyByIds(ids);
    }

    /**
     * 删除医技项目信息
     * 
     * @param id 医技项目主键
     * @return 结果
     */
    @Override
    public int deleteMedicalTechnologyById(Long id)
    {
        return medicalTechnologyMapper.deleteMedicalTechnologyById(id);
    }
}
