package com.ruoyi.hisexam.service;

import java.util.List;
import com.ruoyi.hisexam.domain.ExamApply;

/**
 * 检查/检验/处置申请单Service接口
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
public interface IExamApplyService 
{
    /**
     * 查询检查/检验/处置申请单
     * 
     * @param applyId 检查/检验/处置申请单主键
     * @return 检查/检验/处置申请单
     */
    public ExamApply selectExamApplyByApplyId(Long applyId);

    /**
     * 查询检查/检验/处置申请单列表
     * 
     * @param examApply 检查/检验/处置申请单
     * @return 检查/检验/处置申请单集合
     */
    public List<ExamApply> selectExamApplyList(ExamApply examApply);

    /**
     * 新增检查/检验/处置申请单
     * 
     * @param examApply 检查/检验/处置申请单
     * @return 结果
     */
    public int insertExamApply(ExamApply examApply);

    /**
     * 修改检查/检验/处置申请单
     * 
     * @param examApply 检查/检验/处置申请单
     * @return 结果
     */
    public int updateExamApply(ExamApply examApply);

    /**
     * 批量删除检查/检验/处置申请单
     * 
     * @param applyIds 需要删除的检查/检验/处置申请单主键集合
     * @return 结果
     */
    public int deleteExamApplyByApplyIds(Long[] applyIds);

    /**
     * 删除检查/检验/处置申请单信息
     * 
     * @param applyId 检查/检验/处置申请单主键
     * @return 结果
     */
    public int deleteExamApplyByApplyId(Long applyId);
}
