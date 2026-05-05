-- AI 功能数据库建表脚本
-- 在 photo_controller 数据库中执行

-- AI 对话会话表
CREATE TABLE IF NOT EXISTS `ai_conversation` (
  `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `title`       varchar(200) NOT NULL DEFAULT '新对话' COMMENT '会话标题',
  `user_id`     bigint       NOT NULL COMMENT '用户ID',
  `model`       varchar(100) DEFAULT NULL COMMENT 'AI模型名称',
  `status`      int          DEFAULT 1 COMMENT '状态(1正常 0删除)',
  `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话会话表';

-- AI 对话消息表
CREATE TABLE IF NOT EXISTS `ai_message` (
  `id`              bigint       NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `conversation_id` bigint       NOT NULL COMMENT '会话ID',
  `role`            varchar(20)  NOT NULL COMMENT '角色(user/assistant)',
  `content`         text         NOT NULL COMMENT '消息内容',
  `tokens`          int          DEFAULT 0 COMMENT 'Token消耗数',
  `create_time`     datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_conversation_id` (`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话消息表';

-- AI 生图记录表
CREATE TABLE IF NOT EXISTS `ai_gen_picture` (
  `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`       bigint       NOT NULL COMMENT '用户ID',
  `filepath`      varchar(500) NOT NULL COMMENT '图片本地路径',
  `filename`      varchar(200) NOT NULL COMMENT '原始文件名',
  `workflow_type` varchar(50)  NOT NULL COMMENT '工作流类型',
  `prompt_text`   text         DEFAULT NULL COMMENT '正向提示词',
  `negative_text` text         DEFAULT NULL COMMENT '负向提示词',
  `params_json`   text         DEFAULT NULL COMMENT '生成参数JSON',
  `create_time`   datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI生图记录表';

-- AI 生图工作流路径表
CREATE TABLE IF NOT EXISTS `gen_picture_workflow_path` (
  `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `json_path`     varchar(500) NOT NULL COMMENT '工作流JSON文件路径',
  `workflow_name` varchar(200) NOT NULL COMMENT '工作流名称',
  `workflow_type` varchar(50)  NOT NULL COMMENT '工作流类型',
  `create_time`   datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_workflow_type` (`workflow_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI生图工作流路径表';

-- 种子数据: 工作流路径
INSERT INTO `gen_picture_workflow_path` (`json_path`, `workflow_name`, `workflow_type`)
VALUES ('classpath:workflows/文生图.json', '文生图', 'txt2img');

INSERT INTO `gen_picture_workflow_path` (`json_path`, `workflow_name`, `workflow_type`)
VALUES ('classpath:workflows/图生图.json', '图生图', 'img2img');

INSERT INTO `gen_picture_workflow_path` (`json_path`, `workflow_name`, `workflow_type`)
VALUES ('classpath:workflows/文生图16重放大K-K-K-4x.json', '文生图16重放大K-K-K-4x', 'txt2img_4x_k');

INSERT INTO `gen_picture_workflow_path` (`json_path`, `workflow_name`, `workflow_type`)
VALUES ('classpath:workflows/文生图16重放大K-K-SD-4x.json', '文生图16重放大K-K-SD-4x', 'txt2img_4x_sd');

INSERT INTO `gen_picture_workflow_path` (`json_path`, `workflow_name`, `workflow_type`)
VALUES ('classpath:workflows/图生图16重放大K-K-K-4x.json', '图生图16重放大K-K-K-4x', 'img2img_4x_k');

INSERT INTO `gen_picture_workflow_path` (`json_path`, `workflow_name`, `workflow_type`)
VALUES ('classpath:workflows/图生图16重放大K-K-SD-4x.json', '图生图16重放大K-K-SD-4x', 'img2img_4x_sd');
