package com.ruoyi.hisexam.controller;

import java.util.Map;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.hisexam.service.ILabAiDiagnosisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/lab-analysis", "/hisexam/lab-analysis"})
public class LabAiDiagnosisController extends BaseController
{
    @Autowired
    private ILabAiDiagnosisService labAiDiagnosisService;

    @PostMapping("/ai-assist")
    public AjaxResult assist(@RequestBody Map<String, Object> request)
    {
        try
        {
            return success(labAiDiagnosisService.assist(request));
        }
        catch (ServiceException e)
        {
            return error(e.getMessage());
        }
    }
}
