package com.ruoyi.hisdoctor.service.impl;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.hisdoctor.domain.Schedule;
import com.ruoyi.hisdoctor.domain.ScheduleSlot;
import com.ruoyi.hisdoctor.mapper.ScheduleSlotMapper;
import com.ruoyi.hisdoctor.service.IScheduleSlotService;

/**
 * 医生排班时间片Service业务层处理
 */
@Service
public class ScheduleSlotServiceImpl implements IScheduleSlotService
{
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    private ScheduleSlotMapper scheduleSlotMapper;

    @Override
    public ScheduleSlot selectScheduleSlotBySlotId(Long slotId)
    {
        return scheduleSlotMapper.selectScheduleSlotBySlotId(slotId);
    }

    @Override
    public List<ScheduleSlot> selectScheduleSlotList(ScheduleSlot scheduleSlot)
    {
        return scheduleSlotMapper.selectScheduleSlotList(scheduleSlot);
    }

    @Override
    public List<ScheduleSlot> selectOpenDoctorSlots(ScheduleSlot scheduleSlot)
    {
        return scheduleSlotMapper.selectOpenDoctorSlots(scheduleSlot);
    }

    @Override
    public int insertScheduleSlot(ScheduleSlot scheduleSlot)
    {
        scheduleSlot.setCreateTime(DateUtils.getNowDate());
        return scheduleSlotMapper.insertScheduleSlot(scheduleSlot);
    }

    @Override
    public int updateScheduleSlot(ScheduleSlot scheduleSlot)
    {
        scheduleSlot.setUpdateTime(DateUtils.getNowDate());
        return scheduleSlotMapper.updateScheduleSlot(scheduleSlot);
    }

    @Override
    public int deleteScheduleSlotBySlotId(Long slotId)
    {
        return scheduleSlotMapper.deleteScheduleSlotBySlotId(slotId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int generateSlotsForSchedule(Schedule schedule)
    {
        if (schedule == null || schedule.getScheduleId() == null || schedule.getDoctorId() == null
                || schedule.getScheduleDate() == null)
        {
            return 0;
        }
        if (scheduleSlotMapper.countByScheduleId(schedule.getScheduleId()) > 0)
        {
            return 0;
        }
        if (scheduleSlotMapper.countReservedByScheduleId(schedule.getScheduleId()) > 0)
        {
            throw new ServiceException("该排班已有预约，不能重新生成时间片");
        }
        scheduleSlotMapper.deleteScheduleSlotByScheduleId(schedule.getScheduleId());
        List<TimeRange> ranges = buildTimeRanges(schedule.getTimeSlot());
        if (ranges.isEmpty())
        {
            return 0;
        }

        long maxNumber = schedule.getMaxNumber() == null ? 0L : schedule.getMaxNumber();
        long base = maxNumber / ranges.size();
        long remainder = maxNumber % ranges.size();
        int created = 0;
        for (int i = 0; i < ranges.size(); i++)
        {
            long slotMax = base + (i < remainder ? 1 : 0);
            if (slotMax <= 0)
            {
                continue;
            }
            TimeRange range = ranges.get(i);
            ScheduleSlot slot = new ScheduleSlot();
            slot.setScheduleId(schedule.getScheduleId());
            slot.setDoctorId(schedule.getDoctorId());
            slot.setScheduleDate(schedule.getScheduleDate());
            slot.setStartTime(range.start);
            slot.setEndTime(range.end);
            slot.setMaxNumber(slotMax);
            slot.setReservedNumber(0L);
            slot.setStatus("0");
            slot.setCreateTime(DateUtils.getNowDate());
            created += scheduleSlotMapper.insertScheduleSlot(slot);
        }
        return created;
    }

    @Override
    public boolean bookSlot(Long slotId)
    {
        ScheduleSlot slot = scheduleSlotMapper.selectScheduleSlotBySlotId(slotId);
        if (slot == null)
        {
            throw new ServiceException("预约时间片不存在");
        }
        if (!"0".equals(slot.getStatus()) || slot.getReservedNumber() >= slot.getMaxNumber())
        {
            throw new ServiceException("该时间片已约满或已关闭");
        }
        if (scheduleSlotMapper.incrementReservedNumber(slotId) == 0)
        {
            throw new ServiceException("预约失败，该时间片可能已被其他用户预约");
        }
        return true;
    }

    @Override
    public boolean cancelSlot(Long slotId)
    {
        if (slotId == null)
        {
            return true;
        }
        ScheduleSlot slot = scheduleSlotMapper.selectScheduleSlotBySlotId(slotId);
        if (slot == null)
        {
            return true;
        }
        if (slot.getReservedNumber() != null && slot.getReservedNumber() > 0)
        {
            scheduleSlotMapper.decrementReservedNumber(slotId);
        }
        return true;
    }

    private List<TimeRange> buildTimeRanges(String timeSlot)
    {
        if ("morning".equals(timeSlot))
        {
            return splitHalfHours("08:00", "12:00");
        }
        if ("afternoon".equals(timeSlot))
        {
            return splitHalfHours("14:00", "17:30");
        }
        if ("evening".equals(timeSlot))
        {
            return splitHalfHours("18:00", "21:00");
        }
        return new ArrayList<>();
    }

    private List<TimeRange> splitHalfHours(String start, String end)
    {
        List<TimeRange> ranges = new ArrayList<>();
        LocalTime current = LocalTime.parse(start);
        LocalTime endTime = LocalTime.parse(end);
        while (current.isBefore(endTime))
        {
            LocalTime next = current.plusMinutes(30);
            if (next.isAfter(endTime))
            {
                break;
            }
            ranges.add(new TimeRange(current.format(TIME_FORMATTER), next.format(TIME_FORMATTER)));
            current = next;
        }
        return ranges;
    }

    private static class TimeRange
    {
        private final String start;

        private final String end;

        private TimeRange(String start, String end)
        {
            this.start = start;
            this.end = end;
        }
    }
}
