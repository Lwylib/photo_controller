package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.entity.Comment;
import com.example.entity.User;
import com.example.mapper.CommentMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private UserService userService;

    /**
     * 新增评论
     */
    public void add(Comment comment) {
        comment.setCreateTime(LocalDateTime.now());
        comment.setLikeCount(0);
        comment.setStatus("正常");
        
        // 验证父评论是否存在（如果是回复）
        if (comment.getParentId() != null && comment.getParentId() > 0) {
            Comment parentComment = commentMapper.selectById(comment.getParentId());
            if (parentComment == null) {
                throw new RuntimeException("父评论不存在");
            }
            // 确保回复的相册ID与父评论一致
            comment.setCategoryId(parentComment.getCategoryId());
        }
        
        commentMapper.insert(comment);
    }

    /**
     * 删除评论
     */
    public void deleteById(Integer id) {
        commentMapper.deleteById(id);
    }

    /**
     * 根据相册ID删除所有评论
     */
    public void deleteByCategoryId(Integer categoryId) {
        commentMapper.deleteByCategoryId(categoryId);
    }

    /**
     * 根据相册ID获取评论列表（包含二级评论）
     */
    public List<Comment> getCommentsByCategoryId(Integer categoryId) {
        // 获取顶级评论
        List<Comment> topLevelComments = commentMapper.selectTopLevelByCategoryId(categoryId);
        
        if (topLevelComments.isEmpty()) {
            return topLevelComments;
        }
        
        // 获取顶级评论的ID列表
        List<Integer> topLevelCommentIds = topLevelComments.stream()
                .map(Comment::getId)
                .collect(Collectors.toList());
        
        // 批量查询回复
        List<Comment> allReplies = commentMapper.selectRepliesByCategoryId(categoryId);
        
        // 按父评论ID分组
        Map<Integer, List<Comment>> repliesByParentId = allReplies.stream()
                .collect(Collectors.groupingBy(Comment::getParentId));
        
        // 将回复组装到对应的顶级评论中
        for (Comment topComment : topLevelComments) {
            List<Comment> replies = repliesByParentId.get(topComment.getId());
            topComment.setReplies(replies != null ? replies : new ArrayList<>());
        }
        
        return topLevelComments;
    }

    /**
     * 根据ID查询评论
     */
    public Comment selectById(Integer id) {
        return commentMapper.selectById(id);
    }

    /**
     * 查询所有评论
     */
    public List<Comment> selectAll(Comment comment) {
        return commentMapper.selectAll(comment);
    }

    /**
     * 更新评论点赞数
     */
    public void updateLikeCount(Integer commentId, Integer likeCount) {
        commentMapper.updateLikeCount(commentId, likeCount);
    }

    /**
     * 验证用户是否有权限操作评论
     */
    public boolean hasPermission(Integer commentId, Integer userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            return false;
        }
        return comment.getUserId().equals(userId);
    }
}