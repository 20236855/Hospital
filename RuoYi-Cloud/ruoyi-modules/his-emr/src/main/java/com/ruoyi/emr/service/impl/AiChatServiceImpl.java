package com.ruoyi.emr.service.impl;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.emr.domain.AiChatRecord;
import com.ruoyi.emr.domain.AiChatRequest;
import com.ruoyi.emr.domain.AiChatResponse;
import com.ruoyi.emr.domain.Encounter;
import com.ruoyi.emr.domain.MedicalRecord;
import com.ruoyi.emr.domain.vo.EncounterVo;
import com.ruoyi.emr.mapper.AiChatRecordMapper;
import com.ruoyi.emr.service.IAiChatService;
import com.ruoyi.emr.service.IEncounterService;
import com.ruoyi.emr.service.IMedicalRecordService;
import com.ruoyi.his.api.RemotePatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * AI问诊服务业务层处理
 */
@Service
public class AiChatServiceImpl implements IAiChatService
{
    private static final String SYSTEM_PROMPT = "你是专业医院线上智能问诊助手，仅为本平台实名就诊患者提供健康咨询服务，可调取当前登录患者在本院留存的个人电子病历、既往就诊记录、检查检验报告单、用药记录。沟通全程使用通俗温和的口语，避免晦涩专业术语；问诊优先结合患者既往病历信息，先询问不适症状、发病时长、体感细节，再依托病历历史病史综合分析。严禁开具处方、严禁确诊疾病、不替代执业医师临床诊断，所有医疗建议仅作健康参考，出现危重、急症、持续加重症状时，主动提示患者前往线下医院面诊就医。对话界面为左右分栏聊天样式，简洁应答，不冗余啰嗦。对话交互约束补充：患者描述身体不适后，优先核对电子病历里过往相关病史、过敏史、既往确诊病症，结合现有病情给出居家护理、日常注意事项；若病历无相关就诊数据，如实告知暂无既往记录，依据患者口述症状科普相关健康常识；涉及用药、手术、确诊类关键医疗内容，统一提醒：线上 AI 不能做诊疗定论，建议线下找主治医生面诊。";

    private static final int RECENT_CHAT_LIMIT = 20;

    private static final int LONG_MEMORY_LIMIT = 10;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${ai.deepseek.api-key:${DEEPSEEK_API_KEY:}}")
    private String apiKey;

    @Value("${ai.deepseek.api-url:https://api.deepseek.com/chat/completions}")
    private String apiUrl;

    @Value("${ai.deepseek.model:deepseek-chat}")
    private String model;

    @Autowired
    private AiChatRecordMapper aiChatRecordMapper;

    @Autowired
    private IMedicalRecordService medicalRecordService;

    @Autowired
    private IEncounterService encounterService;

    @Autowired
    private RemotePatientService remotePatientService;

