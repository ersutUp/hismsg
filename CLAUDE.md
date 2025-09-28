# CLAUDE.md

这个文件为 Claude Code (claude.ai/code) 在此代码库中工作时提供指导。

## 项目概述

这是一个消息通知/转发系统，允许用户通过多个平台接收和转发消息。系统采用 Vue3 前端和 Spring Boot 后端设计。

## 技术栈

### 前端
- Vue 3
- Element Plus UI 框架
- 扁平化设计，支持移动端

### 后端
- Spring Boot
- MyBatis Plus
- Maven
- Lombok
- Java 包名：`xyz.ersut.message`

### 数据库
- MySQL（主数据库）
- ClickHouse（消息存储）
- Redis（消息转发的发布订阅）

## 核心功能

1. **用户认证与管理**
   - 用户登录系统
   - 用户个性化配置

2. **多平台消息转发**
   - Bark (https://bark.day.app/#/)
   - 邮箱
   - WxPusher (https://wxpusher.zjiecode.com/docs/#/)
   - PushMe (https://push.i-i.me/docs/index)
   - 可扩展接入其他平台

3. **字典管理**
   - 字典表和字典分类表
   - 管理维护页面

4. **消息存储与处理**
   - ClickHouse 消息存储
   - 与 Bark 格式兼容的 REST API
   - 按类型、用户和来源分类消息
   - Redis 发布订阅实现消息转发解耦

## 系统架构

### 消息处理流程
1. 通过 API 接收消息（用户编号、消息内容、消息类型）
2. 存储到 ClickHouse 数据库
3. 根据用户平台配置通过 Redis 发布订阅转发
4. 推送到配置的通知平台

### API 设计
- 消息通知 API 与 Bark 格式保持一致
- RESTful 配置管理接口
- 基于用户的消息转发设置
- 添加swagger文档

## 开发规范

- 注释要详细充分
- 逻辑结构清晰
- 按模块进行目录结构整理
- 数据库字段可根据需要添加
- 后端：尽量使用工具类
- 前端：CSS 尽量使用 Element Plus 自带样式
- 可自行添加项目依赖

## 沟通注意事项
- 使用中文与我沟通