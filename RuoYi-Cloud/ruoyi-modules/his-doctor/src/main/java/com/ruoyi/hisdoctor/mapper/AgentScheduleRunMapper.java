package com.ruoyi.hisdoctor.mapper;

import java.util.List;
import com.ruoyi.hisdoctor.domain.AgentScheduleRun;

/**
 * 智能排班运行记录Mapper。
 */
public interface AgentScheduleRunMapper
{
    void createAgentScheduleRunTable();

    int insertAgentScheduleRun(AgentScheduleRun run);

    int updateAgentScheduleRun(AgentScheduleRun run);

    AgentScheduleRun selectLatestAgentScheduleRun();

    List<AgentScheduleRun> selectAgentScheduleRunList();
}
