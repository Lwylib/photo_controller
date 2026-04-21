package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.entity.CommentLike;
import com.example.mapper.CommentLikeMapper;
import com.example.mapper.CommentMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CommentLikeService {

    @Resource
    private CommentLikeMapper commentLikeMapper;

    @Resource
    private CommentMapper commentMapper;

    /**
     * 点赞评论
     */
    @Transactional
    public boolean likeComment(Integer commentId, Integer userId) {
        // 检查是否已经点赞
        CommentLike existingLike = commentLikeMapper.selectByCommentIdAndUserId(commentId, userId);
        if (existingLike != null) {
            return false; // 已经点赞过了
        }

        // 新增点赞记录
        CommentLike commentLike = new CommentLike();
        commentLike.setCommentId(commentId);
        commentLike.setUserId(userId);
        commentLike.setCreateTime(LocalDateTime.now());
        commentLikeMapper.insert(commentLike);

        // 更新评论的点赞数
        updateCommentLikeCount(commentId);

        return true;
    }

    /**
     * 取消点赞
     */
    @Transactional
    public boolean unlikeComment(Integer commentId, Integer userId) {
        // 检查是否有点赞记录
        CommentLike existingLike = commentLikeMapper.selectByCommentIdAndUserId(commentId, userId);
        if (existingLike == null) {
            return false; // 没有点赞记录
        }

        // 删除点赞记录
        commentLikeMapper.deleteByCommentIdAndUserId(commentId, userId);

        // 更新评论的点赞数
        updateCommentLikeCount(commentId);

        return true;
    }

    /**
     * 检查用户是否点赞了某个评论
     */
    public boolean isLiked(Integer commentId, Integer userId) {
        CommentLike commentLike = commentLikeMapper.selectByCommentIdAndUserId(commentId, userId);
        return commentLike != null;
    }

    /**
     * 获取评论的点赞数量
     */
    public int getLikeCount(Integer commentId) {
        return commentLikeMapper.countByCommentId(commentId);
    }

    /**
     * 更新评论的点赞数（实时计算）
     */
    private void updateCommentLikeCount(Integer commentId) {
        int likeCount = commentLikeMapper.countByCommentId(commentId);
        commentMapper.updateLikeCount(commentId, likeCount);
    }

    /**
     * 根据评论ID删除所有点赞记录
     */
    public void deleteByCommentId(Integer commentId) {
        commentLikeMapper.deleteByCommentId(commentId);
    }
}