package com.ruoyi.hisexam.mapper;

import java.util.List;
import java.util.Map;
import com.ruoyi.hisexam.domain.ExamResult;

/**
 * 检查检验结果Mapper接口
 *
 * @author ruoyi
 * @date 2026-06-30
 */
public interface ExamResultMapper
{
    /**
     * 查询可录入结果的申请单选项。
     *
     * @return 申请单、患者、医技项目信息
     */
    public List<Map<String, Object>> selectExamApplyOptions();

    /**
     * 查询单个申请单的结果录入上下文。
     *
     * @param applyId 申请单ID
     * @return 申请单、患者、医技项目信息
     */
    public Map<String, Object> selectExamApplyOptionByApplyId(Long applyId);

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

    /**
     * 修改检查检验结果
     *
     * @param examResult 检查检验结果
     * @return 结果
     */
    public int updateExamResult(ExamResult examResult);

    /**
     * 删除检查检验结果
     *
     * @param resultId 检查检验结果主键
     * @return 结果
     */
    public int deleteExamResultByResultId(Long resultId);

    public int deleteExamResultByApplyId(Long applyId);

    /**
     * 批量删除检查检验结果
     *
     * @param resultIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteExamResultByResultIds(Long[] resultIds);
}
