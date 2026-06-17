package com.ruoyi.hisprescription.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hisprescription.mapper.DrugMapper;
import com.ruoyi.hisprescription.domain.Drug;
import com.ruoyi.hisprescription.service.IDrugService;

/**
 * 药品信息Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
@Service
public class DrugServiceImpl implements IDrugService 
{
    @Autowired
    private DrugMapper drugMapper;

    /**
     * 查询药品信息
     * 
     * @param id 药品信息主键
     * @return 药品信息
     */
    @Override
    public Drug selectDrugById(Long id)
    {
        return drugMapper.selectDrugById(id);
    }

    /**
     * 查询药品信息列表
     * 
     * @param drug 药品信息
     * @return 药品信息
     */
    @Override
    public List<Drug> selectDrugList(Drug drug)
    {
        return drugMapper.selectDrugList(drug);
    }

    /**
     * 新增药品信息
     * 
     * @param drug 药品信息
     * @return 结果
     */
    @Override
    public int insertDrug(Drug drug)
    {
        drug.setCreateTime(DateUtils.getNowDate());
        // 自动生成药品编码：YP + yyyyMMdd + 4位随机数
        if (drug.getDrugCode() == null || drug.getDrugCode().isEmpty()) {
            drug.setDrugCode(generateDrugCode());
        }
        return drugMapper.insertDrug(drug);
    }

    private String generateDrugCode() {
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        int randomNum = new Random().nextInt(9000) + 1000;
        return "YP" + dateStr + randomNum;
    }

    /**
     * 修改药品信息
     * 
     * @param drug 药品信息
     * @return 结果
     */
    @Override
    public int updateDrug(Drug drug)
    {
        drug.setUpdateTime(DateUtils.getNowDate());
        return drugMapper.updateDrug(drug);
    }

    /**
     * 批量删除药品信息
     * 
     * @param ids 需要删除的药品信息主键
     * @return 结果
     */
    @Override
    public int deleteDrugByIds(Long[] ids)
    {
        return drugMapper.deleteDrugByIds(ids);
    }

    /**
     * 删除药品信息信息
     * 
     * @param id 药品信息主键
     * @return 结果
     */
    @Override
    public int deleteDrugById(Long id)
    {
        return drugMapper.deleteDrugById(id);
    }
}
