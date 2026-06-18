package com.ruoyi.hisprescription.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hisprescription.mapper.PrescriptionMapper;
import com.ruoyi.hisprescription.domain.Prescription;
import com.ruoyi.hisprescription.service.IPrescriptionService;

/**
 * 处方主Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
@Service
public class PrescriptionServiceImpl implements IPrescriptionService 
{
    @Autowired
    private PrescriptionMapper prescriptionMapper;

    /**
     * 查询处方主
     * 
     * @param prescriptionId 处方主主键
     * @return 处方主
     */
    @Override
    public Prescription selectPrescriptionByPrescriptionId(Long prescriptionId)
    {
        return prescriptionMapper.selectPrescriptionByPrescriptionId(prescriptionId);
    }

    /**
     * 查询处方主列表
     * 
     * @param prescription 处方主
     * @return 处方主
     */
    @Override
    public List<Prescription> selectPrescriptionList(Prescription prescription)
    {
        return prescriptionMapper.selectPrescriptionList(prescription);
    }

    /**
     * 新增处方主
     * 
     * @param prescription 处方主
     * @return 结果
     */
    @Override
    public int insertPrescription(Prescription prescription)
    {
        prescription.setCreateTime(DateUtils.getNowDate());
        // 自动生成处方单号：CF + yyyyMMdd + 4位随机数
        if (prescription.getPrescriptionNo() == null || prescription.getPrescriptionNo().isEmpty()) {
            String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
            int randomNum = new Random().nextInt(9000) + 1000;
            prescription.setPrescriptionNo("CF" + dateStr + randomNum);
        }
        return prescriptionMapper.insertPrescription(prescription);
    }

    /**
     * 修改处方主
     * 
     * @param prescription 处方主
     * @return 结果
     */
    @Override
    public int updatePrescription(Prescription prescription)
    {
        prescription.setUpdateTime(DateUtils.getNowDate());
        return prescriptionMapper.updatePrescription(prescription);
    }

    /**
     * 批量删除处方主
     * 
     * @param prescriptionIds 需要删除的处方主主键
     * @return 结果
     */
    @Override
    public int deletePrescriptionByPrescriptionIds(Long[] prescriptionIds)
    {
        return prescriptionMapper.deletePrescriptionByPrescriptionIds(prescriptionIds);
    }

    /**
     * 删除处方主信息
     * 
     * @param prescriptionId 处方主主键
     * @return 结果
     */
    @Override
    public int deletePrescriptionByPrescriptionId(Long prescriptionId)
    {
        return prescriptionMapper.deletePrescriptionByPrescriptionId(prescriptionId);
    }
}
