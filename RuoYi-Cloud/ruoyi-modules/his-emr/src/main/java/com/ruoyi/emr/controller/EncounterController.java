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
import com.ruoyi.emr.domain.Encounter;
import com.ruoyi.emr.domain.vo.EncounterVo;
import com.ruoyi.emr.service.IEncounterService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 接诊Controller
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
@RestController
@RequestMapping("/encounter")
public class EncounterController extends BaseController
{
    @Autowired
    private IEncounterService encounterService;

    /**
     * 查询接诊列表
     */
    @RequiresPermissions("emr:encounter:list")
    @GetMapping("/list")
    public TableDataInfo list(Encounter encounter)
    {
        startPage();
        List<EncounterVo> list = encounterService.selectEncounterList(encounter);
        return getDataTable(list);
    }

    /**
     * 导出接诊列表
     */
    @RequiresPermissions("emr:encounter:export")
    @Log(title = "接诊", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Encounter encounter)
    {
        List<EncounterVo> list = encounterService.selectEncounterList(encounter);
        ExcelUtil<EncounterVo> util = new ExcelUtil<EncounterVo>(EncounterVo.class);
        util.exportExcel(response, list, "接诊数据");
    }

    /**
     * 获取接诊详细信息
     */
    @RequiresPermissions("emr:encounter:query")
    @GetMapping(value = "/{encounterId}")
    public AjaxResult getInfo(@PathVariable("encounterId") Long encounterId)
    {
        return success(encounterService.selectEncounterByEncounterId(encounterId));
    }

    /**
     * 新增接诊
     */
    @RequiresPermissions("emr:encounter:add")
    @Log(title = "接诊", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Encounter encounter)
    {
        return toAjax(encounterService.insertEncounter(encounter));
    }

    /**
     * 修改接诊
     */
    @RequiresPermissions("emr:encounter:edit")
    @Log(title = "接诊", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Encounter encounter)
    {
        return toAjax(encounterService.updateEncounter(encounter));
    }

    /**
     * 删除接诊
     */
    @RequiresPermissions("emr:encounter:remove")
    @Log(title = "接诊", businessType = BusinessType.DELETE)
	@DeleteMapping("/{encounterIds}")
    public AjaxResult remove(@PathVariable Long[] encounterIds)
    {
        return toAjax(encounterService.deleteEncounterByEncounterIds(encounterIds));
    }
}
