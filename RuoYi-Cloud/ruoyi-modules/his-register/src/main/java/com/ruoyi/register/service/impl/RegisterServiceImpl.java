package com.ruoyi.register.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.register.domain.Register;
import com.ruoyi.register.mapper.RegisterMapper;
import com.ruoyi.register.service.IRegisterService;

/**
 * Register service implementation.
 */
@Service
public class RegisterServiceImpl implements IRegisterService
{
    @Autowired
    private RegisterMapper registerMapper;

    @Override
    public Register selectRegisterByRegisterId(Long registerId)
    {
        return registerMapper.selectRegisterByRegisterId(registerId);
    }

    @Override
    public List<Register> selectRegisterList(Register register)
    {
        return registerMapper.selectRegisterList(register);
    }

    @Override
    public List<Map<String, Object>> selectRegisterLevelList()
    {
        return registerMapper.selectRegisterLevelList();
    }

    @Override
    public List<Map<String, Object>> selectDeptList()
    {
        return registerMapper.selectDeptList();
    }

    @Override
    public List<Map<String, Object>> selectDoctorListByDeptId(Long deptId)
    {
        return registerMapper.selectDoctorListByDeptId(deptId);
    }

    @Override
    public BigDecimal getRegisterFeeByLevelId(Long levelId)
    {
        return registerMapper.selectRegisterFeeByLevelId(levelId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertRegister(Register register)
    {
        register.setRegisterNo(buildRegisterNo());
        if (StringUtils.isEmpty(register.getRegisterType()))
        {
            register.setRegisterType("online");
        }
        register.setRegisterStatus("registered");
        register.setPayStatus("unpaid");
        register.setRemark("online register");
        setPatientByIdCard(register);
        reserveRegisterSource(register);
        setRegisterFeeByLevel(register);
        register.setCreateTime(DateUtils.getNowDate());
        return registerMapper.insertRegister(register);
    }

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

    @Override
    public int deleteRegisterByRegisterIds(Long[] registerIds)
    {
        for (Long registerId : registerIds)
        {
            checkRegisterCanDelete(registerId);
        }
        return registerMapper.deleteRegisterByRegisterIds(registerIds);
    }

    @Override
    public int deleteRegisterByRegisterId(Long registerId)
    {
        checkRegisterCanDelete(registerId);
        return registerMapper.deleteRegisterByRegisterId(registerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelRegister(Long registerId)
    {
        Register register = registerMapper.selectRegisterByRegisterId(registerId);
        if (register == null)
        {
            throw new ServiceException("Register record does not exist");
        }
        if (!"registered".equals(register.getRegisterStatus()))
        {
            throw new ServiceException("Only registered records can be cancelled");
        }
        releaseRegisterSource(register);
        register.setRegisterStatus("cancel");
        register.setUpdateTime(DateUtils.getNowDate());
        return registerMapper.updateRegister(register);
    }

    @Override
    public int markRegisterPaid(Long registerId)
    {
        Register register = registerMapper.selectRegisterByRegisterId(registerId);
        if (register == null)
        {
            throw new ServiceException("Register record does not exist");
        }
        if (!"registered".equals(register.getRegisterStatus()))
        {
            throw new ServiceException("Current register status cannot be paid");
        }
        if ("paid".equalsIgnoreCase(register.getPayStatus()))
        {
            return 1;
        }
        return registerMapper.updateRegisterPayStatus(registerId, "paid", DateUtils.getNowDate());
    }

    @Override
    @Scheduled(fixedDelay = 60000, initialDelay = 60000)
    @Transactional(rollbackFor = Exception.class)
    public int cleanupExpiredUnpaidRegisters()
    {
        Date expireTime = DateUtils.addMinutes(DateUtils.getNowDate(), -30);
        List<Register> expiredList = registerMapper.selectExpiredUnpaidRegisters(expireTime);
        int cleaned = 0;
        for (Register register : expiredList)
        {
            if (registerMapper.countRegisterChildRecords(register.getRegisterId()) > 0)
            {
                continue;
            }
            releaseRegisterSource(register);
            cleaned += registerMapper.deleteRegisterByRegisterId(register.getRegisterId());
        }
        return cleaned;
    }

    private void checkRegisterCanDelete(Long registerId)
    {
        if (registerMapper.countRegisterChildRecords(registerId) > 0)
        {
            throw new ServiceException("该挂号已有签到、接诊、检查检验或处方记录，不能删除，请使用取消挂号或先处理相关业务记录");
        }
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
        if (register.getPatientId() != null)
        {
            Map<String, Object> patient = registerMapper.selectPatientById(register.getPatientId());
            if (patient == null || patient.get("patientId") == null)
            {
                throw new ServiceException("Patient does not exist");
            }
            return;
        }
        if (StringUtils.isEmpty(register.getIdCard()))
        {
            throw new ServiceException("Please select a patient or enter an ID card");
        }
        Map<String, Object> patient = registerMapper.selectPatientByIdCard(register.getIdCard());
        if (patient == null || patient.get("patientId") == null)
        {
            throw new ServiceException("Patient record does not exist");
        }
        register.setPatientId(Long.valueOf(patient.get("patientId").toString()));
    }

    private void setRegisterFeeByLevel(Register register)
    {
        if (register.getLevelId() == null && register.getDoctorId() != null)
        {
            Map<String, Object> feeInfo = registerMapper.selectDoctorRegisterFee(register.getDoctorId());
            if (feeInfo == null || feeInfo.get("levelId") == null)
            {
                throw new ServiceException("Doctor register level is not configured");
            }
            register.setLevelId(((Number) feeInfo.get("levelId")).longValue());
            Object registerFee = feeInfo.get("registerFee");
            if (registerFee instanceof BigDecimal)
            {
                register.setRegisterFee((BigDecimal) registerFee);
                return;
            }
            if (registerFee != null)
            {
                register.setRegisterFee(new BigDecimal(registerFee.toString()));
                return;
            }
        }
        if (register.getLevelId() == null)
        {
            return;
        }
        register.setRegisterFee(registerMapper.selectRegisterFeeByLevelId(register.getLevelId()));
    }

    private void reserveRegisterSource(Register register)
    {
        if (register.getSlotId() != null)
        {
            reserveScheduleSlot(register);
            return;
        }
        reserveSchedule(register.getScheduleId());
    }

    private void reserveScheduleSlot(Register register)
    {
        Map<String, Object> slot = registerMapper.selectScheduleSlotById(register.getSlotId());
        if (slot == null)
        {
            throw new ServiceException("预约时间片不存在");
        }
        String status = String.valueOf(slot.get("status"));
        Long reservedNumber = toLong(slot.get("reservedNumber"));
        Long maxNumber = toLong(slot.get("maxNumber"));
        if (!"0".equals(status) || maxNumber == null || reservedNumber == null || reservedNumber >= maxNumber)
        {
            throw new ServiceException("该时间片已约满或已关闭");
        }

        Long scheduleId = toLong(slot.get("scheduleId"));
        Long doctorId = toLong(slot.get("doctorId"));
        Long deptId = toLong(slot.get("deptId"));
        if (register.getScheduleId() != null && !register.getScheduleId().equals(scheduleId))
        {
            throw new ServiceException("预约时间片与排班不匹配");
        }
        if (register.getDoctorId() != null && !register.getDoctorId().equals(doctorId))
        {
            throw new ServiceException("预约时间片与医生不匹配");
        }
        if (register.getDeptId() != null && !register.getDeptId().equals(deptId))
        {
            throw new ServiceException("预约时间片与科室不匹配");
        }
        register.setScheduleId(scheduleId);
        register.setDoctorId(doctorId);
        register.setDeptId(deptId);
        Object scheduleDate = slot.get("scheduleDate");
        if (scheduleDate instanceof Date)
        {
            register.setRegisterTime((Date) scheduleDate);
        }

        if (registerMapper.incrementScheduleSlotReservedNumber(register.getSlotId()) == 0)
        {
            throw new ServiceException("预约失败，该时间片可能已被其他用户预约");
        }
        reserveSchedule(scheduleId);
    }

    private void reserveSchedule(Long scheduleId)
    {
        if (scheduleId == null)
        {
            throw new ServiceException("Please select a schedule");
        }
        Map<String, Object> schedule = registerMapper.selectScheduleById(scheduleId);
        if (schedule == null)
        {
            throw new ServiceException("Schedule does not exist");
        }
        String status = String.valueOf(schedule.get("status"));
        Long reservedNumber = toLong(schedule.get("reservedNumber"));
        Long maxNumber = toLong(schedule.get("maxNumber"));
        if ("1".equals(status) || maxNumber == null || reservedNumber == null || reservedNumber >= maxNumber)
        {
            throw new ServiceException("Schedule is full");
        }
        registerMapper.incrementScheduleReservedNumber(scheduleId);
        if (reservedNumber + 1 >= maxNumber)
        {
            registerMapper.updateScheduleStatus(scheduleId, "1");
        }
    }

    private void releaseRegisterSource(Register register)
    {
        if (register == null)
        {
            return;
        }
        if (register.getSlotId() != null)
        {
            registerMapper.decrementScheduleSlotReservedNumber(register.getSlotId());
        }
        releaseSchedule(register.getScheduleId());
    }

    private void releaseSchedule(Long scheduleId)
    {
        if (scheduleId == null)
        {
            return;
        }
        Map<String, Object> schedule = registerMapper.selectScheduleById(scheduleId);
        if (schedule == null)
        {
            return;
        }
        Long reservedNumber = toLong(schedule.get("reservedNumber"));
        if (reservedNumber > 0)
        {
            registerMapper.decrementScheduleReservedNumber(scheduleId);
        }
        if ("1".equals(String.valueOf(schedule.get("status"))))
        {
            registerMapper.updateScheduleStatus(scheduleId, "0");
        }
    }

    private Long toLong(Object value)
    {
        if (value == null)
        {
            return null;
        }
        return value instanceof Number ? ((Number) value).longValue() : Long.valueOf(value.toString());
    }
}
