package com.ruoyi.hisdoctor.service;

import java.util.List;
import com.ruoyi.hisdoctor.domain.Schedule;

/**
 * 医生排班Service接口
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
public interface IScheduleService 
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
     * 批量删除医生排班
     * 
     * @param scheduleIds 需要删除的医生排班主键集合
     * @return 结果
     */
    public int deleteScheduleByScheduleIds(Long[] scheduleIds);

    /**
     * 删除医生排班信息
     * 
     * @param scheduleId 医生排班主键
     * @return 结果
     */
    public int deleteScheduleByScheduleId(Long scheduleId);
}
