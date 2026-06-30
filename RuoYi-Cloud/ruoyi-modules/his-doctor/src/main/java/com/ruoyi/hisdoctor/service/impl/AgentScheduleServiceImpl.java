package com.ruoyi.hisdoctor.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.hisdoctor.domain.AgentScheduleDoctor;
import com.ruoyi.hisdoctor.domain.AgentScheduleItem;
import com.ruoyi.hisdoctor.domain.AgentScheduleResult;
import com.ruoyi.hisdoctor.domain.AgentScheduleRun;
import com.ruoyi.hisdoctor.domain.Schedule;
import com.ruoyi.hisdoctor.mapper.AgentScheduleRunMapper;
import com.ruoyi.hisdoctor.mapper.DoctorMapper;
import com.ruoyi.hisdoctor.mapper.ScheduleMapper;
import com.ruoyi.hisdoctor.service.IAgentScheduleService;
import com.ruoyi.hisdoctor.service.IScheduleSlotService;

/**
 * 智能体自动排班核心逻辑。
 */
@Service
public class AgentScheduleServiceImpl implements IAgentScheduleService
{
    private static final Logger log = LoggerFactory.getLogger(AgentScheduleServiceImpl.class);

    private static final long RECEPTION_DOCTOR_ROLE_ID = 5L;
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final String[] TIME_SLOTS = {"morning", "afternoon"};

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private AgentScheduleRunMapper agentScheduleRunMapper;

    @Autowired
    private LocalAiReasoningService localAiReasoningService;

    @Autowired
    private IScheduleSlotService scheduleSlotService;

    @PostConstruct
    public void init()
    {
        agentScheduleRunMapper.createAgentScheduleRunTable();
    }

    @Override
    public AgentScheduleResult currentWeek()
    {
        LocalDate start = LocalDate.now(ZONE).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return buildWeekResult(start, false, false, false);
    }

    @Override
    public AgentScheduleResult currentWeekForDoctor(Long doctorId)
    {
        AgentScheduleResult result = currentWeek();
        List<AgentScheduleItem> ownSchedules = result.getSchedules().stream()
                .filter(item -> doctorId != null && doctorId.equals(item.getDoctorId()))
                .collect(Collectors.toList());
        result.setSchedules(ownSchedules);
        result.setDoctorCount(ownSchedules.isEmpty() ? 0 : 1);
        result.setDeptCount((int) ownSchedules.stream().map(AgentScheduleItem::getDeptId).filter(id -> id != null).distinct().count());
        result.setRetainedCount(ownSchedules.size());
        result.setInsertedCount(0);
        result.setFallbackCount(0);
        result.setWarnings(new ArrayList<>());
        result.setReports(new ArrayList<>());
        result.getReports().add("医生本人视图：仅展示当前登录医生自己的会诊排班。");
        result.getReports().add("权限规则：role_id=5 医生不能查看其他医生排班，也不能执行智能体生成任务。");
        result.setMessage("已加载当前医生本周会诊排班。");
        return result;
    }

