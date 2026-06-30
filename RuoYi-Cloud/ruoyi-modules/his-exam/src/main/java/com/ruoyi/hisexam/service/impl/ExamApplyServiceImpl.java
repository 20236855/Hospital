package com.ruoyi.hisexam.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.exception.ServiceException;
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
     * 查询可用于开立检查/检验/处置申请单的挂号记录
     *
     * @param keyword 挂号编号或患者姓名关键字
     * @param doctorId 门诊医生ID，非门诊医生为空
     * @return 挂号上下文集合
     */
    @Override
    public List<Map<String, Object>> selectExamRegisterOptions(String keyword, Long doctorId)
    {
        return examApplyMapper.selectExamRegisterOptions(keyword, doctorId);
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
        fillContextFromRegister(examApply);
        Date now = DateUtils.getNowDate();
        if (examApply.getApplyTime() == null)
        {
            examApply.setApplyTime(now);
        }
        if (examApply.getApplyStatus() == null)
        {
            examApply.setApplyStatus("待执行");
        }
        if (examApply.getPayStatus() == null)
        {
            examApply.setPayStatus("未支付");
        }
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
        if (examApply.getRegisterId() != null)
        {
            fillContextFromRegister(examApply);
        }
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

    private void fillContextFromRegister(ExamApply examApply)
    {
        if (examApply == null || examApply.getRegisterId() == null)
        {
            throw new ServiceException("请选择挂号单");
        }
        Map<String, Object> context = examApplyMapper.selectExamRegisterContext(examApply.getRegisterId());
        if (context == null)
        {
            throw new ServiceException("挂号单不存在或尚未接诊，不能开立检查检验申请单");
        }
        Long encounterId = toLong(context.get("encounterId"));
        if (encounterId == null)
        {
            throw new ServiceException("该挂号单尚未生成接诊记录，不能开立检查检验申请单");
        }
        examApply.setEncounterId(encounterId);
        examApply.setPatientId(toLong(context.get("patientId")));
        examApply.setDoctorId(toLong(context.get("doctorId")));
        if (examApply.getDeptId() == null)
        {
            examApply.setDeptId(toLong(context.get("deptId")));
        }
    }

    private Long toLong(Object value)
    {
        if (value == null)
        {
            return null;
        }
        return value instanceof Number ? ((Number) value).longValue() : Long.valueOf(value.toString());
    }
}
