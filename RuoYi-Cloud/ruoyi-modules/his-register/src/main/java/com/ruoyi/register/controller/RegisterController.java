package com.ruoyi.register.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.InnerAuth;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.his.api.RemotePatientService;
import com.ruoyi.register.domain.Register;
import com.ruoyi.register.service.IRegisterService;
import com.ruoyi.system.api.model.LoginUser;

/**
 * Register controller.
 */
@RestController
@RequestMapping("/register")
public class RegisterController extends BaseController
{
    private static final Long NURSE_POST_ID = 2L;

    @Autowired
    private IRegisterService registerService;

    @Autowired
    private RemotePatientService remotePatientService;

    @RequiresPermissions("register:register:list")
    @GetMapping("/list")
    public TableDataInfo list(Register register)
    {
        applyPatientScope(register);
        applyNurseScope(register);
        startPage();
        List<Register> list = registerService.selectRegisterList(register);
        return getDataTable(list);
    }

    @GetMapping("/level/list")
    public AjaxResult levelList()
    {
        return success(registerService.selectRegisterLevelList());
    }

    @GetMapping("/dept/list")
    public AjaxResult deptList()
    {
        List<Map<String, Object>> deptList = registerService.selectDeptList();
        if (isDeptScopedUser())
        {
            Long deptId = requireNurseDeptId();
            deptList.removeIf(item -> !Objects.equals(toLong(item.get("deptId")), deptId));
        }
        return success(deptList);
    }

    @GetMapping("/doctor/list/{deptId}")
    public AjaxResult doctorList(@PathVariable("deptId") Long deptId)
    {
        if (isDeptScopedUser() && !Objects.equals(deptId, requireNurseDeptId()))
        {
            throw new ServiceException("No permission to query doctors from another department");
        }
        return success(registerService.selectDoctorListByDeptId(deptId));
    }

    @GetMapping("/getFee")
    public AjaxResult getFee(@RequestParam Long levelId)
    {
        if (levelId == null)
        {
            return AjaxResult.error("Register level cannot be empty");
        }
        BigDecimal fee = registerService.getRegisterFeeByLevelId(levelId);
        return success(fee);
    }

    @RequiresPermissions("register:register:export")
    @Log(title = "Register", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Register register)
    {
        if (isNurseUser())
        {
            throw new ServiceException("Nurse users cannot export register info");
        }
        applyPatientScope(register);
        applyNurseScope(register);
        List<Register> list = registerService.selectRegisterList(register);
        ExcelUtil<Register> util = new ExcelUtil<Register>(Register.class);
        util.exportExcel(response, list, "register");
    }

    @RequiresPermissions("register:register:query")
    @GetMapping(value = "/{registerId}")
    public AjaxResult getInfo(@PathVariable("registerId") Long registerId)
    {
        Register register = registerService.selectRegisterByRegisterId(registerId);
        checkPatientScope(register);
        checkNurseScope(register);
        return success(register);
    }

    @InnerAuth
    @GetMapping(value = "/inner/{registerId}")
    public R<Map<String, Object>> innerInfo(@PathVariable("registerId") Long registerId)
    {
        Register register = registerService.selectRegisterByRegisterId(registerId);
        Map<String, Object> result = new HashMap<>();
        if (register != null)
        {
            result.put("registerNo", register.getRegisterNo());
            result.put("patientId", register.getPatientId());
            result.put("deptId", register.getDeptId());
        }
        return R.ok(result);
    }

