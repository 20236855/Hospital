package com.ruoyi.hisdoctor.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.hisdoctor.domain.Schedule;
import com.ruoyi.hisdoctor.domain.ScheduleSlot;
import com.ruoyi.hisdoctor.service.IScheduleService;
import com.ruoyi.hisdoctor.service.IScheduleSlotService;

/**
 * 医生排班时间片Controller
 */
@RestController
@RequestMapping("/schedule/slot")
public class ScheduleSlotController extends BaseController
{
    private static final Long OUTPATIENT_DOCTOR_ROLE_ID = 5L;

    @Autowired
    private IScheduleSlotService scheduleSlotService;

    @Autowired
    private IScheduleService scheduleService;

    @RequiresPermissions("hisdoctor:schedule:list")
    @GetMapping("/list")
    public TableDataInfo list(ScheduleSlot scheduleSlot)
    {
        startPage();
        List<ScheduleSlot> list = scheduleSlotService.selectScheduleSlotList(scheduleSlot);
        return getDataTable(list);
    }

    @GetMapping(value = "/{slotId}")
    public AjaxResult getInfo(@PathVariable("slotId") Long slotId)
    {
        return success(scheduleSlotService.selectScheduleSlotBySlotId(slotId));
    }

    /**
     * 患者端：按日期聚合可预约医生。
     */
    @GetMapping("/open/doctor-list")
    public TableDataInfo openDoctorList(ScheduleSlot scheduleSlot)
    {
        scheduleSlot.setRoleId(OUTPATIENT_DOCTOR_ROLE_ID);
        ensureSlotsForOpenQuery(scheduleSlot);
        startPage();
        List<ScheduleSlot> list = scheduleSlotService.selectOpenDoctorSlots(scheduleSlot);
        return getDataTable(list);
    }

    /**
     * 患者端：查询医生某天可预约半小时时间片。
     */
    @GetMapping("/open/list")
    public TableDataInfo openSlotList(ScheduleSlot scheduleSlot)
    {
        scheduleSlot.setRoleId(OUTPATIENT_DOCTOR_ROLE_ID);
        scheduleSlot.setStatus("0");
        ensureSlotsForOpenQuery(scheduleSlot);
        startPage();
        List<ScheduleSlot> list = scheduleSlotService.selectScheduleSlotList(scheduleSlot);
        return getDataTable(list);
    }

    /**
     * 为已有半天排班补生成半小时号源。
     */
    @RequiresPermissions("hisdoctor:schedule:edit")
    @PostMapping("/generate/{scheduleId}")
    public AjaxResult generate(@PathVariable("scheduleId") Long scheduleId)
    {
        Schedule schedule = scheduleService.selectScheduleByScheduleId(scheduleId);
        return toAjax(scheduleSlotService.generateSlotsForSchedule(schedule));
    }

    private void ensureSlotsForOpenQuery(ScheduleSlot scheduleSlot)
    {
        List<ScheduleSlot> existing = "0".equals(scheduleSlot.getStatus())
                ? scheduleSlotService.selectScheduleSlotList(scheduleSlot)
                : scheduleSlotService.selectOpenDoctorSlots(scheduleSlot);
        if (existing != null && !existing.isEmpty())
        {
            return;
        }

        Schedule schedule = new Schedule();
        schedule.setDoctorId(scheduleSlot.getDoctorId());
        schedule.setDeptId(scheduleSlot.getDeptId());
        schedule.setRoleId(scheduleSlot.getRoleId());
        schedule.setScheduleDate(scheduleSlot.getScheduleDate());
        schedule.setBeginDate(scheduleSlot.getBeginDate());
        schedule.setEndDate(scheduleSlot.getEndDate());
        schedule.setStatus("0");

        for (Schedule item : scheduleService.selectScheduleList(schedule))
        {
            scheduleSlotService.generateSlotsForSchedule(item);
        }
    }
}
