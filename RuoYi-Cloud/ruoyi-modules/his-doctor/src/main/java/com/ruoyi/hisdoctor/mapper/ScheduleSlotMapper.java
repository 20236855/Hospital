package com.ruoyi.hisdoctor.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.hisdoctor.domain.ScheduleSlot;

/**
 * 医生排班时间片Mapper接口
 */
public interface ScheduleSlotMapper
{
    public ScheduleSlot selectScheduleSlotBySlotId(Long slotId);

    public List<ScheduleSlot> selectScheduleSlotList(ScheduleSlot scheduleSlot);

    public List<ScheduleSlot> selectOpenDoctorSlots(ScheduleSlot scheduleSlot);

    public int insertScheduleSlot(ScheduleSlot scheduleSlot);

    public int updateScheduleSlot(ScheduleSlot scheduleSlot);

    public int deleteScheduleSlotBySlotId(Long slotId);

    public int deleteScheduleSlotByScheduleId(Long scheduleId);

    public int countByScheduleId(Long scheduleId);

    public int countReservedByScheduleId(Long scheduleId);

    public int incrementReservedNumber(Long slotId);

    public int decrementReservedNumber(Long slotId);

    public int updateScheduleSlotStatus(@Param("slotId") Long slotId, @Param("status") String status);
}