    @InnerAuth
    @GetMapping("/inner/pay-info/{registerId}")
    public R<Map<String, Object>> payInfo(@PathVariable Long registerId)
    {
        Register register = registerService.selectRegisterByRegisterId(registerId);
        if (register == null)
        {
            return R.fail("Register record does not exist");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("registerId", register.getRegisterId());
        result.put("registerNo", register.getRegisterNo());
        result.put("patientId", register.getPatientId());
        result.put("scheduleId", register.getScheduleId());
        result.put("registerFee", register.getRegisterFee());
        result.put("registerStatus", register.getRegisterStatus());
        result.put("payStatus", register.getPayStatus());
        return R.ok(result);
    }

    @InnerAuth
    @PutMapping("/inner/pay/{registerId}")
    public R<Boolean> markPaid(@PathVariable Long registerId)
    {
        return R.ok(registerService.markRegisterPaid(registerId) > 0);
    }

    @RequiresPermissions("register:register:add")
    @Log(title = "Register", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Register register)
    {
        applyPatientScope(register);
        applyNurseScope(register);
        checkDoctorDeptScope(register);
        registerService.insertRegister(register);
        return success(registerService.selectRegisterByRegisterId(register.getRegisterId()));
    }

    @RequiresPermissions("register:register:edit")
    @Log(title = "Register", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Register register)
    {
        Register oldRegister = registerService.selectRegisterByRegisterId(register.getRegisterId());
        checkPatientScope(oldRegister);
        checkNurseScope(oldRegister);
        applyPatientScope(register);
        applyNurseScope(register);
        checkDoctorDeptScope(register);
        return toAjax(registerService.updateRegister(register));
    }

    @RequiresPermissions("register:register:remove")
    @Log(title = "Register", businessType = BusinessType.DELETE)
    @DeleteMapping("/{registerIds}")
    public AjaxResult remove(@PathVariable Long[] registerIds)
    {
        for (Long registerId : registerIds)
        {
            Register register = registerService.selectRegisterByRegisterId(registerId);
            checkPatientScope(register);
            checkNurseScope(register);
        }
        if (isNurseUser())
        {
            throw new ServiceException("Nurse users cannot delete register info, use cancel instead");
        }
        return toAjax(registerService.deleteRegisterByRegisterIds(registerIds));
    }

    @RequiresPermissions("register:register:edit")
    @Log(title = "Register", businessType = BusinessType.UPDATE)
    @PutMapping("/cancel/{registerId}")
    public AjaxResult cancel(@PathVariable Long registerId)
    {
        Register register = registerService.selectRegisterByRegisterId(registerId);
        checkPatientScope(register);
        checkNurseScope(register);
        return toAjax(registerService.cancelRegister(registerId));
    }

    private void applyPatientScope(Register register)
    {
        if (isPatientUser())
        {
            register.setPatientId(getCurrentPatientId());
        }
    }

    private void checkPatientScope(Register register)
    {
        if (isPatientUser() && (register == null || !Objects.equals(register.getPatientId(), getCurrentPatientId())))
        {
            throw new ServiceException("No permission to access another patient's info");
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
            throw new ServiceException("Current patient record does not exist");
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
            if (NURSE_POST_ID.equals(postId))
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
            throw new ServiceException("Current medical user has no department configured");
        }
        return deptId;
    }

    private void applyNurseScope(Register register)
    {
        if (isDeptScopedUser())
        {
            register.setDeptId(requireNurseDeptId());
        }
    }

    private void checkNurseScope(Register register)
    {
        if (isDeptScopedUser() && register != null)
        {
            Long nurseDeptId = requireNurseDeptId();
            if (!Objects.equals(register.getDeptId(), nurseDeptId))
            {
                throw new ServiceException("No permission to operate register info from another department");
            }
        }
    }

    private void checkDoctorDeptScope(Register register)
    {
        if (!isDeptScopedUser() || register == null || register.getDoctorId() == null)
        {
            return;
        }
        Long deptId = requireNurseDeptId();
        boolean matched = false;
        for (Map<String, Object> doctor : registerService.selectDoctorListByDeptId(deptId))
        {
            if (Objects.equals(toLong(doctor.get("doctorId")), register.getDoctorId()))
            {
                matched = true;
                break;
            }
        }
        if (!matched)
        {
            throw new ServiceException("No permission to operate register info for doctors from another department");
        }
    }

    private Long toLong(Object value)
    {
        if (value == null)
        {
            return null;
        }
        return value instanceof Number ? ((Number) value).longValue() : Long.valueOf(value.toString());
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
