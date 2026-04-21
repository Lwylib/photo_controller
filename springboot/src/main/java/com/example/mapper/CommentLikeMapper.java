package com.example.mapper;

import com.example.entity.CommentLike;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface CommentLikeMapper {

    /**
     * 新增点赞记录
     */
    int insert(CommentLike commentLike);

    /**
     * 删除点赞记录
     */
    int deleteById(Integer id);

    /**
     * 根据评论ID和用户ID删除点赞记录
     */
    int deleteByCommentIdAndUserId(Integer commentId, Integer userId);

    /**
     * 根据评论ID查询点赞记录
     */
    List<CommentLike> selectByCommentId(Integer commentId);

    /**
     * 根据用户ID查询点赞记录
     */
    List<CommentLike> selectByUserId(Integer userId);

    /**
     * 根据评论ID和用户ID查询点赞记录
     */
    CommentLike selectByCommentIdAndUserId(Integer commentId, Integer userId);

    /**
     * 根据评论ID统计点赞数量
     */
    int countByCommentId(Integer commentId);

    /**
     * 根据评论ID删除所有点赞记录
     */
    int deleteByCommentId(Integer commentId);
}