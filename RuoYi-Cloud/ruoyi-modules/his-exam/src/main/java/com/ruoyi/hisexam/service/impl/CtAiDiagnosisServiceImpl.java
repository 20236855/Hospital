package com.ruoyi.hisexam.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.hisexam.service.ICtAiDiagnosisService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

/**
 * Calls Doubao Ark Responses API for intracranial CT assisted diagnosis.
 */
@Service
public class CtAiDiagnosisServiceImpl implements ICtAiDiagnosisService
{
    @Value("${doubao.ark.base-url:https://ark.cn-beijing.volces.com/api/v3}")
    private String baseUrl;

    @Value("${doubao.ark.model:doubao-seed-2-0-pro-260215}")
    private String model;

    @Value("${doubao.ark.api-key-env:ARK_API_KEY}")
    private String apiKeyEnv;

    @Value("${doubao.ark.api-key-file:D:/RuoYi-Code/Hospital/ApiKey.txt}")
    private String apiKeyFile;

    @Value("${doubao.ark.connect-timeout-ms:10000}")
    private int connectTimeoutMs;

    @Value("${doubao.ark.read-timeout-ms:120000}")
    private int readTimeoutMs;

    @Override
    public Map<String, Object> diagnose(Map<String, Object> request)
    {
        String apiKey = resolveApiKey();
        RestTemplate restTemplate = buildRestTemplate();

        JSONObject body = buildRequestBody(request);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        try
        {
            ResponseEntity<String> response = restTemplate.exchange(
                    normalizedBaseUrl() + "/responses",
                    HttpMethod.POST,
                    new HttpEntity<>(body.toJSONString(), headers),
                    String.class);

            if (!response.getStatusCode().is2xxSuccessful() || !StringUtils.hasText(response.getBody()))
            {
                throw new ServiceException("豆包AI诊断服务返回为空");
            }

            String outputText = extractOutputText(JSON.parseObject(response.getBody()));
            if (!StringUtils.hasText(outputText))
            {
                throw new ServiceException("豆包AI诊断服务未返回有效文本");
            }

            Map<String, Object> result = parseDiagnosisJson(outputText);
            result.put("rawText", outputText);
            result.put("model", model);
            return result;
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("豆包AI诊断调用失败: " + e.getMessage());
        }
    }

    private JSONObject buildRequestBody(Map<String, Object> request)
    {
        JSONObject body = new JSONObject();
        body.put("model", model);

        JSONArray input = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");

        JSONArray content = new JSONArray();
        String imageUrl = normalizeImageUrl(asString(request.get("sliceImageBase64")));
        if (StringUtils.hasText(imageUrl))
        {
            JSONObject image = new JSONObject();
            image.put("type", "input_image");
            image.put("image_url", imageUrl);
            content.add(image);
        }

        JSONObject text = new JSONObject();
        text.put("type", "input_text");
        text.put("text", buildPrompt(request));
        content.add(text);

        message.put("content", content);
        input.add(message);
        body.put("input", input);
        return body;
    }

