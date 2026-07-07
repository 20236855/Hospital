package com.ruoyi.emr.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.emr.domain.AiChatRecord;

/**
 * AI问诊记忆Mapper接口
 */
public interface AiChatRecordMapper
{
    public int insertAiChatRecord(AiChatRecord aiChatRecord);

    public List<AiChatRecord> selectRecentRecords(@Param("patientId") Long patientId,
                                                  @Param("sessionId") String sessionId,
                                                  @Param("limit") Integer limit);

    public List<AiChatRecord> selectLongTermMemories(@Param("patientId") Long patientId,
                                                     @Param("limit") Integer limit);
    
    public List<String> selectDistinctSessionByPatientId(@Param("patientId") Long patientId);

    public List<Map<String, Object>> selectRecentExamResults(@Param("patientId") Long patientId,
                                                             @Param("limit") Integer limit);

    public List<Map<String, Object>> selectRegisterDeptList();

    public List<Map<String, Object>> selectOpenAppointmentSlots(@Param("deptId") Long deptId,
                                                                @Param("scheduleDate") String scheduleDate,
                                                                @Param("doctorName") String doctorName,
                                                                @Param("startTime") String startTime,
                                                                @Param("endTime") String endTime,
                                                                @Param("roleId") Long roleId);
}
