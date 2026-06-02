package com.ruoyi.patient.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.patient.mapper.PatientMapper;
import com.ruoyi.patient.domain.Patient;
import com.ruoyi.patient.service.IPatientService;

/**
 * 患者Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-29
 */
@Service
public class PatientServiceImpl implements IPatientService 
{
    @Autowired
    private PatientMapper patientMapper;

    /**
     * 查询患者
     * 
     * @param patientId 患者主键
     * @return 患者
     */
    @Override
    public Patient selectPatientByPatientId(Long patientId)
    {
        return patientMapper.selectPatientByPatientId(patientId);
    }

    /**
     * 查询患者列表
     * 
     * @param patient 患者
     * @return 患者
     */
    @Override
    public List<Patient> selectPatientList(Patient patient)
    {
        return patientMapper.selectPatientList(patient);
    }

    /**
     * 根据身份证号查询患者
     */
    @Override
    public Patient selectPatientByIdCard(String idCard)
    {
        return patientMapper.selectPatientByIdCard(idCard);
    }

    /**
     * 新增患者
     * 
     * @param patient 患者
     * @return 结果
     */
    @Override
    public int insertPatient(Patient patient)
    {
        // 1.获取当日日期前缀
        String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
        
        // 2.查询当天最大病历号
        String maxMedicalId = patientMapper.selectMaxMedicalIdByDay(today);
        String newNo;
        
        if(maxMedicalId == null){
            //当日第一条，从001开始
            newNo = today + "001";
        }else{
            //截取后3位数字+1，不足3位补0
            String numStr = maxMedicalId.substring(8);
            int num = Integer.parseInt(numStr)+1;
            newNo = today + String.format("%03d",num);
        }
        
        //3.自动赋值给实体，前端不用传
        patient.setPatientNo(newNo);
        
        //4.正常insert入库
        patient.setCreateTime(DateUtils.getNowDate());
        return patientMapper.insertPatient(patient);
    }

    /**
     * 修改患者
     * 
     * @param patient 患者
     * @return 结果
     */
    @Override
    public int updatePatient(Patient patient)
    {
        patient.setUpdateTime(DateUtils.getNowDate());
        return patientMapper.updatePatient(patient);
    }

    /**
     * 批量删除患者
     * 
     * @param patientIds 需要删除的患者主键
     * @return 结果
     */
    @Override
    public int deletePatientByPatientIds(Long[] patientIds)
    {
        return patientMapper.deletePatientByPatientIds(patientIds);
    }

    /**
     * 删除患者信息
     * 
     * @param patientId 患者主键
     * @return 结果
     */
    @Override
    public int deletePatientByPatientId(Long patientId)
    {
        return patientMapper.deletePatientByPatientId(patientId);
    }
}
