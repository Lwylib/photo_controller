package com.example.mapper;

import com.example.entity.Picture;
import java.util.List;

/**
 * 操作picture相关数据接口
*/
public interface PictureMapper {

    /**
      * 新增
    */
    int insert(Picture picture);

    /**
      * 删除
    */
    int deleteById(Integer id);

    /**
      * 修改
    */
    int updateById(Picture picture);

    /**
      * 根据ID查询
    */
    Picture selectById(Integer id);

    /**
      * 查询所有
    */
    List<Picture> selectAll(Picture picture);

    int deleteByCategoryId(Integer categoryId);

    void deleteByUserId(Integer id);

    List<Picture> selectByUserId(Integer id);
}