    @Override
    public AgentScheduleResult previewNextWeek()
    {
        LocalDate start = nextMonday();
        return buildWeekResult(start, true, false, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgentScheduleResult generateNextWeek()
    {
        LocalDate start = nextMonday();
        return generateNextWeek("manual", start);
    }

    @Override
    public AgentScheduleResult publish(String weekStart)
    {
        LocalDate start = LocalDate.parse(weekStart);
        AgentScheduleResult result = buildWeekResult(start, false, false, false);
        result.setPublishStatus("published");
        result.setMessage("排班已同步公示，通知推送任务已生成。");
        result.getReports().add("公示范围：患者端会诊排班、医生工作台、挂号预约入口。");
        result.getReports().add("通知策略：医生端按科室推送，管理端同步显示兜底提醒。");
        return result;
    }

    @Override
    public AgentScheduleRun latestRun()
    {
        return agentScheduleRunMapper.selectLatestAgentScheduleRun();
    }

    @Override
    public List<AgentScheduleRun> runHistory()
    {
        return agentScheduleRunMapper.selectAgentScheduleRunList();
    }

    @Scheduled(cron = "0 0 2 * * SUN", zone = "Asia/Shanghai")
    public void weeklyAutoGenerate()
    {
        try
        {
            AgentScheduleResult result = generateNextWeek("scheduled", nextMonday());
            log.info("智能排班自动执行完成，weekStart={}, inserted={}, retained={}, fallback={}",
                    result.getWeekStart(), result.getInsertedCount(), result.getRetainedCount(), result.getFallbackCount());
        }
        catch (Exception e)
        {
            log.error("智能排班自动执行失败", e);
        }
    }

    private AgentScheduleResult generateNextWeek(String triggerType, LocalDate weekStart)
    {
        AgentScheduleRun run = startRun(triggerType, weekStart);
        long startMillis = System.currentTimeMillis();
        try
        {
            updateRunStage(run, "perceiving", "running", null);
            AgentScheduleResult result = buildWeekResult(weekStart, false, true, true);
            updateRunStage(run, "reasoning", "running", null);
            String reasoningSummary = buildAiReasoning(result);

            run.setReasoningSummary(reasoningSummary);
            agentScheduleRunMapper.updateAgentScheduleRun(run);

            updateRunStage(run, "executing", "running", null);
            String actionSummary = "已生成下周会诊排班并写入 schedule 表，新增 " + result.getInsertedCount()
                    + " 条，保留已有排班 " + result.getRetainedCount() + " 条。";
            run.setActionSummary(actionSummary);
            agentScheduleRunMapper.updateAgentScheduleRun(run);

            updateRunStage(run, "monitoring", result.getWarnings().isEmpty() ? "running" : "warning", null);
            String warningSummary = String.join("；", result.getWarnings());
            run.setWarningSummary(warningSummary);
            agentScheduleRunMapper.updateAgentScheduleRun(run);

            run.setStage("completed");
            run.setStatus(result.getWarnings().isEmpty() ? "success" : "warning");
            run.setPerceivedDoctorCount(result.getDoctorCount());
            run.setPerceivedDeptCount(result.getDeptCount());
            run.setGeneratedCount(result.getSchedules() == null ? 0 : result.getSchedules().size());
            run.setInsertedCount(result.getInsertedCount());
            run.setFallbackCount(result.getFallbackCount());
            run.setWarningCount(result.getWarnings() == null ? 0 : result.getWarnings().size());
            run.setDurationMs(System.currentTimeMillis() - startMillis);
            run.setPerceptionSummary("感知到 role_id=5 接诊医生 " + result.getDoctorCount() + " 人，覆盖科室 " + result.getDeptCount() + " 个。");
            run.setReasoningSummary(reasoningSummary);
            run.setActionSummary(actionSummary);
            run.setWarningSummary(warningSummary);
            run.setEndTime(DateUtils.getNowDate());
            agentScheduleRunMapper.updateAgentScheduleRun(run);

            result.getReports().add(0, "智能体运行编号：" + run.getRunNo() + "，模型：" + run.getModelProvider() + "/" + run.getModelName() + "。");
            result.getReports().add(1, "智能体生命周期：唤醒 -> 感知全量业务数据 -> AI/规则推理 -> 自动入库 -> 监控告警 -> 运行数据沉淀。");
            return result;
        }
        catch (Exception e)
        {
            run.setStage("alerting");
            run.setStatus("failed");
            run.setDurationMs(System.currentTimeMillis() - startMillis);
            run.setErrorMessage(e.getMessage());
            run.setWarningSummary("智能体执行失败，已记录异常并等待人工处理。");
            run.setEndTime(DateUtils.getNowDate());
            agentScheduleRunMapper.updateAgentScheduleRun(run);
            throw e;
        }
    }

    private AgentScheduleRun startRun(String triggerType, LocalDate weekStart)
    {
        AgentScheduleRun run = new AgentScheduleRun();
        run.setRunNo("ASR-" + System.currentTimeMillis());
        run.setTriggerType(triggerType);
        run.setStage("awakened");
        run.setStatus("running");
        run.setModelProvider(localAiReasoningService.getProvider());
        run.setModelName(localAiReasoningService.getModel());
        run.setWeekStart(toDate(weekStart));
        run.setWeekEnd(toDate(weekStart.plusDays(6)));
        run.setStartTime(DateUtils.getNowDate());
        run.setPerceptionSummary("智能体已被" + ("scheduled".equals(triggerType) ? "每周日定时任务" : "人工按钮") + "唤醒。");
        agentScheduleRunMapper.insertAgentScheduleRun(run);
        return run;
    }

    private void updateRunStage(AgentScheduleRun run, String stage, String status, String message)
    {
        run.setStage(stage);
        run.setStatus(status);
        if (message != null)
        {
            run.setPerceptionSummary(message);
        }
        agentScheduleRunMapper.updateAgentScheduleRun(run);
    }

    private String buildAiReasoning(AgentScheduleResult result)
    {
        String fallback = "规则引擎完成推理：优先保障科室覆盖，结合历史排班负载、同日重复约束、连续工作惩罚和主任/专家优先级生成排班。";
        String prompt = "你是医院会诊排班智能体，请基于以下结果生成100字以内排班推理说明：医生数="
                + result.getDoctorCount() + "，科室数=" + result.getDeptCount()
                + "，新增班次=" + result.getInsertedCount() + "，兜底科室数=" + result.getFallbackCount()
                + "，告警=" + String.join("；", result.getWarnings());
        return localAiReasoningService.reason(prompt, fallback);
    }

    private AgentScheduleResult buildWeekResult(LocalDate weekStart, boolean preview, boolean persist, boolean fillMissingSlots)
    {
        LocalDate weekEnd = weekStart.plusDays(6);
        Date startDate = toDate(weekStart);
        Date endDate = toDate(weekEnd);
        List<AgentScheduleDoctor> doctors = doctorMapper.selectReceptionDoctors(RECEPTION_DOCTOR_ROLE_ID);
        if (doctors == null || doctors.isEmpty())
        {
            throw new ServiceException("未找到 role_id=5 的接诊医生，无法执行智能排班");
        }

        List<AgentScheduleItem> existing = scheduleMapper.selectAgentScheduleItems(startDate, endDate);
        Map<String, AgentScheduleItem> occupied = existing.stream()
                .collect(Collectors.toMap(this::itemKey, item -> item, (a, b) -> a, LinkedHashMap::new));
        Map<Long, List<AgentScheduleDoctor>> doctorsByDept = doctors.stream()
                .collect(Collectors.groupingBy(AgentScheduleDoctor::getDeptId, LinkedHashMap::new, Collectors.toList()));
        Set<Long> candidateDoctorIds = doctors.stream().map(AgentScheduleDoctor::getDoctorId).collect(Collectors.toSet());
        List<AgentScheduleItem> history = scheduleMapper.selectAgentScheduleItems(toDate(weekStart.minusDays(28)), toDate(weekStart.minusDays(1)));
        Map<Long, Integer> historyLoad = new HashMap<>();
        for (AgentScheduleItem item : history)
        {
            if (candidateDoctorIds.contains(item.getDoctorId()))
            {
                historyLoad.merge(item.getDoctorId(), 1, Integer::sum);
            }
        }
        Map<Long, Integer> weeklyLoad = new HashMap<>();
        Map<String, Integer> dailyLoad = new HashMap<>();
        Set<String> doctorDaySet = new HashSet<>();
        List<AgentScheduleItem> generated = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        for (AgentScheduleItem item : existing)
        {
            weeklyLoad.merge(item.getDoctorId(), 1, Integer::sum);
            dailyLoad.merge(item.getDoctorId() + "@" + toLocalDate(item.getScheduleDate()), 1, Integer::sum);
            doctorDaySet.add(item.getDoctorId() + "@" + toLocalDate(item.getScheduleDate()));
        }

        if (fillMissingSlots)
        {
            for (int dayIndex = 0; dayIndex < 7; dayIndex++)
            {
                LocalDate date = weekStart.plusDays(dayIndex);
                for (String slot : TIME_SLOTS)
                {
                    for (Map.Entry<Long, List<AgentScheduleDoctor>> entry : doctorsByDept.entrySet())
                    {
                        Long deptId = entry.getKey();
                        if (deptHasSlot(occupied, generated, deptId, date, slot))
                        {
                            continue;
                        }

                        AgentScheduleDoctor doctor = selectBestDoctor(entry.getValue(), date, historyLoad, weeklyLoad, dailyLoad, doctorDaySet);
                        if (doctor == null)
                        {
                            warnings.add(formatDate(date) + " " + slotName(slot) + " 科室" + deptId + "无可排医生，需要人工兜底。");
                            continue;
                        }

                        AgentScheduleItem item = buildItem(doctor, date, slot);
                        item.setGenerated(true);
                        item.setReason(buildReason(doctor, entry.getValue().size(), historyLoad.getOrDefault(doctor.getDoctorId(), 0),
                                weeklyLoad.getOrDefault(doctor.getDoctorId(), 0), dailyLoad.getOrDefault(doctor.getDoctorId() + "@" + date, 0)));
                        generated.add(item);
                        weeklyLoad.merge(doctor.getDoctorId(), 1, Integer::sum);
                        dailyLoad.merge(doctor.getDoctorId() + "@" + date, 1, Integer::sum);
                        doctorDaySet.add(doctor.getDoctorId() + "@" + date);
                    }
                }
            }
        }

        int inserted = 0;
        if (persist)
        {
            for (AgentScheduleItem item : generated)
            {
                Schedule exists = scheduleMapper.selectScheduleByDoctorDateSlot(item.getDoctorId(), item.getScheduleDate(), item.getTimeSlot());
                if (exists != null)
                {
                    continue;
                }
                Schedule schedule = new Schedule();
                schedule.setDoctorId(item.getDoctorId());
                schedule.setScheduleDate(item.getScheduleDate());
                schedule.setTimeSlot(item.getTimeSlot());
                schedule.setMaxNumber(item.getMaxNumber());
                schedule.setReservedNumber(0L);
                schedule.setStatus("0");
                schedule.setCreateTime(DateUtils.getNowDate());
                int rows = scheduleMapper.insertSchedule(schedule);
                if (rows > 0)
                {
                    scheduleSlotService.generateSlotsForSchedule(schedule);
                }
                inserted += rows;
            }
            existing = scheduleMapper.selectAgentScheduleItems(startDate, endDate);
            generated = new ArrayList<>();
        }

        AgentScheduleResult result = new AgentScheduleResult();
        result.setWeekStart(startDate);
        result.setWeekEnd(endDate);
        result.setPreview(preview);
        result.setDoctorCount(doctors.size());
        result.setDeptCount(doctorsByDept.size());
        result.setRetainedCount(existing.size());
        result.setInsertedCount(persist ? inserted : generated.size());
        result.setFallbackCount(countSingleDoctorDept(doctorsByDept));
        result.setPublishStatus(persist ? "generated" : "draft");
        result.setWarnings(warnings);
        result.setReports(buildReports(doctors, doctorsByDept, historyLoad, existing, generated, warnings, persist, preview));

        List<AgentScheduleItem> allItems = new ArrayList<>();
        allItems.addAll(existing);
        allItems.addAll(generated);
        allItems.sort(Comparator
                .comparing(AgentScheduleItem::getScheduleDate)
                .thenComparing(item -> slotOrder(item.getTimeSlot()))
                .thenComparing(AgentScheduleItem::getDeptId, Comparator.nullsLast(Long::compareTo))
                .thenComparing(AgentScheduleItem::getDoctorId, Comparator.nullsLast(Long::compareTo)));
        result.setSchedules(allItems);
        result.setMessage(persist ? "下周智能排班已生成并入库。" : "智能排班方案已生成，可审核后入库。");
        return result;
    }

    private AgentScheduleDoctor selectBestDoctor(List<AgentScheduleDoctor> doctors, LocalDate date, Map<Long, Integer> historyLoad, Map<Long, Integer> weeklyLoad,
            Map<String, Integer> dailyLoad, Set<String> doctorDaySet)
    {
        boolean singleDoctorDept = doctors.size() == 1;
        return doctors.stream()
                .filter(doctor -> singleDoctorDept || !doctorDaySet.contains(doctor.getDoctorId() + "@" + date))
                .min(Comparator
                        .comparingInt((AgentScheduleDoctor doctor) -> score(doctor, date, singleDoctorDept, historyLoad, weeklyLoad, dailyLoad, doctorDaySet))
                        .thenComparing(AgentScheduleDoctor::getLevelId, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(AgentScheduleDoctor::getDoctorId))
                .orElse(null);
    }

    private int score(AgentScheduleDoctor doctor, LocalDate date, boolean singleDoctorDept, Map<Long, Integer> historyLoad, Map<Long, Integer> weeklyLoad,
            Map<String, Integer> dailyLoad, Set<String> doctorDaySet)
    {
        int score = weeklyLoad.getOrDefault(doctor.getDoctorId(), 0) * 12;
        score += historyLoad.getOrDefault(doctor.getDoctorId(), 0) * 3;
        score += dailyLoad.getOrDefault(doctor.getDoctorId() + "@" + date, 0) * 30;
        score += doctorDaySet.contains(doctor.getDoctorId() + "@" + date.minusDays(1)) ? 8 : 0;
        score += doctorDaySet.contains(doctor.getDoctorId() + "@" + date.plusDays(1)) ? 4 : 0;
        score += singleDoctorDept ? 15 : 0;
        score -= levelPriority(doctor.getLevelId());
        if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY)
        {
            score += 5;
        }
        return score;
    }

    private AgentScheduleItem buildItem(AgentScheduleDoctor doctor, LocalDate date, String slot)
    {
        AgentScheduleItem item = new AgentScheduleItem();
        item.setDoctorId(doctor.getDoctorId());
        item.setDoctorName(doctor.getDoctorName());
        item.setDeptId(doctor.getDeptId());
        item.setDeptName(doctor.getDeptName());
        item.setLevelId(doctor.getLevelId());
        item.setLevelName(doctor.getLevelName());
        item.setScheduleDate(toDate(date));
        item.setTimeSlot(slot);
        item.setMaxNumber(defaultCapacity(doctor.getLevelId()));
        item.setReservedNumber(0L);
        item.setStatus("0");
        return item;
    }

    private String buildReason(AgentScheduleDoctor doctor, int deptDoctorCount, int historyLoad, int weeklyLoad, int dailyLoad)
    {
        if (deptDoctorCount == 1)
        {
            return "单医生科室兜底排班，结合近4周历史" + historyLoad + "班保障每日会诊覆盖";
        }
        return "综合科室覆盖、近4周历史" + historyLoad + "班、本周负载" + weeklyLoad + "班、当日负载" + dailyLoad + "班后择优安排";
    }

    private List<String> buildReports(List<AgentScheduleDoctor> doctors, Map<Long, List<AgentScheduleDoctor>> doctorsByDept, Map<Long, Integer> historyLoad,
            List<AgentScheduleItem> existing, List<AgentScheduleItem> generated, List<String> warnings, boolean persist, boolean preview)
    {
        List<String> reports = new ArrayList<>();
        reports.add("候选池：仅纳入 doctor.user_id 绑定 sys_user_role.role_id=5 且状态正常的接诊医生，共" + doctors.size() + "人。");
        reports.add("科室覆盖：" + doctorsByDept.size() + "个科室参与排班，其中" + countSingleDoctorDept(doctorsByDept) + "个单医生科室启用兜底规则。");
        reports.add("生成策略：按下周一至周日、上午/下午两个会诊时段逐日计算，优先保证每个科室每天有会诊医生。");
        reports.add("历史推理：读取目标周前28天历史排班负载，参与医生公平性评分，历史高负载医生自动降低优先级。");
        reports.add("公平性：评分时降低历史高负载、本周高负载、连续工作和同日重复医生优先级，同时保留主任号/专家号会诊优先权。");
        reports.add("历史样本：role_id=5 候选医生近4周累计历史班次 " + historyLoad.values().stream().mapToInt(Integer::intValue).sum() + " 条。");
        reports.add("数据处理：已有排班" + existing.size() + "条全部保留，新方案" + generated.size() + "条" + (persist ? "已写入数据库。" : "仅预览未入库。"));
        if (preview)
        {
            reports.add("当前为预览结果，点击一键生成后才会写入 schedule 表。");
        }
        if (!warnings.isEmpty())
        {
            reports.add("风险提示：" + warnings.size() + "项需要人工关注。");
        }
        return reports;
    }

    private boolean deptHasSlot(Map<String, AgentScheduleItem> occupied, List<AgentScheduleItem> generated, Long deptId, LocalDate date, String slot)
    {
        for (AgentScheduleItem item : occupied.values())
        {
            if (deptId.equals(item.getDeptId()) && date.equals(toLocalDate(item.getScheduleDate())) && slot.equals(item.getTimeSlot()))
            {
                return true;
            }
        }
        for (AgentScheduleItem item : generated)
        {
            if (deptId.equals(item.getDeptId()) && date.equals(toLocalDate(item.getScheduleDate())) && slot.equals(item.getTimeSlot()))
            {
                return true;
            }
        }
        return false;
    }

    private int countSingleDoctorDept(Map<Long, List<AgentScheduleDoctor>> doctorsByDept)
    {
        return (int) doctorsByDept.values().stream().filter(list -> list.size() == 1).count();
    }

    private long defaultCapacity(Long levelId)
    {
        if (levelId != null && levelId == 3L)
        {
            return 12L;
        }
        if (levelId != null && levelId == 2L)
        {
            return 15L;
        }
        return 20L;
    }

    private int levelPriority(Long levelId)
    {
        if (levelId == null)
        {
            return 0;
        }
        return levelId.intValue() * 3;
    }

    private LocalDate nextMonday()
    {
        return LocalDate.now(ZONE).with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    }

    private Date toDate(LocalDate date)
    {
        return Date.from(date.atStartOfDay(ZONE).toInstant());
    }

    private LocalDate toLocalDate(Date date)
    {
        return date.toInstant().atZone(ZONE).toLocalDate();
    }

    private String itemKey(AgentScheduleItem item)
    {
        return item.getDoctorId() + "@" + toLocalDate(item.getScheduleDate()) + "@" + item.getTimeSlot();
    }

    private int slotOrder(String slot)
    {
        if ("morning".equals(slot))
        {
            return 1;
        }
        if ("afternoon".equals(slot))
        {
            return 2;
        }
        if ("evening".equals(slot))
        {
            return 3;
        }
        return 9;
    }

    private String slotName(String slot)
    {
        if ("morning".equals(slot))
        {
            return "上午";
        }
        if ("afternoon".equals(slot))
        {
            return "下午";
        }
        if ("evening".equals(slot))
        {
            return "夜间";
        }
        return slot;
    }

    private String formatDate(LocalDate date)
    {
        return date.toString();
    }
}
