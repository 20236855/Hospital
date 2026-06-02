package com.ruoyi.payment.controller;

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
import com.ruoyi.payment.domain.SettleCategory;
import com.ruoyi.payment.service.ISettleCategoryService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 结算类别Controller
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
@RestController
@RequestMapping("/category")
public class SettleCategoryController extends BaseController
{
    @Autowired
    private ISettleCategoryService settleCategoryService;

    /**
     * 查询结算类别列表
     */
    @RequiresPermissions("payment:category:list")
    @GetMapping("/list")
    public TableDataInfo list(SettleCategory settleCategory)
    {
        startPage();
        List<SettleCategory> list = settleCategoryService.selectSettleCategoryList(settleCategory);
        return getDataTable(list);
    }

    /**
     * 导出结算类别列表
     */
    @RequiresPermissions("payment:category:export")
    @Log(title = "结算类别", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SettleCategory settleCategory)
    {
        List<SettleCategory> list = settleCategoryService.selectSettleCategoryList(settleCategory);
        ExcelUtil<SettleCategory> util = new ExcelUtil<SettleCategory>(SettleCategory.class);
        util.exportExcel(response, list, "结算类别数据");
    }

    /**
     * 获取结算类别详细信息
     */
    @RequiresPermissions("payment:category:query")
    @GetMapping(value = "/{categoryId}")
    public AjaxResult getInfo(@PathVariable("categoryId") Long categoryId)
    {
        return success(settleCategoryService.selectSettleCategoryByCategoryId(categoryId));
    }

    /**
     * 新增结算类别
     */
    @RequiresPermissions("payment:category:add")
    @Log(title = "结算类别", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SettleCategory settleCategory)
    {
        return toAjax(settleCategoryService.insertSettleCategory(settleCategory));
    }

    /**
     * 修改结算类别
     */
    @RequiresPermissions("payment:category:edit")
    @Log(title = "结算类别", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SettleCategory settleCategory)
    {
        return toAjax(settleCategoryService.updateSettleCategory(settleCategory));
    }

    /**
     * 删除结算类别
     */
    @RequiresPermissions("payment:category:remove")
    @Log(title = "结算类别", businessType = BusinessType.DELETE)
	@DeleteMapping("/{categoryIds}")
    public AjaxResult remove(@PathVariable Long[] categoryIds)
    {
        return toAjax(settleCategoryService.deleteSettleCategoryByCategoryIds(categoryIds));
    }
}
