package com.ruoyi.hisdoctor.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.hisdoctor.domain.AgentScheduleItem;
import com.ruoyi.hisdoctor.domain.Schedule;

/**
 * 医生排班Mapper接口
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
public interface ScheduleMapper 
{
    /**
     * 查询医生排班
     * 
     * @param scheduleId 医生排班主键
     * @return 医生排班
     */
    public Schedule selectScheduleByScheduleId(Long scheduleId);

    /**
     * 查询医生排班列表
     * 
     * @param schedule 医生排班
     * @return 医生排班集合
     */
    public List<Schedule> selectScheduleList(Schedule schedule);

    /**
     * 新增医生排班
     * 
     * @param schedule 医生排班
     * @return 结果
     */
    public int insertSchedule(Schedule schedule);

    /**
     * 修改医生排班
     * 
     * @param schedule 医生排班
     * @return 结果
     */
    public int updateSchedule(Schedule schedule);

    /**
     * 删除医生排班
     * 
     * @param scheduleId 医生排班主键
     * @return 结果
     */
    public int deleteScheduleByScheduleId(Long scheduleId);

    /**
     * 批量删除医生排班
     * 
     * @param scheduleIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteScheduleByScheduleIds(Long[] scheduleIds);

    /**
     * 增加排班预约人数
     * 
     * @param scheduleId 排班ID
     * @return 结果
     */
    public int incrementReservedNumber(Long scheduleId);

    /**
     * 更新排班状态
     * 
     * @param scheduleId 排班ID
     * @param status 状态
     * @return 结果
     */
    public int updateScheduleStatus(Long scheduleId, String status);

    /**
     * 减少排班预约人数
     * 
     * @param scheduleId 排班ID
     * @return 结果
     */
    public int decrementReservedNumber(Long scheduleId);

    /**
     * 查询指定周排班明细。
     */
    public List<AgentScheduleItem> selectAgentScheduleItems(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 查询指定医生、日期、午别排班。
     */
    public Schedule selectScheduleByDoctorDateSlot(@Param("doctorId") Long doctorId, @Param("scheduleDate") Date scheduleDate, @Param("timeSlot") String timeSlot);
}
