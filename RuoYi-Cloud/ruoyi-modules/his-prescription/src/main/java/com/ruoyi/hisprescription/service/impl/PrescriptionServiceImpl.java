package com.ruoyi.hisprescription.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import com.ruoyi.common.core.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.hisprescription.mapper.PrescriptionItemMapper;
import com.ruoyi.hisprescription.mapper.PrescriptionMapper;
import com.ruoyi.hisprescription.domain.Prescription;
import com.ruoyi.hisprescription.domain.PrescriptionItem;
import com.ruoyi.hisprescription.service.IPrescriptionService;

/**
 * 处方主Service业务层处理
 */
@Service
public class PrescriptionServiceImpl implements IPrescriptionService 
{
    private static final Logger log = LoggerFactory.getLogger(PrescriptionServiceImpl.class);

    @Autowired
    private PrescriptionMapper prescriptionMapper;

    @Autowired
    private PrescriptionItemMapper prescriptionItemMapper;

    @Override
    public Prescription selectPrescriptionByPrescriptionId(Long prescriptionId)
    {
        return prescriptionMapper.selectPrescriptionByPrescriptionId(prescriptionId);
    }

    @Override
    public List<Prescription> selectPrescriptionList(Prescription prescription)
    {
        return prescriptionMapper.selectPrescriptionList(prescription);
    }

    @Override
    public int insertPrescription(Prescription prescription)
    {
        prescription.setCreateTime(DateUtils.getNowDate());
        if (prescription.getPrescriptionNo() == null || prescription.getPrescriptionNo().isEmpty()) {
            String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
            int randomNum = new Random().nextInt(9000) + 1000;
            prescription.setPrescriptionNo("CF" + dateStr + randomNum);
        }
        return prescriptionMapper.insertPrescription(prescription);
    }

    @Override
    public int updatePrescription(Prescription prescription)
    {
        prescription.setUpdateTime(DateUtils.getNowDate());
        return prescriptionMapper.updatePrescription(prescription);
    }

    @Override
    public int deletePrescriptionByPrescriptionIds(Long[] prescriptionIds)
    {
        return prescriptionMapper.deletePrescriptionByPrescriptionIds(prescriptionIds);
    }

    @Override
    public int deletePrescriptionByPrescriptionId(Long prescriptionId)
    {
        return prescriptionMapper.deletePrescriptionByPrescriptionId(prescriptionId);
    }

    /**
     * 保存处方主+明细（事务统一处理）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Prescription savePrescriptionWithItems(Prescription prescription, List<PrescriptionItem> items)
    {
        // 1. 生成单号
        if (prescription.getPrescriptionNo() == null || prescription.getPrescriptionNo().isEmpty()) {
            String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
            prescription.setPrescriptionNo("CF" + dateStr + (new Random().nextInt(9000) + 1000));
        }
        prescription.setPrescriptionStatus("待缴费");
        prescription.setCreateTime(DateUtils.getNowDate());

        // 2. 插入主表
        prescriptionMapper.insertPrescription(prescription);
        Long prescriptionId = prescription.getPrescriptionId();
        log.info("处方主表已创建, prescriptionId={}", prescriptionId);

        // 3. 批量插入明细 + 计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (items != null && !items.isEmpty()) {
            for (PrescriptionItem item : items) {
                item.setPrescriptionId(prescriptionId);
                item.setCreateTime(DateUtils.getNowDate());
                prescriptionItemMapper.insertPrescriptionItem(item);
                // 小计
                if (item.getDrugPrice() != null && item.getQuantity() != null) {
                    totalAmount = totalAmount.add(
                        item.getDrugPrice().multiply(new BigDecimal(item.getQuantity())));
                }
            }
        }

        // 4. 更新主表总金额
        prescription.setTotalAmount(totalAmount);
        prescriptionMapper.updatePrescription(prescription);
        log.info("处方保存完成, prescriptionId={}, items={}, totalAmount={}",
                prescriptionId, items != null ? items.size() : 0, totalAmount);

        return prescription;
    }

    /**
     * 更新处方主+明细（先删旧明细再插新明细）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Prescription updatePrescriptionWithItems(Prescription prescription, List<PrescriptionItem> items)
    {
        Long prescriptionId = prescription.getPrescriptionId();
        // 1. 更新主表
        prescription.setUpdateTime(DateUtils.getNowDate());
        prescriptionMapper.updatePrescription(prescription);

        // 2. 删除旧明细
        prescriptionItemMapper.deletePrescriptionItemByPrescriptionId(prescriptionId);

        // 3. 插入新明细 + 计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (items != null && !items.isEmpty()) {
            for (PrescriptionItem item : items) {
                item.setPrescriptionId(prescriptionId);
                item.setCreateTime(DateUtils.getNowDate());
                prescriptionItemMapper.insertPrescriptionItem(item);
                if (item.getDrugPrice() != null && item.getQuantity() != null) {
                    totalAmount = totalAmount.add(
                        item.getDrugPrice().multiply(new BigDecimal(item.getQuantity())));
                }
            }
        }

        // 4. 更新主表总金额
        prescription.setTotalAmount(totalAmount);
        prescriptionMapper.updatePrescription(prescription);
        log.info("处方更新完成, prescriptionId={}, items={}, totalAmount={}",
                prescriptionId, items != null ? items.size() : 0, totalAmount);

        return prescription;
    }
}
