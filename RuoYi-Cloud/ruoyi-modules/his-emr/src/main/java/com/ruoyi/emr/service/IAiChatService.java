package com.ruoyi.emr.service;

import java.util.List;
import com.ruoyi.emr.domain.AiChatRecord;
import com.ruoyi.emr.domain.AiChatRequest;
import com.ruoyi.emr.domain.AiChatResponse;

/**
 * AI问诊服务接口
 */
public interface IAiChatService
{
    public AiChatResponse consult(AiChatRequest request);

    public List<AiChatRecord> selectCurrentPatientHistory(String sessionId, Long patientId);
    
    public List<String> getSessionList(Long patientId);
}
