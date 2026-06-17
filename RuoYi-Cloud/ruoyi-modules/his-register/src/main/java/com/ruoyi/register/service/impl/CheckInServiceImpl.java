package com.ruoyi.register.service.impl;

import java.time.Duration;
import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
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

        // 如果调用方未传 queueNo，则自动计算
        if (checkIn.getQueueNo() == null)
        {
            Long queueNo = generateQueueNo(checkIn.getRegisterId());
            checkIn.setQueueNo(queueNo);
        }
        return checkInMapper.insertCheckIn(checkIn);
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
        return checkInMapper.updateCheckIn(checkIn);
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
