package com.ruoyi.hisprescription.service;

import java.util.List;
import com.ruoyi.hisprescription.domain.Drug;

/**
 * 药品信息Service接口
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
public interface IDrugService 
{
    /**
     * 查询药品信息
     * 
     * @param id 药品信息主键
     * @return 药品信息
     */
    public Drug selectDrugById(Long id);

    /**
     * 查询药品信息列表
     * 
     * @param drug 药品信息
     * @return 药品信息集合
     */
    public List<Drug> selectDrugList(Drug drug);

    /**
     * 新增药品信息
     * 
     * @param drug 药品信息
     * @return 结果
     */
    public int insertDrug(Drug drug);

    /**
     * 修改药品信息
     * 
     * @param drug 药品信息
     * @return 结果
     */
    public int updateDrug(Drug drug);

    /**
     * 批量删除药品信息
     * 
     * @param ids 需要删除的药品信息主键集合
     * @return 结果
     */
    public int deleteDrugByIds(Long[] ids);

    /**
     * 删除药品信息信息
     * 
     * @param id 药品信息主键
     * @return 结果
     */
    public int deleteDrugById(Long id);
}
