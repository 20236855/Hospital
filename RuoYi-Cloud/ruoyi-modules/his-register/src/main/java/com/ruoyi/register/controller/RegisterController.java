package com.ruoyi.register.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.InnerAuth;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.register.domain.Register;
import com.ruoyi.register.service.IRegisterService;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 挂号Controller
 *
 * @author ruoyi
 * @date 2026-05-29
 */
@RestController
@RequestMapping("/register")
public class RegisterController extends BaseController
{
    @Autowired
    private IRegisterService registerService;

    /**
     * 查询挂号列表
     */
    @RequiresPermissions("register:register:list")
    @GetMapping("/list")
    public TableDataInfo list(Register register)
    {
        startPage();
        List<Register> list = registerService.selectRegisterList(register);
        return getDataTable(list);
    }

    /**
     * 查询全部挂号级别列表（下拉）
     */
    @GetMapping("/level/list")
    public AjaxResult levelList()
    {
        return success(registerService.selectRegisterLevelList());
    }

    /**
     * 查询全部科室列表（下拉）
     */
    @GetMapping("/dept/list")
    public AjaxResult deptList()
    {
        return success(registerService.selectDeptList());
    }

    /**
     * 根据科室ID查询医生列表（下拉）
     */
    @GetMapping("/doctor/list/{deptId}")
    public AjaxResult doctorList(@PathVariable("deptId") Long deptId)
    {
        return success(registerService.selectDoctorListByDeptId(deptId));
    }

    /**
     * 根据医生ID获取挂号费
     */
    @GetMapping("/getFee")
    public AjaxResult getFee(@RequestParam Long doctorId)
    {
        if(doctorId == null){
            return AjaxResult.error("医生不能为空");
        }
        BigDecimal fee = registerService.getRegisterFeeByDoctorId(doctorId);
        return success(fee);
    }

    /**
     * 导出挂号列表
     */
    @RequiresPermissions("register:register:export")
    @Log(title = "挂号", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Register register)
    {
        List<Register> list = registerService.selectRegisterList(register);
        ExcelUtil<Register> util = new ExcelUtil<Register>(Register.class);
        util.exportExcel(response, list, "挂号数据");
    }

    /**
     * 获取挂号详细信息
     */
    @RequiresPermissions("register:register:query")
    @GetMapping(value = "/{registerId}")
    public AjaxResult getInfo(@PathVariable("registerId") Long registerId)
    {
        return success(registerService.selectRegisterByRegisterId(registerId));
    }

    /**
     * 获取挂号信息（内部接口）
     */
    @InnerAuth
    @GetMapping(value = "/inner/{registerId}")
    public R<Map<String, Object>> innerInfo(@PathVariable("registerId") Long registerId)
    {
        Register register = registerService.selectRegisterByRegisterId(registerId);
        Map<String, Object> result = new HashMap<>();
        if (register != null)
        {
            result.put("registerNo", register.getRegisterNo());
            result.put("patientId", register.getPatientId());
        }
        return R.ok(result);
    }

    /**
     * 新增挂号
     */
    @RequiresPermissions("register:register:add")
    @Log(title = "挂号", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Register register)
    {
        return toAjax(registerService.insertRegister(register));
    }

    /**
     * 修改挂号
     */
    @RequiresPermissions("register:register:edit")
    @Log(title = "挂号", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Register register)
    {
        return toAjax(registerService.updateRegister(register));
    }

    /**
     * 删除挂号
     */
    @RequiresPermissions("register:register:remove")
    @Log(title = "挂号", businessType = BusinessType.DELETE)
	@DeleteMapping("/{registerIds}")
    public AjaxResult remove(@PathVariable Long[] registerIds)
    {
        return toAjax(registerService.deleteRegisterByRegisterIds(registerIds));
    }

    /**
     * 退号
     */
    @RequiresPermissions("register:register:edit")
    @Log(title = "挂号", businessType = BusinessType.UPDATE)
    @PutMapping("/cancel/{registerId}")
    public AjaxResult cancel(@PathVariable Long registerId)
    {
        return toAjax(registerService.cancelRegister(registerId));
    }
}

