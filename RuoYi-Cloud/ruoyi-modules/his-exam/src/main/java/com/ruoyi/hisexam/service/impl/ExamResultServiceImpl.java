package com.ruoyi.hisexam.service.impl;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
     * 查询当前患者的检查检验报告，并按申请单聚合结果明细。
     *
     * @param patientId 患者ID
     * @return 按申请单聚合的检查检验报告
     */
    @Override
    public List<Map<String, Object>> selectPatientExamReports(Long patientId)
    {
        List<Map<String, Object>> rows = examResultMapper.selectPatientExamReportRows(patientId);
        Map<Long, Map<String, Object>> grouped = new LinkedHashMap<>();

        for (Map<String, Object> row : rows)
        {
            Long applyId = getLong(row, "applyId");
            if (applyId == null)
            {
                continue;
            }

            Map<String, Object> report = grouped.get(applyId);
            if (report == null)
            {
                report = new LinkedHashMap<>();
                report.put("applyId", applyId);
                report.put("registerId", row.get("registerId"));
                report.put("registerNo", row.get("registerNo"));
                report.put("applyType", row.get("applyType"));
                report.put("applyTypeName", resolveApplyTypeName(getString(row, "applyType")));
                report.put("applyInfo", row.get("applyInfo"));
                report.put("applyPosition", row.get("applyPosition"));
                report.put("applyTime", row.get("applyTime"));
                report.put("examTime", row.get("examTime"));
                report.put("applyStatus", row.get("applyStatus"));
                report.put("payStatus", row.get("payStatus"));
                report.put("doctorName", row.get("doctorName"));
                report.put("deptName", row.get("deptName"));
                report.put("techId", row.get("techId"));
                report.put("techCode", row.get("techCode"));
                report.put("techName", row.get("techName"));
                report.put("techPrice", row.get("techPrice"));
                report.put("reportTime", row.get("reportTime"));
                report.put("abnormalCount", 0);
                report.put("results", new ArrayList<Map<String, Object>>());
                grouped.put(applyId, report);
            }

            if (report.get("reportTime") == null && row.get("reportTime") != null)
            {
                report.put("reportTime", row.get("reportTime"));
            }

            Long resultId = getLong(row, "resultId");
            if (resultId == null)
            {
                continue;
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("resultId", resultId);
            item.put("itemType", row.get("itemType"));
            item.put("itemCode", row.get("itemCode"));
            item.put("itemName", row.get("itemName"));
            item.put("resultValue", row.get("resultValue"));
            item.put("resultUnit", row.get("resultUnit"));
            item.put("referenceRange", row.get("referenceRange"));
            item.put("abnormalFlag", row.get("abnormalFlag"));
            item.put("imageUrl", row.get("imageUrl"));
            item.put("imageFind", row.get("imageFind"));
            item.put("diagnosisOpinion", row.get("diagnosisOpinion"));
            item.put("diagnosisResult", row.get("diagnosisResult"));
            item.put("suggestion", row.get("suggestion"));
            item.put("sort", row.get("sort"));
            item.put("reportTime", row.get("reportTime"));
            ((List<Map<String, Object>>) report.get("results")).add(item);

            if (isAbnormal(getString(row, "abnormalFlag")))
            {
                report.put("abnormalCount", ((Integer) report.get("abnormalCount")) + 1);
            }
        }

        for (Map<String, Object> report : grouped.values())
        {
            List<Map<String, Object>> results = (List<Map<String, Object>>) report.get("results");
            report.put("resultCount", results.size());
        }
        return new ArrayList<>(grouped.values());
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

    private Long getLong(Map<String, Object> source, String key)
    {
        Object value = source.get(key);
        if (value == null)
        {
            return null;
        }
        return value instanceof Number ? ((Number) value).longValue() : Long.valueOf(value.toString());
    }

    private boolean isAbnormal(String abnormalFlag)
    {
        return StringUtils.isNotBlank(abnormalFlag) && !"0".equals(abnormalFlag) && !"4".equals(abnormalFlag);
    }

    private String resolveApplyTypeName(String applyType)
    {
        if ("CHECK".equals(applyType))
        {
            return "检查";
        }
        if ("INSPEC".equals(applyType) || "LAB".equals(applyType))
        {
            return "检验";
        }
        if ("DISPOSAL".equals(applyType))
        {
            return "处置";
        }
        return "检查检验";
    }
}
