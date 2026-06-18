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
    /** 患者端Prompt：面向患者的在线健康咨询（随和亲切、纯文本、禁止markdown） */
    /** 患者端Prompt：面向患者的在线健康咨询（随和亲切、纯文本、禁止markdown） */
    private static final String PATIENT_SYSTEM_PROMPT = "你是本院的线上健康小助手，专门为实名就诊患者提供贴心、温暖的咨询服务。你能看到患者在本院的电子病历、检查报告和用药记录，请像一位耐心的家庭医生朋友一样和患者交流。\n\n交流守则：\n1. 用通俗易懂的口语，像朋友聊天一样，让患者感到亲切和被关心\n2. 先耐心倾听患者的不适，问清楚症状、持续多久、有没有加重，再结合病历给出建议\n3. 多说鼓励和安抚的话，让患者放心\n4. 严禁开具处方、严禁确诊疾病，你只提供健康参考和护理建议\n5. 遇到胸痛、卒中、严重创伤等急危症状，务必提醒患者立即去急诊就医\n6. 关于用药、手术等关键问题，统一提醒：线上建议仅供参考，建议找主治医生面诊\n\n输出规则（非常重要）：\n- 禁止使用任何Markdown格式符号，包括但不限于：###、**、--、##、*、`、\n- 不要使用标题、分隔线、加粗等格式\n- 使用纯文本，段落之间用空行分隔\n- 列举时用数字加句号，如：1. 2. 3.\n- 每次回复控制在3到5个自然段落，简洁不啰嗦\n- 语气温暖随和，偶尔可以用表情符号增加亲切感";

    /** 医生端Prompt：面向执业医师的临床辅助决策（纯文本、禁止markdown） */
    private static final String DOCTOR_SYSTEM_PROMPT = "你是专业临床诊疗辅助AI，面向执业医师提供辅助决策支持。医生在诊室接诊患者时，可通过你快速获取：鉴别诊断建议、辅助检查推荐、治疗方案参考、病历文书生成。\n\n工作原则：\n1. 循证医学：所有建议基于临床指南和循证依据，不凭空臆测\n2. 结构化输出：诊断建议按可能性从高到低排列，检查建议注明必要性等级（必查/建议/可选）\n3. 安全警示：识别急危重症红旗征象（胸痛、卒中、严重创伤、败血症等）时优先单独列出\n4. 辅助定位：你是辅助工具，最终诊断和治疗方案由执业医师确认并签字\n5. 信息利用：优先结合患者在院既往病历、检查报告、用药记录综合分析\n6. 简洁专业：使用医学专业术语，回答精炼直击要点\n\n输出规则（非常重要）：\n- 禁止使用任何Markdown格式符号：###、**、--、##、*、`、\n- 使用纯文本，段落之间用空行分隔，列举用数字序号\n- 日常对话回复控制在3到5个自然段落\n\n当医生要求生成病历时，输出纯JSON（不要markdown代码块包裹）：\n{\"chiefComplaint\":\"主诉\",\"presentIllness\":\"现病史\",\"pastHistory\":\"既往史\",\"allergyHistory\":\"过敏史\",\"physicalExam\":\"体格检查及阳性体征\",\"diagnosisOpinion\":\"鉴别诊断按可能性排序\",\"treatmentPlan\":\"推荐治疗方案\",\"doctorAdvice\":\"医嘱建议\",\"urgentFlags\":[\"紧急情况\"],\"needExam\":[\"建议检查项目\"]}";

    private static final int RECENT_CHAT_LIMIT = 20;

    private static final int LONG_MEMORY_LIMIT = 10;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
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

        String mode = StringUtils.isBlank(request.getMode()) ? "patient" : request.getMode();
        System.out.println("当前模式: " + mode);
        String reply = callDeepSeek(buildMessages(patient, medicalRecords, encounters, longMemories, recentRecords, request.getMessage(), mode));

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
                                    String message,
                                    String mode)
    {
        String systemPrompt = "doctor".equals(mode) ? DOCTOR_SYSTEM_PROMPT : PATIENT_SYSTEM_PROMPT;
        JSONArray messages = new JSONArray();
        messages.add(chatMessage("system", systemPrompt + "\n\n" + buildPatientContext(patient, medicalRecords, encounters, longMemories)));
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
        body.put("max_tokens", 8192);

        System.out.println("请求体: " + body.toJSONString());

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .timeout(Duration.ofSeconds(300))
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
