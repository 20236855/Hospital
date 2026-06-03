package com.ruoyi.hisdoctor.mapper;

import java.util.List;
import com.ruoyi.hisdoctor.domain.Doctor;

/**
 * 医生信息Mapper接口
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
public interface DoctorMapper 
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
     * 修改医生信息
     * 
     * @param doctor 医生信息
     * @return 结果
     */
    public int updateDoctor(Doctor doctor);

    /**
     * 删除医生信息
     * 
     * @param doctorId 医生信息主键
     * @return 结果
     */
    public int deleteDoctorByDoctorId(Long doctorId);

    /**
     * 批量删除医生信息
     * 
     * @param doctorIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteDoctorByDoctorIds(Long[] doctorIds);
}
