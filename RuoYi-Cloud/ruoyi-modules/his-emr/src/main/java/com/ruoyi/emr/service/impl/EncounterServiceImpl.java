package com.ruoyi.emr.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.ruoyi.his.api.RemotePatientService;
import com.ruoyi.his.api.RemoteRegisterService;
import com.ruoyi.emr.api.RemoteCheckInService;
import com.ruoyi.emr.mapper.EncounterMapper;
import com.ruoyi.emr.domain.Encounter;
import com.ruoyi.emr.domain.vo.EncounterVo;
import com.ruoyi.emr.service.IEncounterService;

/**
 * 接诊Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
@Service
public class EncounterServiceImpl implements IEncounterService
{
    @Autowired
    private EncounterMapper encounterMapper;

    @Autowired
    private RemotePatientService remotePatientService;

    @Autowired
    private RemoteRegisterService remoteRegisterService;

    @Autowired
    private RemoteCheckInService remoteCheckInService;

    /**
     * 查询接诊
     * 
     * @param encounterId 接诊主键
     * @return 接诊
     */
    @Override
    public EncounterVo selectEncounterByEncounterId(Long encounterId)
    {
        return buildEncounterVo(encounterMapper.selectEncounterByEncounterId(encounterId), new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    /**
     * 查询接诊列表
     * 
     * @param encounter 接诊
     * @return 接诊
     */
    @Override
    public List<EncounterVo> selectEncounterList(Encounter encounter)
    {
        encounterMapper.insertMissingCheckedInEncounters(encounter);
        List<Encounter> list = encounterMapper.selectEncounterList(encounter);
        List<EncounterVo> voList = new ArrayList<>();
        Map<Long, Map<String, Object>> patientCache = new HashMap<>();
        Map<Long, Map<String, Object>> registerCache = new HashMap<>();
        Map<Long, Map<String, Object>> checkInCache = new HashMap<>();
        if (StringUtils.isNotEmpty(list))
        {
            for (Encounter item : list)
            {
                EncounterVo vo = buildEncounterVo(item, patientCache, registerCache, checkInCache);
                if (vo != null)
                {
                    voList.add(vo);
                }
            }
        }
        return voList;
    }

    private EncounterVo buildEncounterVo(Encounter encounter, Map<Long, Map<String, Object>> patientCache, Map<Long, Map<String, Object>> registerCache, Map<Long, Map<String, Object>> checkInCache)
    {
        if (encounter == null)
        {
            return null;
        }
        EncounterVo vo = new EncounterVo();
        BeanUtils.copyProperties(encounter, vo);
        setPatientInfo(vo, patientCache);
        setRegisterInfo(vo, registerCache);
        setCheckInInfo(vo, checkInCache);
        return vo;
    }

    private void setPatientInfo(EncounterVo encounterVo, Map<Long, Map<String, Object>> patientCache)
    {
        if (encounterVo == null || encounterVo.getPatientId() == null)
        {
            return;
        }
        Map<String, Object> patient = patientCache.get(encounterVo.getPatientId());
        if (patient == null && !patientCache.containsKey(encounterVo.getPatientId()))
        {
            patient = getPatientInfo(encounterVo.getPatientId());
            patientCache.put(encounterVo.getPatientId(), patient);
        }
        if (patient == null)
        {
            return;
        }
        Object name = patient.get("name");
        Object phone = patient.get("phone");
        encounterVo.setPatientName(name == null ? null : name.toString());
        encounterVo.setPatientPhone(phone == null ? null : phone.toString());
    }

    private Map<String, Object> getPatientInfo(Long patientId)
    {
        try
        {
            R<Map<String, Object>> result = remotePatientService.getPatientInfo(patientId, SecurityConstants.INNER);
            if (result != null && R.isSuccess(result) && result.getData() != null)
            {
                return result.getData();
            }
        }
        catch (Exception e)
        {
            return null;
        }
        return null;
    }

    private void setRegisterInfo(EncounterVo encounterVo, Map<Long, Map<String, Object>> registerCache)
    {
        if (encounterVo == null || encounterVo.getRegisterId() == null)
        {
            return;
        }
        Map<String, Object> register = registerCache.get(encounterVo.getRegisterId());
        if (register == null && !registerCache.containsKey(encounterVo.getRegisterId()))
        {
            register = getRegisterInfo(encounterVo.getRegisterId());
            registerCache.put(encounterVo.getRegisterId(), register);
        }
        if (register == null)
        {
            return;
        }
        Object registerNo = register.get("registerNo");
        encounterVo.setRegisterNo(registerNo == null ? null : registerNo.toString());
    }

    private Map<String, Object> getRegisterInfo(Long registerId)
    {
        try
        {
            R<Map<String, Object>> result = remoteRegisterService.getRegisterInfo(registerId, SecurityConstants.INNER);
            if (result != null && R.isSuccess(result) && result.getData() != null)
            {
                return result.getData();
            }
        }
        catch (Exception e)
        {
            return null;
        }
        return null;
    }

    private void setCheckInInfo(EncounterVo encounterVo, Map<Long, Map<String, Object>> checkInCache)
    {
        if (encounterVo == null || encounterVo.getRegisterId() == null)
        {
            return;
        }
        Map<String, Object> checkIn = checkInCache.get(encounterVo.getRegisterId());
        if (checkIn == null && !checkInCache.containsKey(encounterVo.getRegisterId()))
        {
            checkIn = getCheckInInfo(encounterVo.getRegisterId());
            checkInCache.put(encounterVo.getRegisterId(), checkIn);
        }
        if (checkIn == null)
        {
            return;
        }
        Object queueNo = checkIn.get("queueNo");
        Object checkInTime = checkIn.get("checkInTime");
        if (queueNo != null)
        {
            if (queueNo instanceof Number)
            {
                encounterVo.setQueueNo(((Number) queueNo).longValue());
            }
            else
            {
                try
                {
                    encounterVo.setQueueNo(Long.parseLong(queueNo.toString()));
                }
                catch (Exception e)
                {
                    // ignore
                }
            }
        }
        if (checkInTime != null)
        {
            if (checkInTime instanceof java.util.Date)
            {
                encounterVo.setCheckInTime((java.util.Date) checkInTime);
            }
            else
            {
                try
                {
                    encounterVo.setCheckInTime(com.ruoyi.common.core.utils.DateUtils.parseDate(checkInTime.toString()));
                }
                catch (Exception e)
                {
                    // ignore
                }
            }
        }
    }

    private Map<String, Object> getCheckInInfo(Long registerId)
    {
        try
        {
            R<Map<String, Object>> result = remoteCheckInService.getCheckInInfo(registerId, SecurityConstants.INNER);
            if (result != null && R.isSuccess(result) && result.getData() != null)
            {
                return result.getData();
            }
        }
        catch (Exception e)
        {
            return null;
        }
        return null;
    }

    /**
     * 新增接诊
     * 
     * @param encounter 接诊
     * @return 结果
     */
    @Override
    public int insertEncounter(Encounter encounter)
    {
        encounter.setCreateTime(DateUtils.getNowDate());
        return encounterMapper.insertEncounter(encounter);
    }

    @Override
    public int syncEncounterFromCheckIn(Map<String, Object> data)
    {
        Long registerId = toLong(data.get("registerId"));
        Long patientId = toLong(data.get("patientId"));
        Long doctorId = toLong(data.get("doctorId"));
        Long deptId = toLong(data.get("deptId"));
        Date checkInTime = toDate(data.get("checkInTime"));
        if (registerId == null || patientId == null || doctorId == null || deptId == null)
        {
            return 0;
        }

        Encounter encounter = encounterMapper.selectEncounterByRegisterId(registerId);
        if (encounter == null)
        {
            encounter = new Encounter();
            encounter.setRegisterId(registerId);
            encounter.setPatientId(patientId);
            encounter.setDoctorId(doctorId);
            encounter.setDeptId(deptId);
            encounter.setEncounterType("普通门诊");
            encounter.setEncounterStatus("待接诊");
            encounter.setVisitTime(checkInTime != null ? checkInTime : DateUtils.getNowDate());
            encounter.setCreateTime(DateUtils.getNowDate());
            return encounterMapper.insertEncounter(encounter);
        }

        encounter.setPatientId(patientId);
        encounter.setDoctorId(doctorId);
        encounter.setDeptId(deptId);
        if (StringUtils.isEmpty(encounter.getEncounterType()))
        {
            encounter.setEncounterType("普通门诊");
        }
        if (StringUtils.isEmpty(encounter.getEncounterStatus()))
        {
            encounter.setEncounterStatus("待接诊");
        }
        if (encounter.getVisitTime() == null)
        {
            encounter.setVisitTime(checkInTime != null ? checkInTime : DateUtils.getNowDate());
        }
        encounter.setUpdateTime(DateUtils.getNowDate());
        return encounterMapper.updateEncounter(encounter);
    }

    private Long toLong(Object value)
    {
        if (value == null)
        {
            return null;
        }
        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }
        try
        {
            return Long.valueOf(value.toString());
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private Date toDate(Object value)
    {
        if (value == null)
        {
            return null;
        }
        if (value instanceof Date)
        {
            return (Date) value;
        }
        try
        {
            return DateUtils.parseDate(value.toString());
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * 修改接诊
     * 
     * @param encounter 接诊
     * @return 结果
     */
    @Override
    public int updateEncounter(Encounter encounter)
    {
        encounter.setUpdateTime(DateUtils.getNowDate());
        return encounterMapper.updateEncounter(encounter);
    }

    /**
     * 批量删除接诊
     * 
     * @param encounterIds 需要删除的接诊主键
     * @return 结果
     */
    @Override
    public int deleteEncounterByEncounterIds(Long[] encounterIds)
    {
        return encounterMapper.deleteEncounterByEncounterIds(encounterIds);
    }

    /**
     * 删除接诊信息
     * 
     * @param encounterId 接诊主键
     * @return 结果
     */
    @Override
    public int deleteEncounterByEncounterId(Long encounterId)
    {
        return encounterMapper.deleteEncounterByEncounterId(encounterId);
    }
}
