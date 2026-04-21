package com.example.controller;

import cn.hutool.core.util.ObjectUtil;
import com.example.common.Result;
import com.example.common.BaseContext;
import com.example.entity.Comment;
import com.example.service.CommentService;
import com.example.service.CommentLikeService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论前端操作接口
 */
@RestController
@RequestMapping("/comment")
public class CommentController {

    @Resource
    private CommentService commentService;

    @Resource
    private CommentLikeService commentLikeService;

    /**
     * 新增评论
     */
    @PostMapping("/add")
    public Result add(@RequestBody Comment comment) {
        comment.setUserId(BaseContext.getCurrentId());
        commentService.add(comment);
        return Result.success();
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        // 验证权限：只有评论创建者可以删除
        if (!commentService.hasPermission(id, BaseContext.getCurrentId())) {
            return Result.error("无权限删除该评论");
        }
        commentService.deleteById(id);
        return Result.success();
    }

    /**
     * 根据相册ID获取评论列表
     */
    @GetMapping("/selectByCategoryId/{categoryId}")
    public Result selectByCategoryId(@PathVariable Integer categoryId) {
        List<Comment> comments = commentService.getCommentsByCategoryId(categoryId);
        return Result.success(comments);
    }

    /**
     * 点赞评论
     */
    @PostMapping("/like/{commentId}")
    public Result likeComment(@PathVariable Integer commentId) {
        boolean success = commentLikeService.likeComment(commentId, BaseContext.getCurrentId());
        if (success) {
            return Result.success("点赞成功");
        } else {
            return Result.error("已经点赞过了");
        }
    }

    /**
     * 取消点赞
     */
    @PostMapping("/unlike/{commentId}")
    public Result unlikeComment(@PathVariable Integer commentId) {
        boolean success = commentLikeService.unlikeComment(commentId, BaseContext.getCurrentId());
        if (success) {
            return Result.success("取消点赞成功");
        } else {
            return Result.error("尚未点赞");
        }
    }

    /**
     * 检查用户是否点赞了评论
     */
    @GetMapping("/isLiked/{commentId}")
    public Result isLiked(@PathVariable Integer commentId) {
        boolean isLiked = commentLikeService.isLiked(commentId, BaseContext.getCurrentId());
        return Result.success(isLiked);
    }

    /**
     * 获取评论点赞数量
     */
    @GetMapping("/likeCount/{commentId}")
    public Result getLikeCount(@PathVariable Integer commentId) {
        int likeCount = commentLikeService.getLikeCount(commentId);
        return Result.success(likeCount);
    }

    /**
     * 查询所有评论（管理员用）
     */
    @GetMapping("/selectAll")
    public Result selectAll(Comment comment) {
        List<Comment> list = commentService.selectAll(comment);
        return Result.success(list);
    }
}