package com.example.ai.mapper;

import com.example.ai.domain.GenPictureWorkflowPath;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface GenPictureWorkflowPathMapper {
    GenPictureWorkflowPath selectById(Long id);
    GenPictureWorkflowPath selectByType(String workflowType);
    List<GenPictureWorkflowPath> selectAll();
    int insert(GenPictureWorkflowPath record);
    int updateJsonPath(Long id, String jsonPath);
    int deleteById(Long id);
}