    /**
     * 判断是否需要查询病历
     */
    private boolean shouldQueryMedicalRecord(String message)
    {
        if (StringUtils.isBlank(message))
        {
            return false;
        }
        String[] keywords = {
            "病历", "就诊", "医生叮嘱", "之前看病", "咳嗽", "头晕", 
            "我的病史", "检查记录", "诊断", "医嘱", "病史", "看病记录",
            "诊断记录", "就诊记录", "检查结果", "之前医生说",
            "上次看病", "之前的病历", "我的检查"
        };
        for (String keyword : keywords)
        {
            if (message.contains(keyword))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public AiChatResponse consult(AiChatRequest request)
    {
        System.out.println("=== AI 咨询开始 ===");
        System.out.println("请求消息: " + request.getMessage());
        System.out.println("配置的 API Key: " + (StringUtils.isBlank(apiKey) ? "空" : "已配置 (长度: " + apiKey.length() + ")"));
        
        if (request == null || StringUtils.isBlank(request.getMessage()))
        {
            throw new ServiceException("请输入问诊内容");
        }
        
        if (StringUtils.isBlank(apiKey))
        {
            System.out.println("使用硬编码的 API Key");
            apiKey = "sk-8fe9cdaddd16459b983edb53116ff773";
        }
        
        System.out.println("最终使用的 API Key: " + (StringUtils.isBlank(apiKey) ? "空" : "已配置"));

        Long loginUserId = SecurityUtils.getUserId();
        Map<String, Object> patient = getCurrentPatient(loginUserId, request.getPatientId());
        Long patientId = toLong(patient.get("patientId"));
        Long userId = toLong(patient.get("userId")); // 直接使用 patient 表里的 userId
        if (patientId == null)
        {
            throw new ServiceException("当前账号未绑定患者信息，请先完善患者资料");
        }

        String sessionId = StringUtils.isBlank(request.getSessionId()) ? newSessionId() : request.getSessionId();
        
        // 加载聊天记录
        List<AiChatRecord> recentRecords = aiChatRecordMapper.selectRecentRecords(patientId, sessionId, RECENT_CHAT_LIMIT);
        List<AiChatRecord> longMemories = aiChatRecordMapper.selectLongTermMemories(patientId, LONG_MEMORY_LIMIT);
        System.out.println("加载聊天历史 - recentRecords: " + recentRecords.size() + ", longMemories: " + longMemories.size());
        
        // 关键词拦截：判断是否需要查询病历
        List<MedicalRecord> medicalRecords = new java.util.ArrayList<>();
        List<EncounterVo> encounters = new java.util.ArrayList<>();
        
        if (shouldQueryMedicalRecord(request.getMessage())) {
            System.out.println("命中关键词，查询病历...");
            medicalRecords = selectPatientMedicalRecords(patientId);
            // encounters = selectPatientEncounters(patientId); // 暂时不查询就诊记录，避免日期转换错误
            
            // 只取最新的两条病历
            if (medicalRecords != null && medicalRecords.size() > 2) {
                medicalRecords = medicalRecords.subList(medicalRecords.size() - 2, medicalRecords.size());
            }
            
            System.out.println("查询到病历数量: " + (medicalRecords != null ? medicalRecords.size() : 0));
            // System.out.println("查询到就诊记录数量: " + (encounters != null ? encounters.size() : 0));
        } else {
            System.out.println("未命中关键词，跳过病历查询");
        }

        String reply = callDeepSeek(buildMessages(patient, medicalRecords, encounters, longMemories, recentRecords, request.getMessage()));

        // 保存聊天记录
        System.out.println("保存聊天记录");
        insertRecord(userId, patientId, sessionId, "user", request.getMessage(), "short");
        if (shouldPersistLongMemory(request.getMessage()))
        {
            insertRecord(userId, patientId, sessionId, "user", "用户曾提到：" + limit(request.getMessage(), 500), "long");
        }
        insertRecord(userId, patientId, sessionId, "assistant", reply, "short");

        AiChatResponse response = new AiChatResponse();
        response.setSessionId(sessionId);
        response.setReply(reply);
        response.setPatientId(patientId);
        response.setHasMedicalRecord(medicalRecords != null && !medicalRecords.isEmpty());
        return response;
    }

    @Override
    public List<AiChatRecord> selectCurrentPatientHistory(String sessionId, Long requestPatientId)
    {
        Long loginUserId = SecurityUtils.getUserId();
        Map<String, Object> patient = getCurrentPatient(loginUserId, requestPatientId);
        Long patientId = toLong(patient.get("patientId"));
        if (patientId == null)
        {
            throw new ServiceException("当前账号未绑定患者信息，请先完善患者资料");
        }
        System.out.println("加载聊天历史 - patientId: " + patientId + ", sessionId: " + sessionId);
        return aiChatRecordMapper.selectRecentRecords(patientId, sessionId, 50);
    }
    
    @Override
    public List<String> getSessionList(Long requestPatientId)
    {
        Long loginUserId = SecurityUtils.getUserId();
        Map<String, Object> patient = getCurrentPatient(loginUserId, requestPatientId);
        Long patientId = toLong(patient.get("patientId"));
        if (patientId == null)
        {
            throw new ServiceException("当前账号未绑定患者信息，请先完善患者资料");
        }
        System.out.println("加载会话列表 - patientId: " + patientId);
        return aiChatRecordMapper.selectDistinctSessionByPatientId(patientId);
    }

    private JSONArray buildMessages(Map<String, Object> patient,
                                    List<MedicalRecord> medicalRecords,
                                    List<EncounterVo> encounters,
                                    List<AiChatRecord> longMemories,
                                    List<AiChatRecord> recentRecords,
                                    String message)
    {
        JSONArray messages = new JSONArray();
        messages.add(chatMessage("system", SYSTEM_PROMPT + "\n\n" + buildPatientContext(patient, medicalRecords, encounters, longMemories)));
        for (AiChatRecord record : recentRecords)
        {
            if (StringUtils.isNotEmpty(record.getRole()) && StringUtils.isNotEmpty(record.getContent()))
            {
                messages.add(chatMessage(record.getRole(), record.getContent()));
            }
        }
        messages.add(chatMessage("user", message));
        return messages;
    }

    private String buildPatientContext(Map<String, Object> patient,
                                       List<MedicalRecord> medicalRecords,
                                       List<EncounterVo> encounters,
                                       List<AiChatRecord> longMemories)
    {
        StringBuilder context = new StringBuilder();
        context.append("当前登录患者信息:\n");
        appendLine(context, "姓名", patient.get("name"));
        appendLine(context, "性别", patient.get("gender"));
        appendLine(context, "年龄", patient.get("age"));
        appendLine(context, "血型", patient.get("bloodType"));
        appendLine(context, "过敏史", patient.get("allergyHistory"));
        appendLine(context, "既往史", patient.get("pastHistory"));

        if (longMemories != null && !longMemories.isEmpty())
        {
            context.append("\n长期记忆:\n");
            for (AiChatRecord memory : longMemories)
            {
                context.append("- ").append(limit(memory.getContent(), 300)).append("\n");
            }
        }

        if (medicalRecords != null && !medicalRecords.isEmpty())
        {
            context.append("\n电子病历:\n");
            int index = 1;
            for (MedicalRecord record : medicalRecords)
            {
                context.append(index++).append(". ")
                        .append("病历ID:").append(record.getRecordId()).append(", ")
                        // .append("时间:").append(formatDate(record.getCreateTime())).append(", ") // 暂时不处理时间，避免类型转换错误
                        .append("时间:").append("无记录").append(", ")
                        .append("主诉:").append(emptyText(record.getChiefComplaint())).append(", ")
                        .append("现病史:").append(emptyText(record.getPresentIllness())).append(", ")
                        .append("既往史:").append(emptyText(record.getPastHistory())).append(", ")
                        .append("过敏史:").append(emptyText(record.getAllergyHistory())).append(", ")
                        .append("诊断意见:").append(emptyText(record.getDiagnosisOpinion())).append(", ")
                        .append("治疗计划:").append(emptyText(record.getTreatmentPlan())).append(", ")
                        .append("医嘱:").append(emptyText(record.getDoctorAdvice())).append("\n");
            }
        }
        else
        {
            context.append("\n电子病历: 暂无既往病历记录。\n");
        }

        if (encounters != null && !encounters.isEmpty())
        {
            context.append("\n就诊记录:\n");
            int index = 1;
            for (EncounterVo encounter : encounters)
            {
                context.append(index++).append(". ")
                        .append("接诊ID:").append(encounter.getEncounterId()).append(", ")
                        .append("时间:").append(formatDate(encounter.getVisitTime())).append(", ")
                        .append("类型:").append(emptyText(encounter.getEncounterType())).append(", ")
                        .append("状态:").append(emptyText(encounter.getEncounterStatus())).append(", ")
                        .append("科室ID:").append(encounter.getDeptId()).append("\n");
            }
        }

        return limit(context.toString(), 8000);
    }
    
    private String formatDate(Object dateObj)
    {
        if (dateObj == null)
        {
            return "无记录";
        }
        try
        {
            if (dateObj instanceof java.util.Date)
            {
                return DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, (java.util.Date) dateObj);
            }
            else
            {
                return String.valueOf(dateObj);
            }
        }
        catch (Exception e)
        {
            System.err.println("日期格式化失败: " + e.getMessage());
            return "无记录";
        }
    }

    private String callDeepSeek(JSONArray messages)
    {
        System.out.println("=== 调用 DeepSeek API ===");
        System.out.println("API URL: " + apiUrl);
        System.out.println("Model: " + model);
        System.out.println("Messages 数量: " + messages.size());
        
        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("messages", messages);
        body.put("stream", false);
        body.put("temperature", 0.7);

        System.out.println("请求体: " + body.toJSONString());

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .timeout(Duration.ofSeconds(120))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString()))
                .build();

