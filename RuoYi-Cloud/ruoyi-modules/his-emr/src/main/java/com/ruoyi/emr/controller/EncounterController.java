package com.ruoyi.emr.controller;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.emr.domain.Encounter;
import com.ruoyi.emr.domain.vo.EncounterVo;
import com.ruoyi.emr.service.IEncounterService;
import com.ruoyi.his.api.RemotePatientService;
import com.ruoyi.system.api.model.LoginUser;
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

    @Autowired
    private RemotePatientService remotePatientService;

    /**
     * 查询接诊列表
     */
    @RequiresPermissions("emr:encounter:list")
    @GetMapping("/list")
    public TableDataInfo list(Encounter encounter)
    {
        applyPatientScope(encounter);
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
        checkNurseReadonly();
        applyPatientScope(encounter);
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
        Encounter encounter = encounterService.selectEncounterByEncounterId(encounterId);
        checkPatientScope(encounter);
        return success(encounter);
    }

    /**
     * 新增接诊
     */
    @RequiresPermissions("emr:encounter:add")
    @Log(title = "接诊", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Encounter encounter)
    {
        checkNurseReadonly();
        applyPatientScope(encounter);
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
        checkNurseReadonly();
        checkPatientScope(encounterService.selectEncounterByEncounterId(encounter.getEncounterId()));
        applyPatientScope(encounter);
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
        checkNurseReadonly();
        if (isPatientUser())
        {
            for (Long encounterId : encounterIds)
            {
                checkPatientScope(encounterService.selectEncounterByEncounterId(encounterId));
            }
        }
        return toAjax(encounterService.deleteEncounterByEncounterIds(encounterIds));
    }

    private void applyPatientScope(Encounter encounter)
    {
        if (isPatientUser())
        {
            encounter.setPatientId(getCurrentPatientId());
        }
    }

    private void checkPatientScope(Encounter encounter)
    {
        if (isPatientUser() && (encounter == null || !Objects.equals(encounter.getPatientId(), getCurrentPatientId())))
        {
            throw new ServiceException("无权限访问其他患者信息");
        }
    }

    private Long getCurrentPatientId()
    {
        R<Map<String, Object>> patientR = remotePatientService.getPatientByUserId(SecurityUtils.getUserId(), SecurityConstants.INNER);
        if (R.isError(patientR))
        {
            throw new ServiceException(patientR.getMsg());
        }
        Map<String, Object> patient = patientR.getData();
        Object patientId = patient == null ? null : patient.get("patientId");
        if (patientId == null)
        {
            throw new ServiceException("当前患者档案不存在，请先完善患者信息");
        }
        return patientId instanceof Number ? ((Number) patientId).longValue() : Long.valueOf(patientId.toString());
    }

    private boolean isPatientUser()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        return !SecurityUtils.isAdmin()
                && loginUser != null
                && loginUser.getRoles() != null
                && loginUser.getRoles().contains("patient");
    }

    private void checkNurseReadonly()
    {
        if (isNurseUser())
        {
            throw new ServiceException("护士岗位只能查看接诊信息，不能新增、修改、删除或导出");
        }
    }

    private boolean isNurseUser()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        return !SecurityUtils.isAdmin()
                && loginUser != null
                && loginUser.getRoles() != null
                && loginUser.getRoles().contains("doctor")
                && hasNursePost(loginUser);
    }

    private boolean hasNursePost(LoginUser loginUser)
    {
        if (loginUser == null || loginUser.getSysUser() == null || loginUser.getSysUser().getPostIds() == null)
        {
            return false;
        }
        for (Long postId : loginUser.getSysUser().getPostIds())
        {
            if (Long.valueOf(2L).equals(postId))
            {
                return true;
            }
        }
        return false;
    }
}
