package com.ruoyi.hisexam.service.impl;

import java.util.List;
import java.util.Map;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.hisexam.mapper.ExamResultMapper;
import com.ruoyi.hisexam.domain.ExamResult;
import com.ruoyi.hisexam.service.IExamResultService;

/**
 * 检查检验结果Service业务层处理
 *
 * @author ruoyi
 * @date 2026-06-30
 */
@Service
public class ExamResultServiceImpl implements IExamResultService
{
    @Autowired
    private ExamResultMapper examResultMapper;

    /**
     * 查询可录入结果的申请单选项。
     *
     * @return 申请单、患者、医技项目信息
     */
    @Override
    public List<Map<String, Object>> selectExamApplyOptions()
    {
        return examResultMapper.selectExamApplyOptions();
    }

    /**
     * 查询检查检验结果
     *
     * @param resultId 检查检验结果主键
     * @return 检查检验结果
     */
    @Override
    public ExamResult selectExamResultByResultId(Long resultId)
    {
        return examResultMapper.selectExamResultByResultId(resultId);
    }

    /**
     * 查询检查检验结果列表
     *
     * @param examResult 检查检验结果
     * @return 检查检验结果
     */
    @Override
    public List<ExamResult> selectExamResultList(ExamResult examResult)
    {
        return examResultMapper.selectExamResultList(examResult);
    }

    /**
     * 新增检查检验结果
     *
     * @param examResult 检查检验结果
     * @return 结果
     */
    @Override
    public int insertExamResult(ExamResult examResult)
    {
        fillLinkedFields(examResult);
        examResult.setCreateTime(DateUtils.getNowDate());
        return examResultMapper.insertExamResult(examResult);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveExamResultReport(List<ExamResult> examResults)
    {
        if (examResults == null || examResults.isEmpty())
        {
            throw new ServiceException("请至少录入一项检查检验结果");
        }

        Long applyId = null;
        for (ExamResult examResult : examResults)
        {
            if (examResult != null && examResult.getApplyId() != null)
            {
                applyId = examResult.getApplyId();
                break;
            }
        }
        if (applyId == null)
        {
            throw new ServiceException("请选择检查检验申请单");
        }
        for (ExamResult examResult : examResults)
        {
            if (examResult != null && examResult.getApplyId() != null && !applyId.equals(examResult.getApplyId()))
            {
                throw new ServiceException("一次只能保存同一申请单的检查检验结果");
            }
        }

        examResultMapper.deleteExamResultByApplyId(applyId);

        int rows = 0;
        Long sort = 1L;
        for (ExamResult examResult : examResults)
        {
            if (examResult == null)
            {
                continue;
            }
            examResult.setApplyId(applyId);
            if (examResult.getSort() == null)
            {
                examResult.setSort(sort);
            }
            if (examResult.getReportTime() == null)
            {
                examResult.setReportTime(DateUtils.getNowDate());
            }
            fillLinkedFields(examResult);
            examResult.setCreateTime(DateUtils.getNowDate());
            rows += examResultMapper.insertExamResult(examResult);
            sort++;
        }
        return rows;
    }

    /**
     * 修改检查检验结果
     *
     * @param examResult 检查检验结果
     * @return 结果
     */
    @Override
    public int updateExamResult(ExamResult examResult)
    {
        fillLinkedFields(examResult);
        examResult.setUpdateTime(DateUtils.getNowDate());
        return examResultMapper.updateExamResult(examResult);
    }

    /**
     * 批量删除检查检验结果
     *
     * @param resultIds 需要删除的检查检验结果主键
     * @return 结果
     */
    @Override
    public int deleteExamResultByResultIds(Long[] resultIds)
    {
        return examResultMapper.deleteExamResultByResultIds(resultIds);
    }

    /**
     * 删除检查检验结果信息
     *
     * @param resultId 检查检验结果主键
     * @return 结果
     */
    @Override
    public int deleteExamResultByResultId(Long resultId)
    {
        return examResultMapper.deleteExamResultByResultId(resultId);
    }

    /**
     * 按申请单回填结果类型、明细类型和医技项目，避免前端传错关联字段。
     */
    private void fillLinkedFields(ExamResult examResult)
    {
        if (examResult.getApplyId() == null)
        {
            throw new ServiceException("请选择检查检验申请单");
        }

        Map<String, Object> apply = examResultMapper.selectExamApplyOptionByApplyId(examResult.getApplyId());
        if (apply == null)
        {
            throw new ServiceException("检查检验申请单不存在");
        }

        String applyType = getString(apply, "applyType");
        examResult.setResultType(applyType);
        examResult.setItemType(resolveItemType(applyType));
        if (StringUtils.isBlank(examResult.getItemCode()))
        {
            examResult.setItemCode(getString(apply, "techCode"));
        }
        if (StringUtils.isBlank(examResult.getItemName()))
        {
            examResult.setItemName(getString(apply, "techName"));
        }

        if (examResult.getSort() == null)
        {
            examResult.setSort(1L);
        }
        if (StringUtils.isBlank(examResult.getStatus()))
        {
            examResult.setStatus("0");
        }

        if ("IMAGE".equals(examResult.getItemType()) && !Long.valueOf(1L).equals(examResult.getSort()))
        {
            clearNonPrimaryImageFields(examResult);
        }
    }

    private String resolveItemType(String resultType)
    {
        if ("CHECK".equals(resultType))
        {
            return "IMAGE";
        }
        if ("INSPEC".equals(resultType) || "LAB".equals(resultType))
        {
            return "ITEM";
        }
        return "TEXT";
    }

    private void clearNonPrimaryImageFields(ExamResult examResult)
    {
        examResult.setResultValue(null);
        examResult.setResultUnit(null);
        examResult.setReferenceRange(null);
        examResult.setAbnormalFlag(null);
        examResult.setImageFind(null);
        examResult.setDiagnosisOpinion(null);
        examResult.setDiagnosisResult(null);
        examResult.setSuggestion(null);
    }

    private String getString(Map<String, Object> source, String key)
    {
        Object value = source.get(key);
        return value == null ? null : String.valueOf(value);
    }
}
