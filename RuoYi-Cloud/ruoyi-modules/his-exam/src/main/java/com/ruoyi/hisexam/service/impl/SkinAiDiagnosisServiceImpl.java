package com.ruoyi.hisexam.service.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.ruoyi.hisexam.service.ISkinAiDiagnosisService;

/**
 * 皮肤 AI 诊断服务实现 — 调用豆包 Ark API
 */
@Service
public class SkinAiDiagnosisServiceImpl implements ISkinAiDiagnosisService
{
    private static final Logger log = LoggerFactory.getLogger(SkinAiDiagnosisServiceImpl.class);
    private static final String ARK_API_URL = "https://ark.cn-beijing.volces.com/api/v3/responses";
    private static final String MODEL_ID = "doubao-seed-2-0-pro-260215";
    private static final String API_KEY_PATH = "ApiKey.txt";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> diagnose(Map<String, Object> request)
    {
        try
        {
            String apiKey = readApiKey();
            String question = (String) request.getOrDefault("question", "");
            Map<String, Object> detectionResult = (Map<String, Object>) request.get("result");

            StringBuilder prompt = new StringBuilder();
            prompt.append("你是一位资深的皮肤科医生。请基于以下皮肤病变检测结果回答医生的问题。\n\n");

            if (detectionResult != null)
            {
                prompt.append("=== 检测结果 ===\n");
                prompt.append("是否发现可疑区域: ").append(detectionResult.getOrDefault("hasMole", false)).append("\n");
                prompt.append("可疑病灶数量: ").append(detectionResult.getOrDefault("moleCount", 0)).append("\n");
                prompt.append("置信度: ").append(detectionResult.getOrDefault("confidence", "-")).append("%\n\n");
            }

            prompt.append("=== 医生问题 ===\n");
            prompt.append(question);
            prompt.append("\n\n请用专业的医学语言回答，并提供诊疗建议。");

            // 调用豆包 API
            Map<String, Object> reqBody = new HashMap<>();
            reqBody.put("model", MODEL_ID);
            reqBody.put("stream", false);
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", prompt.toString());
            messages.add(userMessage);
            reqBody.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(reqBody), headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(ARK_API_URL, entity, Map.class);
            Map<String, Object> respBody = response.getBody();

            Map<String, Object> result = new HashMap<>();
            if (respBody != null && respBody.containsKey("choices"))
            {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) respBody.get("choices");
                if (choices != null && !choices.isEmpty())
                {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, Object> messageObj = (Map<String, Object>) choice.get("message");
                    result.put("reply", messageObj != null ? messageObj.get("content") : "");
                }
            }
            else if (respBody != null && respBody.containsKey("output"))
            {
                // 兼容 Doubao Responses API v3 格式
                Object output = respBody.get("output");
                if (output instanceof List && !((List) output).isEmpty())
                {
                    Map<String, Object> firstOutput = (Map<String, Object>) ((List) output).get(0);
                    if (firstOutput != null && firstOutput.containsKey("content"))
                    {
                        List<Map<String, Object>> contentList = (List<Map<String, Object>>) firstOutput.get("content");
                        if (contentList != null && !contentList.isEmpty())
                        {
                            result.put("reply", contentList.get(0).get("text"));
                        }
                    }
                }
            }

            if (!result.containsKey("reply"))
            {
                result.put("reply", "AI 未返回有效诊断结果");
            }

            return result;
        }
        catch (Exception e)
        {
            log.error("皮肤 AI 诊断失败", e);
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("reply", "诊断服务暂时不可用：" + e.getMessage());
            return fallback;
        }
    }

    private String readApiKey() throws Exception
    {
        String envKey = System.getenv("ARK_API_KEY");
        if (envKey != null && !envKey.isEmpty()) return envKey;

        try
        {
            for (Path path : resolveApiKeyCandidates(API_KEY_PATH))
            {
                if (Files.isRegularFile(path))
                {
                    return new String(Files.readAllBytes(path)).trim();
                }
            }
            throw new RuntimeException("API Key file not found");
        }
        catch (Exception e)
        {
            throw new RuntimeException("无法读取 API Key，请在 " + API_KEY_PATH + " 放置密钥或设置 ARK_API_KEY 环境变量");
        }
    }

    private List<Path> resolveApiKeyCandidates(String configuredPath)
    {
        List<Path> candidates = new ArrayList<>();
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
}
