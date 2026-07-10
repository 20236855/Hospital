package com.ruoyi.emr.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 医院知识库检索器：调用 SiliconFlow embedding 接口向量化查询语句，
 * 再查询本地 Chroma 向量库（collection=hospital_kb），返回 Top-K 相关知识文本。
 * 任何异常都会降级返回 null，保证不阻断原有问诊流程。
 */
@Service
public class ChromaRetriever
{
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // Chroma 是本机向量库：单独创建“不走任何系统/全局代理”的客户端（本地服务不应走代理）。
    // 注意：本机 Chroma 实际监听在 IPv6 的 ::1 上，并未监听 IPv4 的 127.0.0.1，
    // 而 Java HttpClient 默认优先 IPv4，会导致 ConnectException。故默认 URL 用 [::1] 直连。
    private final HttpClient chromaHttpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${ai.chroma.url:http://[::1]:8000}")
    private String chromaUrl;

    @Value("${ai.chroma.collection:hospital_kb}")
    private String collection;

    @Value("${ai.embedding.api-url:https://api.siliconflow.cn/v1}")
    private String embeddingUrl;

    @Value("${ai.embedding.model:BAAI/bge-large-zh-v1.5}")
    private String embeddingModel;

    @Value("${ai.embedding.api-key:${EMBEDDING_API_KEY:}}")
    private String embeddingApiKey;

    @Value("${ai.embedding.api-key-file:embedding.txt}")
    private String embeddingKeyFile;

    private static final int REQUEST_TIMEOUT_SECONDS = 20;

    private static final String CHROMA_TENANT = "default_tenant";
    private static final String CHROMA_DATABASE = "default_database";


    public String retrieve(String query)
    {
        return retrieve(query, 8);
    }

    public String retrieve(String query, int topK)
    {
        try
        {
            String key = resolveEmbeddingKey();
            if (StringUtils.isBlank(key))
            {
                System.err.println("[ChromaRetriever] 未配置 embedding key，跳过 RAG");
                return null;
            }
            System.out.println("[ChromaRetriever] 开始 embedding，模型=" + embeddingModel + "，query=" + query);
            List<Double> vector = embed(query, key);
            System.out.println("[ChromaRetriever] embedding 完成，向量维度=" + vector.size());
            JSONObject result = queryChroma(vector, topK);
            System.out.println("[ChromaRetriever] Chroma 查询完成");
            return formatResult(result);
        }
        catch (Exception e)
        {
            // 原实现只打印 e.getMessage()，遇到 null（如 InterruptedException / NPE）时无法定位
            System.err.println("[ChromaRetriever] 检索失败，降级为无知识回答。异常类型: " + e.getClass().getName());
            e.printStackTrace();
            return null;
        }
    }

    private List<Double> embed(String text, String key) throws Exception
    {
        JSONObject body = new JSONObject();
        body.put("model", embeddingModel);
        body.put("input", text);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(embeddingUrl.replaceAll("/$", "") + "/embeddings"))
                .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + key)
                .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString()))
                .build();
        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (resp.statusCode() < 200 || resp.statusCode() >= 300)
        {
            throw new ServiceException("embedding 调用失败: " + resp.statusCode() + " " + resp.body());
        }
        JSONObject data = JSON.parseObject(resp.body());
        JSONArray arr = data.getJSONArray("data");
        List<JSONObject> items = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++)
        {
            items.add(arr.getJSONObject(i));
        }
        items.sort((a, b) -> Integer.compare(a.getIntValue("index"), b.getIntValue("index")));
        JSONArray emb = items.get(0).getJSONArray("embedding");
        List<Double> vec = new ArrayList<>();
        for (int i = 0; i < emb.size(); i++)
        {
            vec.add(emb.getDouble(i));
        }
        return vec;
    }

    private JSONObject queryChroma(List<Double> vector, int topK) throws Exception
    {
        String id = resolveCollectionId();
        JSONObject body = new JSONObject();
        JSONArray qe = new JSONArray();
        qe.add(vector);
        body.put("query_embeddings", qe);
        body.put("n_results", topK);
        JSONArray include = new JSONArray();
        include.add("documents");
        include.add("metadatas");
        body.put("include", include);

        // Chroma 1.x 默认使用 v2 API（v1 已 410 废弃），查询路径为
        // /api/v2/tenants/{tenant}/databases/{database}/collections/{collection_id}/query
        String url = chromaUrl.replaceAll("/$", "")
                + "/api/v2/tenants/" + CHROMA_TENANT
                + "/databases/" + CHROMA_DATABASE
                + "/collections/" + id + "/query";
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString()))
                .build();
        HttpResponse<String> resp = chromaHttpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (resp.statusCode() < 200 || resp.statusCode() >= 300)
        {
            throw new ServiceException("chroma 查询失败: " + resp.statusCode() + " " + resp.body());
        }
        return JSON.parseObject(resp.body());
    }

    private String resolveCollectionId() throws Exception
    {
        // Chroma 1.x 默认使用 v2 API（v1 已 410 废弃），列举路径为
        // /api/v2/tenants/{tenant}/databases/{database}/collections
        String url = chromaUrl.replaceAll("/$", "")
                + "/api/v2/tenants/" + CHROMA_TENANT
                + "/databases/" + CHROMA_DATABASE + "/collections";
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
                .GET()
                .build();
        HttpResponse<String> resp = chromaHttpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (resp.statusCode() < 200 || resp.statusCode() >= 300)
        {
            throw new ServiceException("chroma 列举集合失败: " + resp.statusCode() + " " + resp.body());
        }
        JSONArray list = JSON.parseArray(resp.body());
        if (list != null)
        {
            for (int i = 0; i < list.size(); i++)
            {
                JSONObject c = list.getJSONObject(i);
                if (collection.equals(c.getString("name")))
                {
                    return c.getString("id");
                }
            }
        }
        throw new ServiceException("未找到 Chroma collection: " + collection);
    }

    private String formatResult(JSONObject result)
    {
        if (result == null)
        {
            return null;
        }
        JSONArray documents = result.getJSONArray("documents");
        JSONArray metadatas = result.getJSONArray("metadatas");
        if (documents == null || documents.isEmpty())
        {
            return null;
        }
        JSONArray firstDocs = documents.getJSONArray(0);
        JSONArray firstMeta = metadatas == null ? null : metadatas.getJSONArray(0);
        if (firstDocs == null || firstDocs.isEmpty())
        {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < firstDocs.size(); i++)
        {
            String doc = firstDocs.getString(i);
            String source = "";
            if (firstMeta != null && i < firstMeta.size())
            {
                JSONObject m = firstMeta.getJSONObject(i);
                String title = (m != null && m.getString("title") != null) ? m.getString("title") : "";
                String category = (m != null && m.getString("category") != null) ? m.getString("category") : "";
                if (StringUtils.isNotEmpty(title) || StringUtils.isNotEmpty(category))
                {
                    source = "（来源：" + (StringUtils.isNotEmpty(title) ? title : category) + "）";
                }
            }
            sb.append(source).append("\n").append(doc).append("\n\n");
        }
        return sb.toString().trim();
    }

    private String resolveEmbeddingKey()
    {
        if (StringUtils.isNotEmpty(embeddingApiKey))
        {
            return embeddingApiKey;
        }
        for (Path path : resolveCandidates(embeddingKeyFile))
        {
            try
            {
                if (!Files.isRegularFile(path))
                {
                    continue;
                }
                String key = Files.readString(path, StandardCharsets.UTF_8);
                if (StringUtils.isNotEmpty(key))
                {
                    return key.trim();
                }
            }
            catch (Exception ignored)
            {
            }
        }
        return null;
    }

    private List<Path> resolveCandidates(String configuredPath)
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
