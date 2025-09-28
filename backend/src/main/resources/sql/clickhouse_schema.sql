-- ClickHouse 消息存储数据库

-- 创建数据库
CREATE DATABASE IF NOT EXISTS hismsg_message;

USE hismsg_message;
-- message_record DDL
CREATE TABLE `message_record` (
                                  `id` UInt64 COMMENT '消息ID',
                                  `user_id` UInt64 COMMENT '用户ID',
                                  `user_code` String COMMENT '用户编号',
                                  `message_type` String COMMENT '消息类型（notification、alert、system、custom）',
                                  `title` String COMMENT '消息标题',
                                  `subtitle` Nullable(String) COMMENT '副标题',
                                  `content` String COMMENT '消息内容',
                                  `group` Nullable(String) COMMENT '消息分组',
                                  `url` String DEFAULT '' COMMENT '消息链接',
                                  `source` String DEFAULT '' COMMENT '数据来源',
                                  `level` String DEFAULT 'normal' COMMENT '推送中断级别。 critical: 重要警告, 在静音模式下也会响铃 active：默认值，系统会立即亮屏显示通知 timeSensitive：时效性通知，可在专注状态下显示通知。 passive：仅将通知添加到通知列表，不会亮屏提醒。',
                                  `tags` Array(String) DEFAULT [] COMMENT '消息标签',
                                  `extra_data` String DEFAULT '{}' COMMENT '额外数据（JSON格式）',
                                  `status` UInt8 DEFAULT 1 COMMENT '消息状态（0=删除，1=正常）',
                                  `pushed_platforms` Array(String) DEFAULT [] COMMENT '已推送的平台列表',
                                  `push_success_count` UInt32 DEFAULT 0 COMMENT '推送成功次数',
                                  `push_fail_count` UInt32 DEFAULT 0 COMMENT '推送失败次数',
                                  `create_time` DateTime DEFAULT now() COMMENT '创建时间',
                                  `update_time` DateTime DEFAULT now() COMMENT '更新时间'
) ENGINE = MergeTree ORDER BY (`user_id`,`id`) PARTITION BY (toYYYYMM(create_time)) PRIMARY KEY (`user_id`,`id`) Comment '消息记录表（保留12个月数据）' TTL create_time + toIntervalMonth(12) SETTINGS index_granularity = 8192;
-- message_stats_daily DDL
CREATE TABLE `message_stats_daily` (
                                       `stat_date` Date COMMENT '统计日期',
                                       `user_id` UInt64 COMMENT '用户ID',
                                       `message_type` String COMMENT '消息类型',
                                       `message_count` UInt64 COMMENT '消息总数',
                                       `success_count` UInt64 COMMENT '推送成功数',
                                       `fail_count` UInt64 COMMENT '推送失败数'
) ENGINE = SummingMergeTree ORDER BY (`stat_date`) PARTITION BY (toYYYYMM(stat_date)) PRIMARY KEY (`stat_date`) Comment '消息日统计表（保留12个月数据）' TTL stat_date + toIntervalMonth(12) SETTINGS index_granularity = 8192;
-- push_record DDL
CREATE TABLE `push_record` (
                               `id` UInt64 COMMENT '推送记录ID',
                               `message_id` UInt64 COMMENT '消息ID',
                               `user_id` UInt64 COMMENT '用户ID',
                               `platform` String COMMENT '推送平台',
                               `config_name` String COMMENT '配置名称',
                               `push_status` UInt8 COMMENT '推送状态（0=失败，1=成功，2=进行中）',
                               `request_data` String DEFAULT '' COMMENT '请求数据',
                               `response_data` Nullable(String) DEFAULT '' COMMENT '响应数据',
                               `error_message` Nullable(String) DEFAULT '' COMMENT '错误信息',
                               `retry_count` UInt32 DEFAULT 0 COMMENT '重试次数',
                               `push_time` DateTime COMMENT '推送时间',
                               `create_time` DateTime DEFAULT now() COMMENT '创建时间'
) ENGINE = MergeTree ORDER BY (`user_id`,`id`) PARTITION BY (toYYYYMM(create_time)) PRIMARY KEY (`user_id`,`id`) Comment '推送记录表（保留12个月数据）' TTL create_time + toIntervalMonth(12) SETTINGS index_granularity = 8192;
-- message_record Indexes
;
-- message_stats_daily Indexes
;
-- push_record Indexes
;

-- 数据清理任务配置说明
-- ClickHouse TTL (Time To Live) 会自动删除超过6个月的数据
-- 可以通过以下命令手动触发清理：
-- ALTER TABLE message_record DELETE WHERE create_time < now() - INTERVAL 6 MONTH;
-- ALTER TABLE push_record DELETE WHERE create_time < now() - INTERVAL 6 MONTH;
-- ALTER TABLE message_stats_daily DELETE WHERE stat_date < today() - INTERVAL 6 MONTH;