package com.example.ai.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.ai.domain.GenPictureWorkflowPath;
import com.example.ai.mapper.GenPictureWorkflowPathMapper;
import com.example.ai.service.IGenPictureWorkflowPathService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GenPictureWorkflowPathServiceImpl implements IGenPictureWorkflowPathService {
    private static final Logger log = LoggerFactory.getLogger(GenPictureWorkflowPathServiceImpl.class);

    @Autowired private GenPictureWorkflowPathMapper mapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Map<String, Object>> workflowCache = new HashMap<>();

    @PostConstruct
    public void init() {
        try {
            List<GenPictureWorkflowPath> all = mapper.selectAll();
            for (GenPictureWorkflowPath record : all) {
                workflowCache.put(record.getWorkflowType(), loadFromPath(record.getJsonPath()));
            }
            log.info("Loaded {} ComfyUI workflow(s) from DB", all.size());
        } catch (Exception e) {
            log.warn("Failed to preload ComfyUI workflows: {}", e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getWorkflowJsonById(Long id) {
        GenPictureWorkflowPath record = mapper.selectById(id);
        if (record == null) throw new RuntimeException("未找到工作流配置, id: " + id);
        try { return loadFromPath(record.getJsonPath()); }
        catch (Exception e) { throw new RuntimeException("加载工作流文件失败: " + record.getJsonPath(), e); }
    }

    @Override
    public List<GenPictureWorkflowPath> listAll() { return mapper.selectAll(); }

    @Override
    public int save(GenPictureWorkflowPath record) {
        workflowCache.remove(record.getWorkflowType());
        GenPictureWorkflowPath existing = mapper.selectByType(record.getWorkflowType());
        if (existing != null) return mapper.updateJsonPath(existing.getId(), record.getJsonPath());
        return mapper.insert(record);
    }

    @Override
    public int deleteById(Long id) {
        GenPictureWorkflowPath record = mapper.selectById(id);
        if (record != null) workflowCache.remove(record.getWorkflowType());
        return mapper.deleteById(id);
    }

    private Map<String, Object> loadFromPath(String jsonPath) throws Exception {
        if (jsonPath.startsWith("classpath:"))
            return loadFromClasspath(jsonPath.substring("classpath:".length()));
        else return loadFromFileSystem(jsonPath);
    }

    private Map<String, Object> loadFromClasspath(String path) throws Exception {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream is = resource.getInputStream()) {
            return objectMapper.readValue(is, new TypeReference<Map<String, Object>>() {});
        }
    }

    private Map<String, Object> loadFromFileSystem(String path) throws Exception {
        File file = new File(path);
        byte[] bytes = Files.readAllBytes(file.toPath());
        return objectMapper.readValue(bytes, new TypeReference<Map<String, Object>>() {});
    }
}
