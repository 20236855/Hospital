package com.ruoyi.hisdoctor.service;

import java.util.List;
import com.ruoyi.hisdoctor.domain.Schedule;
import com.ruoyi.hisdoctor.domain.ScheduleSlot;

/**
 * 医生排班时间片Service接口
 */
public interface IScheduleSlotService
{
    public ScheduleSlot selectScheduleSlotBySlotId(Long slotId);

    public List<ScheduleSlot> selectScheduleSlotList(ScheduleSlot scheduleSlot);

    public List<ScheduleSlot> selectOpenDoctorSlots(ScheduleSlot scheduleSlot);

    public int insertScheduleSlot(ScheduleSlot scheduleSlot);

    public int updateScheduleSlot(ScheduleSlot scheduleSlot);

    public int deleteScheduleSlotBySlotId(Long slotId);

    public int generateSlotsForSchedule(Schedule schedule);

    public boolean bookSlot(Long slotId);

    public boolean cancelSlot(Long slotId);
}
