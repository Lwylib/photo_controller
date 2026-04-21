package com.example.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Comment {
    private Integer id;
    private Integer categoryId;      // 相册ID
    private Integer userId;          // 评论用户ID
    private String content;          // 评论内容
    private Integer parentId;        // 父评论ID，0表示顶级评论
    private Integer likeCount;       // 点赞数
    private LocalDateTime createTime;
    private String status;           // 状态

    // 非数据库字段，用于展示用户信息及回复列表
    private String userName;
    private String userAvatar;
    private List<Comment> replies;   // 当前评论的回复列表（仅顶级评论使用）
}
