package com.ruoyi.hisdoctor.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.hisdoctor.domain.Schedule;
import com.ruoyi.hisdoctor.service.IScheduleService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 医生排班Controller
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
@RestController
@RequestMapping("/schedule")
public class ScheduleController extends BaseController
{
    @Autowired
    private IScheduleService scheduleService;

    /**
     * 查询医生排班列表
     */
    @RequiresPermissions("hisdoctor:schedule:list")
    @GetMapping("/list")
    public TableDataInfo list(Schedule schedule)
    {
        startPage();
        List<Schedule> list = scheduleService.selectScheduleList(schedule);
        return getDataTable(list);
    }

    /**
     * 导出医生排班列表
     */
    @RequiresPermissions("hisdoctor:schedule:export")
    @Log(title = "医生排班", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Schedule schedule)
    {
        List<Schedule> list = scheduleService.selectScheduleList(schedule);
        ExcelUtil<Schedule> util = new ExcelUtil<Schedule>(Schedule.class);
        util.exportExcel(response, list, "医生排班数据");
    }

    /**
     * 获取医生排班详细信息
     */
    @RequiresPermissions("hisdoctor:schedule:query")
    @GetMapping(value = "/{scheduleId}")
    public AjaxResult getInfo(@PathVariable("scheduleId") Long scheduleId)
    {
        return success(scheduleService.selectScheduleByScheduleId(scheduleId));
    }

    /**
     * 新增医生排班
     */
    @RequiresPermissions("hisdoctor:schedule:add")
    @Log(title = "医生排班", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Schedule schedule)
    {
        return toAjax(scheduleService.insertSchedule(schedule));
    }

    /**
     * 修改医生排班
     */
    @RequiresPermissions("hisdoctor:schedule:edit")
    @Log(title = "医生排班", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Schedule schedule)
    {
        return toAjax(scheduleService.updateSchedule(schedule));
    }

    /**
     * 删除医生排班
     */
    @RequiresPermissions("hisdoctor:schedule:remove")
    @Log(title = "医生排班", businessType = BusinessType.DELETE)
	@DeleteMapping("/{scheduleIds}")
    public AjaxResult remove(@PathVariable Long[] scheduleIds)
    {
        return toAjax(scheduleService.deleteScheduleByScheduleIds(scheduleIds));
    }

    /**
     * 预约排班（内部接口）
     */
    @PostMapping("/book/{scheduleId}")
    public AjaxResult bookSchedule(@PathVariable Long scheduleId)
    {
        return toAjax(scheduleService.bookSchedule(scheduleId));
    }

    /**
     * 取消排班预约（内部接口）
     */
    @PostMapping("/cancel/{scheduleId}")
    public AjaxResult cancelSchedule(@PathVariable Long scheduleId)
    {
        return toAjax(scheduleService.cancelSchedule(scheduleId));
    }

    /**
     * 获取排班信息（内部接口）
     */
    @GetMapping("/info/{scheduleId}")
    public AjaxResult getScheduleInfo(@PathVariable Long scheduleId)
    {
        return success(scheduleService.selectScheduleByScheduleId(scheduleId));
    }

    /**
     * 查询医生排班列表（患者端使用，无需权限）
     */
    @GetMapping("/open/list")
    public TableDataInfo openList(Schedule schedule)
    {
        startPage();
        List<Schedule> list = scheduleService.selectScheduleList(schedule);
        return getDataTable(list);
    }
}
