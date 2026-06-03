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
import com.ruoyi.hisexam.domain.ExamApply;
import com.ruoyi.hisexam.service.IExamApplyService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 检查/检验/处置申请单Controller
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
@RestController
@RequestMapping("/apply")
public class ExamApplyController extends BaseController
{
    @Autowired
    private IExamApplyService examApplyService;

    /**
     * 查询检查/检验/处置申请单列表
     */
    @RequiresPermissions("hisexam:apply:list")
    @GetMapping("/list")
    public TableDataInfo list(ExamApply examApply)
    {
        startPage();
        List<ExamApply> list = examApplyService.selectExamApplyList(examApply);
        return getDataTable(list);
    }

    /**
     * 导出检查/检验/处置申请单列表
     */
    @RequiresPermissions("hisexam:apply:export")
    @Log(title = "检查/检验/处置申请单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ExamApply examApply)
    {
        List<ExamApply> list = examApplyService.selectExamApplyList(examApply);
        ExcelUtil<ExamApply> util = new ExcelUtil<ExamApply>(ExamApply.class);
        util.exportExcel(response, list, "检查/检验/处置申请单数据");
    }

    /**
     * 获取检查/检验/处置申请单详细信息
     */
    @RequiresPermissions("hisexam:apply:query")
    @GetMapping(value = "/{applyId}")
    public AjaxResult getInfo(@PathVariable("applyId") Long applyId)
    {
        return success(examApplyService.selectExamApplyByApplyId(applyId));
    }

    /**
     * 新增检查/检验/处置申请单
     */
    @RequiresPermissions("hisexam:apply:add")
    @Log(title = "检查/检验/处置申请单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ExamApply examApply)
    {
        return toAjax(examApplyService.insertExamApply(examApply));
    }

    /**
     * 修改检查/检验/处置申请单
     */
    @RequiresPermissions("hisexam:apply:edit")
    @Log(title = "检查/检验/处置申请单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ExamApply examApply)
    {
        return toAjax(examApplyService.updateExamApply(examApply));
    }

    /**
     * 删除检查/检验/处置申请单
     */
    @RequiresPermissions("hisexam:apply:remove")
    @Log(title = "检查/检验/处置申请单", businessType = BusinessType.DELETE)
	@DeleteMapping("/{applyIds}")
    public AjaxResult remove(@PathVariable Long[] applyIds)
    {
        return toAjax(examApplyService.deleteExamApplyByApplyIds(applyIds));
    }
}
