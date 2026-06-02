package com.ruoyi.register.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.ruoyi.common.security.annotation.InnerAuth;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.register.domain.CheckIn;
import com.ruoyi.register.service.ICheckInService;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 签到Controller
 * 
 * @author ruoyi
 * @date 2026-05-29
 */
@RestController
@RequestMapping("/in")
public class CheckInController extends BaseController
{
    @Autowired
    private ICheckInService checkInService;

    /**
     * 查询签到列表
     */
    @RequiresPermissions("register:in:list")
    @GetMapping("/list")
    public TableDataInfo list(CheckIn checkIn)
    {
        startPage();
        List<CheckIn> list = checkInService.selectCheckInList(checkIn);
        return getDataTable(list);
    }

    /**
     * 导出签到列表
     */
    @RequiresPermissions("register:in:export")
    @Log(title = "签到", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CheckIn checkIn)
    {
        List<CheckIn> list = checkInService.selectCheckInList(checkIn);
        ExcelUtil<CheckIn> util = new ExcelUtil<CheckIn>(CheckIn.class);
        util.exportExcel(response, list, "签到数据");
    }

    /**
     * 获取签到详细信息
     */
    @RequiresPermissions("register:in:query")
    @GetMapping(value = "/{checkInId}")
    public AjaxResult getInfo(@PathVariable("checkInId") Long checkInId)
    {
        return success(checkInService.selectCheckInByCheckInId(checkInId));
    }

    /**
     * 获取签到信息（内部接口）
     */
    @InnerAuth
    @GetMapping(value = "/inner/{registerId}")
    public R<Map<String, Object>> innerInfo(@PathVariable("registerId") Long registerId)
    {
        CheckIn checkIn = checkInService.selectCheckInByRegisterId(registerId);
        Map<String, Object> result = new HashMap<>();
        if (checkIn != null)
        {
            result.put("queueNo", checkIn.getQueueNo());
            result.put("checkInTime", checkIn.getCheckInTime());
        }
        return R.ok(result);
    }

    /**
     * 新增签到
     */
    @RequiresPermissions("register:in:add")
    @Log(title = "签到", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CheckIn checkIn)
    {
        return toAjax(checkInService.insertCheckIn(checkIn));
    }

    /**
     * 修改签到
     */
    @RequiresPermissions("register:in:edit")
    @Log(title = "签到", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CheckIn checkIn)
    {
        return toAjax(checkInService.updateCheckIn(checkIn));
    }

    /**
     * 删除签到
     */
    @RequiresPermissions("register:in:remove")
    @Log(title = "签到", businessType = BusinessType.DELETE)
	@DeleteMapping("/{checkInIds}")
    public AjaxResult remove(@PathVariable Long[] checkInIds)
    {
        return toAjax(checkInService.deleteCheckInByCheckInIds(checkInIds));
    }
}
