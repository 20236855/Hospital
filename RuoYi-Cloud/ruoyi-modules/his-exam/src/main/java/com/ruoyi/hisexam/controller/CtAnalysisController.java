package com.ruoyi.hisexam.controller;

import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;

/**
 * CT影像分析Controller
 * 负责接收前端CT文件上传，转发到Python金属伪影检测服务
 *
 * @author ruoyi
 * @date 2026-06-25
 */
@RestController
@RequestMapping("/ct-analysis")
public class CtAnalysisController extends BaseController {

    /**
     * Python CT分析服务地址，可在配置文件中覆盖
     */
    @Value("${ct.analysis.service.url:http://127.0.0.1:5001}")
    private String ctAnalysisServiceUrl;

    /**
     * RestTemplate 用于转发 multipart 请求到 Python CT分析服务。
     * 使用默认构造函数，内置的 FormHttpMessageConverter + ResourceHttpMessageConverter 
     * 即可正确处理 multipart/form-data 请求和 JSON 响应。
     */
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public AjaxResult health() {
        try {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map> response = restTemplate.getForEntity(
                ctAnalysisServiceUrl + "/api/health", Map.class);

            Map<String, Object> result = new HashMap<>();
            result.put("available", response.getStatusCode().is2xxSuccessful());
            result.put("python", response.getBody());
            return success(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("available", false);
            result.put("message", "CT分析服务未启动，请确保Python服务已运行在 " + ctAnalysisServiceUrl);
            return success(result);
        }
    }

    /**
     * CT金属伪影分析
     * 上传CT文件（DICOM zip / MHD / NIfTI），返回标注后的切片图像
     * 权限由前端路由层控制（仅lab医生可访问brain-ct页面），此处不再重复校验
     */
    @PostMapping("/artifact-detect")
    public AjaxResult detectArtifact(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "params", required = false, defaultValue = "{}") String params) {

        if (file == null || file.isEmpty()) {
            return error("请选择要上传的CT文件");
        }

        String filename = file.getOriginalFilename();
        if (filename == null ||
            !(filename.toLowerCase().endsWith(".zip") ||
              filename.toLowerCase().endsWith(".mhd") ||
              filename.toLowerCase().endsWith(".nii") ||
              filename.toLowerCase().endsWith(".nii.gz") ||
              filename.toLowerCase().endsWith(".dcm"))) {
            return error("不支持的文件格式，请上传 DICOM(zip)、MHD 或 NIfTI 文件");
        }

        if (file.getSize() > 500 * 1024 * 1024) {
            return error("文件大小不能超过500MB");
        }

        try {
            String targetUrl = ctAnalysisServiceUrl + "/api/analyze";

            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            // 构建multipart body
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileResource);
            body.add("params", params);

            // 关键：不要手动设 Content-Type，让 FormHttpMessageConverter 自动生成带 boundary 的 header
            HttpHeaders headers = new HttpHeaders();
            // 不设置 Content-Type！Spring 的 FormHttpMessageConverter 会自动设置正确的
            // multipart/form-data; boundary=xxxx

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, headers);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map> response = restTemplate.exchange(
                targetUrl, HttpMethod.POST, requestEntity, Map.class);

            if (response.getBody() == null) {
                return error("CT分析服务返回为空");
            }

            return success(response.getBody());

        } catch (org.springframework.web.client.ResourceAccessException e) {
            logger.error("无法连接CT分析服务: {} - {}", ctAnalysisServiceUrl, e.getMessage());
            return error("CT分析服务未启动，请确保Python服务已运行在 " + ctAnalysisServiceUrl +
                        "（启动命令: python ct_artifact_service.py --port 5001）");
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            logger.error("CT分析服务拒绝请求 (HTTP {}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            return error("CT分析服务返回错误: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("CT分析失败", e);
            return error("CT分析处理失败: " + e.getMessage());
        }
    }

    /**
     * 单层切片分析
     */
    @PostMapping("/artifact-detect-slice")
    public AjaxResult detectArtifactSlice(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "sliceIndex", defaultValue = "0") int sliceIndex) {

        if (files == null || files.isEmpty()) {
            return error("请选择要上传的CT切片文件");
        }

        try {
            String targetUrl = ctAnalysisServiceUrl + "/api/analyze-slice";

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            for (MultipartFile file : files) {
                ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                    @Override
                    public String getFilename() {
                        return file.getOriginalFilename();
                    }
                };
                body.add("file", fileResource);
            }
            body.add("sliceIndex", String.valueOf(sliceIndex));

            // 不手动设置 Content-Type，让 Spring 自动生成 boundary
            HttpHeaders headers = new HttpHeaders();

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, headers);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map> response = restTemplate.exchange(
                targetUrl, HttpMethod.POST, requestEntity, Map.class);

            if (response.getBody() == null) {
                return error("CT分析服务返回为空");
            }

            return success(response.getBody());

        } catch (org.springframework.web.client.ResourceAccessException e) {
            return error("CT分析服务未启动，请确保Python服务已运行在 " + ctAnalysisServiceUrl);
        } catch (Exception e) {
            logger.error("CT切片分析失败", e);
            return error("CT切片分析失败: " + e.getMessage());
        }
    }
}
