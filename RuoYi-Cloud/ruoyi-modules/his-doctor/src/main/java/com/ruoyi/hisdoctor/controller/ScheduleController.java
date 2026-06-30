package com.ruoyi.hisdoctor.controller;

import java.util.List;
import java.util.Objects;
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
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.hisdoctor.domain.Doctor;
import com.ruoyi.hisdoctor.domain.Schedule;
import com.ruoyi.hisdoctor.service.IDoctorService;
import com.ruoyi.hisdoctor.service.IScheduleService;
import com.ruoyi.system.api.domain.SysRole;
import com.ruoyi.system.api.model.LoginUser;
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
    private static final Long OUTPATIENT_DOCTOR_ROLE_ID = 5L;

    @Autowired
    private IScheduleService scheduleService;

    @Autowired
    private IDoctorService doctorService;

    /**
     * 查询医生排班列表
     */
    @RequiresPermissions("hisdoctor:schedule:list")
    @GetMapping("/list")
    public TableDataInfo list(Schedule schedule)
    {
        applyDoctorScope(schedule);
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
        applyDoctorScope(schedule);
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
        Schedule schedule = scheduleService.selectScheduleByScheduleId(scheduleId);
        checkDoctorScope(schedule);
        return success(schedule);
    }

    /**
     * 新增医生排班
     */
    @RequiresPermissions("hisdoctor:schedule:add")
    @Log(title = "医生排班", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Schedule schedule)
    {
        if (isDoctorUser())
        {
            schedule.setDoctorId(getCurrentDoctorId());
        }
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
        Schedule oldSchedule = scheduleService.selectScheduleByScheduleId(schedule.getScheduleId());
        checkDoctorScope(oldSchedule);
        if (isDoctorUser())
        {
            schedule.setDoctorId(getCurrentDoctorId());
        }
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
        if (isDoctorUser())
        {
            for (Long scheduleId : scheduleIds)
            {
                checkDoctorScope(scheduleService.selectScheduleByScheduleId(scheduleId));
            }
        }
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
        schedule.setRoleId(OUTPATIENT_DOCTOR_ROLE_ID);
        startPage();
        List<Schedule> list = scheduleService.selectScheduleList(schedule);
        return getDataTable(list);
    }

    private void applyDoctorScope(Schedule schedule)
    {
        if (isDoctorUser())
        {
            schedule.setDoctorId(getCurrentDoctorId());
        }
    }

    private void checkDoctorScope(Schedule schedule)
    {
        if (isDoctorUser() && (schedule == null || !Objects.equals(schedule.getDoctorId(), getCurrentDoctorId())))
        {
            throw new ServiceException("无权限访问其他医生排班");
        }
    }

    private Long getCurrentDoctorId()
    {
        Doctor doctor = doctorService.getDoctorByUserId(SecurityUtils.getUserId());
        if (doctor == null || doctor.getDoctorId() == null)
        {
            throw new ServiceException("当前医生档案不存在，请先完善医生信息");
        }
        return doctor.getDoctorId();
    }

    private boolean isDoctorUser()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        return !SecurityUtils.isAdmin()
                && loginUser != null
                && loginUser.getSysUser() != null
                && loginUser.getSysUser().getRoles() != null
                && loginUser.getSysUser().getRoles().stream()
                    .map(SysRole::getRoleId)
                    .anyMatch(roleId -> Objects.equals(roleId, OUTPATIENT_DOCTOR_ROLE_ID));
    }
}
