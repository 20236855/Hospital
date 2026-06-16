package com.ruoyi.hisdoctor.controller;

import java.util.Collections;
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
import com.ruoyi.hisdoctor.domain.Doctor;
import com.ruoyi.hisdoctor.service.IDoctorService;
import com.ruoyi.system.api.domain.SysRole;
import com.ruoyi.system.api.model.LoginUser;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 医生信息Controller
 *
 * @author ruoyi
 * @date 2026-05-30
 */
@RestController
@RequestMapping("/doctor")
public class DoctorController extends BaseController
{
    private static final Long OUTPATIENT_DOCTOR_ROLE_ID = 5L;
    private static final Long LAB_DOCTOR_ROLE_ID = 7L;

    @Autowired
    private IDoctorService doctorService;

    /**
     * 查询医生信息列表
     */
    @RequiresPermissions("hisdoctor:doctor:list")
    @GetMapping("/list")
    public TableDataInfo list(Doctor doctor)
    {
        applyDoctorScope(doctor);
        startPage();
        List<Doctor> list = doctorService.selectDoctorList(doctor);
        return getDataTable(list);
    }

    /**
     * 查询可创建医生档案的系统用户
     */
    @RequiresPermissions("hisdoctor:doctor:list")
    @GetMapping("/availableUsers")
    public AjaxResult availableUsers()
    {
        if (isOutpatientDoctorRole())
        {
            return success(Collections.emptyList());
        }
        return success(doctorService.selectAvailableDoctorUsers());
    }

    /**
     * 导出医生信息列表
     */
    @RequiresPermissions("hisdoctor:doctor:export")
    @Log(title = "医生信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Doctor doctor)
    {
        applyDoctorScope(doctor);
        List<Doctor> list = doctorService.selectDoctorList(doctor);
        ExcelUtil<Doctor> util = new ExcelUtil<Doctor>(Doctor.class);
        util.exportExcel(response, list, "医生信息数据");
    }

    /**
     * 获取医生信息详细信息
     */
    @RequiresPermissions("hisdoctor:doctor:query")
    @GetMapping(value = "/{doctorId}")
    public AjaxResult getInfo(@PathVariable("doctorId") Long doctorId)
    {
        Doctor doctor = doctorService.selectDoctorByDoctorId(doctorId);
        checkDoctorScope(doctor);
        return success(doctor);
    }

    /**
     * 根据用户ID查询医生信息
     */
    @GetMapping(value = "/userId/{userId}")
    public AjaxResult getByUserId(@PathVariable("userId") Long userId)
    {
        Doctor doctor = doctorService.getDoctorByUserId(userId);
        checkDoctorScope(doctor);
        return success(doctor);
    }

    /**
     * 内部接口：根据用户ID查询医生
     */
    @InnerAuth
    @GetMapping(value = "/inner/user/{userId}")
    public R<Doctor> innerInfoByUserId(@PathVariable("userId") Long userId)
    {
        return R.ok(doctorService.getDoctorByUserId(userId));
    }

    /**
     * 内部接口：根据用户ID查询医生（返回Map）
     */
    @InnerAuth
    @GetMapping(value = "/getByUserId/{userId}")
    public R<java.util.Map<String, Object>> getDoctorByUserId(@PathVariable("userId") Long userId)
    {
        Doctor doctor = doctorService.getDoctorByUserId(userId);
        if (doctor == null) {
            return R.ok(null);
        }
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("doctorId", doctor.getDoctorId());
        map.put("doctorNo", doctor.getDoctorNo());
        map.put("userId", doctor.getUserId());
        map.put("deptId", doctor.getDeptId());
        map.put("doctorName", doctor.getDoctorName());
        map.put("phone", doctor.getPhone());
        return R.ok(map);
    }

    /**
     * 新增医生信息
     */
    @RequiresPermissions("hisdoctor:doctor:add")
    @Log(title = "医生信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Doctor doctor)
    {
        if (isOutpatientDoctorRole())
        {
            doctor.setUserId(SecurityUtils.getUserId());
        }
        return toAjax(doctorService.insertDoctor(doctor));
    }

    /**
     * 修改医生信息
     */
    @RequiresPermissions("hisdoctor:doctor:edit")
    @Log(title = "医生信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Doctor doctor)
    {
        checkDoctorScope(doctorService.selectDoctorByDoctorId(doctor.getDoctorId()));
        if (isOutpatientDoctorRole())
        {
            doctor.setUserId(SecurityUtils.getUserId());
        }
        return toAjax(doctorService.updateDoctor(doctor));
    }

    /**
     * 删除医生信息
     */
    @RequiresPermissions("hisdoctor:doctor:remove")
    @Log(title = "医生信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{doctorIds}")
    public AjaxResult remove(@PathVariable Long[] doctorIds)
    {
        if (isOutpatientDoctorRole())
        {
            for (Long doctorId : doctorIds)
            {
                checkDoctorScope(doctorService.selectDoctorByDoctorId(doctorId));
            }
        }
        return toAjax(doctorService.deleteDoctorByDoctorIds(doctorIds));
    }

    /**
     * 自助完善医生信息（登录用户使用）
     */
    @PostMapping("/complete")
    public AjaxResult complete(@RequestBody Doctor doctor)
    {
        // 自动获取当前登录用户ID
        Long userId = SecurityUtils.getUserId();
        doctor.setUserId(userId);
        return toAjax(doctorService.completeDoctor(doctor));
    }

    /**
     * 查询医生信息列表（患者端使用，无需权限）
     */
    @GetMapping("/open/list")
    public TableDataInfo openList(Doctor doctor)
    {
        startPage();
        List<Doctor> list = doctorService.selectDoctorList(doctor);
        return getDataTable(list);
    }

    private void applyDoctorScope(Doctor doctor)
    {
        if (isOutpatientDoctorRole() || isLabDoctorRole())
        {
            doctor.setDoctorId(getCurrentDoctorId());
        }
    }

    private void checkDoctorScope(Doctor doctor)
    {
        if ((isOutpatientDoctorRole() || isLabDoctorRole()) && (doctor == null || !Objects.equals(doctor.getDoctorId(), getCurrentDoctorId())))
        {
            throw new ServiceException("无权限访问其他医生信息");
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

    private boolean isOutpatientDoctorRole()
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

    private boolean isLabDoctorRole()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        return !SecurityUtils.isAdmin()
                && loginUser != null
                && loginUser.getSysUser() != null
                && loginUser.getSysUser().getRoles() != null
                && loginUser.getSysUser().getRoles().stream()
                    .map(SysRole::getRoleId)
                    .anyMatch(roleId -> Objects.equals(roleId, LAB_DOCTOR_ROLE_ID));
    }
}
