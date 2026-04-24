package com.example.controller;

import cn.hutool.core.util.ObjectUtil;
import com.example.common.Result;
import com.example.common.BaseContext;
import com.example.entity.Comment;
import com.example.service.CommentService;
import com.example.service.CommentLikeService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论前端操作接口
 */
@RestController
@RequestMapping("/comment")
@Slf4j
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
        log.info("CommentController.add() - 新增评论, 用户ID: {}, 相册ID: {}", BaseContext.getCurrentId(), comment.getCategoryId());
        comment.setUserId(BaseContext.getCurrentId());
        commentService.add(comment);
        log.info("CommentController.add() - 新增评论成功");
        return Result.success();
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        log.info("CommentController.deleteById() - 删除评论, ID: {}, 用户ID: {}", id, BaseContext.getCurrentId());
        // 验证权限：只有评论创建者可以删除
        if (!commentService.hasPermission(id, BaseContext.getCurrentId())) {
            log.warn("CommentController.deleteById() - 无权限删除评论, ID: {}", id);
            return Result.error("无权限删除该评论");
        }
        commentService.deleteById(id);
        log.info("CommentController.deleteById() - 删除评论成功");
        return Result.success();
    }

    /**
     * 根据相册ID获取评论列表（分页）
     */
    @GetMapping("/selectByCategoryId/{categoryId}")
    public Result selectByCategoryId(@PathVariable Integer categoryId, 
                                    @RequestParam(defaultValue = "1") Integer pageNum,
                                    @RequestParam(defaultValue = "5") Integer pageSize) {
        log.info("CommentController.selectByCategoryId() - 查询相册评论, 相册ID: {}, 页码: {}, 页大小: {}", categoryId, pageNum, pageSize);
        PageInfo<Comment> pageInfo = commentService.getCommentsByCategoryIdPage(categoryId, pageNum, pageSize);
        log.info("CommentController.selectByCategoryId() - 查询成功, 总评论数: {}", pageInfo.getTotal());
        return Result.success(pageInfo);
    }

    /**
     * 点赞评论
     */
    @PostMapping("/like/{commentId}")
    public Result likeComment(@PathVariable Integer commentId) {
        log.info("CommentController.likeComment() - 点赞评论, 评论ID: {}, 用户ID: {}", commentId, BaseContext.getCurrentId());
        boolean success = commentLikeService.likeComment(commentId, BaseContext.getCurrentId());
        if (success) {
            log.info("CommentController.likeComment() - 点赞成功");
            return Result.success("点赞成功");
        } else {
            log.info("CommentController.likeComment() - 已经点赞过了");
            return Result.error("已经点赞过了");
        }
    }

    /**
     * 取消点赞
     */
    @PostMapping("/unlike/{commentId}")
    public Result unlikeComment(@PathVariable Integer commentId) {
        log.info("CommentController.unlikeComment() - 取消点赞, 评论ID: {}, 用户ID: {}", commentId, BaseContext.getCurrentId());
        boolean success = commentLikeService.unlikeComment(commentId, BaseContext.getCurrentId());
        if (success) {
            log.info("CommentController.unlikeComment() - 取消点赞成功");
            return Result.success("取消点赞成功");
        } else {
            log.info("CommentController.unlikeComment() - 尚未点赞");
            return Result.error("尚未点赞");
        }
    }

    /**
     * 检查用户是否点赞了评论
     */
    @GetMapping("/isLiked/{commentId}")
    public Result isLiked(@PathVariable Integer commentId) {
        log.info("CommentController.isLiked() - 检查点赞状态, 评论ID: {}, 用户ID: {}", commentId, BaseContext.getCurrentId());
        boolean isLiked = commentLikeService.isLiked(commentId, BaseContext.getCurrentId());
        log.info("CommentController.isLiked() - 点赞状态: {}", isLiked);
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