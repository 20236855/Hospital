package com.ruoyi.emr.controller;

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
import com.ruoyi.emr.domain.MedicalRecord;
import com.ruoyi.emr.service.IMedicalRecordService;
import com.ruoyi.his.api.RemoteDoctorService;
import com.ruoyi.his.api.RemotePatientService;
import com.ruoyi.system.api.domain.SysRole;
import com.ruoyi.system.api.model.LoginUser;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 电子病历Controller
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
@RestController
@RequestMapping("/record")
public class MedicalRecordController extends BaseController
{
    private static final Long OUTPATIENT_DOCTOR_ROLE_ID = 5L;

    @Autowired
    private IMedicalRecordService medicalRecordService;

    @Autowired
    private RemotePatientService remotePatientService;

    @Autowired
    private RemoteDoctorService remoteDoctorService;

    /**
     * 查询电子病历列表
     */
    @RequiresPermissions("emr:record:list")
    @GetMapping("/list")
    public TableDataInfo list(MedicalRecord medicalRecord)
    {
        applyPatientScope(medicalRecord);
        applyDoctorScope(medicalRecord);
        startPage();
        List<MedicalRecord> list = medicalRecordService.selectMedicalRecordList(medicalRecord);
        return getDataTable(list);
    }

    /**
     * 导出电子病历列表
     */
    @RequiresPermissions("emr:record:export")
    @Log(title = "电子病历", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MedicalRecord medicalRecord)
    {
        applyPatientScope(medicalRecord);
        applyDoctorScope(medicalRecord);
        List<MedicalRecord> list = medicalRecordService.selectMedicalRecordList(medicalRecord);
        ExcelUtil<MedicalRecord> util = new ExcelUtil<MedicalRecord>(MedicalRecord.class);
        util.exportExcel(response, list, "电子病历数据");
    }

    /**
     * 获取电子病历详细信息
     */
    @RequiresPermissions("emr:record:query")
    @GetMapping(value = "/{recordId}")
    public AjaxResult getInfo(@PathVariable("recordId") Long recordId)
    {
        MedicalRecord medicalRecord = medicalRecordService.selectMedicalRecordByRecordId(recordId);
        checkPatientScope(medicalRecord);
        checkDoctorScope(medicalRecord);
        return success(medicalRecord);
    }

    /**
     * 新增电子病历
     */
    @RequiresPermissions("emr:record:add")
    @Log(title = "电子病历", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MedicalRecord medicalRecord)
    {
        checkPatientReadonly();
        checkDoctorScopeOnCreate(medicalRecord);
        return toAjax(medicalRecordService.insertMedicalRecord(medicalRecord));
    }

    /**
     * 保存电子病历（存在则更新，不存在则新增，适用于AI一键生成等场景）
     */
    @RequiresPermissions("emr:record:add")
    @Log(title = "电子病历", businessType = BusinessType.INSERT)
    @PostMapping("/save")
    public AjaxResult save(@RequestBody MedicalRecord medicalRecord)
    {
        checkPatientReadonly();
        if (medicalRecord.getEncounterId() == null)
        {
            return error("接诊ID不能为空");
        }
        // 先校验医生权限：通过 encounterId 查询关联的 doctorId
        checkDoctorScopeOnCreate(medicalRecord);
        return toAjax(medicalRecordService.saveMedicalRecord(medicalRecord));
    }

    /**
     * 修改电子病历
     */
    @RequiresPermissions("emr:record:edit")
    @Log(title = "电子病历", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MedicalRecord medicalRecord)
    {
        checkPatientReadonly();
        MedicalRecord oldRecord = medicalRecordService.selectMedicalRecordByRecordId(medicalRecord.getRecordId());
        checkDoctorScope(oldRecord);
        return toAjax(medicalRecordService.updateMedicalRecord(medicalRecord));
    }

    /**
     * 删除电子病历
     */
    @RequiresPermissions("emr:record:remove")
    @Log(title = "电子病历", businessType = BusinessType.DELETE)
	@DeleteMapping("/{recordIds}")
    public AjaxResult remove(@PathVariable Long[] recordIds)
    {
        checkPatientReadonly();
        for (Long recordId : recordIds)
        {
            MedicalRecord medicalRecord = medicalRecordService.selectMedicalRecordByRecordId(recordId);
            checkDoctorScope(medicalRecord);
        }
        return toAjax(medicalRecordService.deleteMedicalRecordByRecordIds(recordIds));
    }

    private void applyPatientScope(MedicalRecord medicalRecord)
    {
        if (isPatientUser())
        {
            medicalRecord.setPatientId(getCurrentPatientId());
        }
    }

    private void checkPatientScope(MedicalRecord medicalRecord)
    {
        if (isPatientUser() && (medicalRecord == null || !Objects.equals(medicalRecord.getPatientId(), getCurrentPatientId())))
        {
            throw new ServiceException("无权限访问其他患者信息");
        }
    }

    private void checkPatientReadonly()
    {
        if (isPatientUser())
        {
            throw new ServiceException("患者无权限修改电子病历信息");
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

    private void applyDoctorScope(MedicalRecord medicalRecord)
    {
        if (isOutpatientDoctorRole())
        {
            medicalRecord.setDoctorId(getCurrentDoctorId());
        }
    }

    private void checkDoctorScope(MedicalRecord medicalRecord)
    {
        if (!isOutpatientDoctorRole())
        {
            return;
        }
        if (medicalRecord == null || !Objects.equals(medicalRecord.getDoctorId(), getCurrentDoctorId()))
        {
            throw new ServiceException("无权限访问其他医生的电子病历信息");
        }
    }

    private void checkDoctorScopeOnCreate(MedicalRecord medicalRecord)
    {
        if (!isOutpatientDoctorRole())
        {
            return;
        }
        if (medicalRecord == null || medicalRecord.getEncounterId() == null)
        {
            throw new ServiceException("电子病历缺少接诊ID，不能校验医生权限");
        }
        Long currentDoctorId = getCurrentDoctorId();
        Long recordDoctorId = medicalRecordService.selectDoctorIdByEncounterId(medicalRecord.getEncounterId());
        if (recordDoctorId == null)
        {
            throw new ServiceException("接诊记录不存在或未绑定医生，不能创建电子病历");
        }
        if (!Objects.equals(recordDoctorId, currentDoctorId))
        {
            throw new ServiceException("无权限为其他医生的接诊记录创建电子病历");
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
            throw new ServiceException("当前医生档案不存在，请先完善医生信息");
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
}
