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
import com.ruoyi.hisprescription.domain.PrescriptionItem;
import com.ruoyi.hisprescription.service.IPrescriptionItemService;
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

    /**
     * 查询处方明细列表
     */
    @RequiresPermissions("hisprescription:item:list")
    @GetMapping("/list")
    public TableDataInfo list(PrescriptionItem prescriptionItem)
    {
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
        return success(prescriptionItemService.selectPrescriptionItemByItemId(itemId));
    }

    /**
     * 新增处方明细
     */
    @RequiresPermissions("hisprescription:item:add")
    @Log(title = "处方明细", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PrescriptionItem prescriptionItem)
    {
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
        return toAjax(prescriptionItemService.deletePrescriptionItemByItemIds(itemIds));
    }
}
