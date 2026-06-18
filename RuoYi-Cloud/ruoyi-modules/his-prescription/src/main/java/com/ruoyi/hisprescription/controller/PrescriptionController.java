package com.ruoyi.hisprescription.controller;

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
import com.ruoyi.his.api.RemotePatientService;
import com.ruoyi.hisprescription.domain.Prescription;
import com.ruoyi.hisprescription.service.IPrescriptionService;
import com.ruoyi.system.api.domain.SysRole;
import com.ruoyi.system.api.model.LoginUser;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 处方主Controller
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
@RestController
@RequestMapping("/prescription")
public class PrescriptionController extends BaseController
{
    private static final Long OUTPATIENT_DOCTOR_ROLE_ID = 5L;

    @Autowired
    private IPrescriptionService prescriptionService;

    @Autowired
    private RemotePatientService remotePatientService;

    @Autowired
    private com.ruoyi.his.api.RemoteDoctorService remoteDoctorService;

    /**
     * 查询处方主列表
     */
    @RequiresPermissions("hisprescription:prescription:list")
    @GetMapping("/list")
    public TableDataInfo list(Prescription prescription)
    {
        applyPatientScope(prescription);
        applyDoctorScope(prescription);
        startPage();
        List<Prescription> list = prescriptionService.selectPrescriptionList(prescription);
        return getDataTable(list);
    }

    /**
     * 导出处方主列表
     */
    @RequiresPermissions("hisprescription:prescription:export")
    @Log(title = "处方主", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Prescription prescription)
    {
        applyPatientScope(prescription);
        applyDoctorScope(prescription);
        List<Prescription> list = prescriptionService.selectPrescriptionList(prescription);
        ExcelUtil<Prescription> util = new ExcelUtil<Prescription>(Prescription.class);
        util.exportExcel(response, list, "处方主数据");
    }

    /**
     * 获取处方主详细信息
     */
    @RequiresPermissions("hisprescription:prescription:query")
    @GetMapping(value = "/{prescriptionId}")
    public AjaxResult getInfo(@PathVariable("prescriptionId") Long prescriptionId)
    {
        Prescription prescription = prescriptionService.selectPrescriptionByPrescriptionId(prescriptionId);
        checkPatientScope(prescription);
        checkDoctorScope(prescription);
        return success(prescription);
    }

    /**
     * 新增处方主
     */
    @RequiresPermissions("hisprescription:prescription:add")
    @Log(title = "处方主", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Prescription prescription)
    {
        applyPatientScope(prescription);
        // 兜底：确保处方单号不为空（service层也会自动生成）
        if (prescription.getPrescriptionNo() == null || prescription.getPrescriptionNo().isEmpty()) {
            String dateStr = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
            int rand = new java.util.Random().nextInt(9000) + 1000;
            prescription.setPrescriptionNo("CF" + dateStr + rand);
        }
        return toAjax(prescriptionService.insertPrescription(prescription));
    }

    /**
     * 修改处方主
     */
    @RequiresPermissions("hisprescription:prescription:edit")
    @Log(title = "处方主", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Prescription prescription)
    {
        Prescription oldPrescription = prescriptionService.selectPrescriptionByPrescriptionId(prescription.getPrescriptionId());
        checkPatientScope(oldPrescription);
        checkDoctorScope(oldPrescription);
        applyPatientScope(prescription);
        return toAjax(prescriptionService.updatePrescription(prescription));
    }

    /**
     * 删除处方主
     */
    @RequiresPermissions("hisprescription:prescription:remove")
    @Log(title = "处方主", businessType = BusinessType.DELETE)
	@DeleteMapping("/{prescriptionIds}")
    public AjaxResult remove(@PathVariable Long[] prescriptionIds)
    {
        if (isPatientUser() || isOutpatientDoctorRole())
        {
            for (Long prescriptionId : prescriptionIds)
            {
                Prescription prescription = prescriptionService.selectPrescriptionByPrescriptionId(prescriptionId);
                checkPatientScope(prescription);
                checkDoctorScope(prescription);
            }
        }
        return toAjax(prescriptionService.deletePrescriptionByPrescriptionIds(prescriptionIds));
    }

    private void applyPatientScope(Prescription prescription)
    {
        if (isPatientUser())
        {
            prescription.setPatientId(getCurrentPatientId());
        }
    }

    private void checkPatientScope(Prescription prescription)
    {
        if (isPatientUser() && (prescription == null || !Objects.equals(prescription.getPatientId(), getCurrentPatientId())))
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

    private void applyDoctorScope(Prescription prescription)
    {
        if (isOutpatientDoctorRole())
        {
            prescription.setDoctorId(getCurrentDoctorId());
        }
    }

    private void checkDoctorScope(Prescription prescription)
    {
        if (!isOutpatientDoctorRole())
        {
            return;
        }
        if (prescription == null || !Objects.equals(prescription.getDoctorId(), getCurrentDoctorId()))
        {
            throw new ServiceException("No permission to access other doctor's prescription");
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
}
