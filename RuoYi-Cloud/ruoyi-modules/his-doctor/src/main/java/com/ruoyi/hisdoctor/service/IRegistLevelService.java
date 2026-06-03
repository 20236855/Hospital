package com.ruoyi.hisdoctor.service;

import java.util.List;
import com.ruoyi.hisdoctor.domain.RegistLevel;

/**
 * 挂号级别Service接口
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
public interface IRegistLevelService 
{
    /**
     * 查询挂号级别
     * 
     * @param levelId 挂号级别主键
     * @return 挂号级别
     */
    public RegistLevel selectRegistLevelByLevelId(Long levelId);

    /**
     * 查询挂号级别列表
     * 
     * @param registLevel 挂号级别
     * @return 挂号级别集合
     */
    public List<RegistLevel> selectRegistLevelList(RegistLevel registLevel);

    /**
     * 新增挂号级别
     * 
     * @param registLevel 挂号级别
     * @return 结果
     */
    public int insertRegistLevel(RegistLevel registLevel);

    /**
     * 修改挂号级别
     * 
     * @param registLevel 挂号级别
     * @return 结果
     */
    public int updateRegistLevel(RegistLevel registLevel);

    /**
     * 批量删除挂号级别
     * 
     * @param levelIds 需要删除的挂号级别主键集合
     * @return 结果
     */
    public int deleteRegistLevelByLevelIds(Long[] levelIds);

    /**
     * 删除挂号级别信息
     * 
     * @param levelId 挂号级别主键
     * @return 结果
     */
    public int deleteRegistLevelByLevelId(Long levelId);
}
