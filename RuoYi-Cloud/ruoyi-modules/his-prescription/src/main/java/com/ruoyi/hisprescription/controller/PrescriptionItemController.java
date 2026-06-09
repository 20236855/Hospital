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
import com.ruoyi.hisprescription.domain.PrescriptionItem;
import com.ruoyi.hisprescription.service.IPrescriptionService;
import com.ruoyi.hisprescription.service.IPrescriptionItemService;
import com.ruoyi.system.api.model.LoginUser;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 处方明细Controller
 * 
 * @author ruoyi
 * @date 2026-06-03
 */
@RestController
@RequestMapping("/item")
public class PrescriptionItemController extends BaseController
{
    @Autowired
    private IPrescriptionItemService prescriptionItemService;

    @Autowired
    private IPrescriptionService prescriptionService;

    @Autowired
    private RemotePatientService remotePatientService;

    /**
     * 查询处方明细列表
     */
    @RequiresPermissions("hisprescription:item:list")
    @GetMapping("/list")
    public TableDataInfo list(PrescriptionItem prescriptionItem)
    {
        applyPatientScope(prescriptionItem);
        startPage();
        List<PrescriptionItem> list = prescriptionItemService.selectPrescriptionItemList(prescriptionItem);
        return getDataTable(list);
    }

    /**
     * 导出处方明细列表
     */
    @RequiresPermissions("hisprescription:item:export")
    @Log(title = "处方明细", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, PrescriptionItem prescriptionItem)
    {
        applyPatientScope(prescriptionItem);
        List<PrescriptionItem> list = prescriptionItemService.selectPrescriptionItemList(prescriptionItem);
        ExcelUtil<PrescriptionItem> util = new ExcelUtil<PrescriptionItem>(PrescriptionItem.class);
        util.exportExcel(response, list, "处方明细数据");
    }

    /**
     * 获取处方明细详细信息
     */
    @RequiresPermissions("hisprescription:item:query")
    @GetMapping(value = "/{itemId}")
    public AjaxResult getInfo(@PathVariable("itemId") Long itemId)
    {
        PrescriptionItem prescriptionItem = prescriptionItemService.selectPrescriptionItemByItemId(itemId);
        checkPatientScope(prescriptionItem);
        return success(prescriptionItem);
    }

    /**
     * 新增处方明细
     */
    @RequiresPermissions("hisprescription:item:add")
    @Log(title = "处方明细", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PrescriptionItem prescriptionItem)
    {
        checkPatientScope(prescriptionItem);
        return toAjax(prescriptionItemService.insertPrescriptionItem(prescriptionItem));
    }

    /**
     * 修改处方明细
     */
    @RequiresPermissions("hisprescription:item:edit")
    @Log(title = "处方明细", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PrescriptionItem prescriptionItem)
    {
        checkPatientScope(prescriptionItemService.selectPrescriptionItemByItemId(prescriptionItem.getItemId()));
        checkPatientScope(prescriptionItem);
        return toAjax(prescriptionItemService.updatePrescriptionItem(prescriptionItem));
    }

    /**
     * 删除处方明细
     */
    @RequiresPermissions("hisprescription:item:remove")
    @Log(title = "处方明细", businessType = BusinessType.DELETE)
	@DeleteMapping("/{itemIds}")
    public AjaxResult remove(@PathVariable Long[] itemIds)
    {
        if (isPatientUser())
        {
            for (Long itemId : itemIds)
            {
                checkPatientScope(prescriptionItemService.selectPrescriptionItemByItemId(itemId));
            }
        }
        return toAjax(prescriptionItemService.deletePrescriptionItemByItemIds(itemIds));
    }

    private void applyPatientScope(PrescriptionItem prescriptionItem)
    {
        if (isPatientUser())
        {
            prescriptionItem.setPatientId(getCurrentPatientId());
        }
    }

    private void checkPatientScope(PrescriptionItem prescriptionItem)
    {
        if (!isPatientUser())
        {
            return;
        }
        if (prescriptionItem == null || prescriptionItem.getPrescriptionId() == null)
        {
            throw new ServiceException("无权限访问其他患者信息");
        }
        Prescription prescription = prescriptionService.selectPrescriptionByPrescriptionId(prescriptionItem.getPrescriptionId());
        if (prescription == null || !Objects.equals(prescription.getPatientId(), getCurrentPatientId()))
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
}
