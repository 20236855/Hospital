package com.ruoyi.register.service.impl;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.his.api.RemoteEncounterService;
import com.ruoyi.register.domain.Register;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.ruoyi.register.mapper.CheckInMapper;
import com.ruoyi.register.mapper.RegisterMapper;
import com.ruoyi.register.domain.CheckIn;
import com.ruoyi.register.service.ICheckInService;

/**
 * 签到Service业务层处理
 *
 * @author ruoyi
 * @date 2026-05-29
 */
@Service
public class CheckInServiceImpl implements ICheckInService
{
    private static final Logger log = LoggerFactory.getLogger(CheckInServiceImpl.class);

    /** 排队号分布式锁 Key 前缀 */
    private static final String QUEUE_LOCK_KEY = "his:checkin:queue:";

    /** 排队号锁超时时间 */
    private static final Duration LOCK_TIMEOUT = Duration.ofSeconds(5);

    @Autowired
    private CheckInMapper checkInMapper;

    @Autowired
    private RegisterMapper registerMapper;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    private RemoteEncounterService remoteEncounterService;

    @Override
    public CheckIn selectCheckInByCheckInId(Long checkInId)
    {
        return checkInMapper.selectCheckInByCheckInId(checkInId);
    }

    @Override
    public CheckIn selectCheckInByRegisterId(Long registerId)
    {
        return checkInMapper.selectCheckInByRegisterId(registerId);
    }

    @Override
    public List<CheckIn> selectCheckInList(CheckIn checkIn)
    {
        return checkInMapper.selectCheckInList(checkIn);
    }

    /**
     * 新增签到（自动计算排队号）
     * <p>
     * queue_no 表示同一排班(schedule_id)下当前患者是第几个签到的人。
     * 通过 Redis 分布式锁保证并发签到排队号不重复。
     */
    @Override
    public int insertCheckIn(CheckIn checkIn)
    {
        checkIn.setCreateTime(DateUtils.getNowDate());
        if (checkIn.getCheckInTime() == null)
        {
            checkIn.setCheckInTime(DateUtils.getNowDate());
        }
        if (checkIn.getStatus() == null)
        {
            checkIn.setStatus("已签到");
        }

        // 如果调用方未传 queueNo，则自动计算
        if (checkIn.getQueueNo() == null)
        {
            Long queueNo = generateQueueNo(checkIn.getRegisterId());
            checkIn.setQueueNo(queueNo);
        }
        int rows = checkInMapper.insertCheckIn(checkIn);
        if (rows > 0)
        {
            syncEncounter(checkIn);
        }
        return rows;
    }

    /**
     * 根据挂号记录自动生成排队号
     * <p>
     * 算法：查该挂号对应的排班(schedule_id)下已签到的最大排队号 + 1
     * 通过 Redis 分布式锁保证同一排班并发签到时的排队号不冲突
     */
    private Long generateQueueNo(Long registerId)
    {
        // 1. 从挂号记录获取 schedule_id
        Register register = registerMapper.selectRegisterByRegisterId(registerId);
        if (register == null || register.getScheduleId() == null)
        {
            log.warn("挂号记录不存在或无排班信息, registerId={}", registerId);
            return 1L;
        }
        Long scheduleId = register.getScheduleId();
        String lockKey = QUEUE_LOCK_KEY + scheduleId;

        // 2. 获取 Redis 分布式锁
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", LOCK_TIMEOUT);
        if (Boolean.FALSE.equals(locked))
        {
            // 获取锁失败，自旋等待后重试（最多1秒）
            long deadline = System.currentTimeMillis() + 1000;
            while (System.currentTimeMillis() < deadline)
            {
                try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
                locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", LOCK_TIMEOUT);
                if (Boolean.TRUE.equals(locked)) break;
            }
        }

        try
        {
            // 3. 查询当前排班下已签到的最大排队号
            Long maxQueueNo = checkInMapper.selectMaxQueueNoByScheduleId(scheduleId);
            long nextQueueNo = (maxQueueNo != null ? maxQueueNo : 0) + 1;
            log.info("签到处计算排队号: scheduleId={}, maxQueueNo={}, newQueueNo={}",
                    scheduleId, maxQueueNo, nextQueueNo);
            return nextQueueNo;
        }
        finally
        {
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    public int updateCheckIn(CheckIn checkIn)
    {
        checkIn.setUpdateTime(DateUtils.getNowDate());
        int rows = checkInMapper.updateCheckIn(checkIn);
        if (rows > 0)
        {
            CheckIn latest = checkIn.getCheckInId() != null ? checkInMapper.selectCheckInByCheckInId(checkIn.getCheckInId()) : checkIn;
            syncEncounter(latest);
        }
        return rows;
    }

    private void syncEncounter(CheckIn checkIn)
    {
        if (checkIn == null || checkIn.getRegisterId() == null)
        {
            return;
        }
        Register register = registerMapper.selectRegisterByRegisterId(checkIn.getRegisterId());
        if (register == null)
        {
            log.warn("签到后同步接诊失败：挂号记录不存在, registerId={}", checkIn.getRegisterId());
            return;
        }
        if (register.getPatientId() == null || register.getDoctorId() == null || register.getDeptId() == null)
        {
            log.warn("签到后同步接诊失败：挂号记录缺少患者/医生/科室, registerId={}", checkIn.getRegisterId());
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("registerId", register.getRegisterId());
        data.put("patientId", register.getPatientId());
        data.put("doctorId", register.getDoctorId());
        data.put("deptId", register.getDeptId());
        data.put("checkInTime", checkIn.getCheckInTime());
        data.put("queueNo", checkIn.getQueueNo());
        R<Boolean> result = remoteEncounterService.syncFromCheckIn(data, SecurityConstants.INNER);
        if (result == null || R.isError(result) || !Boolean.TRUE.equals(result.getData()))
        {
            log.warn("签到后同步接诊失败：registerId={}, msg={}", checkIn.getRegisterId(), result == null ? "empty response" : result.getMsg());
        }
    }

    @Override
    public int deleteCheckInByCheckInIds(Long[] checkInIds)
    {
        return checkInMapper.deleteCheckInByCheckInIds(checkInIds);
    }

    @Override
    public int deleteCheckInByCheckInId(Long checkInId)
    {
        return checkInMapper.deleteCheckInByCheckInId(checkInId);
    }
}
