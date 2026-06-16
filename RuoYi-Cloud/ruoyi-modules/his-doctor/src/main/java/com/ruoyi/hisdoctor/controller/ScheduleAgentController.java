package com.ruoyi.hisdoctor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.hisdoctor.service.IAgentScheduleService;

/**
 * 智能排班Controller。
 */
@RestController
@RequestMapping("/schedule/agent")
public class ScheduleAgentController extends BaseController
{
    @Autowired
    private IAgentScheduleService agentScheduleService;

    @GetMapping("/current-week")
    public AjaxResult currentWeek()
    {
        return success(agentScheduleService.currentWeek());
    }

    @GetMapping("/next-week-preview")
    public AjaxResult nextWeekPreview()
    {
        return success(agentScheduleService.previewNextWeek());
    }

    @PostMapping("/generate-next-week")
    public AjaxResult generateNextWeek()
    {
        return success(agentScheduleService.generateNextWeek());
    }

    @PostMapping("/publish/{weekStart}")
    public AjaxResult publish(@PathVariable String weekStart)
    {
        return success(agentScheduleService.publish(weekStart));
    }

    @GetMapping("/status")
    public AjaxResult status()
    {
        return success(agentScheduleService.latestRun());
    }

    @GetMapping("/runs")
    public AjaxResult runs()
    {
        return success(agentScheduleService.runHistory());
    }
}
