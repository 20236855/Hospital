package com.ruoyi.hisexam.service;

import java.util.List;
import com.ruoyi.hisexam.domain.MedicalTechnology;

/**
 * 医技项目Service接口
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
public interface IMedicalTechnologyService 
{
    /**
     * 查询医技项目
     * 
     * @param id 医技项目主键
     * @return 医技项目
     */
    public MedicalTechnology selectMedicalTechnologyById(Long id);

    /**
     * 查询医技项目列表
     * 
     * @param medicalTechnology 医技项目
     * @return 医技项目集合
     */
    public List<MedicalTechnology> selectMedicalTechnologyList(MedicalTechnology medicalTechnology);

    /**
     * 查询检验类医技项目，兼容历史INSPEC类型并补齐标准检验项目
     *
     * @return 检验类医技项目集合
     */
    public List<MedicalTechnology> selectLabTechnologyOptions();

    /**
     * 新增医技项目
     * 
     * @param medicalTechnology 医技项目
     * @return 结果
     */
    public int insertMedicalTechnology(MedicalTechnology medicalTechnology);

    /**
     * 修改医技项目
     * 
     * @param medicalTechnology 医技项目
     * @return 结果
     */
    public int updateMedicalTechnology(MedicalTechnology medicalTechnology);

    /**
     * 批量删除医技项目
     * 
     * @param ids 需要删除的医技项目主键集合
     * @return 结果
     */
    public int deleteMedicalTechnologyByIds(Long[] ids);

    /**
     * 删除医技项目信息
     * 
     * @param id 医技项目主键
     * @return 结果
     */
    public int deleteMedicalTechnologyById(Long id);
}
