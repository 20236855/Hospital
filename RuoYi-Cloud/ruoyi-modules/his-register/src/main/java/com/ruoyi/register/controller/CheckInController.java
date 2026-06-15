package com.ruoyi.register.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.InnerAuth;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.his.api.RemotePatientService;
import com.ruoyi.register.domain.CheckIn;
import com.ruoyi.register.domain.Register;
import com.ruoyi.register.service.ICheckInService;
import com.ruoyi.register.service.IRegisterService;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.model.LoginUser;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 签到Controller
 * 
 * @author ruoyi
 * @date 2026-05-29
 */
@RestController
@RequestMapping("/in")
public class CheckInController extends BaseController
{
    @Autowired
    private ICheckInService checkInService;

    @Autowired
    private IRegisterService registerService;

    @Autowired
    private RemotePatientService remotePatientService;

    /**
     * 查询签到列表
     */
    @RequiresPermissions("register:in:list")
    @GetMapping("/list")
    public TableDataInfo list(CheckIn checkIn)
    {
        applyPatientScope(checkIn);
        applyNurseScope(checkIn);
        startPage();
        List<CheckIn> list = checkInService.selectCheckInList(checkIn);
        return getDataTable(list);
    }

    /**
     * 导出签到列表
     */
    @RequiresPermissions("register:in:export")
    @Log(title = "签到", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CheckIn checkIn)
    {
        if (isNurseUser())
        {
            throw new ServiceException("护士岗位不能导出签到信息");
        }
        applyPatientScope(checkIn);
        applyNurseScope(checkIn);
        List<CheckIn> list = checkInService.selectCheckInList(checkIn);
        ExcelUtil<CheckIn> util = new ExcelUtil<CheckIn>(CheckIn.class);
        util.exportExcel(response, list, "签到数据");
    }

    /**
     * 获取签到详细信息
     */
    @RequiresPermissions("register:in:query")
    @GetMapping(value = "/{checkInId}")
    public AjaxResult getInfo(@PathVariable("checkInId") Long checkInId)
    {
        CheckIn checkIn = checkInService.selectCheckInByCheckInId(checkInId);
        checkPatientScope(checkIn);
        checkNurseScope(checkIn);
        return success(checkIn);
    }

    /**
     * 获取签到信息（内部接口）
     */
    @InnerAuth
    @GetMapping(value = "/inner/{registerId}")
    public R<Map<String, Object>> innerInfo(@PathVariable("registerId") Long registerId)
    {
        CheckIn checkIn = checkInService.selectCheckInByRegisterId(registerId);
        Map<String, Object> result = new HashMap<>();
        if (checkIn != null)
        {
            result.put("queueNo", checkIn.getQueueNo());
            result.put("checkInTime", checkIn.getCheckInTime());
        }
        return R.ok(result);
    }

    /**
     * 新增签到
     */
    @RequiresPermissions("register:in:add")
    @Log(title = "签到", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CheckIn checkIn)
    {
        checkPatientReadonly();
        checkNurseScope(checkIn);
        return toAjax(checkInService.insertCheckIn(checkIn));
    }

    /**
     * 修改签到
     */
    @RequiresPermissions("register:in:edit")
    @Log(title = "签到", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CheckIn checkIn)
    {
        checkPatientReadonly();
        CheckIn oldCheckIn = checkInService.selectCheckInByCheckInId(checkIn.getCheckInId());
        checkNurseScope(oldCheckIn);
        if (checkIn.getRegisterId() != null)
        {
            checkNurseScope(checkIn);
        }
        return toAjax(checkInService.updateCheckIn(checkIn));
    }

    /**
     * 删除签到
     */
    @RequiresPermissions("register:in:remove")
    @Log(title = "签到", businessType = BusinessType.DELETE)
	@DeleteMapping("/{checkInIds}")
    public AjaxResult remove(@PathVariable Long[] checkInIds)
    {
        checkPatientReadonly();
        if (isNurseUser())
        {
            throw new ServiceException("护士岗位不能删除签到信息");
        }
        for (Long checkInId : checkInIds)
        {
            checkNurseScope(checkInService.selectCheckInByCheckInId(checkInId));
        }
        return toAjax(checkInService.deleteCheckInByCheckInIds(checkInIds));
    }

