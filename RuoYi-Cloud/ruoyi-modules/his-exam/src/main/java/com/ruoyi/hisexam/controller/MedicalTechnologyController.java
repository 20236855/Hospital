package com.ruoyi.hisexam.controller;

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
import com.ruoyi.hisexam.domain.MedicalTechnology;
import com.ruoyi.hisexam.service.IMedicalTechnologyService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 医技项目Controller
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
@RestController
@RequestMapping("/technology")
public class MedicalTechnologyController extends BaseController
{
    @Autowired
    private IMedicalTechnologyService medicalTechnologyService;

    /**
     * 查询医技项目列表
     */
    @RequiresPermissions("hisexam:technology:list")
    @GetMapping("/list")
    public TableDataInfo list(MedicalTechnology medicalTechnology)
    {
        startPage();
        List<MedicalTechnology> list = medicalTechnologyService.selectMedicalTechnologyList(medicalTechnology);
        return getDataTable(list);
    }

    /**
     * 查询检验申请可用医技项目，兼容历史INSPEC类型
     */
    @GetMapping("/lab/options")
    public AjaxResult labOptions()
    {
        return success(medicalTechnologyService.selectLabTechnologyOptions());
    }

    /**
     * 导出医技项目列表
     */
    @RequiresPermissions("hisexam:technology:export")
    @Log(title = "医技项目", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MedicalTechnology medicalTechnology)
    {
        List<MedicalTechnology> list = medicalTechnologyService.selectMedicalTechnologyList(medicalTechnology);
        ExcelUtil<MedicalTechnology> util = new ExcelUtil<MedicalTechnology>(MedicalTechnology.class);
        util.exportExcel(response, list, "医技项目数据");
    }

    /**
     * 获取医技项目详细信息
     */
    @RequiresPermissions("hisexam:technology:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(medicalTechnologyService.selectMedicalTechnologyById(id));
    }

    /**
     * 新增医技项目
     */
    @RequiresPermissions("hisexam:technology:add")
    @Log(title = "医技项目", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MedicalTechnology medicalTechnology)
    {
        return toAjax(medicalTechnologyService.insertMedicalTechnology(medicalTechnology));
    }

    /**
     * 修改医技项目
     */
    @RequiresPermissions("hisexam:technology:edit")
    @Log(title = "医技项目", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MedicalTechnology medicalTechnology)
    {
        return toAjax(medicalTechnologyService.updateMedicalTechnology(medicalTechnology));
    }

    /**
     * 删除医技项目
     */
    @RequiresPermissions("hisexam:technology:remove")
    @Log(title = "医技项目", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(medicalTechnologyService.deleteMedicalTechnologyByIds(ids));
    }
}
