package com.ruoyi.emr.service;

import java.util.List;
import com.ruoyi.emr.domain.Encounter;
import com.ruoyi.emr.domain.vo.EncounterVo;

/**
 * 接诊Service接口
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
public interface IEncounterService 
{
    /**
     * 查询接诊
     * 
     * @param encounterId 接诊主键
     * @return 接诊
     */
    public EncounterVo selectEncounterByEncounterId(Long encounterId);

    /**
     * 查询接诊列表
     * 
     * @param encounter 接诊
     * @return 接诊集合
     */
    public List<EncounterVo> selectEncounterList(Encounter encounter);

    /**
     * 新增接诊
     * 
     * @param encounter 接诊
     * @return 结果
     */
    public int insertEncounter(Encounter encounter);

    /**
     * 修改接诊
     * 
     * @param encounter 接诊
     * @return 结果
     */
    public int updateEncounter(Encounter encounter);

    /**
     * 批量删除接诊
     * 
     * @param encounterIds 需要删除的接诊主键集合
     * @return 结果
     */
    public int deleteEncounterByEncounterIds(Long[] encounterIds);

    /**
     * 删除接诊信息
     * 
     * @param encounterId 接诊主键
     * @return 结果
     */
    public int deleteEncounterByEncounterId(Long encounterId);
}
