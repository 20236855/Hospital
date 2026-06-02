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
import com.ruoyi.emr.domain.MedicalRecordDisease;
import com.ruoyi.emr.service.IMedicalRecordDiseaseService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 病历疾病关联Controller
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
@RestController
@RequestMapping("/recorddisease")
public class MedicalRecordDiseaseController extends BaseController
{
    @Autowired
    private IMedicalRecordDiseaseService medicalRecordDiseaseService;

    /**
     * 查询病历疾病关联列表
     */
    @RequiresPermissions("emr:recorddisease:list")
    @GetMapping("/list")
    public TableDataInfo list(MedicalRecordDisease medicalRecordDisease)
    {
        startPage();
        List<MedicalRecordDisease> list = medicalRecordDiseaseService.selectMedicalRecordDiseaseList(medicalRecordDisease);
        return getDataTable(list);
    }

    /**
     * 导出病历疾病关联列表
     */
    @RequiresPermissions("emr:recorddisease:export")
    @Log(title = "病历疾病关联", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MedicalRecordDisease medicalRecordDisease)
    {
        List<MedicalRecordDisease> list = medicalRecordDiseaseService.selectMedicalRecordDiseaseList(medicalRecordDisease);
        ExcelUtil<MedicalRecordDisease> util = new ExcelUtil<MedicalRecordDisease>(MedicalRecordDisease.class);
        util.exportExcel(response, list, "病历疾病关联数据");
    }

    /**
     * 获取病历疾病关联详细信息
     */
    @RequiresPermissions("emr:recorddisease:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(medicalRecordDiseaseService.selectMedicalRecordDiseaseById(id));
    }

    /**
     * 新增病历疾病关联
     */
    @RequiresPermissions("emr:recorddisease:add")
    @Log(title = "病历疾病关联", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MedicalRecordDisease medicalRecordDisease)
    {
        return toAjax(medicalRecordDiseaseService.insertMedicalRecordDisease(medicalRecordDisease));
    }

    /**
     * 修改病历疾病关联
     */
    @RequiresPermissions("emr:recorddisease:edit")
    @Log(title = "病历疾病关联", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MedicalRecordDisease medicalRecordDisease)
    {
        return toAjax(medicalRecordDiseaseService.updateMedicalRecordDisease(medicalRecordDisease));
    }

    /**
     * 删除病历疾病关联
     */
    @RequiresPermissions("emr:recorddisease:remove")
    @Log(title = "病历疾病关联", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(medicalRecordDiseaseService.deleteMedicalRecordDiseaseByIds(ids));
    }
}
