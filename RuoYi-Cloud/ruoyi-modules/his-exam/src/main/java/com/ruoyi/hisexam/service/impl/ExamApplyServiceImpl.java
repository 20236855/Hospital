package com.ruoyi.hisexam.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hisexam.mapper.ExamApplyMapper;
import com.ruoyi.hisexam.domain.ExamApply;
import com.ruoyi.hisexam.service.IExamApplyService;

/**
 * 检查/检验/处置申请单Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
@Service
public class ExamApplyServiceImpl implements IExamApplyService 
{
    @Autowired
    private ExamApplyMapper examApplyMapper;

    /**
     * 查询检查/检验/处置申请单
     * 
     * @param applyId 检查/检验/处置申请单主键
     * @return 检查/检验/处置申请单
     */
    @Override
    public ExamApply selectExamApplyByApplyId(Long applyId)
    {
        return examApplyMapper.selectExamApplyByApplyId(applyId);
    }

    /**
     * 查询检查/检验/处置申请单列表
     * 
     * @param examApply 检查/检验/处置申请单
     * @return 检查/检验/处置申请单
     */
    @Override
    public List<ExamApply> selectExamApplyList(ExamApply examApply)
    {
        return examApplyMapper.selectExamApplyList(examApply);
    }

    /**
     * 新增检查/检验/处置申请单
     * 
     * @param examApply 检查/检验/处置申请单
     * @return 结果
     */
    @Override
    public int insertExamApply(ExamApply examApply)
    {
        examApply.setCreateTime(DateUtils.getNowDate());
        return examApplyMapper.insertExamApply(examApply);
    }

    /**
     * 修改检查/检验/处置申请单
     * 
     * @param examApply 检查/检验/处置申请单
     * @return 结果
     */
    @Override
    public int updateExamApply(ExamApply examApply)
    {
        examApply.setUpdateTime(DateUtils.getNowDate());
        return examApplyMapper.updateExamApply(examApply);
    }

    /**
     * 批量删除检查/检验/处置申请单
     * 
     * @param applyIds 需要删除的检查/检验/处置申请单主键
     * @return 结果
     */
    @Override
    public int deleteExamApplyByApplyIds(Long[] applyIds)
    {
        return examApplyMapper.deleteExamApplyByApplyIds(applyIds);
    }

    /**
     * 删除检查/检验/处置申请单信息
     * 
     * @param applyId 检查/检验/处置申请单主键
     * @return 结果
     */
    @Override
    public int deleteExamApplyByApplyId(Long applyId)
    {
        return examApplyMapper.deleteExamApplyByApplyId(applyId);
    }
}
