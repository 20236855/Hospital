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
import com.ruoyi.emr.domain.MedicalRecord;
import com.ruoyi.emr.service.IMedicalRecordService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 电子病历Controller
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
@RestController
@RequestMapping("/record")
public class MedicalRecordController extends BaseController
{
    @Autowired
    private IMedicalRecordService medicalRecordService;

    /**
     * 查询电子病历列表
     */
    @RequiresPermissions("emr:record:list")
    @GetMapping("/list")
    public TableDataInfo list(MedicalRecord medicalRecord)
    {
        startPage();
        List<MedicalRecord> list = medicalRecordService.selectMedicalRecordList(medicalRecord);
        return getDataTable(list);
    }

    /**
     * 导出电子病历列表
     */
    @RequiresPermissions("emr:record:export")
    @Log(title = "电子病历", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MedicalRecord medicalRecord)
    {
        List<MedicalRecord> list = medicalRecordService.selectMedicalRecordList(medicalRecord);
        ExcelUtil<MedicalRecord> util = new ExcelUtil<MedicalRecord>(MedicalRecord.class);
        util.exportExcel(response, list, "电子病历数据");
    }

    /**
     * 获取电子病历详细信息
     */
    @RequiresPermissions("emr:record:query")
    @GetMapping(value = "/{recordId}")
    public AjaxResult getInfo(@PathVariable("recordId") Long recordId)
    {
        return success(medicalRecordService.selectMedicalRecordByRecordId(recordId));
    }

    /**
     * 新增电子病历
     */
    @RequiresPermissions("emr:record:add")
    @Log(title = "电子病历", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MedicalRecord medicalRecord)
    {
        return toAjax(medicalRecordService.insertMedicalRecord(medicalRecord));
    }

    /**
     * 修改电子病历
     */
    @RequiresPermissions("emr:record:edit")
    @Log(title = "电子病历", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MedicalRecord medicalRecord)
    {
        return toAjax(medicalRecordService.updateMedicalRecord(medicalRecord));
    }

    /**
     * 删除电子病历
     */
    @RequiresPermissions("emr:record:remove")
    @Log(title = "电子病历", businessType = BusinessType.DELETE)
	@DeleteMapping("/{recordIds}")
    public AjaxResult remove(@PathVariable Long[] recordIds)
    {
        return toAjax(medicalRecordService.deleteMedicalRecordByRecordIds(recordIds));
    }
}
