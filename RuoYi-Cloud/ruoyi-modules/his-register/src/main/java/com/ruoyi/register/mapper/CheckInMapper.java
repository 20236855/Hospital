package com.ruoyi.register.mapper;

import java.util.List;
import com.ruoyi.register.domain.CheckIn;

/**
 * 签到Mapper接口
 * 
 * @author ruoyi
 * @date 2026-05-29
 */
public interface CheckInMapper 
{
    /**
     * 查询签到
     * 
     * @param checkInId 签到主键
     * @return 签到
     */
    public CheckIn selectCheckInByCheckInId(Long checkInId);

    /**
     * 根据挂号ID查询签到
     * 
     * @param registerId 挂号ID
     * @return 签到
     */
    public CheckIn selectCheckInByRegisterId(Long registerId);

    /**
     * 查询签到列表
     * 
     * @param checkIn 签到
     * @return 签到集合
     */
    public List<CheckIn> selectCheckInList(CheckIn checkIn);

    /**
     * 新增签到
     * 
     * @param checkIn 签到
     * @return 结果
     */
    public int insertCheckIn(CheckIn checkIn);

    /**
     * 修改签到
     * 
     * @param checkIn 签到
     * @return 结果
     */
    public int updateCheckIn(CheckIn checkIn);

    /**
     * 删除签到
     * 
     * @param checkInId 签到主键
     * @return 结果
     */
    public int deleteCheckInByCheckInId(Long checkInId);

    /**
     * 批量删除签到
     * 
     * @param checkInIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCheckInByCheckInIds(Long[] checkInIds);

    /**
     * 查询指定排班下已签到的最大排队号（无签到时返回0）
     * @param scheduleId 排班ID
     * @return 当前最大排队号
     */
    public Long selectMaxQueueNoByScheduleId(Long scheduleId);
}
