package com.ruoyi.hisexam.service;

import java.util.List;
import java.util.Map;
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
     * 查询可用于开立检查/检验/处置申请单的挂号记录
     * 
     * @param keyword 挂号编号或患者姓名关键字
     * @param doctorId 门诊医生ID，非门诊医生为空
     * @return 挂号上下文集合
     */
    public List<Map<String, Object>> selectExamRegisterOptions(String keyword, Long doctorId);

    /**
     * 查询患者端检查检验缴费项。
     *
     * @param patientId 患者ID
     * @return 检查检验申请及项目费用
     */
    public List<Map<String, Object>> selectPatientExamPaymentItems(Long patientId);

    /**
     * 查询某挂号单未缴费检查检验支付信息。
     *
     * @param registerId 挂号ID
     * @return 支付信息
     */
    public Map<String, Object> selectExamPayInfoByRegisterId(Long registerId);

    /**
     * 标记某挂号单下检查检验申请为已缴费。
     *
     * @param registerId 挂号ID
     * @return 是否成功
     */
    public boolean markRegisterExamPaid(Long registerId);

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
