-- 创建数据库
CREATE DATABASE IF NOT EXISTS hismsg DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE hismsg;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                            `username` varchar(50) NOT NULL COMMENT '用户名',
                            `password` varchar(255) NOT NULL COMMENT '密码（加密存储）',
                            `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
                            `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
                            `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
                            `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
                            `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态（0=禁用，1=启用）',
                            `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                            `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标记（0=正常，1=删除）',
                            `user_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '根据用户名md5(16)得来',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_username` (`username`),
                            KEY `idx_email` (`email`),
                            KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统用户表';

-- ----------------------------
-- Records of sys_user
-- ----------------------------
BEGIN;
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `email`, `phone`, `avatar`, `status`, `remark`, `create_time`, `update_time`, `deleted`, `user_key`) VALUES (1, 'admin', '$2a$10$7JB720yubVSOfvVame0OMuURRd1ZjgB0DLnj0davVdvFYH95Elwt6', '系统管理员', 'admin@example.com', NULL, NULL, 1, '系统管理员账户', '2025-09-17 06:53:06', '2025-09-28 07:32:23', 0, '7A57A5A743894A0E');
COMMIT;

-- ----------------------------
-- Table structure for user_push_config
-- ----------------------------
DROP TABLE IF EXISTS `user_push_config`;
CREATE TABLE `user_push_config` (
                                    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
                                    `user_id` bigint NOT NULL COMMENT '用户ID',
                                    `platform` varchar(50) NOT NULL COMMENT '推送平台（bark、email、wxpusher、pushme）',
                                    `config_name` varchar(100) NOT NULL COMMENT '配置名称',
                                    `config_data` text NOT NULL COMMENT '配置数据（JSON格式）',
                                    `is_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用（0=禁用，1=启用）',
                                    `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
                                    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                                    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标记（0=正常，1=删除）',
                                    PRIMARY KEY (`id`),
                                    KEY `idx_user_id` (`user_id`),
                                    KEY `idx_platform` (`platform`),
                                    KEY `idx_create_time` (`create_time`),
                                    CONSTRAINT `fk_user_push_config_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户推送配置表';

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '字典类型ID',
                                 `dict_name` varchar(100) NOT NULL COMMENT '字典名称',
                                 `dict_type` varchar(100) NOT NULL COMMENT '字典类型',
                                 `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态（0=禁用，1=启用）',
                                 `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                                 `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标记（0=正常，1=删除）',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_dict_type` (`dict_type`),
                                 KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典类型表';

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
BEGIN;
INSERT INTO `sys_dict_type` (`id`, `dict_name`, `dict_type`, `status`, `remark`, `create_time`, `update_time`, `deleted`) VALUES (1, '消息类型', 'message_type', 1, '消息推送的类型分类', '2025-09-17 06:53:06', '2025-09-17 06:53:06', 0);
INSERT INTO `sys_dict_type` (`id`, `dict_name`, `dict_type`, `status`, `remark`, `create_time`, `update_time`, `deleted`) VALUES (2, '推送平台', 'push_platform', 1, '消息推送平台类型', '2025-09-17 06:53:06', '2025-09-17 06:53:06', 0);
INSERT INTO `sys_dict_type` (`id`, `dict_name`, `dict_type`, `status`, `remark`, `create_time`, `update_time`, `deleted`) VALUES (3, '用户状态', 'user_status', 1, '用户账户状态', '2025-09-17 06:53:06', '2025-09-17 06:53:06', 0);
INSERT INTO `sys_dict_type` (`id`, `dict_name`, `dict_type`, `status`, `remark`, `create_time`, `update_time`, `deleted`) VALUES (4, '系统状态', 'sys_status', 1, '系统通用状态', '2025-09-17 06:53:06', '2025-09-17 06:53:06', 0);
INSERT INTO `sys_dict_type` (`id`, `dict_name`, `dict_type`, `status`, `remark`, `create_time`, `update_time`, `deleted`) VALUES (5, '消息级别', 'message_level', 1, '消息优先级分类', '2025-09-30 06:53:06', '2025-09-30 06:53:06', 0);
COMMIT;

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '字典数据ID',
                                 `dict_type` varchar(100) NOT NULL COMMENT '字典类型',
                                 `dict_label` varchar(100) NOT NULL COMMENT '字典标签',
                                 `dict_value` varchar(100) NOT NULL COMMENT '字典键值',
                                 `dict_sort` int NOT NULL DEFAULT '0' COMMENT '字典排序',
                                 `css_class` varchar(100) DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
                                 `list_class` varchar(100) DEFAULT NULL COMMENT '表格回显样式',
                                 `is_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否默认（1=是，0=否）',
                                 `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态（0=禁用，1=启用）',
                                 `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                                 `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标记（0=正常，1=删除）',
                                 PRIMARY KEY (`id`),
                                 KEY `idx_dict_type` (`dict_type`),
                                 KEY `idx_dict_sort` (`dict_sort`),
                                 KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典数据表';

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
BEGIN;
INSERT INTO `sys_dict_data` (`id`, `dict_type`, `dict_label`, `dict_value`, `dict_sort`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_time`, `update_time`, `deleted`) VALUES (1, 'message_type', '通知', 'notification', 1, NULL, 'primary', 1, 1, NULL, '2025-09-17 06:53:06', '2025-09-17 06:53:06', 0);
INSERT INTO `sys_dict_data` (`id`, `dict_type`, `dict_label`, `dict_value`, `dict_sort`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_time`, `update_time`, `deleted`) VALUES (2, 'message_type', '告警', 'alert', 2, NULL, 'warning', 0, 1, NULL, '2025-09-17 06:53:06', '2025-09-17 06:53:06', 0);
INSERT INTO `sys_dict_data` (`id`, `dict_type`, `dict_label`, `dict_value`, `dict_sort`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_time`, `update_time`, `deleted`) VALUES (3, 'message_type', '系统', 'system', 3, NULL, 'info', 0, 1, NULL, '2025-09-17 06:53:06', '2025-09-17 06:53:06', 0);
INSERT INTO `sys_dict_data` (`id`, `dict_type`, `dict_label`, `dict_value`, `dict_sort`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_time`, `update_time`, `deleted`) VALUES (4, 'message_type', '自定义', 'custom', 4, NULL, 'success', 0, 1, NULL, '2025-09-17 06:53:06', '2025-09-17 06:53:06', 0);
INSERT INTO `sys_dict_data` (`id`, `dict_type`, `dict_label`, `dict_value`, `dict_sort`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_time`, `update_time`, `deleted`) VALUES (5, 'push_platform', 'Bark', 'bark', 1, NULL, 'primary', 0, 1, NULL, '2025-09-17 06:53:06', '2025-09-17 06:53:06', 0);
INSERT INTO `sys_dict_data` (`id`, `dict_type`, `dict_label`, `dict_value`, `dict_sort`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_time`, `update_time`, `deleted`) VALUES (6, 'push_platform', '邮箱', 'email', 2, NULL, 'success', 0, 1, NULL, '2025-09-17 06:53:06', '2025-09-17 06:53:06', 0);
INSERT INTO `sys_dict_data` (`id`, `dict_type`, `dict_label`, `dict_value`, `dict_sort`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_time`, `update_time`, `deleted`) VALUES (7, 'push_platform', 'WxPusher', 'wxpusher', 3, NULL, 'warning', 0, 1, NULL, '2025-09-17 06:53:06', '2025-09-28 14:57:43', 1);
INSERT INTO `sys_dict_data` (`id`, `dict_type`, `dict_label`, `dict_value`, `dict_sort`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_time`, `update_time`, `deleted`) VALUES (8, 'push_platform', 'PushMe', 'pushme', 4, NULL, 'info', 0, 1, NULL, '2025-09-17 06:53:06', '2025-09-17 06:53:06', 0);
INSERT INTO `sys_dict_data` (`id`, `dict_type`, `dict_label`, `dict_value`, `dict_sort`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_time`, `update_time`, `deleted`) VALUES (9, 'user_status', '正常', '1', 1, NULL, 'primary', 1, 1, NULL, '2025-09-17 06:53:06', '2025-09-17 06:53:06', 0);
INSERT INTO `sys_dict_data` (`id`, `dict_type`, `dict_label`, `dict_value`, `dict_sort`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_time`, `update_time`, `deleted`) VALUES (10, 'user_status', '禁用', '0', 2, NULL, 'danger', 0, 1, NULL, '2025-09-17 06:53:06', '2025-09-17 06:53:06', 0);
INSERT INTO `sys_dict_data` (`id`, `dict_type`, `dict_label`, `dict_value`, `dict_sort`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_time`, `update_time`, `deleted`) VALUES (11, 'sys_status', '正常', '1', 1, NULL, 'primary', 1, 1, NULL, '2025-09-17 06:53:06', '2025-09-17 06:53:06', 0);
INSERT INTO `sys_dict_data` (`id`, `dict_type`, `dict_label`, `dict_value`, `dict_sort`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_time`, `update_time`, `deleted`) VALUES (12, 'sys_status', '停用', '0', 2, NULL, 'info', 0, 1, NULL, '2025-09-17 06:53:06', '2025-09-17 06:53:06', 0);
INSERT INTO `sys_dict_data` (`id`, `dict_type`, `dict_label`, `dict_value`, `dict_sort`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_time`, `update_time`, `deleted`) VALUES (13, 'message_type', '短信', 'sms', 0, '', '', 0, 1, '', '2025-09-28 13:36:26', '2025-09-28 14:01:30', 1);
INSERT INTO `sys_dict_data` (`id`, `dict_type`, `dict_label`, `dict_value`, `dict_sort`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_time`, `update_time`, `deleted`) VALUES (14, 'message_level', '低', 'low', 1, NULL, 'info', 0, 1, '低优先级消息', '2025-09-30 06:53:06', '2025-09-30 06:53:06', 0);
INSERT INTO `sys_dict_data` (`id`, `dict_type`, `dict_label`, `dict_value`, `dict_sort`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_time`, `update_time`, `deleted`) VALUES (15, 'message_level', '普通', 'normal', 2, NULL, 'primary', 1, 1, '普通优先级消息', '2025-09-30 06:53:06', '2025-09-30 06:53:06', 0);
INSERT INTO `sys_dict_data` (`id`, `dict_type`, `dict_label`, `dict_value`, `dict_sort`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_time`, `update_time`, `deleted`) VALUES (16, 'message_level', '高', 'high', 3, NULL, 'warning', 0, 1, '高优先级消息', '2025-09-30 06:53:06', '2025-09-30 06:53:06', 0);
INSERT INTO `sys_dict_data` (`id`, `dict_type`, `dict_label`, `dict_value`, `dict_sort`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_time`, `update_time`, `deleted`) VALUES (17, 'message_level', '紧急', 'critical', 4, NULL, 'danger', 0, 1, '紧急优先级消息', '2025-09-30 06:53:06', '2025-09-30 06:53:06', 0);
COMMIT;

-- ----------------------------
-- Table structure for tag_push_config
-- ----------------------------
DROP TABLE IF EXISTS `tag_push_config`;
CREATE TABLE `tag_push_config` (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                   `user_id` bigint NOT NULL COMMENT '用户ID',
                                   `tag_name` varchar(50) NOT NULL COMMENT '标签名称',
                                   `push_config_ids` json COMMENT '推送配置ID列表（JSON格式）',
                                   `is_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用（1-启用，0-禁用）',
                                   `remark` varchar(255) DEFAULT NULL COMMENT '备注说明',
                                   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除（0-未删除，1-已删除）',
                                   PRIMARY KEY (`id`),
                                   KEY `idx_user_id` (`user_id`),
                                   KEY `idx_tag_name` (`tag_name`),
                                   KEY `idx_create_time` (`create_time`),
                                   CONSTRAINT `fk_tag_push_config_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='标签推送配置表';

SET FOREIGN_KEY_CHECKS = 1;