        int maxRetries = 3;
        int retryCount = 0;
        ServiceException lastException = null;
        
        while (retryCount < maxRetries)
        {
            try
            {
                if (retryCount > 0)
                {
                    System.out.println("正在进行第 " + (retryCount + 1) + " 次重试...");
                    Thread.sleep(2000);
                }
                
                System.out.println("正在发送请求...");
                HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                System.out.println("响应状态码: " + response.statusCode());
                System.out.println("响应体: " + response.body());
                
                if (response.statusCode() < 200 || response.statusCode() >= 300)
                {
                    lastException = new ServiceException("AI服务调用失败，状态码：" + response.statusCode() + "，响应：" + response.body());
                    
                    if (response.statusCode() == 429 || response.statusCode() >= 500)
                    {
                        retryCount++;
                        continue;
                    }
                    
                    throw lastException;
                }
                
                JSONObject data = JSON.parseObject(response.body());
                JSONArray choices = data.getJSONArray("choices");
                if (choices == null || choices.isEmpty())
                {
                    throw new ServiceException("AI服务返回内容为空");
                }
                JSONObject message = choices.getJSONObject(0).getJSONObject("message");
                String content = message == null ? null : message.getString("content");
                if (StringUtils.isBlank(content))
                {
                    throw new ServiceException("AI服务返回内容为空");
                }
                System.out.println("AI 返回内容: " + content);
                return content;
            }
            catch (IOException e)
            {
                System.err.println("IO 异常: " + e.getMessage());
                e.printStackTrace();
                lastException = new ServiceException("AI服务网络异常").setDetailMessage(e.getMessage());
                retryCount++;
            }
            catch (InterruptedException e)
            {
                System.err.println("调用被中断: " + e.getMessage());
                Thread.currentThread().interrupt();
                throw new ServiceException("AI服务调用被中断").setDetailMessage(e.getMessage());
            }
        }
        
