package com.example.ai.mapper;

import com.example.ai.domain.AiGenPicture;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface AiGenPictureMapper {
    AiGenPicture selectById(Long id);
    List<AiGenPicture> selectByUserId(Long userId);
    int insert(AiGenPicture record);
    int deleteById(Long id, Long userId);
}
