package com.ruoyi.register.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.ruoyi.register.domain.Register;

/**
 * 挂号Service接口
 *
 * @author ruoyi
 * @date 2026-05-29
 */
public interface IRegisterService
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
    public List<Map<String, Object>> selectDoctorListByDeptId(Long deptId);

    /**
     * 根据医生ID查询挂号费
     *
     * @param doctorId 医生ID
     * @return 挂号费
     */
    public BigDecimal getRegisterFeeByDoctorId(Long doctorId);

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
     * 批量删除挂号
     *
     * @param registerIds 需要删除的挂号主键集合
     * @return 结果
     */
    public int deleteRegisterByRegisterIds(Long[] registerIds);

    /**
     * 删除挂号信息
     *
     * @param registerId 挂号主键
     * @return 结果
     */
    public int deleteRegisterByRegisterId(Long registerId);

    /**
     * 退号
     *
     * @param registerId 挂号主键
     * @return 结果
     */
    public int cancelRegister(Long registerId);

    /**
     * 标记挂号单已支付
     *
     * @param registerId 挂号主键
     * @return 结果
     */
    public int markRegisterPaid(Long registerId);

    /**
     * 清理超时未支付挂号单
     *
     * @return 清理数量
     */
    public int cleanupExpiredUnpaidRegisters();

}
