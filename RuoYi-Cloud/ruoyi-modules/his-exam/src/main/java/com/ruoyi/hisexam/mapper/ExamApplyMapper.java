package com.ruoyi.hisexam.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.hisexam.domain.ExamApply;

/**
 * 检查/检验/处置申请单Mapper接口
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
public interface ExamApplyMapper 
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
     * 查询可用于开立检查/检验/处置申请单的挂号记录。
     *
     * @param keyword 挂号编号或患者姓名关键字
     * @param doctorId 门诊医生ID，非门诊医生为空
     * @return 挂号、接诊、患者、医生、科室信息
     */
    public List<Map<String, Object>> selectExamRegisterOptions(@Param("keyword") String keyword, @Param("doctorId") Long doctorId);

    /**
     * 根据挂号ID查询开单上下文。
     *
     * @param registerId 挂号ID
     * @return 挂号、接诊、患者、医生、科室信息
     */
    public Map<String, Object> selectExamRegisterContext(@Param("registerId") Long registerId);

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
     * 删除检查/检验/处置申请单
     * 
     * @param applyId 检查/检验/处置申请单主键
     * @return 结果
     */
    public int deleteExamApplyByApplyId(Long applyId);

    /**
     * 批量删除检查/检验/处置申请单
     * 
     * @param applyIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteExamApplyByApplyIds(Long[] applyIds);
}
