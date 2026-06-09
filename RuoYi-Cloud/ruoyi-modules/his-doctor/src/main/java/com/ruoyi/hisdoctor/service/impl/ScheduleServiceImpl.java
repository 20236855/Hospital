package com.ruoyi.hisdoctor.service.impl;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.hisdoctor.mapper.ScheduleMapper;
import com.ruoyi.hisdoctor.domain.Schedule;
import com.ruoyi.hisdoctor.service.IScheduleService;

/**
 * 医生排班Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
@Service
public class ScheduleServiceImpl implements IScheduleService 
{
    private static final Logger log = LoggerFactory.getLogger(ScheduleServiceImpl.class);
    
    // 用于跟踪请求
    private static final AtomicLong requestCounter = new AtomicLong(0);
    
    @Autowired
    private ScheduleMapper scheduleMapper;

    /**
     * 查询医生排班
     * 
     * @param scheduleId 医生排班主键
     * @return 医生排班
     */
    @Override
    public Schedule selectScheduleByScheduleId(Long scheduleId)
    {
        return scheduleMapper.selectScheduleByScheduleId(scheduleId);
    }

    /**
     * 查询医生排班列表
     * 
     * @param schedule 医生排班
     * @return 医生排班
     */
    @Override
    public List<Schedule> selectScheduleList(Schedule schedule)
    {
        return scheduleMapper.selectScheduleList(schedule);
    }

    /**
     * 新增医生排班
     * 
     * @param schedule 医生排班
     * @return 结果
     */
    @Override
    public int insertSchedule(Schedule schedule)
    {
        schedule.setCreateTime(DateUtils.getNowDate());
        return scheduleMapper.insertSchedule(schedule);
    }

    /**
     * 修改医生排班
     * 
     * @param schedule 医生排班
     * @return 结果
     */
    @Override
    public int updateSchedule(Schedule schedule)
    {
        schedule.setUpdateTime(DateUtils.getNowDate());
        return scheduleMapper.updateSchedule(schedule);
    }

    /**
     * 批量删除医生排班
     * 
     * @param scheduleIds 需要删除的医生排班主键
     * @return 结果
     */
    @Override
    public int deleteScheduleByScheduleIds(Long[] scheduleIds)
    {
        return scheduleMapper.deleteScheduleByScheduleIds(scheduleIds);
    }

    /**
     * 删除医生排班信息
     * 
     * @param scheduleId 医生排班主键
     * @return 结果
     */
    @Override
    public int deleteScheduleByScheduleId(Long scheduleId)
    {
        return scheduleMapper.deleteScheduleByScheduleId(scheduleId);
    }

    /**
     * 增加排班预约人数
     * 
     * @param scheduleId 排班ID
     * @return 结果
     */
    @Override
    public int incrementReservedNumber(Long scheduleId)
    {
        return scheduleMapper.incrementReservedNumber(scheduleId);
    }

    /**
     * 更新排班状态
     * 
     * @param scheduleId 排班ID
     * @param status 状态
     * @return 结果
     */
    @Override
    public int updateScheduleStatus(Long scheduleId, String status)
    {
        return scheduleMapper.updateScheduleStatus(scheduleId, status);
    }

    /**
     * 减少排班预约人数
     * 
     * @param scheduleId 排班ID
     * @return 结果
     */
    @Override
    public int decrementReservedNumber(Long scheduleId)
    {
        return scheduleMapper.decrementReservedNumber(scheduleId);
    }

    /**
     * 预约排班（检查可用性并增加预约人数）
     * 
     * @param scheduleId 排班ID
     * @return 结果
     */
    @Override
    public boolean bookSchedule(Long scheduleId)
    {
        long requestId = requestCounter.incrementAndGet();
        long startTime = System.currentTimeMillis();
        log.info("[预约请求-{}] 开始处理预约请求，scheduleId={}", requestId, scheduleId);
        
        try {
            // 第一步：查询当前排班状态（事务外查询，避免锁冲突）
            Schedule scheduleBefore = scheduleMapper.selectScheduleByScheduleId(scheduleId);
            if (scheduleBefore == null) {
                log.error("[预约请求-{}] 排班不存在，scheduleId={}", requestId, scheduleId);
                throw new ServiceException("排班不存在");
            }
            log.info("[预约请求-{}] 当前排班状态：scheduleId={}, maxNumber={}, reservedNumber={}, status={}", 
                    requestId, scheduleId, scheduleBefore.getMaxNumber(), scheduleBefore.getReservedNumber(), scheduleBefore.getStatus());
            
            // 检查是否已满
            if ("1".equals(scheduleBefore.getStatus()) || scheduleBefore.getReservedNumber() >= scheduleBefore.getMaxNumber()) {
                log.warn("[预约请求-{}] 排班已满，无法预约。当前预约数={}, 最大预约数={}, 状态={}", 
                        requestId, scheduleBefore.getReservedNumber(), scheduleBefore.getMaxNumber(), scheduleBefore.getStatus());
                throw new ServiceException("该排班已满，无法预约");
            }
            
            // 第二步：执行更新操作（直接调用Mapper，避免事务冲突）
            log.info("[预约请求-{}] 开始执行数据库更新操作...", requestId);
            long updateStartTime = System.currentTimeMillis();
            int rows = scheduleMapper.incrementReservedNumber(scheduleId);
            long updateEndTime = System.currentTimeMillis();
            log.info("[预约请求-{}] 数据库更新完成，耗时={}ms, 影响行数={}", requestId, (updateEndTime - updateStartTime), rows);
            
            if (rows == 0) {
                log.error("[预约请求-{}] 更新失败，影响行数为0，可能被其他请求抢先预约", requestId);
                // 再次查询状态确认（事务外查询）
                Schedule scheduleAfter = scheduleMapper.selectScheduleByScheduleId(scheduleId);
                if (scheduleAfter != null) {
                    log.error("[预约请求-{}] 更新失败后状态：reservedNumber={}, maxNumber={}, status={}", 
                            requestId, scheduleAfter.getReservedNumber(), scheduleAfter.getMaxNumber(), scheduleAfter.getStatus());
                }
                throw new ServiceException("预约失败，该排班可能已被其他用户预约，请稍后重试");
            }
            
            log.info("[预约请求-{}] 预约成功！", requestId);
            
            return true;
        } catch (ServiceException e) {
            log.error("[预约请求-{}] 预约失败（业务异常）：{}", requestId, e.getMessage());
            throw e;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("[预约请求-{}] 预约失败（系统异常）：{}，耗时={}ms，完整异常:", requestId, e.getMessage(), (endTime - startTime), e);
            throw new ServiceException("预约失败，请稍后重试");
        } finally {
            long endTime = System.currentTimeMillis();
            log.info("[预约请求-{}] 预约请求处理结束，总耗时={}ms", requestId, (endTime - startTime));
        }
    }

    /**
     * 取消排班预约（减少预约人数）
     * 
     * @param scheduleId 排班ID
     * @return 结果
     */
    @Override
    @Transactional
    public boolean cancelSchedule(Long scheduleId)
    {
        Schedule schedule = scheduleMapper.selectScheduleByScheduleId(scheduleId);
        if (schedule == null) {
            throw new ServiceException("排班不存在");
        }
        if (schedule.getReservedNumber() > 0) {
            scheduleMapper.decrementReservedNumber(scheduleId);
            // 如果之前是满的，现在不是满的了，更新状态
            if ("1".equals(schedule.getStatus())) {
                scheduleMapper.updateScheduleStatus(scheduleId, "0");
            }
        }
        return true;
    }
}
