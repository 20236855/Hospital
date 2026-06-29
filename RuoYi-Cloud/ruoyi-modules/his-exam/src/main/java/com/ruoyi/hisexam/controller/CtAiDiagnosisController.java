package com.ruoyi.hisexam.controller;

import java.util.Map;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.hisexam.service.ICtAiDiagnosisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * CT AI assisted diagnosis controller.
 */
@RestController
@RequestMapping("/ct-analysis")
public class CtAiDiagnosisController extends BaseController
{
    @Autowired
    private ICtAiDiagnosisService ctAiDiagnosisService;

    @PostMapping("/ai-diagnosis")
    public AjaxResult diagnose(@RequestBody Map<String, Object> request)
    {
        try
        {
            return success(ctAiDiagnosisService.diagnose(request));
        }
        catch (ServiceException e)
        {
            return error(e.getMessage());
        }
    }
}
