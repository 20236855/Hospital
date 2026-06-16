package com.ruoyi.hisdoctor.service;

import java.util.List;
import com.ruoyi.hisdoctor.domain.AgentScheduleRun;
import com.ruoyi.hisdoctor.domain.AgentScheduleResult;

/**
 * 智能排班服务。
 */
public interface IAgentScheduleService
{
    AgentScheduleResult currentWeek();

    AgentScheduleResult currentWeekForDoctor(Long doctorId);

    AgentScheduleResult previewNextWeek();

    AgentScheduleResult generateNextWeek();

    AgentScheduleResult publish(String weekStart);

    AgentScheduleRun latestRun();

    List<AgentScheduleRun> runHistory();
}
