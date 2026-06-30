package com.ruoyi.hisexam.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.hisexam.service.ILabAiDiagnosisService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LabAiDiagnosisServiceImpl implements ILabAiDiagnosisService
{
    @Value("${ai.deepseek.api-key:${DEEPSEEK_API_KEY:}}")
    private String apiKey;

    @Value("${ai.deepseek.api-key-file:DeepSeekApiKey.txt}")
    private String apiKeyFile;

    @Value("${ai.deepseek.api-url:https://api.deepseek.com/chat/completions}")
    private String apiUrl;

    @Value("${ai.deepseek.model:deepseek-chat}")
    private String model;

    @Value("${ai.deepseek.connect-timeout-ms:10000}")
    private int connectTimeoutMs;

    @Value("${ai.deepseek.read-timeout-ms:180000}")
    private int readTimeoutMs;

    @Override
    public Map<String, Object> assist(Map<String, Object> request)
    {
        String key = resolveApiKey();
        RestTemplate restTemplate = buildRestTemplate();

        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("stream", false);
        body.put("temperature", 0.2);

        JSONArray messages = new JSONArray();
        messages.add(message("system", systemPrompt()));
        messages.add(message("user", buildPrompt(request)));
        body.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(key);

        try
        {
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(body.toJSONString(), headers),
                    String.class);
            if (!response.getStatusCode().is2xxSuccessful())
            {
                throw new ServiceException("DeepSeek检验AI调用失败，状态码：" + response.getStatusCode().value() + "，响应：" + response.getBody());
            }
            if (StringUtils.isBlank(response.getBody()))
            {
                throw new ServiceException("DeepSeek检验AI返回为空");
            }
            JSONObject data = JSON.parseObject(response.getBody());
            JSONArray choices = data.getJSONArray("choices");
            if (choices == null || choices.isEmpty())
            {
                throw new ServiceException("DeepSeek检验AI返回为空");
            }
            JSONObject msg = choices.getJSONObject(0).getJSONObject("message");
            String content = msg == null ? null : msg.getString("content");
            if (StringUtils.isBlank(content))
            {
                throw new ServiceException("DeepSeek检验AI返回内容为空");
            }
            return parseReply(content);
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("DeepSeek检验AI连接失败：" + e.getMessage());
        }
    }

    private String systemPrompt()
    {
        return "你是医院检验科主任技师，负责审核真实医院检验报告。"
                + "你必须从标本质量、质控状态、异常值组合、危急值、复查建议、临床提示等角度进行专业审核。"
                + "输出纯JSON，不要Markdown。JSON格式："
                + "{\"riskLevel\":\"normal|warning|critical\",\"reply\":\"审核意见，包含异常解释、复查/危急值建议、给临床医生的提示\"}";
    }

    private String buildPrompt(Map<String, Object> request)
    {
        return "检验类型：" + request.get("labType")
                + "\n患者与申请信息：" + JSON.toJSONString(request.get("patient"))
                + "\n标本信息：" + JSON.toJSONString(request.get("sample"))
                + "\n检验结果：" + JSON.toJSONString(request.get("results"))
                + "\n医生问题：" + request.getOrDefault("question", "请审核本次检验结果");
    }

    private JSONObject message(String role, String content)
    {
        JSONObject item = new JSONObject();
        item.put("role", role);
        item.put("content", content);
        return item;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseReply(String content)
    {
        String json = extractJson(content);
        try
        {
            return JSON.parseObject(json, Map.class);
        }
        catch (Exception e)
        {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("riskLevel", content.contains("危急") ? "critical" : "warning");
            fallback.put("reply", content);
            return fallback;
        }
    }

    private String extractJson(String content)
    {
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start >= 0 && end > start)
        {
            return content.substring(start, end + 1);
        }
        return content;
    }

    private String resolveApiKey()
    {
        if (StringUtils.isNotEmpty(apiKey))
        {
            return apiKey.trim();
        }
        for (String fileName : new String[] { apiKeyFile })
        {
            for (Path path : resolvePathCandidates(fileName))
            {
                try
                {
                    if (Files.isRegularFile(path))
                    {
                        String key = Files.readString(path, StandardCharsets.UTF_8);
                        if (StringUtils.isNotEmpty(key))
                        {
                            return key.trim();
                        }
                    }
                }
                catch (IOException ignored)
                {
                }
            }
        }
        throw new ServiceException("未配置DeepSeek API Key，请设置 DEEPSEEK_API_KEY 或放置 DeepSeekApiKey.txt");
    }

    private List<Path> resolvePathCandidates(String configuredPath)
    {
        List<Path> candidates = new ArrayList<>();
        if (StringUtils.isBlank(configuredPath))
        {
            return candidates;
        }
        Path configured = Paths.get(configuredPath);
        if (configured.isAbsolute())
        {
            candidates.add(configured);
            return candidates;
        }
        Path cursor = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        while (cursor != null)
        {
            candidates.add(cursor.resolve(configured).normalize());
            cursor = cursor.getParent();
        }
        return candidates;
    }

    private RestTemplate buildRestTemplate()
    {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        factory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        return new RestTemplate(factory);
    }
}