    private String buildPrompt(Map<String, Object> request)
    {
        Object patientInfo = request.getOrDefault("patientInfo", Collections.emptyMap());
        Object artifactStats = request.getOrDefault("artifactStats", Collections.emptyMap());
        String question = asString(request.get("question"));
        String sliceSource = asString(request.get("sliceSource"));
        String analysisScene = asString(request.get("analysisScene"));

        return "你是面向影像科医生的颅内病变CT辅助诊断AI。请基于输入的CT切片图像、患者基本信息和本地算法统计结果，生成辅助诊断建议。"
                + "优先回答医生提出的具体问题；如果问题与当前切片证据不完全匹配，请说明不确定性和建议补充查看的层面。"
                + "注意：只能作为医生参考，不能替代医生最终诊断；不确定时明确写出需要进一步检查。"
                + "请严格输出纯JSON，不要Markdown，不要解释JSON外的文字。JSON结构必须为："
                + "{\"findings\":[{\"name\":\"异常名称\",\"description\":\"影像描述\",\"confidence\":0,\"severity\":\"high|medium|low\"}],"
                + "\"answer\":\"针对医生问题的直接回答\","
                + "\"suggestion\":\"AI诊断建议\","
                + "\"features\":{\"lesionCount\":\"病灶数量\",\"maxSize\":\"最大病灶大小\",\"location\":\"位置\",\"density\":\"密度\"},"
                + "\"report\":{\"findings\":\"影像所见\",\"impression\":\"诊断意见\",\"conclusion\":\"normal|benign|malignant|uncertain\",\"recommendations\":[\"mri|followup|biopsy|surgery\"],\"remark\":\"备注\"}}。"
                + "患者信息：" + JSON.toJSONString(patientInfo)
                + "；本地CT算法统计：" + JSON.toJSONString(artifactStats)
                + "；当前切片序号：" + request.get("currentSlice")
                + "；当前Z层：" + request.get("currentSliceZIndex")
                + "；切片来源：" + sliceSource
                + "；分析场景：" + analysisScene
                + "；医生问题：" + (StringUtils.hasText(question) ? question : "请进行常规辅助诊断")
                + "；文件名：" + request.get("uploadedFileName") + "。";
    }

    private String extractOutputText(JSONObject response)
    {
        String outputText = response.getString("output_text");
        if (StringUtils.hasText(outputText))
        {
            return outputText;
        }

        JSONArray output = response.getJSONArray("output");
        if (output == null)
        {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < output.size(); i++)
        {
            JSONObject item = output.getJSONObject(i);
            JSONArray content = item == null ? null : item.getJSONArray("content");
            if (content == null)
            {
                continue;
            }
            for (int j = 0; j < content.size(); j++)
            {
                JSONObject part = content.getJSONObject(j);
                if (part == null)
                {
                    continue;
                }
                String text = part.getString("text");
                if (!StringUtils.hasText(text))
                {
                    text = part.getString("output_text");
                }
                if (StringUtils.hasText(text))
                {
                    builder.append(text);
                }
            }
        }
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseDiagnosisJson(String outputText)
    {
        String json = extractJsonObject(outputText);
        try
        {
            return JSON.parseObject(json, Map.class);
        }
        catch (Exception e)
        {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("findings", Collections.emptyList());
            fallback.put("suggestion", outputText);
            fallback.put("features", Collections.emptyMap());
            fallback.put("report", Collections.emptyMap());
            return fallback;
        }
    }

    private String extractJsonObject(String text)
    {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start)
        {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private String normalizeImageUrl(String image)
    {
        if (!StringUtils.hasText(image))
        {
            return null;
        }
        String value = image.trim();
        if (value.startsWith("data:image/"))
        {
            return value;
        }
        return "data:image/png;base64," + value;
    }

    private String resolveApiKey()
    {
        if (StringUtils.hasText(apiKeyFile))
        {
            try
            {
                String apiKey = Files.readString(Path.of(apiKeyFile), StandardCharsets.UTF_8);
                if (StringUtils.hasText(apiKey))
                {
                    return apiKey.trim();
                }
            }
            catch (IOException ignored)
            {
                // Fall through to the explicit configuration error below.
            }
        }

        String apiKey = System.getenv(apiKeyEnv);
        if (StringUtils.hasText(apiKey))
        {
            return apiKey.trim();
        }

        throw new ServiceException("未配置豆包API Key，请设置环境变量 " + apiKeyEnv + " 或配置 doubao.ark.api-key-file");
    }

    private RestTemplate buildRestTemplate()
    {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        factory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        return new RestTemplate(factory);
    }

    private String normalizedBaseUrl()
    {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private String asString(Object value)
    {
        return value == null ? null : String.valueOf(value);
    }
}
