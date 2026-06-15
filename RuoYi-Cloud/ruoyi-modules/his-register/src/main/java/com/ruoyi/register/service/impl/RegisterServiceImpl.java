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
        setRegisterFeeByLevel(register);
        reserveSchedule(register.getScheduleId());
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
        return registerMapper.deleteRegisterByRegisterIds(registerIds);
    }

    @Override
    public int deleteRegisterByRegisterId(Long registerId)
    {
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
        releaseSchedule(register.getScheduleId());
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
            releaseSchedule(register.getScheduleId());
            cleaned += registerMapper.deleteRegisterByRegisterId(register.getRegisterId());
        }
        return cleaned;
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
        Long reservedNumber = ((Number) schedule.get("reservedNumber")).longValue();
        Long maxNumber = ((Number) schedule.get("maxNumber")).longValue();
        if ("1".equals(status) || reservedNumber >= maxNumber)
        {
            throw new ServiceException("Schedule is full");
        }
        registerMapper.incrementScheduleReservedNumber(scheduleId);
        if (reservedNumber + 1 >= maxNumber)
        {
            registerMapper.updateScheduleStatus(scheduleId, "1");
        }
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
        Long reservedNumber = ((Number) schedule.get("reservedNumber")).longValue();
        if (reservedNumber > 0)
        {
            registerMapper.decrementScheduleReservedNumber(scheduleId);
        }
        if ("1".equals(String.valueOf(schedule.get("status"))))
        {
            registerMapper.updateScheduleStatus(scheduleId, "0");
        }
    }
}
