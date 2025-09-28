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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * PushMe推送服务实现
 * 
 * @author ersut
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PushMePushServiceImpl implements PushService {
    
    private final MessageRecordService messageRecordService;
    private final UserPushConfigService userPushConfigService;
    
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build();
    
    private static final String PUSHME_API_URL = "https://push.i-i.me";
    
    @Override
    public PushRecord pushMessage(MessageRecord messageRecord, UserPushConfig config) {
        PushRecord pushRecord = new PushRecord();
        pushRecord.setId(messageRecordService.generatePushRecordId());
        pushRecord.setMessageId(messageRecord.getId());
        pushRecord.setUserId(messageRecord.getUserId());
        pushRecord.setPlatform("pushme");
        pushRecord.setConfigName(config.getConfigName());
        pushRecord.setPushStatus(2); // 推送中
        pushRecord.setPushTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        pushRecord.setCreateTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        
        try {
            // 解析PushMe配置
            UserPushConfigDto configDto = userPushConfigService.convertConfigDataToDto("pushme", config.getConfigData());
            UserPushConfigDto.PushMeConfig pushmeConfig = configDto.getPushme();
            
            if (pushmeConfig == null || StrUtil.isBlank(pushmeConfig.getPushKey())) {
                throw new RuntimeException("PushMe推送Key未配置");
            }
            
            // 构建推送URL
            String pushUrl = String.format("%s?push_key=%s", PUSHME_API_URL, pushmeConfig.getPushKey());
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("title", messageRecord.getTitle());
            requestBody.put("content", messageRecord.getContent());
            
            if (StrUtil.isNotBlank(messageRecord.getUrl())) {
                requestBody.put("url", messageRecord.getUrl());
            }
            
            if (StrUtil.isNotBlank(pushmeConfig.getTemplate())) {
                requestBody.put("template", pushmeConfig.getTemplate());
            }
            
            String jsonBody = JSON.toJSONString(requestBody);
            pushRecord.setRequestData(jsonBody);
            
            // 发送HTTP请求
            RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                .url(pushUrl)
                .post(body)
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";
                pushRecord.setResponseData(responseBody);
                
                if (response.isSuccessful()) {
                    pushRecord.setPushStatus(1); // 推送成功
                    log.info("PushMe推送成功: messageId={}, pushKey={}", messageRecord.getId(), 
                        maskPushKey(pushmeConfig.getPushKey()));
                } else {
                    pushRecord.setPushStatus(0); // 推送失败
                    pushRecord.setErrorMessage("HTTP错误: " + response.code());
                    log.warn("PushMe推送失败: messageId={}, httpCode={}, response={}", 
                        messageRecord.getId(), response.code(), responseBody);
                }
            }
            
        } catch (Exception e) {
            pushRecord.setPushStatus(0); // 推送失败
            pushRecord.setErrorMessage(e.getMessage());
            log.error("PushMe推送异常: messageId={}, error={}", messageRecord.getId(), e.getMessage(), e);
        }
        
        return pushRecord;
    }
    
    @Override
    public boolean testConfig(UserPushConfig config) {
        try {
            // 解析配置
            UserPushConfigDto configDto = userPushConfigService.convertConfigDataToDto("pushme", config.getConfigData());
            UserPushConfigDto.PushMeConfig pushmeConfig = configDto.getPushme();
            
            if (pushmeConfig == null || StrUtil.isBlank(pushmeConfig.getPushKey())) {
                return false;
            }
            
            // 发送测试消息
            String pushUrl = String.format("%s?push_key=%s", PUSHME_API_URL, pushmeConfig.getPushKey());
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("title", "配置测试");
            requestBody.put("content", "这是一条测试消息，您的PushMe配置正常！");
            
            String jsonBody = JSON.toJSONString(requestBody);
            RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                .url(pushUrl)
                .post(body)
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                boolean success = response.isSuccessful();
                log.info("PushMe配置测试{}: pushKey={}", success ? "成功" : "失败", 
                    maskPushKey(pushmeConfig.getPushKey()));
                return success;
            }
            
        } catch (Exception e) {
            log.error("PushMe配置测试异常: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public String getPlatformCode() {
        return "pushme";
    }
    
    /**
     * 掩码推送Key，用于日志输出
     */
    private String maskPushKey(String pushKey) {
        if (StrUtil.isBlank(pushKey) || pushKey.length() <= 8) {
            return "****";
        }
        return pushKey.substring(0, 4) + "****" + pushKey.substring(pushKey.length() - 4);
    }
}