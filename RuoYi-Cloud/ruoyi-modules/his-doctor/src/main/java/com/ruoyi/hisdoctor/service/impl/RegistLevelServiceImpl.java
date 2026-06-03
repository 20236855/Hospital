package com.ruoyi.hisdoctor.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hisdoctor.mapper.RegistLevelMapper;
import com.ruoyi.hisdoctor.domain.RegistLevel;
import com.ruoyi.hisdoctor.service.IRegistLevelService;

/**
 * 挂号级别Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
@Service
public class RegistLevelServiceImpl implements IRegistLevelService 
{
    @Autowired
    private RegistLevelMapper registLevelMapper;

    /**
     * 查询挂号级别
     * 
     * @param levelId 挂号级别主键
     * @return 挂号级别
     */
    @Override
    public RegistLevel selectRegistLevelByLevelId(Long levelId)
    {
        return registLevelMapper.selectRegistLevelByLevelId(levelId);
    }

    /**
     * 查询挂号级别列表
     * 
     * @param registLevel 挂号级别
     * @return 挂号级别
     */
    @Override
    public List<RegistLevel> selectRegistLevelList(RegistLevel registLevel)
    {
        return registLevelMapper.selectRegistLevelList(registLevel);
    }

    /**
     * 新增挂号级别
     * 
     * @param registLevel 挂号级别
     * @return 结果
     */
    @Override
    public int insertRegistLevel(RegistLevel registLevel)
    {
        registLevel.setCreateTime(DateUtils.getNowDate());
        return registLevelMapper.insertRegistLevel(registLevel);
    }

    /**
     * 修改挂号级别
     * 
     * @param registLevel 挂号级别
     * @return 结果
     */
    @Override
    public int updateRegistLevel(RegistLevel registLevel)
    {
        registLevel.setUpdateTime(DateUtils.getNowDate());
        return registLevelMapper.updateRegistLevel(registLevel);
    }

    /**
     * 批量删除挂号级别
     * 
     * @param levelIds 需要删除的挂号级别主键
     * @return 结果
     */
    @Override
    public int deleteRegistLevelByLevelIds(Long[] levelIds)
    {
        return registLevelMapper.deleteRegistLevelByLevelIds(levelIds);
    }

    /**
     * 删除挂号级别信息
     * 
     * @param levelId 挂号级别主键
     * @return 结果
     */
    @Override
    public int deleteRegistLevelByLevelId(Long levelId)
    {
        return registLevelMapper.deleteRegistLevelByLevelId(levelId);
    }
}
