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
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.his.api.RemoteDoctorService;
import com.ruoyi.his.api.RemotePatientService;
import com.ruoyi.hisexam.domain.ExamApply;
import com.ruoyi.hisexam.domain.ExamResult;
import com.ruoyi.hisexam.service.IExamApplyService;
import com.ruoyi.hisexam.service.IExamResultService;
import com.ruoyi.system.api.domain.SysRole;
import com.ruoyi.system.api.model.LoginUser;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 检查检验结果Controller
 *
 * @author ruoyi
 * @date 2026-06-30
 */
@RestController
@RequestMapping("/result")
public class ExamResultController extends BaseController
{
    private static final Long PATIENT_ROLE_ID = 4L;
    private static final Long OUTPATIENT_DOCTOR_ROLE_ID = 5L;

    @Autowired
    private IExamResultService examResultService;

    @Autowired
    private IExamApplyService examApplyService;

    @Autowired
    private RemotePatientService remotePatientService;

    @Autowired
    private RemoteDoctorService remoteDoctorService;

    /**
     * 查询可录入结果的申请单选项。
     */
    @GetMapping("/applyOptions")
    public AjaxResult applyOptions()
    {
        List<Map<String, Object>> list = examResultService.selectExamApplyOptions();
        if (isPatientRole())
        {
            Long patientId = getCurrentPatientId();
            list.removeIf(item -> !Objects.equals(getLong(item, "patientId"), patientId));
        }
        if (isOutpatientDoctorRole())
        {
            Long doctorId = getCurrentDoctorId();
            list.removeIf(item -> !Objects.equals(getLong(item, "doctorId"), doctorId));
        }
        return success(list);
    }

    /**
     * 查询检查检验结果列表
     */
    @RequiresPermissions("hisexam:result:list")
    @GetMapping("/list")
    public TableDataInfo list(ExamResult examResult)
    {
        applyResultScope(examResult);
        startPage();
        List<ExamResult> list = examResultService.selectExamResultList(examResult);
        return getDataTable(list);
    }

    /**
     * 导出检查检验结果列表
     */
    @RequiresPermissions("hisexam:result:export")
    @Log(title = "检查检验结果", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ExamResult examResult)
    {
        applyResultScope(examResult);
        List<ExamResult> list = examResultService.selectExamResultList(examResult);
        ExcelUtil<ExamResult> util = new ExcelUtil<ExamResult>(ExamResult.class);
        util.exportExcel(response, list, "检查检验结果数据");
    }

    /**
     * 获取检查检验结果详细信息
     */
    @RequiresPermissions("hisexam:result:query")
    @GetMapping(value = "/{resultId}")
    public AjaxResult getInfo(@PathVariable("resultId") Long resultId)
    {
        ExamResult examResult = examResultService.selectExamResultByResultId(resultId);
        checkResultScope(examResult);
        return success(examResult);
    }

    /**
     * 新增检查检验结果
     */
    @RequiresPermissions("hisexam:result:add")
    @Log(title = "检查检验结果", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ExamResult examResult)
    {
        return toAjax(examResultService.insertExamResult(examResult));
    }

    @RequiresPermissions("hisexam:result:add")
    @Log(title = "检查检验结果", businessType = BusinessType.INSERT)
    @PostMapping("/report")
    public AjaxResult saveReport(@RequestBody List<ExamResult> examResults)
    {
        return toAjax(examResultService.saveExamResultReport(examResults));
    }

    /**
     * 修改检查检验结果
     */
    @RequiresPermissions("hisexam:result:edit")
    @Log(title = "检查检验结果", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ExamResult examResult)
    {
        return toAjax(examResultService.updateExamResult(examResult));
    }

    /**
     * 删除检查检验结果
     */
    @RequiresPermissions("hisexam:result:remove")
    @Log(title = "检查检验结果", businessType = BusinessType.DELETE)
    @DeleteMapping("/{resultIds}")
    public AjaxResult remove(@PathVariable Long[] resultIds)
    {
        return toAjax(examResultService.deleteExamResultByResultIds(resultIds));
    }

    private void applyResultScope(ExamResult examResult)
    {
        if (isPatientRole())
        {
            examResult.getParams().put("applyPatientId", getCurrentPatientId());
        }
        if (isOutpatientDoctorRole())
        {
            examResult.getParams().put("applyDoctorId", getCurrentDoctorId());
        }
    }

    private void checkResultScope(ExamResult examResult)
    {
        if (examResult == null)
        {
            throw new ServiceException("检查检验结果不存在");
        }
        if (!isPatientRole() && !isOutpatientDoctorRole())
        {
            return;
        }

        ExamApply examApply = examApplyService.selectExamApplyByApplyId(examResult.getApplyId());
        if (isPatientRole() && (examApply == null || !Objects.equals(examApply.getPatientId(), getCurrentPatientId())))
        {
            throw new ServiceException("无权限访问其他患者的检查检验结果");
        }
        if (isOutpatientDoctorRole() && (examApply == null || !Objects.equals(examApply.getDoctorId(), getCurrentDoctorId())))
        {
            throw new ServiceException("无权限访问其他医生的检查检验结果");
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
            throw new ServiceException("当前医生档案不存在，请先完善医生信息");
        }
        return doctorId instanceof Number ? ((Number) doctorId).longValue() : Long.valueOf(doctorId.toString());
    }

    private boolean isPatientRole()
    {
        return hasRoleId(PATIENT_ROLE_ID);
    }

    private boolean isOutpatientDoctorRole()
    {
        return hasRoleId(OUTPATIENT_DOCTOR_ROLE_ID);
    }

    private boolean hasRoleId(Long roleId)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        return !SecurityUtils.isAdmin()
                && loginUser != null
                && loginUser.getSysUser() != null
                && loginUser.getSysUser().getRoles() != null
                && loginUser.getSysUser().getRoles().stream()
                    .map(SysRole::getRoleId)
                    .anyMatch(id -> Objects.equals(id, roleId));
    }

    private Long getLong(Map<String, Object> source, String key)
    {
        Object value = source == null ? null : source.get(key);
        if (value == null)
        {
            return null;
        }
        return value instanceof Number ? ((Number) value).longValue() : Long.valueOf(value.toString());
    }
}
