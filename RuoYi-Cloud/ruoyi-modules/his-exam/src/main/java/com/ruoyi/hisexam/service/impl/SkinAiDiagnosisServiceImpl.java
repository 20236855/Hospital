package com.ruoyi.hisexam.service.impl;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
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
    private static final String API_KEY_PATH = "D:/RuoYi-Code/Hospital/ApiKey.txt";

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
            String imageBase64 = (String) request.getOrDefault("imageBase64", "");

            StringBuilder prompt = new StringBuilder();
            prompt.append("你是一位资深的皮肤科医生。请基于以下皮肤病变检测结果和图像回答医生的问题。\n\n");

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

            prompt.append("\n\n重要：请严格输出纯JSON，不要Markdown，不要解释JSON外的文字。JSON结构必须为：");
            prompt.append("{\"opinion\":\"简要精炼的诊断意见，一句话概括，不超过50字\",\"findings\":\"影像所见描述\",\"suggestion\":\"诊疗建议\",\"risk\":\"风险评估简要说明\"}。");

            Map<String, Object> reqBody = new HashMap<>();
            reqBody.put("model", MODEL_ID);
            reqBody.put("stream", false);

            List<Map<String, Object>> input = new ArrayList<>();
            Map<String, Object> userMessage = new HashMap<>();
            userMessage.put("role", "user");

            List<Map<String, Object>> contentParts = new ArrayList<>();

            if (StringUtils.hasText(imageBase64))
            {
                String imageUrl = normalizeImageUrl(imageBase64);
                Map<String, Object> imagePart = new HashMap<>();
                imagePart.put("type", "input_image");
                imagePart.put("image_url", imageUrl);
                contentParts.add(imagePart);
            }

            Map<String, Object> textPart = new HashMap<>();
            textPart.put("type", "input_text");
            textPart.put("text", prompt.toString());
            contentParts.add(textPart);

            userMessage.put("content", contentParts);
            input.add(userMessage);
            reqBody.put("input", input);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(reqBody), headers);

            log.info("皮肤AI诊断请求: model={}, hasImage={}, promptLength={}", MODEL_ID, StringUtils.hasText(imageBase64), prompt.length());

            ResponseEntity<Map> response = restTemplate.postForEntity(ARK_API_URL, entity, Map.class);
            Map<String, Object> respBody = response.getBody();

            log.info("皮肤AI诊断响应: status={}, bodyKeys={}", response.getStatusCode(), respBody != null ? respBody.keySet() : "null");

            String reply = extractReply(respBody);
            Map<String, Object> result = new HashMap<>();

            if (StringUtils.hasText(reply))
            {
                Map<String, Object> parsed = parseSkinDiagnosisJson(reply);
                result.putAll(parsed);
                result.put("rawText", reply);
            }
            else
            {
                result.put("opinion", "AI 未返回有效诊断结果");
                result.put("suggestion", "");
                result.put("findings", "");
                result.put("risk", "");
            }

            return result;
        }
        catch (Exception e)
        {
            log.error("皮肤 AI 诊断失败", e);
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("opinion", "诊断服务暂时不可用：" + e.getMessage());
            fallback.put("suggestion", "");
            fallback.put("findings", "");
            fallback.put("risk", "");
            return fallback;
        }
    }

    private Map<String, Object> parseSkinDiagnosisJson(String outputText)
    {
        String json = extractJsonObject(outputText);
        try
        {
            return JSON.parseObject(json, Map.class);
        }
        catch (Exception e)
        {
            log.warn("解析皮肤AI诊断JSON失败，尝试提取文本: {}", e.getMessage());
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("opinion", extractOpinionFromText(outputText));
            fallback.put("suggestion", "");
            fallback.put("findings", outputText);
            fallback.put("risk", "");
            return fallback;
        }
    }

    private String extractOpinionFromText(String text)
    {
        if (!StringUtils.hasText(text))
        {
            return "";
        }
        text = text.replaceAll("#", "").replaceAll("\\*\\*", "").replaceAll("\\*", "").trim();
        int maxLen = 50;
        if (text.length() <= maxLen)
        {
            return text;
        }
        int end = text.indexOf("。");
        if (end > 0 && end <= maxLen)
        {
            return text.substring(0, end);
        }
        return text.substring(0, maxLen) + "...";
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

    private String extractReply(Map<String, Object> respBody)
    {
        if (respBody == null)
        {
            return null;
        }

        if (respBody.containsKey("output_text"))
        {
            String outputText = (String) respBody.get("output_text");
            if (StringUtils.hasText(outputText))
            {
                log.info("从 output_text 提取回复");
                return outputText.trim();
            }
        }

        if (respBody.containsKey("choices"))
        {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) respBody.get("choices");
            if (choices != null && !choices.isEmpty())
            {
                Map<String, Object> choice = choices.get(0);
                Map<String, Object> messageObj = (Map<String, Object>) choice.get("message");
                if (messageObj != null && messageObj.containsKey("content"))
                {
                    Object content = messageObj.get("content");
                    if (content instanceof String)
                    {
                        log.info("从 choices[0].message.content (String) 提取回复");
                        return ((String) content).trim();
                    }
                    else if (content instanceof List)
                    {
                        List<Map<String, Object>> contentList = (List<Map<String, Object>>) content;
                        if (!contentList.isEmpty())
                        {
                            String text = (String) contentList.get(0).get("text");
                            if (StringUtils.hasText(text))
                            {
                                log.info("从 choices[0].message.content[0].text 提取回复");
                                return text.trim();
                            }
                        }
                    }
                }
            }
        }

        if (respBody.containsKey("output"))
        {
            Object output = respBody.get("output");
            if (output instanceof List)
            {
                List<Map<String, Object>> outputList = (List<Map<String, Object>>) output;
                for (Map<String, Object> outputItem : outputList)
                {
                    if (outputItem == null) continue;

                    String itemType = (String) outputItem.get("type");
                    log.info("处理 output 元素: type={}", itemType);

                    if ("message".equals(itemType) && outputItem.containsKey("content"))
                    {
                        Object content = outputItem.get("content");
                        if (content instanceof List)
                        {
                            List<Map<String, Object>> contentList = (List<Map<String, Object>>) content;
                            for (Map<String, Object> contentItem : contentList)
                            {
                                if (contentItem == null) continue;
                                String contentType = (String) contentItem.get("type");
                                if ("output_text".equals(contentType))
                                {
                                    String text = (String) contentItem.get("text");
                                    if (StringUtils.hasText(text))
                                    {
                                        log.info("从 output[].content[type=output_text].text 提取回复");
                                        return text.trim();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        log.warn("无法从响应中提取回复: {}", respBody);
        return null;
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

    private String readApiKey() throws Exception
    {
        String envKey = System.getenv("ARK_API_KEY");
        if (envKey != null && !envKey.isEmpty()) return envKey;

        try
        {
            return new String(Files.readAllBytes(Paths.get(API_KEY_PATH))).trim();
        }
        catch (Exception e)
        {
            throw new RuntimeException("无法读取 API Key，请在 " + API_KEY_PATH + " 放置密钥或设置 ARK_API_KEY 环境变量");
        }
    }
}
