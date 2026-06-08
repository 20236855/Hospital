package com.ruoyi.emr.controller;

import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.emr.domain.AiChatRequest;
import com.ruoyi.emr.service.IAiChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI智能问诊Controller
 */
@RestController
@RequestMapping("/chat")
public class AiChatController extends BaseController
{
    @Autowired
    private IAiChatService aiChatService;

    @PostMapping("/consult")
    public AjaxResult consult(@RequestBody AiChatRequest request)
    {
        return success(aiChatService.consult(request));
    }

    @GetMapping("/history")
    public AjaxResult history(@RequestParam(value = "sessionId", required = false) String sessionId,
                             @RequestParam(value = "patientId", required = false) Long patientId)
    {
        return success(aiChatService.selectCurrentPatientHistory(sessionId, patientId));
    }
    
    @GetMapping("/sessionList")
    public AjaxResult sessionList(@RequestParam(value = "patientId", required = false) Long patientId)
    {
        return success(aiChatService.getSessionList(patientId));
    }
}
