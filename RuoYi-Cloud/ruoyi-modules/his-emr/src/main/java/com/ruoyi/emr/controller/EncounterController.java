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
import com.ruoyi.emr.domain.Encounter;
import com.ruoyi.emr.domain.vo.EncounterVo;
import com.ruoyi.emr.service.IEncounterService;
import com.ruoyi.his.api.RemoteDoctorService;
import com.ruoyi.his.api.RemotePatientService;
import com.ruoyi.system.api.domain.SysRole;
import com.ruoyi.system.api.model.LoginUser;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 接诊Controller
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
@RestController
@RequestMapping("/encounter")
public class EncounterController extends BaseController
{
    private static final Long OUTPATIENT_DOCTOR_ROLE_ID = 5L;

    @Autowired
    private IEncounterService encounterService;

    @Autowired
    private RemotePatientService remotePatientService;

    @Autowired
    private RemoteDoctorService remoteDoctorService;

    /**
     * 查询接诊列表
     */
    @RequiresPermissions("emr:encounter:list")
    @GetMapping("/list")
    public TableDataInfo list(Encounter encounter)
    {
        applyPatientScope(encounter);
        applyDoctorScope(encounter);
        startPage();
        List<EncounterVo> list = encounterService.selectEncounterList(encounter);
        return getDataTable(list);
    }

    /**
     * 导出接诊列表
     */
    @RequiresPermissions("emr:encounter:export")
    @Log(title = "接诊", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Encounter encounter)
    {
        applyPatientScope(encounter);
        applyDoctorScope(encounter);
        List<EncounterVo> list = encounterService.selectEncounterList(encounter);
        ExcelUtil<EncounterVo> util = new ExcelUtil<EncounterVo>(EncounterVo.class);
        util.exportExcel(response, list, "接诊数据");
    }

    /**
     * 获取接诊详细信息
     */
    @RequiresPermissions("emr:encounter:query")
    @GetMapping(value = "/{encounterId}")
    public AjaxResult getInfo(@PathVariable("encounterId") Long encounterId)
    {
        Encounter encounter = encounterService.selectEncounterByEncounterId(encounterId);
        checkPatientScope(encounter);
        checkDoctorScope(encounter);
        return success(encounter);
    }

    /**
     * 新增接诊
     */
    @RequiresPermissions("emr:encounter:add")
    @Log(title = "接诊", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Encounter encounter)
    {
        applyPatientScope(encounter);
        applyDoctorScope(encounter);
        return toAjax(encounterService.insertEncounter(encounter));
    }

    /**
     * 修改接诊
     */
    @RequiresPermissions("emr:encounter:edit")
    @Log(title = "接诊", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Encounter encounter)
    {
        Encounter oldEncounter = encounterService.selectEncounterByEncounterId(encounter.getEncounterId());
        checkPatientScope(oldEncounter);
        checkDoctorScope(oldEncounter);
        applyPatientScope(encounter);
        return toAjax(encounterService.updateEncounter(encounter));
    }

    /**
     * 删除接诊
     */
    @RequiresPermissions("emr:encounter:remove")
    @Log(title = "接诊", businessType = BusinessType.DELETE)
	@DeleteMapping("/{encounterIds}")
    public AjaxResult remove(@PathVariable Long[] encounterIds)
    {
        for (Long encounterId : encounterIds)
        {
            Encounter encounter = encounterService.selectEncounterByEncounterId(encounterId);
            checkPatientScope(encounter);
            checkDoctorScope(encounter);
        }
        return toAjax(encounterService.deleteEncounterByEncounterIds(encounterIds));
    }

    private void applyPatientScope(Encounter encounter)
    {
        if (isPatientUser())
        {
            encounter.setPatientId(getCurrentPatientId());
        }
    }

    private void checkPatientScope(Encounter encounter)
    {
        if (isPatientUser() && (encounter == null || !Objects.equals(encounter.getPatientId(), getCurrentPatientId())))
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

    private void applyDoctorScope(Encounter encounter)
    {
        if (isOutpatientDoctorRole())
        {
            encounter.setDoctorId(getCurrentDoctorId());
        }
    }

    private void checkDoctorScope(Encounter encounter)
    {
        if (!isOutpatientDoctorRole())
        {
            return;
        }
        if (encounter == null || !Objects.equals(encounter.getDoctorId(), getCurrentDoctorId()))
        {
            throw new ServiceException("无权限访问其他医生的接诊信息");
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
