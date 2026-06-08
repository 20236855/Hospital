package com.ruoyi.patient.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.patient.mapper.PatientMapper;
import com.ruoyi.patient.domain.Patient;
import com.ruoyi.patient.service.IPatientService;
import com.ruoyi.system.api.RemoteUserService;
import com.ruoyi.system.api.domain.SysUser;

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
    
    @Autowired
    private RemoteUserService remoteUserService;

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
    @Transactional(rollbackFor = Exception.class)
    public int insertPatient(Patient patient)
    {
        // 1.自动生成病历号：P+年月日+6位随机数
        if (patient.getPatientNo() == null || patient.getPatientNo().isEmpty()) {
            patient.setPatientNo(generatePatientNo());
        }
        
        // 2.线下建档：手机号有效时自动创建或绑定系统用户
        bindSystemUserIfNecessary(patient);
        
        // 3.正常insert入库
        patient.setCreateTime(DateUtils.getNowDate());
        return patientMapper.insertPatient(patient);
    }

    /**
     * 自助完善患者信息：如果已存在则更新，否则新增
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int completePatient(Patient patient)
    {
        if (patient.getUserId() == null)
        {
            throw new ServiceException("当前登录用户不能为空");
        }
        
        // 计算年龄
        if (patient.getBirthday() != null && patient.getAge() == null)
        {
            patient.setAge(calculateAge(patient.getBirthday()));
        }
        
        Patient existPatient = patientMapper.selectPatientByUserId(patient.getUserId());
        if (existPatient != null)
        {
            // 更新现有档案
            patient.setPatientId(existPatient.getPatientId());
            patient.setPatientNo(existPatient.getPatientNo());
            patient.setUserId(existPatient.getUserId());
            patient.setUpdateTime(DateUtils.getNowDate());
            return patientMapper.updatePatient(patient);
        }
        else
        {
            // 新增档案
            if (StringUtils.isEmpty(patient.getPatientNo()))
            {
                patient.setPatientNo(generatePatientNo());
            }
            patient.setCreateTime(DateUtils.getNowDate());
            return patientMapper.insertPatient(patient);
        }
    }

    /**
     * 根据出生日期计算年龄
     */
    private Long calculateAge(Date birthday)
    {
        if (birthday == null) return null;
        
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int yearNow = cal.get(java.util.Calendar.YEAR);
        int monthNow = cal.get(java.util.Calendar.MONTH);
        int dayNow = cal.get(java.util.Calendar.DAY_OF_MONTH);
        
        cal.setTime(birthday);
        int yearBirth = cal.get(java.util.Calendar.YEAR);
        int monthBirth = cal.get(java.util.Calendar.MONTH);
        int dayBirth = cal.get(java.util.Calendar.DAY_OF_MONTH);
        
        long age = yearNow - yearBirth;
        if (monthNow < monthBirth || (monthNow == monthBirth && dayNow < dayBirth))
        {
            age--;
        }
        return age;
    }

    /**
     * 修改患者
     * 
     * @param patient 患者
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePatient(Patient patient)
    {
        // 如果手机号有变化或之前没有userId
        Patient oldPatient = patientMapper.selectPatientByPatientId(patient.getPatientId());
        if (StringUtils.isNotEmpty(patient.getPhone()) && (oldPatient == null || oldPatient.getUserId() == null))
        {
            bindSystemUserIfNecessary(patient);
        }
        
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

    /**
     * 根据用户ID查询患者
     */
    @Override
    public Patient getPatientByUserId(Long userId)
    {
        return patientMapper.selectPatientByUserId(userId);
    }

    private void bindSystemUserIfNecessary(Patient patient)
    {
        if (patient.getUserId() != null || StringUtils.isEmpty(patient.getPhone()))
        {
            return;
        }
        SysUser existUser = getUserByPhonenumber(patient.getPhone());
        if (existUser == null)
        {
            SysUser newUser = new SysUser();
            newUser.setUserName(patient.getPhone());
            newUser.setNickName(patient.getName());
            newUser.setPhonenumber(patient.getPhone());
            newUser.setPassword(SecurityUtils.encryptPassword("123456"));
            newUser.setUserType("patient");
            newUser.setStatus("0");
            R<Boolean> registerResult = remoteUserService.registerUserInfo(newUser, SecurityConstants.INNER);
            if (R.isError(registerResult))
            {
                throw new ServiceException(registerResult.getMsg());
            }
            existUser = getUserByPhonenumber(patient.getPhone());
        }
        if (existUser != null)
        {
            patient.setUserId(existUser.getUserId());
        }
    }

    private SysUser getUserByPhonenumber(String phonenumber)
    {
        R<SysUser> userResult = remoteUserService.getUserInfoByPhonenumber(phonenumber, SecurityConstants.INNER);
        if (R.isError(userResult))
        {
            throw new ServiceException(userResult.getMsg());
        }
        return userResult.getData();
    }
    
    /**
     * 生成病历号：P+年月日+6位随机数
     */
    private String generatePatientNo() {
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        Random random = new Random();
        int randomNum = random.nextInt(900000) + 100000; // 6位随机数
        return "P" + dateStr + String.valueOf(randomNum);
    }
}
