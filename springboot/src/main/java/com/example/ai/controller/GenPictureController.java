package com.example.ai.controller;

import com.example.ai.client.ComfyUIClient;
import com.example.ai.config.ComfyUIProperties;
import com.example.ai.domain.AiGenPicture;
import com.example.ai.service.IAiGenPictureService;
import com.example.ai.service.IGenPictureWorkflowPathService;
import com.example.ai.domain.GenPictureWorkflowPath;
import com.example.common.Result;
import com.example.entity.Account;
import com.example.utils.TokenUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/ai/gen-picture")
public class GenPictureController {
    private static final Logger log = LoggerFactory.getLogger(GenPictureController.class);

    @Autowired private IGenPictureWorkflowPathService workflowPathService;
    @Autowired private IAiGenPictureService aiGenPictureService;
    @Autowired private ComfyUIClient comfyUIClient;
    @Autowired private ComfyUIProperties comfyUIProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/workflow")
    public Result getWorkflow(@RequestParam Long id) {
        Map<String, Object> workflow = workflowPathService.getWorkflowJsonById(id);
        return Result.success(workflow);
    }

    @GetMapping("/workflow-types")
    public Result getWorkflowTypes() {
        List<GenPictureWorkflowPath> all = workflowPathService.listAll();
        List<Map<String, Object>> types = new ArrayList<>();
        for (GenPictureWorkflowPath r : all) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("type", r.getWorkflowType());
            m.put("name", r.getWorkflowName());
            types.add(m);
        }
        return Result.success(types);
    }

    @GetMapping("/models")
    public Result getModels() {
        // Return available checkpoint models for the frontend dropdown.
        // Extend this to scan ComfyUI models folder if needed.
        List<Map<String, String>> models = new ArrayList<>();
        // Seed models can be added here or loaded from config/DB
        return Result.success(models);
    }

    @PostMapping("/upload-image")
    public Result uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) originalFilename = "image.png";
            String filename = comfyUIClient.uploadImage(file.getBytes(), originalFilename);
            return Result.success(filename);
        } catch (Exception e) {
            log.error("图片上传失败", e);
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/generate")
    public Result generate(@RequestBody Map<String, Object> workflowJson) {
        try {
            Account user = TokenUtils.getCurrentUser();
            Long userId = Long.valueOf(user.getId());
            log.info("User {} 开始生图", userId);

            Map<String, Object> comfyResult = comfyUIClient.generateImage(workflowJson);

            String workflowType = detectWorkflowType(workflowJson);
            String promptText = extractPromptText(workflowJson);
            String negativeText = extractNegativeText(workflowJson);
            String paramsJson = extractParamsJson(workflowJson);

            String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String outputDir = comfyUIProperties.getOutputPath() + File.separator + dateStr;

            List<Map<String, Object>> resultFiles = new ArrayList<>();
            List<Map<String, Object>> comfyFiles = (List<Map<String, Object>>) comfyResult.get("files");
            if (comfyFiles != null) {
                for (Map<String, Object> fileInfo : comfyFiles) {
                    String originFilename = (String) fileInfo.get("filename");
                    String comfyUrl = (String) fileInfo.get("url");
                    if (originFilename.startsWith("ComfyUI")) continue;

                    String localPath = outputDir + File.separator + originFilename;
                    comfyUIClient.downloadImage(comfyUrl, localPath);

                    AiGenPicture record = aiGenPictureService.saveRecord(
                            userId, originFilename, workflowType,
                            promptText, negativeText, paramsJson, localPath);

                    Map<String, Object> resultFile = new LinkedHashMap<>();
                    resultFile.put("id", record.getId());
                    resultFile.put("filename", originFilename);
                    resultFile.put("url", "/ai/gen-picture/image/" + record.getId());
                    resultFiles.add(resultFile);
                }
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("prompt_id", comfyResult.get("prompt_id"));
            result.put("files", resultFiles);
            return Result.success(result);
        } catch (Exception e) {
            log.error("生图失败", e);
            return Result.error("生图失败: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    public Result history() {
        Account user = TokenUtils.getCurrentUser();
        Long userId = Long.valueOf(user.getId());
        List<AiGenPicture> list = aiGenPictureService.listByUser(userId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (AiGenPicture record : list) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", record.getId());
            item.put("filename", record.getFilename());
            item.put("workflowType", record.getWorkflowType());
            item.put("promptText", record.getPromptText());
            item.put("negativeText", record.getNegativeText());
            item.put("paramsJson", record.getParamsJson());
            item.put("createTime", record.getCreateTime());
            item.put("url", "/ai/gen-picture/image/" + record.getId());
            result.add(item);
        }
        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        Account user = TokenUtils.getCurrentUser();
        Long userId = Long.valueOf(user.getId());
        AiGenPicture record = aiGenPictureService.getById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            return Result.error("记录不存在");
        }
        try { java.nio.file.Files.deleteIfExists(new File(record.getFilepath()).toPath()); } catch (Exception ignored) {}
        aiGenPictureService.deleteById(id, userId);
        return Result.success();
    }

    @GetMapping("/image/{id}")
    public ResponseEntity<Resource> serveImage(@PathVariable Long id) {
        AiGenPicture record = aiGenPictureService.getById(id);
        if (record == null) return ResponseEntity.notFound().build();
        File file = new File(record.getFilepath());
        if (!file.exists()) return ResponseEntity.notFound().build();

        Resource resource = new FileSystemResource(file);
        String contentType = "image/png";
        String fname = record.getFilename().toLowerCase();
        if (fname.endsWith(".jpg") || fname.endsWith(".jpeg")) contentType = "image/jpeg";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + record.getFilename() + "\"")
                .body(resource);
    }

    @SuppressWarnings("unchecked")
    private String detectWorkflowType(Map<String, Object> workflow) {
        for (Object node : workflow.values()) {
            Map<String, Object> n = (Map<String, Object>) node;
            if ("VAEEncode".equals(n.get("class_type"))) return "img2img";
        }
        return "txt2img";
    }

    @SuppressWarnings("unchecked")
    private String extractPromptText(Map<String, Object> workflow) {
        for (Map.Entry<String, Object> entry : workflow.entrySet()) {
            Map<String, Object> node = (Map<String, Object>) entry.getValue();
            if ("KSampler".equals(node.get("class_type"))) {
                Map<String, Object> inputs = (Map<String, Object>) node.get("inputs");
                Object positiveRef = inputs.get("positive");
                if (positiveRef instanceof List && ((List<?>) positiveRef).size() >= 1) {
                    String posNodeId = String.valueOf(((List<?>) positiveRef).get(0));
                    Map<String, Object> posNode = (Map<String, Object>) workflow.get(posNodeId);
                    if (posNode != null) {
                        Map<String, Object> posInputs = (Map<String, Object>) posNode.get("inputs");
                        Object text = posInputs.get("text");
                        return text != null ? text.toString() : "";
                    }
                }
            }
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    private String extractNegativeText(Map<String, Object> workflow) {
        for (Map.Entry<String, Object> entry : workflow.entrySet()) {
            Map<String, Object> node = (Map<String, Object>) entry.getValue();
            if ("KSampler".equals(node.get("class_type"))) {
                Map<String, Object> inputs = (Map<String, Object>) node.get("inputs");
                Object negativeRef = inputs.get("negative");
                if (negativeRef instanceof List && ((List<?>) negativeRef).size() >= 1) {
                    String negNodeId = String.valueOf(((List<?>) negativeRef).get(0));
                    Map<String, Object> negNode = (Map<String, Object>) workflow.get(negNodeId);
                    if (negNode != null) {
                        Map<String, Object> negInputs = (Map<String, Object>) negNode.get("inputs");
                        Object text = negInputs.get("text");
                        return text != null ? text.toString() : "";
                    }
                }
            }
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    private String extractParamsJson(Map<String, Object> workflow) {
        Map<String, Object> params = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : workflow.entrySet()) {
            Map<String, Object> node = (Map<String, Object>) entry.getValue();
            String classType = (String) node.get("class_type");
            Map<String, Object> inputs = (Map<String, Object>) node.get("inputs");
            if ("KSampler".equals(classType)) {
                params.put("seed", inputs.get("seed"));
                params.put("steps", inputs.get("steps"));
                params.put("cfg", inputs.get("cfg"));
                params.put("sampler_name", inputs.get("sampler_name"));
                params.put("scheduler", inputs.get("scheduler"));
                params.put("denoise", inputs.get("denoise"));
            } else if ("EmptyLatentImage".equals(classType)) {
                params.put("width", inputs.get("width"));
                params.put("height", inputs.get("height"));
                params.put("batch_size", inputs.get("batch_size"));
            } else if ("CheckpointLoaderSimple".equals(classType)) {
                params.put("model", inputs.get("ckpt_name"));
            }
        }
        try { return objectMapper.writeValueAsString(params); } catch (Exception e) { return "{}"; }
    }
}
