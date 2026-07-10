package com.ruoyi.emr.service.impl;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import com.ruoyi.emr.service.ChromaRetriever;
import com.ruoyi.his.api.RemotePatientService;
import com.ruoyi.his.api.RemoteRegisterService;
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

    private static final int EXAM_CONTEXT_LIMIT = 30;

    private static final Long OUTPATIENT_DOCTOR_ROLE_ID = 5L;

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    @Value("${ai.deepseek.api-key:${DEEPSEEK_API_KEY:}}")
    private String apiKey;

    @Value("${ai.deepseek.api-key-file:DeepSeekApiKey.txt}")
    private String apiKeyFile;

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

    @Autowired
    private RemoteRegisterService remoteRegisterService;

    @Autowired
    private ChromaRetriever chromaRetriever;

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

    /**
     * 判断问题是否属于医院资源/政策类，需要走 RAG 知识库检索。
     * 命中个人病历/检查记录类问法时不进入 RAG，仍走原有病历逻辑。
     */
    private boolean shouldUseRag(String message)
    {
        if (StringUtils.isBlank(message))
        {
            return false;
        }
        // 个人病历/检查记录类问法不进入 RAG，避免与个人病历查询冲突
        String[] personal = { "我的", "病历", "记录", "上次", "之前", "历史", "我做过", "检查结果", "我的检查" };
        for (String p : personal)
        {
            if (message.contains(p))
            {
                return false;
            }
        }
        String[] kb = {
            "医院", "云医智联", "地址", "在哪", "位置", "院长", "副院长",
            "检查项目", "做哪些检查", "医院检查", "能做什么检查", "有哪些检查",
            "CT", "皮肤病变", "肺部", "颅脑",
            "报告", "多久出", "几天出", "报告查询", "报告怎么",
            "政策", "三年行动", "医疗质量", "医保", "报销", "隐私条款", "免责"
        };
        for (String k : kb)
        {
            if (message.contains(k))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 构造 RAG 场景的对话消息：把知识库检索结果拼进 system prompt。
     */
    private JSONArray buildRagMessages(String knowledge,
                                       List<AiChatRecord> longMemories,
                                       List<AiChatRecord> recentRecords,
                                       String message,
                                       String mode)
    {
        String systemPrompt = "doctor".equals(mode) ? DOCTOR_SYSTEM_PROMPT : PATIENT_SYSTEM_PROMPT;
        JSONArray messages = new JSONArray();
        StringBuilder ctx = new StringBuilder();
        if (StringUtils.isNotEmpty(knowledge))
        {
            ctx.append("【医院知识库检索结果】以下为本院官方信息，请仅依据这些内容回答；若知识库未覆盖，请如实说明无法回答并建议咨询院方：\n")
                    .append(knowledge).append("\n");
        }
        if (longMemories != null && !longMemories.isEmpty())
        {
            ctx.append("\n长期记忆:\n");
            for (AiChatRecord memory : longMemories)
            {
                ctx.append("- ").append(limit(memory.getContent(), 300)).append("\n");
            }
        }
        messages.add(chatMessage("system", systemPrompt + "\n\n" + ctx.toString()));
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

    @Override
    public AiChatResponse consult(AiChatRequest request)
    {
        System.out.println("=== AI 咨询开始 ===");
        if (request == null || StringUtils.isBlank(request.getMessage()))
        {
            throw new ServiceException("请输入问诊内容");
        }
        System.out.println("请求消息: " + request.getMessage());

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

        String agentReply = handleRegistrationAgent(request.getMessage(), patientId, recentRecords);
        if (!StringUtils.isBlank(agentReply))
        {
            insertRecord(userId, patientId, sessionId, "user", request.getMessage(), "short");
            insertRecord(userId, patientId, sessionId, "assistant", agentReply, "short");
            return buildChatResponse(sessionId, agentReply, patientId, false);
        }

        System.out.println("配置的 API Key: " + (StringUtils.isBlank(apiKey) ? "空" : "已配置 (长度: " + apiKey.length() + ")"));
        if (StringUtils.isBlank(apiKey))
        {
            apiKey = resolveApiKeyFromFile();
        }
        System.out.println("最终使用的 API Key: " + (StringUtils.isBlank(apiKey) ? "空" : "已配置"));

        // RAG 分支：仅当问题属于医院资源/政策类时，检索 Chroma 知识库后作答
        if (shouldUseRag(request.getMessage()))
        {
            System.out.println("命中医院知识库 RAG 分支");
            String knowledge = chromaRetriever.retrieve(request.getMessage(), 8);
            // 检索无结果（知识库不可达或未命中）时，给出可控兜底，避免模型凭空编造
            if (StringUtils.isBlank(knowledge))
            {
                String fallback = "抱歉，我暂时无法连接医院知识库来获取这类政策或服务信息。"
                        + "您可以稍后重试，或前往院务公开栏 / 官网查看最新介绍。";
                insertRecord(userId, patientId, sessionId, "user", request.getMessage(), "short");
                insertRecord(userId, patientId, sessionId, "assistant", fallback, "short");
                return buildChatResponse(sessionId, fallback, patientId, false);
            }
            String mode = StringUtils.isBlank(request.getMode()) ? "patient" : request.getMode();
            String reply = callDeepSeek(buildRagMessages(knowledge, longMemories, recentRecords, request.getMessage(), mode));
            insertRecord(userId, patientId, sessionId, "user", request.getMessage(), "short");
            insertRecord(userId, patientId, sessionId, "assistant", reply, "short");
            return buildChatResponse(sessionId, reply, patientId, false);
        }

        // 关键词拦截：判断是否需要查询病历
        List<MedicalRecord> medicalRecords = new java.util.ArrayList<>();
        List<EncounterVo> encounters = new java.util.ArrayList<>();
        List<Map<String, Object>> examResults = new java.util.ArrayList<>();
        
        if (shouldQueryMedicalRecord(request.getMessage())) {
            System.out.println("命中关键词，查询病历...");
            medicalRecords = selectPatientMedicalRecords(patientId);
            examResults = aiChatRecordMapper.selectRecentExamResults(patientId, EXAM_CONTEXT_LIMIT);
            // encounters = selectPatientEncounters(patientId); // 暂时不查询就诊记录，避免日期转换错误
            
            // 只取最新的两条病历
            if (medicalRecords != null && medicalRecords.size() > 2) {
                medicalRecords = medicalRecords.subList(medicalRecords.size() - 2, medicalRecords.size());
            }
            
            System.out.println("查询到病历数量: " + (medicalRecords != null ? medicalRecords.size() : 0));
            System.out.println("查询到检查检验结果数量: " + (examResults != null ? examResults.size() : 0));
            // System.out.println("查询到就诊记录数量: " + (encounters != null ? encounters.size() : 0));
        } else {
            System.out.println("未命中关键词，跳过病历查询");
        }

        String mode = StringUtils.isBlank(request.getMode()) ? "patient" : request.getMode();
        System.out.println("当前模式: " + mode);
        String reply = callDeepSeek(buildMessages(patient, medicalRecords, encounters, examResults, longMemories, recentRecords, request.getMessage(), mode));

        // 保存聊天记录
        System.out.println("保存聊天记录");
        insertRecord(userId, patientId, sessionId, "user", request.getMessage(), "short");
        if (shouldPersistLongMemory(request.getMessage()))
        {
            insertRecord(userId, patientId, sessionId, "user", "用户曾提到：" + limit(request.getMessage(), 500), "long");
        }
        insertRecord(userId, patientId, sessionId, "assistant", reply, "short");

        return buildChatResponse(sessionId, reply, patientId, medicalRecords != null && !medicalRecords.isEmpty());
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

    private String handleRegistrationAgent(String message, Long patientId, List<AiChatRecord> recentRecords)
    {
        if (!isRegistrationIntent(message))
        {
            return null;
        }

        String contextText = buildRegistrationContext(message, recentRecords);
        // 日期只从当前消息解析，避免历史消息里的"今天"等关键词干扰
        LocalDate date = parseAppointmentDate(message);
        Map<String, Object> dept = resolveDept(contextText);
        TimeRange range = parseTimeRange(message);
        boolean booking = isBookingCommand(message);

        if (date == null)
        {
            return "可以的。请告诉我想挂哪一天，例如：7月8号皮肤科有哪些可挂号医生。";
        }
        if (dept == null)
        {
            return "可以帮你查号源。请告诉我想挂哪个科室，例如：皮肤科、内科、外科。";
        }

        Long deptId = toLong(dept.get("deptId"));
        String deptName = String.valueOf(dept.get("deptName"));
        String dateText = date.format(DATE_FORMATTER);

        if (booking)
        {
            if (range == null)
            {
                return "我可以帮你挂号。请再说一下具体时间段，例如：7月8号上午8点到9点半。";
            }
            String doctorName = resolveDoctorName(message, deptId, dateText);
            List<Map<String, Object>> slots = aiChatRecordMapper.selectOpenAppointmentSlots(
                    deptId, dateText, doctorName, range.startTime, range.endTime, OUTPATIENT_DOCTOR_ROLE_ID);
            if (slots == null || slots.isEmpty())
            {
                return "没有查到" + deptName + dateText + " " + range.startTime + "-" + range.endTime + "可预约的号源。你可以换一个时间段，我再帮你查。";
            }

            Map<Long, List<Map<String, Object>>> byDoctor = groupSlotsByDoctor(slots);
            if (byDoctor.size() > 1 && StringUtils.isBlank(doctorName))
            {
                return "这个时间段有多位医生可以预约，请再指定医生姓名：\n" + formatDoctorSlotGroups(byDoctor);
            }

            Map<String, Object> slot = slots.get(0);
            Map<String, Object> payload = new HashMap<>();
            payload.put("patientId", patientId);
            payload.put("scheduleId", slot.get("scheduleId"));
            payload.put("slotId", slot.get("slotId"));
            payload.put("doctorId", slot.get("doctorId"));
            payload.put("deptId", slot.get("deptId"));
            payload.put("levelId", slot.get("levelId"));

            R<Map<String, Object>> result = remoteRegisterService.createAgentRegister(payload, SecurityConstants.INNER);
            if (result == null || R.isError(result))
            {
                String msg = result == null ? "挂号服务暂时不可用" : result.getMsg();
                return "挂号没有成功：" + msg + "。你可以稍后再试，或换一个号源。";
            }
            return formatRegisterSuccess(result.getData(), dateText);
        }

        List<Map<String, Object>> slots = aiChatRecordMapper.selectOpenAppointmentSlots(
                deptId, dateText, null, null, null, OUTPATIENT_DOCTOR_ROLE_ID);
        if (slots == null || slots.isEmpty())
        {
            return "目前没有查到" + deptName + dateText + "可预约的医生。你可以换一天，我再帮你查。";
        }
        return "查到了，" + deptName + dateText + "可以预约的医生有：\n" + formatDoctorSlotGroups(groupSlotsByDoctor(slots))
                + "\n如果要挂号，可以直接说：我要挂某月某日上午或下午某时间段的某某医生。";
    }

    private boolean isRegistrationIntent(String message)
    {
        if (StringUtils.isBlank(message))
        {
            return false;
        }
        // 覆盖“想挂个号 / 挂个号 / 去挂号 / 约个号 / 看医生 / 就诊”等自然说法
        String[] keywords = { "挂号", "挂个号", "挂门诊", "预约", "号源", "排班", "可挂",
                "医生有哪些", "帮我挂", "我要挂", "想挂", "去挂", "约个号", "看医生", "就诊" };
        for (String keyword : keywords)
        {
            if (message.contains(keyword))
            {
                return true;
            }
        }
        return false;
    }

    private boolean isBookingCommand(String message)
    {
        if (StringUtils.isBlank(message))
        {
            return false;
        }
        boolean action = message.contains("我要挂") || message.contains("帮我挂") || message.contains("预约")
                || message.contains("挂这个") || message.contains("就挂") || message.contains("挂");
        boolean query = message.contains("哪些") || message.contains("有什么") || message.contains("查询")
                || message.contains("查一下") || message.contains("列出") || message.contains("可以挂号的医生");
        return action && !query;
    }

    private String buildRegistrationContext(String message, List<AiChatRecord> recentRecords)
    {
        StringBuilder text = new StringBuilder(message);
        if (recentRecords != null)
        {
            for (int i = recentRecords.size() - 1; i >= 0; i--)
            {
                AiChatRecord record = recentRecords.get(i);
                if (record != null && "user".equals(record.getRole()) && StringUtils.isNotEmpty(record.getContent()))
                {
                    text.append(" ").append(record.getContent());
                }
            }
        }
        return text.toString();
    }

    private LocalDate parseAppointmentDate(String text)
    {
        String normalized = normalizeCnNumber(text);
        LocalDate today = LocalDate.now(ZONE);
        if (normalized.contains("后天"))
        {
            return today.plusDays(2);
        }
        if (normalized.contains("明天"))
        {
            return today.plusDays(1);
        }
        if (normalized.contains("今天"))
        {
            return today;
        }

        Matcher full = Pattern.compile("(20\\d{2})[年/-](\\d{1,2})[月/-](\\d{1,2})").matcher(normalized);
        if (full.find())
        {
            return LocalDate.of(Integer.parseInt(full.group(1)), Integer.parseInt(full.group(2)), Integer.parseInt(full.group(3)));
        }
        Matcher monthDay = Pattern.compile("(\\d{1,2})月(\\d{1,2})[日号]?").matcher(normalized);
        if (monthDay.find())
        {
            LocalDate date = LocalDate.of(today.getYear(), Integer.parseInt(monthDay.group(1)), Integer.parseInt(monthDay.group(2)));
            if (date.isBefore(today.minusDays(1)))
            {
                date = date.plusYears(1);
            }
            return date;
        }
        return null;
    }

    private Map<String, Object> resolveDept(String text)
    {
        List<Map<String, Object>> depts = aiChatRecordMapper.selectRegisterDeptList();
        if (depts == null)
        {
            return null;
        }
        for (Map<String, Object> dept : depts)
        {
            String deptName = String.valueOf(dept.get("deptName"));
            String shortName = deptName.replace("科", "");
            if (text.contains(deptName) || (!shortName.equals(deptName) && text.contains(shortName)))
            {
                return dept;
            }
        }
        return null;
    }

    private String resolveDoctorName(String message, Long deptId, String dateText)
    {
        List<Map<String, Object>> slots = aiChatRecordMapper.selectOpenAppointmentSlots(
                deptId, dateText, null, null, null, OUTPATIENT_DOCTOR_ROLE_ID);
        if (slots == null)
        {
            return null;
        }
        for (Map<String, Object> slot : slots)
        {
            String doctorName = String.valueOf(slot.get("doctorName"));
            if (StringUtils.isNotEmpty(doctorName) && message.contains(doctorName))
            {
                return doctorName;
            }
        }
        return null;
    }

    private TimeRange parseTimeRange(String text)
    {
        String normalized = normalizeCnNumber(text);
        Matcher rangeMatcher = Pattern.compile("(\\d{1,2})(?:[:点时])?([0-5]\\d|半)?\\s*(?:到|至|-|~|—)\\s*(\\d{1,2})(?:[:点时])?([0-5]\\d|半)?").matcher(normalized);
        if (rangeMatcher.find())
        {
            int startHour = adjustHour(Integer.parseInt(rangeMatcher.group(1)), normalized);
            int endHour = adjustHour(Integer.parseInt(rangeMatcher.group(3)), normalized);
            int startMinute = parseMinute(rangeMatcher.group(2));
            int endMinute = parseMinute(rangeMatcher.group(4));
            return new TimeRange(formatTime(startHour, startMinute), formatTime(endHour, endMinute));
        }

        Matcher singleMatcher = Pattern.compile("(\\d{1,2})(?:[:点时])([0-5]\\d|半)?").matcher(normalized);
        if (singleMatcher.find())
        {
            int startHour = adjustHour(Integer.parseInt(singleMatcher.group(1)), normalized);
            int startMinute = parseMinute(singleMatcher.group(2));
            LocalTime start = LocalTime.of(startHour, startMinute);
            LocalTime end = start.plusMinutes(30);
            return new TimeRange(start.toString(), end.toString());
        }
        return null;
    }

    private int adjustHour(int hour, String text)
    {
        if ((text.contains("下午") || text.contains("晚上")) && hour < 12)
        {
            return hour + 12;
        }
        return hour;
    }

    private int parseMinute(String value)
    {
        if (StringUtils.isBlank(value))
        {
            return 0;
        }
        if ("半".equals(value))
        {
            return 30;
        }
        return Integer.parseInt(value);
    }

    private String formatTime(int hour, int minute)
    {
        return String.format("%02d:%02d", hour, minute);
    }

    private String normalizeCnNumber(String text)
    {
        if (text == null)
        {
            return "";
        }
        return text.replace("十一", "11")
                .replace("十二", "12")
                .replace("十", "10")
                .replace("九", "9")
                .replace("八", "8")
                .replace("七", "7")
                .replace("六", "6")
                .replace("五", "5")
                .replace("四", "4")
                .replace("三", "3")
                .replace("二", "2")
                .replace("两", "2")
                .replace("一", "1")
                .replace("零", "0");
    }

    private Map<Long, List<Map<String, Object>>> groupSlotsByDoctor(List<Map<String, Object>> slots)
    {
        Map<Long, List<Map<String, Object>>> grouped = new LinkedHashMap<>();
        for (Map<String, Object> slot : slots)
        {
            Long doctorId = toLong(slot.get("doctorId"));
            grouped.computeIfAbsent(doctorId, key -> new ArrayList<>()).add(slot);
        }
        return grouped;
    }

    private String formatDoctorSlotGroups(Map<Long, List<Map<String, Object>>> grouped)
    {
        StringBuilder builder = new StringBuilder();
        int index = 1;
        for (List<Map<String, Object>> doctorSlots : grouped.values())
        {
            if (doctorSlots == null || doctorSlots.isEmpty())
            {
                continue;
            }
            Map<String, Object> first = doctorSlots.get(0);
            String start = String.valueOf(doctorSlots.get(0).get("startTime"));
            String end = String.valueOf(doctorSlots.get(doctorSlots.size() - 1).get("endTime"));
            long available = 0L;
            for (Map<String, Object> slot : doctorSlots)
            {
                Long count = toLong(slot.get("availableNumber"));
                available += count == null ? 0L : count;
            }
            builder.append(index++).append(". ")
                    .append(first.get("doctorName")).append("，")
                    .append(emptyDisplay(first.get("levelName"))).append("，")
                    .append(start).append("-").append(end)
                    .append("，剩余").append(available).append("个号");
            if (first.get("fee") != null)
            {
                builder.append("，挂号费").append(first.get("fee")).append("元");
            }
            builder.append("\n");
        }
        return builder.toString().trim();
    }

    private String formatRegisterSuccess(Map<String, Object> data, String appointmentDate)
    {
        if (data == null)
        {
            return "挂号成功，请到我的预约中查看详情。";
        }
        // 格式化日期为用户友好的中文格式（7月6日）
        String friendlyDate = appointmentDate;
        try
        {
            LocalDate parsed = LocalDate.parse(appointmentDate, DATE_FORMATTER);
            friendlyDate = parsed.getMonthValue() + "月" + parsed.getDayOfMonth() + "日";
        }
        catch (Exception ignored) {}
        return "已为你挂号成功。\n"
                + "挂号单号：" + emptyDisplay(data.get("registerNo")) + "\n"
                + "科室：" + emptyDisplay(data.get("deptName")) + "\n"
                + "医生：" + emptyDisplay(data.get("doctorName")) + "\n"
                + "日期：" + friendlyDate + " " + emptyDisplay(data.get("startTime")) + "-" + emptyDisplay(data.get("endTime")) + "\n"
                + "费用：" + emptyDisplay(data.get("registerFee")) + "元\n"
                + "支付状态：" + ("paid".equalsIgnoreCase(String.valueOf(data.get("payStatus"))) ? "已支付" : "未支付，请在我的缴费中完成支付");
    }

    private String emptyDisplay(Object value)
    {
        return value == null || StringUtils.isBlank(String.valueOf(value)) ? "--" : String.valueOf(value);
    }

    private AiChatResponse buildChatResponse(String sessionId, String reply, Long patientId, boolean hasMedicalRecord)
    {
        AiChatResponse response = new AiChatResponse();
        response.setSessionId(sessionId);
        response.setReply(reply);
        response.setPatientId(patientId);
        response.setHasMedicalRecord(hasMedicalRecord);
        return response;
    }

    private static class TimeRange
    {
        private final String startTime;

        private final String endTime;

        private TimeRange(String startTime, String endTime)
        {
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

    private JSONArray buildMessages(Map<String, Object> patient,
                                    List<MedicalRecord> medicalRecords,
                                    List<EncounterVo> encounters,
                                    List<Map<String, Object>> examResults,
                                    List<AiChatRecord> longMemories,
                                    List<AiChatRecord> recentRecords,
                                    String message,
                                    String mode)
    {
        String systemPrompt = "doctor".equals(mode) ? DOCTOR_SYSTEM_PROMPT : PATIENT_SYSTEM_PROMPT;
        JSONArray messages = new JSONArray();
        messages.add(chatMessage("system", systemPrompt + "\n\n" + buildPatientContext(patient, medicalRecords, encounters, examResults, longMemories)));
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
                                       List<Map<String, Object>> examResults,
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

        if (examResults != null && !examResults.isEmpty())
        {
            context.append("\n检查检验结果:\n");
            int index = 1;
            for (Map<String, Object> result : examResults)
            {
                context.append(index++).append(". ")
                        .append("申请ID:").append(emptyText(result.get("applyId"))).append(", ")
                        .append("项目:").append(emptyText(result.get("techName"))).append(", ")
                        .append("明细:").append(emptyText(result.get("itemName"))).append(", ")
                        .append("结果:").append(emptyText(result.get("resultValue"))).append(emptyText(result.get("resultUnit"))).append(", ")
                        .append("参考范围:").append(emptyText(result.get("referenceRange"))).append(", ")
                        .append("异常标识:").append(resolveAbnormalText(result.get("abnormalFlag"))).append(", ")
                        .append("诊断意见:").append(emptyText(result.get("diagnosisOpinion"))).append(", ")
                        .append("建议:").append(emptyText(result.get("suggestion"))).append(", ")
                        .append("报告时间:").append(formatDate(result.get("reportTime"))).append("\n");
            }
        }
        else
        {
            context.append("\n检查检验结果: 暂无已发布检查检验结果。\n");
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

    private String resolveApiKeyFromFile()
    {
        if (StringUtils.isBlank(apiKeyFile))
        {
            throw new ServiceException("未配置DeepSeek API Key，请设置 DEEPSEEK_API_KEY 或配置 ai.deepseek.api-key-file");
        }

        for (Path path : resolveApiKeyCandidates(apiKeyFile))
        {
            try
            {
                if (!Files.isRegularFile(path))
                {
                    continue;
                }
                String key = Files.readString(path, StandardCharsets.UTF_8);
                if (StringUtils.isNotEmpty(key))
                {
                    return key.trim();
                }
            }
            catch (IOException ignored)
            {
            }
        }
        throw new ServiceException("未读取到DeepSeek API Key，请将DeepSeekApiKey.txt放到项目根目录或服务运行目录");
    }

    private List<Path> resolveApiKeyCandidates(String configuredPath)
    {
        List<Path> candidates = new ArrayList<>();
        Path configured = Paths.get(configuredPath);
        if (configured.isAbsolute())
        {
            candidates.add(configured);
            return candidates;
        }

        Path cursor = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        while (cursor != null)
        {
            candidates.add(cursor.resolve(configured).normalize());
            cursor = cursor.getParent();
        }
        return candidates;
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
        body.put("max_tokens", 2048);

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

    private String emptyText(Object value)
    {
        String text = value == null ? null : String.valueOf(value);
        return StringUtils.isBlank(text) ? "无记录" : limit(text, 300);
    }

    private String resolveAbnormalText(Object value)
    {
        String flag = value == null ? "0" : String.valueOf(value);
        if ("1".equals(flag))
        {
            return "偏高";
        }
        if ("2".equals(flag))
        {
            return "偏低";
        }
        if ("3".equals(flag))
        {
            return "阳性";
        }
        if ("4".equals(flag))
        {
            return "阴性";
        }
        return "正常";
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
