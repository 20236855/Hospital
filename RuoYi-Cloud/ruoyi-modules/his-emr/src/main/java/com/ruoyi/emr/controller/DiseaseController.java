package com.ruoyi.emr.controller;

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
import com.ruoyi.emr.domain.Disease;
import com.ruoyi.emr.service.IDiseaseService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 疾病字典Controller
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
@RestController
@RequestMapping("/disease")
public class DiseaseController extends BaseController
{
    @Autowired
    private IDiseaseService diseaseService;

    /**
     * 查询疾病字典列表
     */
    @RequiresPermissions("emr:disease:list")
    @GetMapping("/list")
    public TableDataInfo list(Disease disease)
    {
        startPage();
        List<Disease> list = diseaseService.selectDiseaseList(disease);
        return getDataTable(list);
    }

    /**
     * 导出疾病字典列表
     */
    @RequiresPermissions("emr:disease:export")
    @Log(title = "疾病字典", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Disease disease)
    {
        List<Disease> list = diseaseService.selectDiseaseList(disease);
        ExcelUtil<Disease> util = new ExcelUtil<Disease>(Disease.class);
        util.exportExcel(response, list, "疾病字典数据");
    }

    /**
     * 获取疾病字典详细信息
     */
    @RequiresPermissions("emr:disease:query")
    @GetMapping(value = "/{diseaseId}")
    public AjaxResult getInfo(@PathVariable("diseaseId") Long diseaseId)
    {
        return success(diseaseService.selectDiseaseByDiseaseId(diseaseId));
    }

    /**
     * 新增疾病字典
     */
    @RequiresPermissions("emr:disease:add")
    @Log(title = "疾病字典", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Disease disease)
    {
        return toAjax(diseaseService.insertDisease(disease));
    }

    /**
     * 修改疾病字典
     */
    @RequiresPermissions("emr:disease:edit")
    @Log(title = "疾病字典", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Disease disease)
    {
        return toAjax(diseaseService.updateDisease(disease));
    }

    /**
     * 删除疾病字典
     */
    @RequiresPermissions("emr:disease:remove")
    @Log(title = "疾病字典", businessType = BusinessType.DELETE)
	@DeleteMapping("/{diseaseIds}")
    public AjaxResult remove(@PathVariable Long[] diseaseIds)
    {
        return toAjax(diseaseService.deleteDiseaseByDiseaseIds(diseaseIds));
    }
}
