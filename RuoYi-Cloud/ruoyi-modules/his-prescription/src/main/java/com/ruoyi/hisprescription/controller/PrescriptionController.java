package com.ruoyi.hisprescription.controller;

import java.util.List;
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
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.hisprescription.domain.Prescription;
import com.ruoyi.hisprescription.service.IPrescriptionService;
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
    @Autowired
    private IPrescriptionService prescriptionService;

    /**
     * 查询处方主列表
     */
    @RequiresPermissions("hisprescription:prescription:list")
    @GetMapping("/list")
    public TableDataInfo list(Prescription prescription)
    {
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
        return success(prescriptionService.selectPrescriptionByPrescriptionId(prescriptionId));
    }

    /**
     * 新增处方主
     */
    @RequiresPermissions("hisprescription:prescription:add")
    @Log(title = "处方主", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Prescription prescription)
    {
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
        return toAjax(prescriptionService.deletePrescriptionByPrescriptionIds(prescriptionIds));
    }
}
