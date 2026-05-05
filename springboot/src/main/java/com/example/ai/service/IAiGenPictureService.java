package com.example.ai.service;

import com.example.ai.domain.AiGenPicture;
import java.util.List;

public interface IAiGenPictureService {
    AiGenPicture saveRecord(Long userId, String filename, String workflowType,
                            String promptText, String negativeText, String paramsJson, String localPath);
    List<AiGenPicture> listByUser(Long userId);
    AiGenPicture getById(Long id);
    int deleteById(Long id, Long userId);
}
