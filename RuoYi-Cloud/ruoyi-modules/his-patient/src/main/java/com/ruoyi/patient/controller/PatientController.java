package com.ruoyi.patient.controller;

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
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.InnerAuth;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.patient.domain.Patient;
import com.ruoyi.patient.service.IPatientService;
import com.ruoyi.system.api.model.LoginUser;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 患者Controller
 * 
 * @author ruoyi
 * @date 2026-05-29
 */
@RestController
@RequestMapping("/patient")
public class PatientController extends BaseController
{
    @Autowired
    private IPatientService patientService;

    /**
     * 查询患者列表
     */
    @RequiresPermissions("patient:patient:list")
    @GetMapping("/list")
    public TableDataInfo list(Patient patient)
    {
        applyPatientScope(patient);
        startPage();
        List<Patient> list = patientService.selectPatientList(patient);
        return getDataTable(list);
    }

    /**
     * 根据身份证号查询患者信息
     */
    @GetMapping(value = "/idCard/{idCard}")
    public AjaxResult getByIdCard(@PathVariable("idCard") String idCard)
    {
        Patient patient = patientService.selectPatientByIdCard(idCard);
        checkPatientScope(patient);
        return success(patient);
    }

    /**
     * 根据用户ID查询患者信息
     */
    @GetMapping(value = "/userId/{userId}")
    public AjaxResult getByUserId(@PathVariable("userId") Long userId)
    {
        if (isPatientUser() && !Objects.equals(SecurityUtils.getUserId(), userId))
        {
            throw new ServiceException("无权限访问其他患者信息");
        }
        return success(patientService.getPatientByUserId(userId));
    }

    /**
     * 导出患者列表
     */
    @RequiresPermissions("patient:patient:export")
    @Log(title = "患者", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Patient patient)
    {
        if (isNurseUser())
        {
            throw new ServiceException("护士岗位不能导出患者档案");
        }
        applyPatientScope(patient);
        List<Patient> list = patientService.selectPatientList(patient);
        ExcelUtil<Patient> util = new ExcelUtil<Patient>(Patient.class);
        util.exportExcel(response, list, "患者数据");
    }

    /**
     * 获取患者详细信息
     */
    @RequiresPermissions("patient:patient:query")
    @GetMapping(value = "/{patientId}")
    public AjaxResult getInfo(@PathVariable("patientId") Long patientId)
    {
        Patient patient = patientService.selectPatientByPatientId(patientId);
        checkPatientScope(patient);
        return success(patient);
    }

    @InnerAuth
    @GetMapping(value = "/inner/{patientId}")
    public R<Patient> innerInfo(@PathVariable("patientId") Long patientId)
    {
        return R.ok(patientService.selectPatientByPatientId(patientId));
    }
    
    /**
     * 内部接口：根据用户ID查询患者
     */
    @InnerAuth
    @GetMapping(value = "/inner/user/{userId}")
    public R<Patient> innerInfoByUserId(@PathVariable("userId") Long userId)
    {
        return R.ok(patientService.getPatientByUserId(userId));
    }
    
    /**
     * 内部接口：根据用户ID查询患者（返回Map）
     */
    @InnerAuth
    @GetMapping(value = "/getByUserId/{userId}")
    public R<java.util.Map<String, Object>> getPatientByUserId(@PathVariable("userId") Long userId)
    {
        Patient patient = patientService.getPatientByUserId(userId);
        if (patient == null) {
            return R.ok(null);
        }
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("patientId", patient.getPatientId());
        map.put("patientNo", patient.getPatientNo());
        map.put("userId", patient.getUserId());
        map.put("name", patient.getName());
        map.put("phone", patient.getPhone());
        return R.ok(map);
    }

    /**
     * 新增患者
     */
    @RequiresPermissions("patient:patient:add")
    @Log(title = "患者", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Patient patient)
    {
        if (isPatientUser())
        {
            throw new ServiceException("患者账号不能新增患者档案");
        }
        return toAjax(patientService.insertPatient(patient));
    }

    /**
     * 修改患者
     */
    @RequiresPermissions("patient:patient:edit")
    @Log(title = "患者", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Patient patient)
    {
        checkPatientScope(patientService.selectPatientByPatientId(patient.getPatientId()));
        if (isPatientUser())
        {
            patient.setUserId(SecurityUtils.getUserId());
        }
        return toAjax(patientService.updatePatient(patient));
    }

    /**
     * 删除患者
     */
    @RequiresPermissions("patient:patient:remove")
    @Log(title = "患者", businessType = BusinessType.DELETE)
	@DeleteMapping("/{patientIds}")
    public AjaxResult remove(@PathVariable Long[] patientIds)
    {
        if (isPatientUser())
        {
            throw new ServiceException("患者账号不能删除患者档案");
        }
        if (isNurseUser())
        {
            throw new ServiceException("护士岗位不能删除患者档案");
        }
        return toAjax(patientService.deletePatientByPatientIds(patientIds));
    }
    
    /**
     * 自助完善患者信息（登录用户使用）
     */
    @PostMapping("/complete")
    public AjaxResult complete(@RequestBody Patient patient)
    {
        // 自动获取当前登录用户ID
        Long userId = SecurityUtils.getUserId();
        patient.setUserId(userId);
        return toAjax(patientService.completePatient(patient));
    }

    private void applyPatientScope(Patient patient)
    {
        if (isPatientUser())
        {
            patient.setUserId(SecurityUtils.getUserId());
        }
    }

    private void checkPatientScope(Patient patient)
    {
        if (isPatientUser() && (patient == null || !Objects.equals(patient.getUserId(), SecurityUtils.getUserId())))
        {
            throw new ServiceException("无权限访问其他患者信息");
        }
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
}
