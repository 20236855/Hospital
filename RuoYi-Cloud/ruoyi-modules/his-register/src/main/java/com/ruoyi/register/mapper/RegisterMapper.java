package com.ruoyi.register.mapper;

import java.util.List;
import java.math.BigDecimal;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.register.domain.Register;

/**
 * 挂号Mapper接口
 *
 * @author ruoyi
 * @date 2026-05-29
 */
@Mapper
public interface RegisterMapper
{
    /**
     * 查询挂号
     *
     * @param registerId 挂号主键
     * @return 挂号
     */
    public Register selectRegisterByRegisterId(Long registerId);

    /**
     * 查询挂号列表
     *
     * @param register 挂号
     * @return 挂号集合
     */
    public List<Register> selectRegisterList(Register register);

    /**
     * 查询当天最大挂号单号
     *
     * @param day 日期前缀（yyyyMMdd）
     * @return 最大挂号单号
     */
    public String selectMaxRegisterNo(@Param("day") String day);

    /**
     * 根据身份证号查询患者
     *
     * @param idCard 身份证号
     * @return 患者信息
     */
    public Map<String, Object> selectPatientByIdCard(@Param("idCard") String idCard);
    
    /**
     * 根据患者ID查询患者
     *
     * @param patientId 患者ID
     * @return 患者信息
     */
    public Map<String, Object> selectPatientById(@Param("patientId") Long patientId);

    /**
     * 根据挂号级别ID查询挂号费
     *
     * @param levelId 挂号级别ID
     * @return 挂号费用
     */
    BigDecimal selectRegisterFeeByLevelId(@Param("levelId") Long levelId);

    /**
     * 查询全部挂号级别
     */
    public List<Map<String, Object>> selectRegisterLevelList();

    /**
     * 查询全部科室
     */
    public List<Map<String, Object>> selectDeptList();

    /**
     * 根据科室ID查询医生
     *
     * @param deptId 科室ID
     */
    public List<Map<String, Object>> selectDoctorListByDeptId(@Param("deptId") Long deptId);

    /**
     * 新增挂号
     *
     * @param register 挂号
     * @return 结果
     */
    public int insertRegister(Register register);

    /**
     * 修改挂号
     *
     * @param register 挂号
     * @return 结果
     */
    public int updateRegister(Register register);

    /**
     * 删除挂号
     *
     * @param registerId 挂号主键
     * @return 结果
     */
    public int deleteRegisterByRegisterId(Long registerId);

    /**
     * 批量删除挂号
     *
     * @param registerIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteRegisterByRegisterIds(Long[] registerIds);

    /**
     * 根据排班ID查询排班
     *
     * @param scheduleId 排班ID
     * @return 排班信息
     */
    public Map<String, Object> selectScheduleById(@Param("scheduleId") Long scheduleId);

    /**
     * 增加排班预约人数
     *
     * @param scheduleId 排班ID
     * @return 结果
     */
    public int incrementScheduleReservedNumber(@Param("scheduleId") Long scheduleId);

    /**
     * 减少排班预约人数
     *
     * @param scheduleId 排班ID
     * @return 结果
     */
    public int decrementScheduleReservedNumber(@Param("scheduleId") Long scheduleId);

    /**
     * 更新排班状态
     *
     * @param scheduleId 排班ID
     * @param status 状态
     * @return 结果
     */
    public int updateScheduleStatus(@Param("scheduleId") Long scheduleId, @Param("status") String status);
}
