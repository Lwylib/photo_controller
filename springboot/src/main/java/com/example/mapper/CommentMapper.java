package com.example.mapper;

import com.example.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface CommentMapper {

    /**
     * 新增评论
     */
    int insert(Comment comment);

    /**
     * 根据ID删除评论
     */
    int deleteById(Integer id);

    /**
     * 根据ID查询评论
     */
    Comment selectById(Integer id);

    /**
     * 查询所有评论
     */
    List<Comment> selectAll(Comment comment);

    /**
     * 根据相册ID查询顶级评论（parent_id = 0）
     */
    List<Comment> selectTopLevelByCategoryId(Integer categoryId);

    /**
     * 根据父评论ID查询回复列表
     */
    List<Comment> selectRepliesByParentId(Integer parentId);

    /**
     * 根据相册ID批量查询回复
     */
    List<Comment> selectRepliesByCategoryId(Integer categoryId);

    /**
     * 更新评论点赞数
     */
    int updateLikeCount(Integer id, Integer likeCount);

    /**
     * 根据相册ID删除评论
     */
    int deleteByCategoryId(Integer categoryId);
}