        System.err.println("已达到最大重试次数 " + maxRetries);
        String errorDetail = lastException != null ? lastException.getMessage() : "";
        throw new ServiceException("AI服务调用失败，已重试 " + maxRetries + " 次").setDetailMessage(errorDetail);
    }

    private List<MedicalRecord> selectPatientMedicalRecords(Long patientId)
    {
        MedicalRecord query = new MedicalRecord();
        query.setPatientId(patientId);
        return medicalRecordService.selectMedicalRecordList(query);
    }

    private List<EncounterVo> selectPatientEncounters(Long patientId)
    {
        Encounter query = new Encounter();
        query.setPatientId(patientId);
        return encounterService.selectEncounterList(query);
    }

    private Map<String, Object> getCurrentPatient(Long userId)
    {
        return getCurrentPatient(userId, null);
    }

    private Map<String, Object> getCurrentPatient(Long userId, Long requestPatientId) {
        System.out.println("=== 获取当前患者信息 ===");
        System.out.println("当前登录 userId: " + userId);
        System.out.println("请求的 patientId: " + requestPatientId);
        
        if (userId == null) {
            throw new ServiceException("登录状态已失效，请重新登录");
        }
        
        R<Map<String, Object>> result = null;
        
        // 1. 先尝试通过当前登录用户的 userId 查找患者
        result = remotePatientService.getPatientByUserId(userId, SecurityConstants.INNER);
        System.out.println("通过 userId 查找结果: " + (result != null && result.getData() != null ? "找到" : "未找到"));
        
        // 2. 如果没找到，尝试用前端传的 patientId
        if ((result == null || result.getData() == null) && requestPatientId != null) {
            result = remotePatientService.getPatientInfo(requestPatientId, SecurityConstants.INNER);
            System.out.println("通过 requestPatientId 查找结果: " + (result != null && result.getData() != null ? "找到" : "未找到"));
        }
        
        // 3. 如果都没找到，抛出异常
        if (result == null || result.getData() == null) {
            throw new ServiceException("当前账号未绑定患者信息，请先完善患者资料");
        }
        
        Map<String, Object> patient = result.getData();
        System.out.println("找到的患者信息: " + patient);
        
        // 获取详细信息
        Long patientId = toLong(patient.get("patientId"));
        if (patientId != null) {
            R<Map<String, Object>> detail = remotePatientService.getPatientInfo(patientId, SecurityConstants.INNER);
            if (detail != null && detail.getData() != null) {
                patient.putAll(detail.getData());
            }
        }
        return patient;
    }

    private void insertRecord(Long userId, Long patientId, String sessionId, String role, String content, String memoryType)
    {
        AiChatRecord record = new AiChatRecord();
        record.setUserId(userId);
        record.setPatientId(patientId);
        record.setSessionId(sessionId);
        record.setRole(role);
        record.setContent(content);
        record.setMemoryType(memoryType);
        record.setCreateTime(DateUtils.getNowDate());
        aiChatRecordMapper.insertAiChatRecord(record);
    }

    private JSONObject chatMessage(String role, String content)
    {
        JSONObject message = new JSONObject();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private void appendLine(StringBuilder context, String label, Object value)
    {
        if (value != null && StringUtils.isNotEmpty(String.valueOf(value)))
        {
            context.append(label).append(": ").append(value).append("\n");
        }
    }

    private String emptyText(String value)
    {
        return StringUtils.isBlank(value) ? "无记录" : limit(value, 300);
    }

    private String limit(String value, int maxLength)
    {
        if (value == null || value.length() <= maxLength)
        {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }

    private Long toLong(Object value)
    {
        if (value == null)
        {
            return null;
        }
        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }
        try
        {
            return Long.valueOf(String.valueOf(value));
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    private String newSessionId()
    {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private boolean shouldPersistLongMemory(String message)
    {
        if (StringUtils.isBlank(message))
        {
            return false;
        }
        String[] keywords = { "过敏", "既往", "病史", "慢性", "长期", "常年", "手术", "住院", "服用", "用药", "怀孕", "孕期", "哺乳", "高血压", "糖尿病" };
        for (String keyword : keywords)
        {
            if (message.contains(keyword))
            {
                return true;
            }
        }
        return false;
    }
}
