package com.ruoyi.payment.controller;

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
import com.ruoyi.his.api.RemotePatientService;
import com.ruoyi.his.api.RemoteRegisterService;
import com.ruoyi.payment.domain.Payment;
import com.ruoyi.payment.service.IPaymentService;
import com.ruoyi.system.api.model.LoginUser;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 收费Controller
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
@RestController
@RequestMapping("/payment")
public class PaymentController extends BaseController
{
    @Autowired
    private IPaymentService paymentService;

    @Autowired
    private RemotePatientService remotePatientService;

    @Autowired
    private RemoteRegisterService remoteRegisterService;

    /**
     * 查询收费列表
     */
    @RequiresPermissions("payment:payment:list")
    @GetMapping("/list")
    public TableDataInfo list(Payment payment)
    {
        applyPatientScope(payment);
        applyNurseScope(payment);
        startPage();
        List<Payment> list = paymentService.selectPaymentList(payment);
        return getDataTable(list);
    }

    /**
     * 导出收费列表
     */
    @RequiresPermissions("payment:payment:export")
    @Log(title = "收费", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Payment payment)
    {
        applyPatientScope(payment);
        applyNurseScope(payment);
        List<Payment> list = paymentService.selectPaymentList(payment);
        ExcelUtil<Payment> util = new ExcelUtil<Payment>(Payment.class);
        util.exportExcel(response, list, "收费数据");
    }

    /**
     * 获取收费详细信息
     */
    @RequiresPermissions("payment:payment:query")
    @GetMapping(value = "/{paymentId}")
    public AjaxResult getInfo(@PathVariable("paymentId") Long paymentId)
    {
        Payment payment = paymentService.selectPaymentByPaymentId(paymentId);
        checkPatientScope(payment);
        checkNurseScope(payment);
        return success(payment);
    }

    /**
     * 新增收费
     */
    @RequiresPermissions("payment:payment:add")
    @Log(title = "收费", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Payment payment)
    {
        applyPatientScope(payment);
        checkNurseScope(payment);
        return toAjax(paymentService.insertPayment(payment));
    }

    /**
     * 修改收费
     */
    @RequiresPermissions("payment:payment:edit")
    @Log(title = "收费", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Payment payment)
    {
        checkPatientScope(paymentService.selectPaymentByPaymentId(payment.getPaymentId()));
        checkNurseScope(paymentService.selectPaymentByPaymentId(payment.getPaymentId()));
        applyPatientScope(payment);
        return toAjax(paymentService.updatePayment(payment));
    }

    /**
     * 删除收费
     */
    @RequiresPermissions("payment:payment:remove")
    @Log(title = "收费", businessType = BusinessType.DELETE)
	@DeleteMapping("/{paymentIds}")
    public AjaxResult remove(@PathVariable Long[] paymentIds)
    {
        if (isPatientUser())
        {
            for (Long paymentId : paymentIds)
            {
                checkPatientScope(paymentService.selectPaymentByPaymentId(paymentId));
            }
        }
        if (isNurseUser())
        {
            for (Long paymentId : paymentIds)
            {
                checkNurseScope(paymentService.selectPaymentByPaymentId(paymentId));
            }
        }
        return toAjax(paymentService.deletePaymentByPaymentIds(paymentIds));
    }

    private void applyPatientScope(Payment payment)
    {
        if (isPatientUser())
        {
            payment.setPatientId(getCurrentPatientId());
        }
    }

    private void checkPatientScope(Payment payment)
    {
        if (isPatientUser() && (payment == null || !Objects.equals(payment.getPatientId(), getCurrentPatientId())))
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

    private boolean isNurseUser()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        return !SecurityUtils.isAdmin()
                && loginUser != null
                && loginUser.getRoles() != null
                && loginUser.getRoles().contains("medical");
    }

    private Long getNurseDeptId()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null || loginUser.getSysUser() == null)
        {
            return null;
        }
        return loginUser.getSysUser().getDeptId();
    }

    private void applyNurseScope(Payment payment)
    {
        if (isNurseUser())
        {
            Long deptId = getNurseDeptId();
            if (deptId != null)
            {
                payment.setDeptId(deptId);
            }
        }
    }

    private void checkNurseScope(Payment payment)
    {
        if (isNurseUser() && payment != null && payment.getRegisterId() != null)
        {
            Long nurseDeptId = getNurseDeptId();
            if (nurseDeptId == null)
            {
                return;
            }
            R<Map<String, Object>> registerR = remoteRegisterService.getRegisterInfo(payment.getRegisterId(), SecurityConstants.INNER);
            if (R.isError(registerR) || registerR.getData() == null)
            {
                throw new ServiceException("无权限操作其他科室收费信息");
            }
            Object deptIdObj = registerR.getData().get("deptId");
            Long registerDeptId = deptIdObj instanceof Number ? ((Number) deptIdObj).longValue()
                    : (deptIdObj != null ? Long.valueOf(deptIdObj.toString()) : null);
            if (registerDeptId == null || !Objects.equals(registerDeptId, nurseDeptId))
            {
                throw new ServiceException("无权限操作其他科室收费信息");
            }
        }
    }
}
