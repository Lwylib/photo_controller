package com.example.mapper;

import com.example.entity.Appeal;
import java.util.List;

/**
 * 操作appeal相关数据接口
*/
public interface AppealMapper {

    /**
      * 新增
    */
    int insert(Appeal appeal);

    /**
      * 删除
    */
    int deleteById(Integer id);

    /**
      * 修改
    */
    int updateById(Appeal appeal);

    /**
      * 根据ID查询
    */
    Appeal selectById(Integer id);

    /**
      * 查询所有
    */
    List<Appeal> selectAll(Appeal appeal);

    void deleteByUserId(Integer id);
}