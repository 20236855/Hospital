package com.ruoyi.hisexam.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.hisexam.service.ISkinAiDiagnosisService;

/**
 * 皮肤病变检查 Controller
 * 接收前端图片上传，转发到 Python Flask 分析服务，再调用 AI 诊断
 */
@RestController
@RequestMapping("/skin-analysis")
public class SkinAnalysisController extends BaseController
{
    private static final String PYTHON_SERVICE_URL = "http://127.0.0.1:5002/api/analyze";
    private static final String PYTHON_HEALTH_URL = "http://127.0.0.1:5002/api/health";

    @Autowired(required = false)
    private ISkinAiDiagnosisService skinAiDiagnosisService;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 健康检查
     */
    @RequiresPermissions("hisexam:skin:list")
    @GetMapping("/health")
    public AjaxResult health()
    {
        try
        {
            ResponseEntity<String> resp = restTemplate.getForEntity(PYTHON_HEALTH_URL, String.class);
            return success(resp.getBody());
        }
        catch (Exception e)
        {
            return error("Python 服务不可用：" + e.getMessage());
        }
    }

    /**
     * 皮肤病变分析：上传图片 → Python Flask 服务 → 返回检测结果 + 标注图
     */
    @RequiresPermissions("hisexam:skin:list")
    @PostMapping("/analyze")
    public AjaxResult analyze(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "patientId", required = false) Long patientId)
    {
        try
        {
            // 1. 转发文件到 Python Flask 服务
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", file.getResource());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> pythonResp = restTemplate.postForEntity(
                    PYTHON_SERVICE_URL, requestEntity, Map.class);

            Map<String, Object> result = new HashMap<>();
            if (pythonResp.getBody() != null)
            {
                result.putAll(pythonResp.getBody());
            }

            result.put("patientId", patientId);
            result.put("fileName", file.getOriginalFilename());

            return success(result);
        }
        catch (Exception e)
        {
            return error("皮肤分析失败：" + e.getMessage());
        }
    }

    /**
     * AI 辅助诊断（基于分析结果自然语言提问）
     */
    @RequiresPermissions("hisexam:skin:list")
    @PostMapping("/ai-diagnosis")
    public AjaxResult aiDiagnosis(@RequestBody Map<String, Object> requestMap)
    {
        if (skinAiDiagnosisService == null)
        {
            return error("AI 诊断服务未配置");
        }
        try
        {
            Map<String, Object> reply = skinAiDiagnosisService.diagnose(requestMap);
            return success(reply);
        }
        catch (Exception e)
        {
            return error("AI 诊断失败：" + e.getMessage());
        }
    }
}
