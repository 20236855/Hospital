package com.ruoyi.register.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.register.mapper.RegisterMapper;
import com.ruoyi.register.domain.Register;
import com.ruoyi.register.service.IRegisterService;

/**
 * 挂号Service业务层处理
 *
 * @author ruoyi
 * @date 2026-05-29
 */
@Service
public class RegisterServiceImpl implements IRegisterService
{
    @Autowired
    private RegisterMapper registerMapper;

    /**
     * 查询挂号
     *
     * @param registerId 挂号主键
     * @return 挂号
     */
    @Override
    public Register selectRegisterByRegisterId(Long registerId)
    {
        return registerMapper.selectRegisterByRegisterId(registerId);
    }

    /**
     * 查询挂号列表
     *
     * @param register 挂号
     * @return 挂号
     */
    @Override
    public List<Register> selectRegisterList(Register register)
    {
        return registerMapper.selectRegisterList(register);
    }

    /**
     * 查询全部挂号级别
     */
    @Override
    public List<Map<String, Object>> selectRegisterLevelList()
    {
        return registerMapper.selectRegisterLevelList();
    }

    /**
     * 查询全部科室
     */
    @Override
    public List<Map<String, Object>> selectDeptList()
    {
        return registerMapper.selectDeptList();
    }

    /**
     * 根据科室ID查询医生
     */
    @Override
    public List<Map<String, Object>> selectDoctorListByDeptId(Long deptId)
    {
        return registerMapper.selectDoctorListByDeptId(deptId);
    }

    /**
     * 新增挂号
     *
     * @param register 挂号
     * @return 结果
     */
    @Override
    @Transactional
    public int insertRegister(Register register)
    {
        register.setRegisterNo(buildRegisterNo());
        if (StringUtils.isEmpty(register.getRegisterType())) {
            register.setRegisterType("online");
        }
        register.setRegisterStatus("registered");
        setPatientByIdCard(register);
        setRegisterFeeByLevel(register);

        // 检查并更新排班
        if (register.getScheduleId() != null) {
            Map<String, Object> schedule = registerMapper.selectScheduleById(register.getScheduleId());
            if (schedule == null) {
                throw new ServiceException("排班不存在");
            }
            String status = (String) schedule.get("status");
            Long reservedNumber = ((Number) schedule.get("reservedNumber")).longValue();
            Long maxNumber = ((Number) schedule.get("maxNumber")).longValue();
            
            if ("1".equals(status) || reservedNumber >= maxNumber) {
                throw new ServiceException("该排班已满，无法预约");
            }

            // 增加预约人数
            registerMapper.incrementScheduleReservedNumber(register.getScheduleId());

            // 检查是否满了，如果满了更新状态
            Map<String, Object> updatedSchedule = registerMapper.selectScheduleById(register.getScheduleId());
            Long newReservedNumber = ((Number) updatedSchedule.get("reservedNumber")).longValue();
            if (newReservedNumber >= maxNumber) {
                registerMapper.updateScheduleStatus(register.getScheduleId(), "1");
            }
        } else {
            throw new ServiceException("请选择排班时间");
        }

        register.setCreateTime(DateUtils.getNowDate());
        return registerMapper.insertRegister(register);
    }

    /**
     * 修改挂号
     *
     * @param register 挂号
     * @return 结果
     */
    @Override
    public int updateRegister(Register register)
    {
        register.setRegisterNo(null);
        register.setPatientId(null);
        register.setRegisterType(null);
        setRegisterFeeByLevel(register);
        register.setUpdateTime(DateUtils.getNowDate());
        return registerMapper.updateRegister(register);
    }

    /**
     * 批量删除挂号
     *
     * @param registerIds 需要删除的挂号主键
     * @return 结果
     */
    @Override
    public int deleteRegisterByRegisterIds(Long[] registerIds)
    {
        return registerMapper.deleteRegisterByRegisterIds(registerIds);
    }

    /**
     * 删除挂号信息
     *
     * @param registerId 挂号主键
     * @return 结果
     */
    @Override
    public int deleteRegisterByRegisterId(Long registerId)
    {
        return registerMapper.deleteRegisterByRegisterId(registerId);
    }

    private String buildRegisterNo()
    {
        String day = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String maxNo = registerMapper.selectMaxRegisterNo(day);
        if (StringUtils.isEmpty(maxNo))
        {
            return day + "0001";
        }
        int num = Integer.parseInt(maxNo.substring(8)) + 1;
        return day + String.format("%04d", num);
    }

    private void setPatientByIdCard(Register register)
    {
        // 患者手机端已经登录，直接使用patientId
        if (register.getPatientId() != null)
        {
            // 验证患者是否存在
            Map<String, Object> patient = registerMapper.selectPatientById(register.getPatientId());
            if (patient == null || patient.get("patientId") == null)
            {
                throw new ServiceException("患者不存在");
            }
            return;
        }
        
        // 网页端可以通过身份证查找（保留原有功能）
        if (StringUtils.isEmpty(register.getIdCard()))
        {
            throw new ServiceException("请选择患者或输入身份证号");
        }
        Map<String, Object> patient = registerMapper.selectPatientByIdCard(register.getIdCard());
        if (patient == null || patient.get("patientId") == null)
        {
            throw new ServiceException("患者未建档，请前往患者模块新增");
        }
        register.setPatientId(Long.valueOf(patient.get("patientId").toString()));
    }

    private void setRegisterFeeByLevel(Register register)
    {
        if (register.getLevelId() == null)
        {
            return;
        }
        BigDecimal registerFee = registerMapper.selectRegisterFeeByLevelId(register.getLevelId());
        register.setRegisterFee(registerFee);
    }

    @Override
    @Transactional
    public int cancelRegister(Long registerId)
    {
        Register register = registerMapper.selectRegisterByRegisterId(registerId);
        if (register == null)
        {
            throw new ServiceException("挂号记录不存在");
        }
        if (!"registered".equals(register.getRegisterStatus()))
        {
            throw new ServiceException("只有已挂号单据可退号");
        }
        register.setRegisterStatus("cancel");
        register.setUpdateTime(DateUtils.getNowDate());
        int result = registerMapper.updateRegister(register);

        // 退号时减少排班预约人数
        if (register.getScheduleId() != null) {
            Map<String, Object> schedule = registerMapper.selectScheduleById(register.getScheduleId());
            if (schedule != null) {
                Long reservedNumber = ((Number) schedule.get("reservedNumber")).longValue();
                if (reservedNumber > 0) {
                    registerMapper.decrementScheduleReservedNumber(register.getScheduleId());
                    String status = (String) schedule.get("status");
                    if ("1".equals(status)) {
                        registerMapper.updateScheduleStatus(register.getScheduleId(), "0");
                    }
                }
            }
        }

        return result;
    }

    @Override
    public BigDecimal getRegisterFeeByLevelId(Long levelId)
    {
        return registerMapper.selectRegisterFeeByLevelId(levelId);
    }
}
