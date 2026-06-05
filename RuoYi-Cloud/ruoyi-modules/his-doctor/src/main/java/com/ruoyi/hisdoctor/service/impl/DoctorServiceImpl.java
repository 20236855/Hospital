package com.ruoyi.hisdoctor.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.hisdoctor.mapper.DoctorMapper;
import com.ruoyi.hisdoctor.domain.Doctor;
import com.ruoyi.hisdoctor.service.IDoctorService;

/**
 * 医生信息Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
@Service
public class DoctorServiceImpl implements IDoctorService 
{
    @Autowired
    private DoctorMapper doctorMapper;

    /**
     * 查询医生信息
     * 
     * @param doctorId 医生信息主键
     * @return 医生信息
     */
    @Override
    public Doctor selectDoctorByDoctorId(Long doctorId)
    {
        return doctorMapper.selectDoctorByDoctorId(doctorId);
    }

    /**
     * 查询医生信息列表
     * 
     * @param doctor 医生信息
     * @return 医生信息
     */
    @Override
    public List<Doctor> selectDoctorList(Doctor doctor)
    {
        return doctorMapper.selectDoctorList(doctor);
    }

    /**
     * 新增医生信息
     * 
     * @param doctor 医生信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertDoctor(Doctor doctor)
    {
        doctor.setCreateTime(DateUtils.getNowDate());
        
        // 自动生成医生工号：D+科室ID+6位序号
        if (doctor.getDoctorNo() == null || doctor.getDoctorNo().isEmpty()) {
            String doctorNo = generateDoctorNo(doctor.getDeptId());
            doctor.setDoctorNo(doctorNo);
        }
        
        return doctorMapper.insertDoctor(doctor);
    }

    /**
     * 自助完善医生信息。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int completeDoctor(Doctor doctor)
    {
        if (doctor.getUserId() == null)
        {
            throw new ServiceException("当前登录用户不能为空");
        }
        if (doctorMapper.selectDoctorByUserId(doctor.getUserId()) != null)
        {
            throw new ServiceException("医生档案已存在，请勿重复完善");
        }
        if (doctor.getDeptId() == null)
        {
            throw new ServiceException("请选择所属科室");
        }
        if (doctor.getDoctorNo() == null || doctor.getDoctorNo().isEmpty())
        {
            doctor.setDoctorNo(generateDoctorNo(doctor.getDeptId()));
        }
        doctor.setCreateTime(DateUtils.getNowDate());
        return doctorMapper.insertDoctor(doctor);
    }

    /**
     * 修改医生信息
     * 
     * @param doctor 医生信息
     * @return 结果
     */
    @Override
    public int updateDoctor(Doctor doctor)
    {
        doctor.setUpdateTime(DateUtils.getNowDate());
        return doctorMapper.updateDoctor(doctor);
    }

    /**
     * 批量删除医生信息
     * 
     * @param doctorIds 需要删除的医生信息主键
     * @return 结果
     */
    @Override
    public int deleteDoctorByDoctorIds(Long[] doctorIds)
    {
        return doctorMapper.deleteDoctorByDoctorIds(doctorIds);
    }

    /**
     * 删除医生信息信息
     * 
     * @param doctorId 医生信息主键
     * @return 结果
     */
    @Override
    public int deleteDoctorByDoctorId(Long doctorId)
    {
        return doctorMapper.deleteDoctorByDoctorId(doctorId);
    }

    /**
     * 根据用户ID查询医生信息
     * 
     * @param userId 用户ID
     * @return 医生信息
     */
    @Override
    public Doctor getDoctorByUserId(Long userId)
    {
        return doctorMapper.selectDoctorByUserId(userId);
    }

    /**
     * 生成医生工号：D+科室ID+6位序号
     */
    private String generateDoctorNo(Long deptId) {
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        // 简化处理：D+科室ID+6位随机数
        Random random = new Random();
        int randomNum = random.nextInt(900000) + 100000; // 6位随机数
        return "D" + deptId + String.valueOf(randomNum);
    }
}
