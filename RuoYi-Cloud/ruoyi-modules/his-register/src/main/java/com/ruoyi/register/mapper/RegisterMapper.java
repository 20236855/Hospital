package com.ruoyi.register.mapper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.register.domain.Register;

/**
 * Register mapper.
 */
@Mapper
public interface RegisterMapper
{
    public Register selectRegisterByRegisterId(Long registerId);

    public List<Register> selectRegisterList(Register register);

    public String selectMaxRegisterNo(@Param("day") String day);

    public Map<String, Object> selectPatientByIdCard(@Param("idCard") String idCard);

    public Map<String, Object> selectPatientById(@Param("patientId") Long patientId);

    public BigDecimal selectRegisterFeeByLevelId(@Param("levelId") Long levelId);

    public Map<String, Object> selectDoctorRegisterFee(@Param("doctorId") Long doctorId);

    public List<Map<String, Object>> selectRegisterLevelList();

    public List<Map<String, Object>> selectDeptList();

    public List<Map<String, Object>> selectDoctorListByDeptId(@Param("deptId") Long deptId);

    public int insertRegister(Register register);

    public int updateRegister(Register register);

    public int deleteRegisterByRegisterId(Long registerId);

    public int deleteRegisterByRegisterIds(Long[] registerIds);

    public int countRegisterChildRecords(@Param("registerId") Long registerId);

    public Map<String, Object> selectScheduleById(@Param("scheduleId") Long scheduleId);

    public int incrementScheduleReservedNumber(@Param("scheduleId") Long scheduleId);

    public int decrementScheduleReservedNumber(@Param("scheduleId") Long scheduleId);

    public int updateScheduleStatus(@Param("scheduleId") Long scheduleId, @Param("status") String status);

    public int updateRegisterPayStatus(@Param("registerId") Long registerId, @Param("payStatus") String payStatus, @Param("updateTime") Date updateTime);

    public List<Register> selectExpiredUnpaidRegisters(@Param("expireTime") Date expireTime);
}
