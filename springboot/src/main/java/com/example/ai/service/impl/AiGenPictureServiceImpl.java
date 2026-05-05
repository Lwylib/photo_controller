package com.example.ai.service.impl;

import com.example.ai.domain.AiGenPicture;
import com.example.ai.mapper.AiGenPictureMapper;
import com.example.ai.service.IAiGenPictureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AiGenPictureServiceImpl implements IAiGenPictureService {
    @Autowired
    private AiGenPictureMapper mapper;

    @Override
    public AiGenPicture saveRecord(Long userId, String filename, String workflowType,
                                   String promptText, String negativeText, String paramsJson, String localPath) {
        AiGenPicture record = new AiGenPicture();
        record.setUserId(userId);
        record.setFilename(filename);
        record.setWorkflowType(workflowType);
        record.setPromptText(promptText);
        record.setNegativeText(negativeText);
        record.setParamsJson(paramsJson);
        record.setFilepath(localPath);
        mapper.insert(record);
        return record;
    }

    @Override
    public List<AiGenPicture> listByUser(Long userId) { return mapper.selectByUserId(userId); }

    @Override
    public AiGenPicture getById(Long id) { return mapper.selectById(id); }

    @Override
    public int deleteById(Long id, Long userId) { return mapper.deleteById(id, userId); }
}
