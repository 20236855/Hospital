package com.ruoyi.hisdoctor.controller;

import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.hisdoctor.domain.Doctor;
import com.ruoyi.hisdoctor.service.IDoctorService;
import com.ruoyi.hisdoctor.service.IAgentScheduleService;
import com.ruoyi.system.api.domain.SysRole;
import com.ruoyi.system.api.model.LoginUser;

/**
 * 智能排班Controller。
 */
@RestController
@RequestMapping("/schedule/agent")
public class ScheduleAgentController extends BaseController
{
    private static final Long ADMIN_ROLE_ID = 1L;
    private static final Long RECEPTION_DOCTOR_ROLE_ID = 5L;

    @Autowired
    private IAgentScheduleService agentScheduleService;

    @Autowired
    private IDoctorService doctorService;

    @GetMapping("/current-week")
    public AjaxResult currentWeek()
    {
        if (hasRole(ADMIN_ROLE_ID))
        {
            return success(agentScheduleService.currentWeek());
        }
        if (hasRole(RECEPTION_DOCTOR_ROLE_ID))
        {
            return success(agentScheduleService.currentWeekForDoctor(getCurrentDoctorId()));
        }
        throw new ServiceException("无权限查看智能排班");
    }

    @GetMapping("/admin/current-week")
    public AjaxResult adminCurrentWeek()
    {
        assertAdminRole();
        return success(agentScheduleService.currentWeek());
    }

    @GetMapping("/next-week-preview")
    public AjaxResult nextWeekPreview()
    {
        assertAdminRole();
        return success(agentScheduleService.previewNextWeek());
    }

    @PostMapping("/generate-next-week")
    public AjaxResult generateNextWeek()
    {
        assertAdminRole();
        return success(agentScheduleService.generateNextWeek());
    }

    @PostMapping("/publish/{weekStart}")
    public AjaxResult publish(@PathVariable String weekStart)
    {
        assertAdminRole();
        return success(agentScheduleService.publish(weekStart));
    }

    @GetMapping("/status")
    public AjaxResult status()
    {
        assertAdminRole();
        return success(agentScheduleService.latestRun());
    }

    @GetMapping("/runs")
    public AjaxResult runs()
    {
        assertAdminRole();
        return success(agentScheduleService.runHistory());
    }

    private void assertAdminRole()
    {
        if (!hasRole(ADMIN_ROLE_ID))
        {
            throw new ServiceException("只有管理员可以执行智能体自动排班操作");
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

    private boolean hasRole(Long roleId)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        return loginUser != null
                && loginUser.getSysUser() != null
                && loginUser.getSysUser().getRoles() != null
                && loginUser.getSysUser().getRoles().stream()
                    .map(SysRole::getRoleId)
                    .anyMatch(id -> Objects.equals(id, roleId));
    }
}