    private void applyPatientScope(CheckIn checkIn)
    {
        if (isPatientUser())
        {
            checkIn.setPatientId(getCurrentPatientId());
        }
    }

    private void checkPatientScope(CheckIn checkIn)
    {
        if (!isPatientUser())
        {
            return;
        }
        if (checkIn == null || checkIn.getRegisterId() == null)
        {
            throw new ServiceException("无权限访问其他患者信息");
        }
        Register register = registerService.selectRegisterByRegisterId(checkIn.getRegisterId());
        if (register == null || !Objects.equals(register.getPatientId(), getCurrentPatientId()))
        {
            throw new ServiceException("无权限访问其他患者信息");
        }
    }

    private void checkPatientReadonly()
    {
        if (isPatientUser())
        {
            throw new ServiceException("患者无权限修改签到信息");
        }
    }

    private Long getCurrentPatientId()
    {
        R<Map<String, Object>> patientR = remotePatientService.getPatientByUserId(SecurityUtils.getUserId(), SecurityConstants.INNER);
        if (R.isError(patientR))
        {
            throw new ServiceException(patientR.getMsg());
        }
        Map<String, Object> patient = patientR.getData();
        Object patientId = patient == null ? null : patient.get("patientId");
        if (patientId == null)
        {
            throw new ServiceException("当前患者档案不存在，请先完善患者信息");
        }
        return patientId instanceof Number ? ((Number) patientId).longValue() : Long.valueOf(patientId.toString());
    }

    private boolean isPatientUser()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        return !SecurityUtils.isAdmin()
                && loginUser != null
                && loginUser.getRoles() != null
                && loginUser.getRoles().contains("patient");
    }

    private boolean isNurseUser()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        return !SecurityUtils.isAdmin()
                && loginUser != null
                && loginUser.getRoles() != null
                && loginUser.getRoles().contains("doctor")
                && hasNursePost(loginUser);
    }

    private boolean hasNursePost(LoginUser loginUser)
    {
        if (loginUser == null || loginUser.getSysUser() == null || loginUser.getSysUser().getPostIds() == null)
        {
            return false;
        }
        for (Long postId : loginUser.getSysUser().getPostIds())
        {
            if (Long.valueOf(2L).equals(postId))
            {
                return true;
            }
        }
        return false;
    }

    private Long getNurseDeptId()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null || loginUser.getSysUser() == null)
        {
            return null;
        }
        return loginUser.getDeptId() != null ? loginUser.getDeptId() : loginUser.getSysUser().getDeptId();
    }

    private Long requireNurseDeptId()
    {
        Long deptId = getNurseDeptId();
        if (deptId == null)
        {
            throw new ServiceException("当前护士未配置所属科室，不能操作签到信息");
        }
        return deptId;
    }

    private void applyNurseScope(CheckIn checkIn)
    {
        if (isDeptScopedUser())
        {
            checkIn.setDeptId(requireNurseDeptId());
        }
    }

    private void checkNurseScope(CheckIn checkIn)
    {
        if (isDeptScopedUser() && checkIn != null)
        {
            Long nurseDeptId = requireNurseDeptId();
            if (checkIn.getRegisterId() == null)
            {
                throw new ServiceException("签到信息缺少挂号ID，不能校验科室权限");
            }
            if (checkIn.getRegisterId() != null)
            {
                Register register = registerService.selectRegisterByRegisterId(checkIn.getRegisterId());
                if (register == null || !Objects.equals(register.getDeptId(), nurseDeptId))
                {
                    throw new ServiceException("无权限操作其他科室签到信息");
                }
            }
        }
    }

    private boolean isDeptScopedUser()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        return !SecurityUtils.isAdmin()
                && loginUser != null
                && loginUser.getRoles() != null
                && loginUser.getRoles().contains("doctor")
                && getNurseDeptId() != null;
    }
}
