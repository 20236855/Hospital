package com.ruoyi.register.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.register.mapper.CheckInMapper;
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
    @Autowired
    private CheckInMapper checkInMapper;

    /**
     * 查询签到
     * 
     * @param checkInId 签到主键
     * @return 签到
     */
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

    /**
     * 查询签到列表
     * 
     * @param checkIn 签到
     * @return 签到
     */
    @Override
    public List<CheckIn> selectCheckInList(CheckIn checkIn)
    {
        return checkInMapper.selectCheckInList(checkIn);
    }

    /**
     * 新增签到
     * 
     * @param checkIn 签到
     * @return 结果
     */
    @Override
    public int insertCheckIn(CheckIn checkIn)
    {
        checkIn.setCreateTime(DateUtils.getNowDate());
        return checkInMapper.insertCheckIn(checkIn);
    }

    /**
     * 修改签到
     * 
     * @param checkIn 签到
     * @return 结果
     */
    @Override
    public int updateCheckIn(CheckIn checkIn)
    {
        checkIn.setUpdateTime(DateUtils.getNowDate());
        return checkInMapper.updateCheckIn(checkIn);
    }

    /**
     * 批量删除签到
     * 
     * @param checkInIds 需要删除的签到主键
     * @return 结果
     */
    @Override
    public int deleteCheckInByCheckInIds(Long[] checkInIds)
    {
        return checkInMapper.deleteCheckInByCheckInIds(checkInIds);
    }

    /**
     * 删除签到信息
     * 
     * @param checkInId 签到主键
     * @return 结果
     */
    @Override
    public int deleteCheckInByCheckInId(Long checkInId)
    {
        return checkInMapper.deleteCheckInByCheckInId(checkInId);
    }
}
