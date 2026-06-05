package com.ruoyi.hisdoctor.service;

import java.util.List;
import com.ruoyi.hisdoctor.domain.Doctor;

/**
 * 医生信息Service接口
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
public interface IDoctorService 
{
    /**
     * 查询医生信息
     * 
     * @param doctorId 医生信息主键
     * @return 医生信息
     */
    public Doctor selectDoctorByDoctorId(Long doctorId);

    /**
     * 查询医生信息列表
     * 
     * @param doctor 医生信息
     * @return 医生信息集合
     */
    public List<Doctor> selectDoctorList(Doctor doctor);

    /**
     * 新增医生信息
     * 
     * @param doctor 医生信息
     * @return 结果
     */
    public int insertDoctor(Doctor doctor);

    /**
     * 自助完善医生信息
     *
     * @param doctor 医生信息
     * @return 结果
     */
    public int completeDoctor(Doctor doctor);

    /**
     * 修改医生信息
     * 
     * @param doctor 医生信息
     * @return 结果
     */
    public int updateDoctor(Doctor doctor);

    /**
     * 批量删除医生信息
     * 
     * @param doctorIds 需要删除的医生信息主键集合
     * @return 结果
     */
    public int deleteDoctorByDoctorIds(Long[] doctorIds);

    /**
     * 删除医生信息信息
     * 
     * @param doctorId 医生信息主键
     * @return 结果
     */
    public int deleteDoctorByDoctorId(Long doctorId);

    /**
     * 根据用户ID查询医生信息
     * 
     * @param userId 用户ID
     * @return 医生信息
     */
    public Doctor getDoctorByUserId(Long userId);
}
