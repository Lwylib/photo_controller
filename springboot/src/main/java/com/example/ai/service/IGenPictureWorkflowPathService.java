package com.example.ai.service;

import com.example.ai.domain.GenPictureWorkflowPath;
import java.util.List;
import java.util.Map;

public interface IGenPictureWorkflowPathService {
    Map<String, Object> getWorkflowJsonById(Long id);
    List<GenPictureWorkflowPath> listAll();
    int save(GenPictureWorkflowPath record);
    int deleteById(Long id);
}
