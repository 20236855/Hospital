package com.ruoyi.hisdoctor.service.impl;

import java.util.List;
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
    @Transactional
    public boolean bookSchedule(Long scheduleId)
    {
        Schedule schedule = scheduleMapper.selectScheduleByScheduleId(scheduleId);
        if (schedule == null) {
            throw new ServiceException("排班不存在");
        }
        if ("1".equals(schedule.getStatus()) || schedule.getReservedNumber() >= schedule.getMaxNumber()) {
            throw new ServiceException("该排班已满，无法预约");
        }
        // 增加预约人数
        scheduleMapper.incrementReservedNumber(scheduleId);
        // 重新查询检查是否已满
        Schedule updatedSchedule = scheduleMapper.selectScheduleByScheduleId(scheduleId);
        if (updatedSchedule.getReservedNumber() >= updatedSchedule.getMaxNumber()) {
            scheduleMapper.updateScheduleStatus(scheduleId, "1");
        }
        return true;
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
