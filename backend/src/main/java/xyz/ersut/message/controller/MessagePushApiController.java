package xyz.ersut.message.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import cn.hutool.core.util.StrUtil;
import xyz.ersut.message.dto.MessagePushRequest;
import xyz.ersut.message.dto.Result;
import xyz.ersut.message.service.MessageForwardService;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 消息推送API控制器（兼容Bark格式）
 * 
 * @author ersut
 */
@Tag(name = "消息推送API", description = "消息推送相关接口，兼容Bark格式")
@Slf4j
@RestController
@RequestMapping("/api/message/push")
@RequiredArgsConstructor
public class MessagePushApiController {
    
    private final MessageForwardService messageForwardService;
    
    /**
     * Bark兼容格式推送接口
     * 支持以下格式：
     * GET  /api/message/push/{userKey}
     * GET  /api/message/push/{userKey}/{title}
     * GET  /api/message/push/{userKey}/{title}/{content}
     * GET /api/message/push/{userKey}/{title}/{subtitle}/{content}
     * 
     * @param userKey 用户秘钥
     * @param title 消息标题（可选）
     * @param subtitle 消息副标题（可选）
     * @param content 消息内容（可选）
     * @param request HTTP请求
     * @return 推送结果
     */
    @Operation(summary = "Bark兼容格式推送消息（GET）", description = "通过GET请求推送消息，兼容Bark客户端格式")
    @GetMapping({
        "/{userKey}",
        "/{userKey}/{title}",
        "/{userKey}/{title}/{content}",
        "/{userKey}/{title}/{subtitle}/{content}"
    })
    public Result<Map<String, Object>> pushMessageGet(@Parameter(description = "用户编号") @PathVariable String userKey,
                                                      @Parameter(description = "消息标题") @PathVariable(required = false) String title,
                                                      @Parameter(description = "消息副标题") @PathVariable(required = false) String subtitle,
                                                     @Parameter(description = "消息内容") @PathVariable(required = false) String content,
                                                     HttpServletRequest request) {
        try {
            // 构建推送请求
            MessagePushRequest pushRequest = buildPushRequestFromGet(userKey, title, subtitle, content, request);
            
            // 执行推送
            Long messageId = messageForwardService.pushMessage(pushRequest);
            
            // 返回Bark兼容的响应格式
            return Result.success("推送成功", Map.of(
                "timestamp", System.currentTimeMillis() / 1000,
                "messageId", messageId
            ));
            
        } catch (Exception e) {
            log.error("消息推送失败: userKey={}, error={}", userKey, e.getMessage(), e);
            return Result.error(400, e.getMessage());
        }
    }
    
