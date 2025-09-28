package xyz.ersut.message.service.push.impl;

import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;
import cn.hutool.core.util.StrUtil;
import xyz.ersut.message.dto.UserPushConfigDto;
import xyz.ersut.message.entity.MessageRecord;
import xyz.ersut.message.entity.PushRecord;
import xyz.ersut.message.entity.UserPushConfig;
import xyz.ersut.message.service.MessageRecordService;
import xyz.ersut.message.service.UserPushConfigService;
import xyz.ersut.message.service.push.PushService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * WxPusher推送服务实现
 * 
 * @author ersut
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WxPusherPushServiceImpl implements PushService {
    
    private final MessageRecordService messageRecordService;
    private final UserPushConfigService userPushConfigService;
    
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build();
    
    private static final String WXPUSHER_API_URL = "https://wxpusher.zjiecode.com/api/send/message";
    
    @Override
    public PushRecord pushMessage(MessageRecord messageRecord, UserPushConfig config) {
        PushRecord pushRecord = new PushRecord();
        pushRecord.setId(messageRecordService.generatePushRecordId());
        pushRecord.setMessageId(messageRecord.getId());
        pushRecord.setUserId(messageRecord.getUserId());
        pushRecord.setPlatform("wxpusher");
        pushRecord.setConfigName(config.getConfigName());
        pushRecord.setPushStatus(2); // 推送中
        pushRecord.setPushTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        pushRecord.setCreateTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        
        try {
            // 解析WxPusher配置
            UserPushConfigDto configDto = userPushConfigService.convertConfigDataToDto("wxpusher", config.getConfigData());
            UserPushConfigDto.WxPusherConfig wxpusherConfig = configDto.getWxpusher();
            
            if (wxpusherConfig == null || StrUtil.isBlank(wxpusherConfig.getAppToken())) {
                throw new RuntimeException("WxPusher应用Token未配置");
            }
            
            if (StrUtil.isBlank(wxpusherConfig.getUid()) && StrUtil.isBlank(wxpusherConfig.getTopicId())) {
                throw new RuntimeException("WxPusher用户UID或主题ID至少配置一个");
            }
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("appToken", wxpusherConfig.getAppToken());
            requestBody.put("content", buildContent(messageRecord, wxpusherConfig));
            requestBody.put("summary", buildSummary(messageRecord, wxpusherConfig));
            requestBody.put("contentType", wxpusherConfig.getContentType() != null ? wxpusherConfig.getContentType() : 1);
            
            // 设置接收者
            if (StrUtil.isNotBlank(wxpusherConfig.getUid())) {
                List<String> uids = new ArrayList<>();
                uids.add(wxpusherConfig.getUid());
                requestBody.put("uids", uids);
            }
            
            if (StrUtil.isNotBlank(wxpusherConfig.getTopicId())) {
                List<String> topicIds = new ArrayList<>();
                topicIds.add(wxpusherConfig.getTopicId());
                requestBody.put("topicIds", topicIds);
            }
            
            // 设置链接
            if (StrUtil.isNotBlank(messageRecord.getUrl())) {
                requestBody.put("url", messageRecord.getUrl());
            }
            
            String jsonBody = JSON.toJSONString(requestBody);
            pushRecord.setRequestData(jsonBody);
            
            // 发送HTTP请求
            RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                .url(WXPUSHER_API_URL)
                .post(body)
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";
                pushRecord.setResponseData(responseBody);
                
                if (response.isSuccessful()) {
                    // 解析响应判断是否真正成功
                    Map<String, Object> responseMap = JSON.parseObject(responseBody, Map.class);
                    Integer code = (Integer) responseMap.get("code");
                    
                    if (code != null && code == 1000) {
                        pushRecord.setPushStatus(1); // 推送成功
                        log.info("WxPusher推送成功: messageId={}, appToken={}", messageRecord.getId(), 
                            maskAppToken(wxpusherConfig.getAppToken()));
                    } else {
                        pushRecord.setPushStatus(0); // 推送失败
                        pushRecord.setErrorMessage("API返回错误: " + responseMap.get("msg"));
                        log.warn("WxPusher推送失败: messageId={}, code={}, msg={}", 
                            messageRecord.getId(), code, responseMap.get("msg"));
                    }
                } else {
                    pushRecord.setPushStatus(0); // 推送失败
                    pushRecord.setErrorMessage("HTTP错误: " + response.code());
                    log.warn("WxPusher推送失败: messageId={}, httpCode={}, response={}", 
                        messageRecord.getId(), response.code(), responseBody);
                }
            }
            
        } catch (Exception e) {
            pushRecord.setPushStatus(0); // 推送失败
            pushRecord.setErrorMessage(e.getMessage());
            log.error("WxPusher推送异常: messageId={}, error={}", messageRecord.getId(), e.getMessage(), e);
        }
        
        return pushRecord;
    }
    
    @Override
    public boolean testConfig(UserPushConfig config) {
        try {
            // 解析配置
            UserPushConfigDto configDto = userPushConfigService.convertConfigDataToDto("wxpusher", config.getConfigData());
            UserPushConfigDto.WxPusherConfig wxpusherConfig = configDto.getWxpusher();
            
            if (wxpusherConfig == null || StrUtil.isBlank(wxpusherConfig.getAppToken())) {
                return false;
            }
            
            if (StrUtil.isBlank(wxpusherConfig.getUid()) && StrUtil.isBlank(wxpusherConfig.getTopicId())) {
                return false;
            }
            
            // 发送测试消息
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("appToken", wxpusherConfig.getAppToken());
            requestBody.put("content", "这是一条测试消息，您的WxPusher配置正常！");
            requestBody.put("summary", "配置测试");
            requestBody.put("contentType", 1);
            
            if (StrUtil.isNotBlank(wxpusherConfig.getUid())) {
                List<String> uids = new ArrayList<>();
                uids.add(wxpusherConfig.getUid());
                requestBody.put("uids", uids);
            }
            
            if (StrUtil.isNotBlank(wxpusherConfig.getTopicId())) {
                List<String> topicIds = new ArrayList<>();
                topicIds.add(wxpusherConfig.getTopicId());
                requestBody.put("topicIds", topicIds);
            }
            
            String jsonBody = JSON.toJSONString(requestBody);
            RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                .url(WXPUSHER_API_URL)
                .post(body)
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    Map<String, Object> responseMap = JSON.parseObject(responseBody, Map.class);
                    Integer code = (Integer) responseMap.get("code");
                    
                    boolean success = code != null && code == 1000;
                    log.info("WxPusher配置测试{}: appToken={}", success ? "成功" : "失败", 
                        maskAppToken(wxpusherConfig.getAppToken()));
                    return success;
                }
                return false;
            }
            
        } catch (Exception e) {
            log.error("WxPusher配置测试异常: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public String getPlatformCode() {
        return "wxpusher";
    }
    
    /**
     * 构建消息内容
     */
    private String buildContent(MessageRecord messageRecord, UserPushConfigDto.WxPusherConfig config) {
        StringBuilder content = new StringBuilder();
        
        // 根据内容类型构建不同格式的内容
        Integer contentType = config.getContentType() != null ? config.getContentType() : 1;
        
        if (contentType == 3) { // Markdown格式
            content.append("## ").append(messageRecord.getTitle()).append("\n\n");
            content.append(messageRecord.getContent()).append("\n\n");
            
            if (StrUtil.isNotBlank(messageRecord.getUrl())) {
                content.append("[点击查看详情](").append(messageRecord.getUrl()).append(")\n\n");
            }
            
            content.append("---\n");
            content.append("**消息信息**\n");
            content.append("- 类型：").append(getMessageTypeText(messageRecord.getMessageType())).append("\n");
            content.append("- 级别：").append(getLevelText(messageRecord.getLevel())).append("\n");
            
            if (StrUtil.isNotBlank(messageRecord.getSource())) {
                content.append("- 来源：").append(messageRecord.getSource()).append("\n");
            }
            
            content.append("- 时间：").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
        } else if (contentType == 2) { // HTML格式
            content.append("<h3>").append(messageRecord.getTitle()).append("</h3>");
            content.append("<p>").append(messageRecord.getContent().replace("\n", "<br>")).append("</p>");
            
            if (StrUtil.isNotBlank(messageRecord.getUrl())) {
                content.append("<p><a href=\"").append(messageRecord.getUrl()).append("\">点击查看详情</a></p>");
            }
            
            content.append("<hr>");
            content.append("<p><strong>消息信息</strong><br>");
            content.append("类型：").append(getMessageTypeText(messageRecord.getMessageType())).append("<br>");
            content.append("级别：").append(getLevelText(messageRecord.getLevel())).append("<br>");
            
            if (StrUtil.isNotBlank(messageRecord.getSource())) {
                content.append("来源：").append(messageRecord.getSource()).append("<br>");
            }
            
            content.append("时间：").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            content.append("</p>");
            
        } else { // 纯文本格式
            content.append(messageRecord.getTitle()).append("\n\n");
            content.append(messageRecord.getContent()).append("\n\n");
            
            if (StrUtil.isNotBlank(messageRecord.getUrl())) {
                content.append("详情链接：").append(messageRecord.getUrl()).append("\n");
            }
            
            content.append("类型：").append(getMessageTypeText(messageRecord.getMessageType())).append("\n");
            content.append("级别：").append(getLevelText(messageRecord.getLevel())).append("\n");
            
            if (StrUtil.isNotBlank(messageRecord.getSource())) {
                content.append("来源：").append(messageRecord.getSource()).append("\n");
            }
            
            content.append("时间：").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        
        return content.toString();
    }
    
    /**
     * 构建消息摘要
     */
    private String buildSummary(MessageRecord messageRecord, UserPushConfigDto.WxPusherConfig config) {
        String summary = messageRecord.getTitle();
        
        // 如果配置了摘要长度，则截取
        Integer summaryLength = config.getSummaryLength();
        if (summaryLength != null && summaryLength > 0 && summary.length() > summaryLength) {
            summary = summary.substring(0, summaryLength) + "...";
        }
        
        return summary;
    }
    
    /**
     * 获取消息类型文本
     */
    private String getMessageTypeText(String messageType) {
        return switch (messageType) {
            case "notification" -> "通知";
            case "alert" -> "告警";
            case "system" -> "系统";
            case "custom" -> "自定义";
            default -> "未知";
        };
    }
    
    /**
     * 获取级别文本
     */
    private String getLevelText(String level) {
        return switch (level) {
            case "low" -> "低";
            case "normal" -> "普通";
            case "high" -> "高";
            case "critical" -> "紧急";
            default -> "普通";
        };
    }
    
    /**
     * 掩码应用Token，用于日志输出
     */
    private String maskAppToken(String appToken) {
        if (StrUtil.isBlank(appToken) || appToken.length() <= 8) {
            return "****";
        }
        return appToken.substring(0, 4) + "****" + appToken.substring(appToken.length() - 4);
    }
}