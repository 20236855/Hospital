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
import com.ruoyi.hisprescription.domain.Drug;
import com.ruoyi.hisprescription.service.IDrugService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 药品信息Controller
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
@RestController
@RequestMapping("/drug")
public class DrugController extends BaseController
{
    @Autowired
    private IDrugService drugService;

    /**
     * 查询药品信息列表
     */
    @RequiresPermissions("hisprescription:drug:list")
    @GetMapping("/list")
    public TableDataInfo list(Drug drug)
    {
        startPage();
        List<Drug> list = drugService.selectDrugList(drug);
        return getDataTable(list);
    }

    /**
     * 导出药品信息列表
     */
    @RequiresPermissions("hisprescription:drug:export")
    @Log(title = "药品信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Drug drug)
    {
        List<Drug> list = drugService.selectDrugList(drug);
        ExcelUtil<Drug> util = new ExcelUtil<Drug>(Drug.class);
        util.exportExcel(response, list, "药品信息数据");
    }

    /**
     * 获取药品信息详细信息
     */
    @RequiresPermissions("hisprescription:drug:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(drugService.selectDrugById(id));
    }

    /**
     * 新增药品信息
     */
    @RequiresPermissions("hisprescription:drug:add")
    @Log(title = "药品信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Drug drug)
    {
        return toAjax(drugService.insertDrug(drug));
    }

    /**
     * 修改药品信息
     */
    @RequiresPermissions("hisprescription:drug:edit")
    @Log(title = "药品信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Drug drug)
    {
        return toAjax(drugService.updateDrug(drug));
    }

    /**
     * 删除药品信息
     */
    @RequiresPermissions("hisprescription:drug:remove")
    @Log(title = "药品信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(drugService.deleteDrugByIds(ids));
    }
}