    /**
     * POST方式推送消息（支持更多参数）
     * 
     * @param userKey 用户密钥
     * @param requestBody 请求体
     * @return 推送结果
     */
    @Operation(summary = "Bark兼容格式推送消息（POST）", description = "通过POST请求推送消息，支持更多参数")
    @PostMapping("/{userKey}")
    public Result<Map<String, Object>> pushMessagePost(@Parameter(description = "用户编号") @PathVariable String userKey,
                                                      @RequestBody(required = false) Map<String, Object> requestBody) {
        try {
            // 构建推送请求
            MessagePushRequest pushRequest = buildPushRequestFromPost(userKey, requestBody);
            
            // 执行推送
            Long messageId = messageForwardService.pushMessage(pushRequest);
            
            // 返回Bark兼容的响应格式
            return Result.success("推送成功", Map.of(
                "timestamp", System.currentTimeMillis() / 1000,
                "messageId", messageId
            ));
            
        } catch (Exception e) {
            log.error("消息推送失败: userKey={}, error={}", userKey, e.getMessage(), e);
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 通用推送接口（支持JSON格式）
     * 
     * @param pushRequest 推送请求
     * @return 推送结果
     */
    @Operation(summary = "通用推送接口", description = "支持JSON格式的通用消息推送接口")
    @PostMapping("/send")
    public Result<Map<String, Object>> sendMessage(@RequestBody MessagePushRequest pushRequest) {
        try {
            // 参数验证
            if (StrUtil.isBlank(pushRequest.getUserKey())) {
                return Result.error("用户密钥不能全部为空");
            }
            
            // 执行推送
            Long messageId = messageForwardService.pushMessage(pushRequest);
            
            return Result.success("推送成功", Map.of(
                "messageId", messageId,
                "timestamp", System.currentTimeMillis() / 1000
            ));
            
        } catch (Exception e) {
            log.error("消息推送失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 健康检查接口
     * 
     * @return 系统状态
     */
    @Operation(summary = "健康检查", description = "检查系统运行状态")
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        return Result.success("系统运行正常", Map.of(
            "status", "ok",
            "timestamp", System.currentTimeMillis() / 1000,
            "version", "1.0.0"
        ));
    }
    
    /**
     * 从GET请求构建推送请求对象
     *
     * @param userKey 用户密钥
     * @param title 标题
     * @param content 内容
     * @param request HTTP请求
     * @return 推送请求对象
     */
    private MessagePushRequest buildPushRequestFromGet(String userKey, String title, String subtitle, String content, HttpServletRequest request) {
        MessagePushRequest pushRequest = new MessagePushRequest();
        pushRequest.setUserKey(userKey);
        
        // 从URL参数中获取标题和内容
        if (StrUtil.isNotBlank(title)) {
            pushRequest.setTitle(urlDecode(title));
        }
        if (StrUtil.isNotBlank(subtitle)) {
            pushRequest.setSubtitle(urlDecode(subtitle));
        }
        if (StrUtil.isNotBlank(content)) {
            pushRequest.setContent(urlDecode(content));
        }
        
        // 从查询参数中获取额外参数
        String queryTitle = request.getParameter("title");
        String queryContent = request.getParameter("body");
        String queryUrl = request.getParameter("url");
        String querySound = request.getParameter("sound");
        String queryGroup = request.getParameter("group");
        String queryIcon = request.getParameter("icon");
        String queryLevel = request.getParameter("level");
        String queryCategory = request.getParameter("category");
        
        // 查询参数优先级更高
        if (StrUtil.isNotBlank(queryTitle)) {
            pushRequest.setTitle(queryTitle);
        }
        if (StrUtil.isNotBlank(queryContent)) {
            pushRequest.setContent(queryContent);
        }
        if (StrUtil.isNotBlank(queryUrl)) {
            pushRequest.setUrl(queryUrl);
        }
        
        // 如果没有标题，使用默认标题
        if (StrUtil.isBlank(pushRequest.getTitle()) && StrUtil.isNotBlank(pushRequest.getContent())) {
            pushRequest.setTitle("新消息");
        }
        
        // 设置消息级别
        if (StrUtil.isNotBlank(queryLevel)) {
            pushRequest.setLevel(mapBarkLevelToSystemLevel(queryLevel));
        }
        
        // 设置消息类型
        if (StrUtil.isNotBlank(queryCategory)) {
            pushRequest.setMessageType(queryCategory);
        } else {
            pushRequest.setMessageType("notification");
        }
        
        // 设置来源
//        pushRequest.setSource("bark-api");
        
        return pushRequest;
    }
    
    /**
     * 从POST请求构建推送请求对象
     *
     * @param userKey 用户密钥
     * @param requestBody 请求体
     * @return 推送请求对象
     */
    @SuppressWarnings("unchecked")
    private MessagePushRequest buildPushRequestFromPost(String userKey, Map<String, Object> requestBody) {
        MessagePushRequest pushRequest = new MessagePushRequest();
        pushRequest.setUserKey(userKey);
        
        if (requestBody == null) {
            throw new RuntimeException("请求体不能为空");
        }
        
        // 从请求体中提取参数
        pushRequest.setTitle((String) requestBody.get("title"));
        pushRequest.setContent((String) requestBody.getOrDefault("body", requestBody.get("content")));
        pushRequest.setUrl((String) requestBody.get("url"));
        
        // 设置消息级别
        String level = (String) requestBody.get("level");
        if (StrUtil.isNotBlank(level)) {
            pushRequest.setLevel(mapBarkLevelToSystemLevel(level));
        }
        
        // 设置消息类型
        String category = (String) requestBody.get("category");
        if (StrUtil.isNotBlank(category)) {
            pushRequest.setMessageType(category);
        } else {
            pushRequest.setMessageType("notification");
        }
        
        // 设置标签
        Object tagsObj = requestBody.get("tags");
        if (tagsObj instanceof List) {
            pushRequest.setTags((List<String>) tagsObj);
        }
        
        // 设置来源
//        pushRequest.setSource("bark-api");
        
        return pushRequest;
    }

    /**
     * 将Bark的级别映射到系统级别
     * 
     * @param barkLevel Bark级别
     * @return 系统级别
     */
    private String mapBarkLevelToSystemLevel(String barkLevel) {
        return switch (barkLevel.toLowerCase()) {
            case "passive" -> "low";
            case "active" -> "normal";
            case "timesensitive" -> "high";
            case "critical" -> "critical";
            default -> "normal";
        };
    }
    
    /**
     * URL解码
     * 
     * @param str 待解码字符串
     * @return 解码后的字符串
     */
    private String urlDecode(String str) {
        if (StrUtil.isBlank(str)) {
            return str;
        }
        try {
            return java.net.URLDecoder.decode(str, "UTF-8");
        } catch (Exception e) {
            log.warn("URL解码失败: {}", str);
            return str;
        }
    }
}