package com.ruoyi.hisdoctor.service.impl;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 可选本地AI推理服务。默认走规则引擎兜底；本机启动Ollama后可生成自然语言推理摘要。
 */
@Service
public class LocalAiReasoningService
{
    @Value("${agent.schedule.ai.enabled:false}")
    private boolean enabled;

    @Value("${agent.schedule.ai.provider:rules}")
    private String provider;

    @Value("${agent.schedule.ai.ollama.url:http://localhost:11434/api/generate}")
    private String ollamaUrl;

    @Value("${agent.schedule.ai.ollama.model:qwen2.5:7b}")
    private String model;

    public String getProvider()
    {
        return enabled ? provider : "rules";
    }

    public String getModel()
    {
        return enabled ? model : "deterministic-rule-engine";
    }

    public String reason(String prompt, String fallback)
    {
        if (!enabled || !"ollama".equalsIgnoreCase(provider))
        {
            return fallback;
        }
        try
        {
            HttpURLConnection connection = (HttpURLConnection) URI.create(ollamaUrl).toURL().openConnection();
            connection.setConnectTimeout(1200);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setDoOutput(true);
            String body = "{\"model\":\"" + escape(model) + "\",\"prompt\":\"" + escape(prompt) + "\",\"stream\":false}";
            try (OutputStream outputStream = connection.getOutputStream())
            {
                outputStream.write(body.getBytes(StandardCharsets.UTF_8));
            }
            if (connection.getResponseCode() >= 200 && connection.getResponseCode() < 300)
            {
                String response = new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                String text = extractResponse(response);
                return text == null || text.isBlank() ? fallback : text;
            }
            return fallback + "；本地AI服务未返回成功状态，已启用规则引擎兜底。";
        }
        catch (Exception e)
        {
            return fallback + "；本地AI服务不可用，已启用规则引擎兜底：" + e.getMessage();
        }
    }

    private String extractResponse(String json)
    {
        String key = "\"response\":\"";
        int start = json.indexOf(key);
        if (start < 0)
        {
            return null;
        }
        start += key.length();
        StringBuilder builder = new StringBuilder();
        boolean escape = false;
        for (int i = start; i < json.length(); i++)
        {
            char c = json.charAt(i);
            if (escape)
            {
                if (c == 'n')
                {
                    builder.append('\n');
                }
                else
                {
                    builder.append(c);
                }
                escape = false;
            }
            else if (c == '\\')
            {
                escape = true;
            }
            else if (c == '"')
            {
                break;
            }
            else
            {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    private String escape(String value)
    {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "").replace("\n", "\\n");
    }
}
