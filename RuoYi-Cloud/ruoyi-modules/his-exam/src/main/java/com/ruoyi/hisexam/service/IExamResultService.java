package com.ruoyi.hisexam.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.hisexam.domain.ExamResult;

/**
 * 检查检验结果Service接口
 *
 * @author ruoyi
 * @date 2026-06-30
 */
public interface IExamResultService
{
    /**
     * 查询可录入结果的申请单选项。
     *
     * @return 申请单、患者、医技项目信息
     */
    public List<Map<String, Object>> selectExamApplyOptions();

    /**
     * 查询当前患者的检查检验报告。
     *
     * @param patientId 患者ID
     * @return 按申请单聚合的检查检验报告
     */
    public List<Map<String, Object>> selectPatientExamReports(Long patientId);

    /**
     * 查询检查检验结果
     *
     * @param resultId 检查检验结果主键
     * @return 检查检验结果
     */
    public ExamResult selectExamResultByResultId(Long resultId);

    /**
     * 查询检查检验结果列表
     *
     * @param examResult 检查检验结果
     * @return 检查检验结果集合
     */
    public List<ExamResult> selectExamResultList(ExamResult examResult);

    /**
     * 新增检查检验结果
     *
     * @param examResult 检查检验结果
     * @return 结果
     */
    public int insertExamResult(ExamResult examResult);

    public int saveExamResultReport(List<ExamResult> examResults);

    /**
     * 修改检查检验结果
     *
     * @param examResult 检查检验结果
     * @return 结果
     */
    public int updateExamResult(ExamResult examResult);

    /**
     * 批量删除检查检验结果
     *
     * @param resultIds 需要删除的检查检验结果主键集合
     * @return 结果
     */
    public int deleteExamResultByResultIds(Long[] resultIds);

    /**
     * 删除检查检验结果信息
     *
     * @param resultId 检查检验结果主键
     * @return 结果
     */
    public int deleteExamResultByResultId(Long resultId);
}
