package com.ruoyi.hisexam.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.his.api.RemotePatientService;
import com.ruoyi.hisexam.domain.ExamApply;
import com.ruoyi.hisexam.service.IExamApplyService;
import com.ruoyi.system.api.domain.SysRole;
import com.ruoyi.system.api.model.LoginUser;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 检查/检验/处置申请单Controller
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
@RestController
@RequestMapping("/apply")
public class ExamApplyController extends BaseController
{
    private static final Long OUTPATIENT_DOCTOR_ROLE_ID = 5L;

    @Autowired
    private IExamApplyService examApplyService;

    @Autowired
    private RemotePatientService remotePatientService;

    @Autowired
    private com.ruoyi.his.api.RemoteDoctorService remoteDoctorService;

    /**
     * 查询检查/检验/处置申请单列表
     */
    @RequiresPermissions("hisexam:apply:list")
    @GetMapping("/list")
    public TableDataInfo list(ExamApply examApply)
    {
        applyPatientScope(examApply);
        applyDoctorScope(examApply);
        startPage();
        List<ExamApply> list = examApplyService.selectExamApplyList(examApply);
        return getDataTable(list);
    }

    /**
     * 查询可开检查/检验/处置申请单的挂号记录。
     */
    @RequiresPermissions("hisexam:apply:list")
    @GetMapping("/register/options")
    public AjaxResult registerOptions(@RequestParam(required = false) String keyword)
    {
        Long doctorId = isOutpatientDoctorRole() ? getCurrentDoctorId() : null;
        return success(examApplyService.selectExamRegisterOptions(keyword, doctorId));
    }

    /**
     * 导出检查/检验/处置申请单列表
     */
    @RequiresPermissions("hisexam:apply:export")
    @Log(title = "检查/检验/处置申请单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ExamApply examApply)
    {
        applyPatientScope(examApply);
        applyDoctorScope(examApply);
        List<ExamApply> list = examApplyService.selectExamApplyList(examApply);
        ExcelUtil<ExamApply> util = new ExcelUtil<ExamApply>(ExamApply.class);
        util.exportExcel(response, list, "检查/检验/处置申请单数据");
    }

    /**
     * 获取检查/检验/处置申请单详细信息
     */
    @RequiresPermissions("hisexam:apply:query")
    @GetMapping(value = "/{applyId}")
    public AjaxResult getInfo(@PathVariable("applyId") Long applyId)
    {
        ExamApply examApply = examApplyService.selectExamApplyByApplyId(applyId);
        checkPatientScope(examApply);
        checkDoctorScope(examApply);
        return success(examApply);
    }

    /**
     * 新增检查/检验/处置申请单
     */
    @RequiresPermissions("hisexam:apply:add")
    @Log(title = "检查/检验/处置申请单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ExamApply examApply)
    {
        applyPatientScope(examApply);
        applyCurrentDept(examApply);
        return toAjax(examApplyService.insertExamApply(examApply));
    }

    /**
     * 修改检查/检验/处置申请单
     */
    @RequiresPermissions("hisexam:apply:edit")
    @Log(title = "检查/检验/处置申请单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ExamApply examApply)
    {
        ExamApply oldApply = examApplyService.selectExamApplyByApplyId(examApply.getApplyId());
        checkPatientScope(oldApply);
        checkDoctorScope(oldApply);
        applyPatientScope(examApply);
        if (examApply.getRegisterId() != null)
        {
            applyCurrentDept(examApply);
        }
        return toAjax(examApplyService.updateExamApply(examApply));
    }

    /**
     * 删除检查/检验/处置申请单
     */
    @RequiresPermissions("hisexam:apply:remove")
    @Log(title = "检查/检验/处置申请单", businessType = BusinessType.DELETE)
	@DeleteMapping("/{applyIds}")
    public AjaxResult remove(@PathVariable Long[] applyIds)
    {
        if (isPatientUser() || isOutpatientDoctorRole())
        {
            for (Long applyId : applyIds)
            {
                ExamApply examApply = examApplyService.selectExamApplyByApplyId(applyId);
                checkPatientScope(examApply);
                checkDoctorScope(examApply);
            }
        }
        return toAjax(examApplyService.deleteExamApplyByApplyIds(applyIds));
    }

    private void applyPatientScope(ExamApply examApply)
    {
        if (isPatientUser())
        {
            examApply.setPatientId(getCurrentPatientId());
        }
    }

    private void checkPatientScope(ExamApply examApply)
    {
        if (isPatientUser() && (examApply == null || !Objects.equals(examApply.getPatientId(), getCurrentPatientId())))
        {
            throw new ServiceException("无权限访问其他患者信息");
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

    private void applyDoctorScope(ExamApply examApply)
    {
        if (isOutpatientDoctorRole())
        {
            examApply.setDoctorId(getCurrentDoctorId());
        }
    }

    private void checkDoctorScope(ExamApply examApply)
    {
        if (!isOutpatientDoctorRole())
        {
            return;
        }
        if (examApply == null || !Objects.equals(examApply.getDoctorId(), getCurrentDoctorId()))
        {
            throw new ServiceException("No permission to access other doctor's exam apply");
        }
    }

    private Long getCurrentDoctorId()
    {
        R<Map<String, Object>> doctorR = remoteDoctorService.getDoctorByUserId(SecurityUtils.getUserId(), SecurityConstants.INNER);
        if (R.isError(doctorR))
        {
            throw new ServiceException(doctorR.getMsg());
        }
        Map<String, Object> doctor = doctorR.getData();
        Object doctorId = doctor == null ? null : doctor.get("doctorId");
        if (doctorId == null)
        {
            throw new ServiceException("Current doctor profile not found, please complete doctor information first");
        }
        return doctorId instanceof Number ? ((Number) doctorId).longValue() : Long.valueOf(doctorId.toString());
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

    private void applyCurrentDept(ExamApply examApply)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null)
        {
            return;
        }
        Long deptId = loginUser.getDeptId();
        if (deptId == null && loginUser.getSysUser() != null)
        {
            deptId = loginUser.getSysUser().getDeptId();
        }
        if (deptId != null)
        {
            examApply.setDeptId(deptId);
        }
    }
}
