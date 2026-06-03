package com.ruoyi.hisdoctor.controller;

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
import com.ruoyi.hisdoctor.domain.RegistLevel;
import com.ruoyi.hisdoctor.service.IRegistLevelService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 挂号级别Controller
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
@RestController
@RequestMapping("/doctorlevel")
public class RegistLevelController extends BaseController
{
    @Autowired
    private IRegistLevelService registLevelService;

    /**
     * 查询挂号级别列表
     */
    @RequiresPermissions("hisdoctor:doctorlevel:list")
    @GetMapping("/list")
    public TableDataInfo list(RegistLevel registLevel)
    {
        startPage();
        List<RegistLevel> list = registLevelService.selectRegistLevelList(registLevel);
        return getDataTable(list);
    }

    /**
     * 导出挂号级别列表
     */
    @RequiresPermissions("hisdoctor:doctorlevel:export")
    @Log(title = "挂号级别", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, RegistLevel registLevel)
    {
        List<RegistLevel> list = registLevelService.selectRegistLevelList(registLevel);
        ExcelUtil<RegistLevel> util = new ExcelUtil<RegistLevel>(RegistLevel.class);
        util.exportExcel(response, list, "挂号级别数据");
    }

    /**
     * 获取挂号级别详细信息
     */
    @RequiresPermissions("hisdoctor:doctorlevel:query")
    @GetMapping(value = "/{levelId}")
    public AjaxResult getInfo(@PathVariable("levelId") Long levelId)
    {
        return success(registLevelService.selectRegistLevelByLevelId(levelId));
    }

    /**
     * 新增挂号级别
     */
    @RequiresPermissions("hisdoctor:doctorlevel:add")
    @Log(title = "挂号级别", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody RegistLevel registLevel)
    {
        return toAjax(registLevelService.insertRegistLevel(registLevel));
    }

    /**
     * 修改挂号级别
     */
    @RequiresPermissions("hisdoctor:doctorlevel:edit")
    @Log(title = "挂号级别", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody RegistLevel registLevel)
    {
        return toAjax(registLevelService.updateRegistLevel(registLevel));
    }

    /**
     * 删除挂号级别
     */
    @RequiresPermissions("hisdoctor:doctorlevel:remove")
    @Log(title = "挂号级别", businessType = BusinessType.DELETE)
	@DeleteMapping("/{levelIds}")
    public AjaxResult remove(@PathVariable Long[] levelIds)
    {
        return toAjax(registLevelService.deleteRegistLevelByLevelIds(levelIds));
    }
}
