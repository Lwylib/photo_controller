package com.example.ai.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.ai.config.ComfyUIProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.*;

@Component
public class ComfyUIClient {
    private static final Logger log = LoggerFactory.getLogger(ComfyUIClient.class);

    @Autowired
    private ComfyUIProperties props;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    @SuppressWarnings("unchecked")
    public Map<String, Object> generateImage(Map<String, Object> workflow) throws Exception {
        String promptId = submitPrompt(workflow);
        log.info("Prompt submitted: {}", promptId);

        long start = System.currentTimeMillis();
        long timeoutMs = props.getTimeout() * 1000L;
        int pollIntervalMs = props.getPollInterval() * 1000;

        while (System.currentTimeMillis() - start < timeoutMs) {
            Thread.sleep(pollIntervalMs);
            Map<String, Object> history = fetchHistory(promptId);
            if (history != null && history.containsKey(promptId)) {
                Map<String, Object> promptData = (Map<String, Object>) history.get(promptId);
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("prompt_id", promptId);
                result.put("files", extractImages(promptData));
                return result;
            }
        }
        throw new RuntimeException("生图超时 (" + props.getTimeout() + "s)");
    }

    @SuppressWarnings("unchecked")
    private String submitPrompt(Map<String, Object> workflow) throws IOException, InterruptedException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("prompt", workflow);

        String json = objectMapper.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(props.getBaseUrl() + "/prompt"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .timeout(Duration.ofSeconds(props.getTimeout()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("ComfyUI 提交失败: HTTP " + response.statusCode());
        }

        Map<String, Object> result = objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
        Object promptId = result.get("prompt_id");
        if (promptId == null) throw new RuntimeException("ComfyUI 提交失败: " + response.body());
        return promptId.toString();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchHistory(String promptId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(props.getBaseUrl() + "/history/" + promptId))
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractImages(Map<String, Object> promptData) {
        List<Map<String, Object>> files = new ArrayList<>();
        Map<String, Object> outputs = (Map<String, Object>) promptData.get("outputs");
        if (outputs != null) {
            for (Map.Entry<String, Object> entry : outputs.entrySet()) {
                Map<String, Object> nodeOutput = (Map<String, Object>) entry.getValue();
                if (nodeOutput == null) continue;
                List<Map<String, Object>> images = (List<Map<String, Object>>) nodeOutput.get("images");
                if (images != null) {
                    for (Map<String, Object> img : images) {
                        Map<String, Object> fileInfo = new LinkedHashMap<>();
                        String filename = (String) img.get("filename");
                        fileInfo.put("filename", filename);
                        fileInfo.put("subfolder", img.getOrDefault("subfolder", ""));
                        fileInfo.put("type", img.getOrDefault("type", "output"));
                        fileInfo.put("url", buildImageUrl(filename,
                                (String) img.getOrDefault("subfolder", ""),
                                (String) img.getOrDefault("type", "output")));
                        files.add(fileInfo);
                    }
                }
            }
        }
        return files;
    }

    @SuppressWarnings("unchecked")
    public String uploadImage(byte[] imageData, String filename) throws IOException, InterruptedException {
        String boundary = "----ComfyUIUpload" + UUID.randomUUID();
        ByteBuffer body = buildMultipartBody(imageData, filename, boundary);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(props.getBaseUrl() + "/upload/image"))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body.array()))
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("图片上传失败: HTTP " + response.statusCode());
        }
        Map<String, Object> result = objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
        return (String) result.getOrDefault("name", filename);
    }

    public String downloadImage(String comfyUrl, String localPath) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(comfyUrl))
                .GET()
                .timeout(Duration.ofSeconds(60))
                .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() != 200) {
            throw new RuntimeException("下载图片失败: HTTP " + response.statusCode());
        }

        java.io.File file = new java.io.File(localPath);
        java.io.File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
        java.nio.file.Files.write(file.toPath(), response.body());
        log.info("Image saved to: {}", localPath);
        return localPath;
    }

    private ByteBuffer buildMultipartBody(byte[] imageData, String filename, String boundary) {
        StringBuilder sb = new StringBuilder();
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"image\"; filename=\"").append(filename).append("\"\r\n");
        sb.append("Content-Type: image/png\r\n\r\n");

        byte[] header = sb.toString().getBytes();
        byte[] footer = ("\r\n--" + boundary + "--\r\n").getBytes();

        ByteBuffer buffer = ByteBuffer.allocate(header.length + imageData.length + footer.length);
        buffer.put(header);
        buffer.put(imageData);
        buffer.put(footer);
        buffer.flip();
        return buffer;
    }

    public String buildImageUrl(String filename, String subfolder, String type) {
        StringBuilder url = new StringBuilder(props.getBaseUrl());
        url.append("/view?filename=").append(filename);
        url.append("&type=").append(type);
        if (subfolder != null && !subfolder.isEmpty()) url.append("&subfolder=").append(subfolder);
        return url.toString();
    }
}
