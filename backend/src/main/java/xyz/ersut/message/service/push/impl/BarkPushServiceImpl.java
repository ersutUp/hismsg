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
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Bark推送服务实现
 * 
 * @author ersut
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BarkPushServiceImpl implements PushService {
    
    private final MessageRecordService messageRecordService;
    private final UserPushConfigService userPushConfigService;
    
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build();
    
    @Override
    public PushRecord pushMessage(MessageRecord messageRecord, UserPushConfig config) {
        PushRecord pushRecord = new PushRecord();
        pushRecord.setId(messageRecordService.generatePushRecordId());
        pushRecord.setMessageId(messageRecord.getId());
        pushRecord.setUserId(messageRecord.getUserId());
        pushRecord.setPlatform("bark");
        pushRecord.setConfigName(config.getConfigName());
        pushRecord.setPushStatus(2); // 推送中
        pushRecord.setPushTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        pushRecord.setCreateTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        
        try {
            // 解析Bark配置
            UserPushConfigDto configDto = userPushConfigService.convertConfigDataToDto("bark", config.getConfigData());
            UserPushConfigDto.BarkConfig barkConfig = configDto.getBark();
            
            if (barkConfig == null || StrUtil.isBlank(barkConfig.getDeviceKey())) {
                throw new RuntimeException("Bark设备Key未配置");
            }
            
            // 构建推送URL
            String serverUrl = StrUtil.isNotBlank(barkConfig.getServerUrl()) ? 
                barkConfig.getServerUrl() : "https://api.day.app";
            String pushUrl = String.format("%s/%s", serverUrl, barkConfig.getDeviceKey());
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("title", messageRecord.getTitle());

            if (StrUtil.isNotBlank(messageRecord.getSubtitle())) {
                requestBody.put("subtitle", messageRecord.getSubtitle());
            }

            StringBuilder contentBuilder = new StringBuilder();
            contentBuilder.append(messageRecord.getContent());
            if (StrUtil.isNotBlank(messageRecord.getSource())) {
                contentBuilder.append("\n数据来源：").append(messageRecord.getSource());
            }
            contentBuilder.append("\nhismsg转发时间：").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            requestBody.put("body", contentBuilder.toString());
            
            if (StrUtil.isNotBlank(messageRecord.getUrl())) {
                requestBody.put("url", messageRecord.getUrl());
            }
            if (StrUtil.isNotBlank(barkConfig.getSound())) {
                requestBody.put("sound", barkConfig.getSound());
            }
            if (StrUtil.isNotBlank(barkConfig.getGroup())) {
                requestBody.put("group", barkConfig.getGroup());
            }
            if (StrUtil.isNotBlank(barkConfig.getIcon())) {
                requestBody.put("icon", barkConfig.getIcon());
            }
            
            // 设置消息级别对应的优先级
            if ("high".equals(messageRecord.getLevel()) || "critical".equals(messageRecord.getLevel())) {
                requestBody.put("level", "timeSensitive");
            } else if ("low".equals(messageRecord.getLevel())) {
                requestBody.put("level", "passive");
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
                    log.info("Bark推送成功: messageId={}, deviceKey={}", messageRecord.getId(), 
                        maskDeviceKey(barkConfig.getDeviceKey()));
                } else {
                    pushRecord.setPushStatus(0); // 推送失败
                    pushRecord.setErrorMessage("HTTP错误: " + response.code());
                    log.warn("Bark推送失败: messageId={}, httpCode={}, response={}", 
                        messageRecord.getId(), response.code(), responseBody);
                }
            }
            
        } catch (Exception e) {
            pushRecord.setPushStatus(0); // 推送失败
            pushRecord.setErrorMessage(e.getMessage());
            log.error("Bark推送异常: messageId={}, error={}", messageRecord.getId(), e.getMessage(), e);
        }
        
        return pushRecord;
    }
    
    @Override
    public boolean testConfig(UserPushConfig config) {
        try {
            // 解析配置
            UserPushConfigDto configDto = userPushConfigService.convertConfigDataToDto("bark", config.getConfigData());
            UserPushConfigDto.BarkConfig barkConfig = configDto.getBark();
            
            if (barkConfig == null || StrUtil.isBlank(barkConfig.getDeviceKey())) {
                return false;
            }
            
            // 发送测试消息
            String serverUrl = StrUtil.isNotBlank(barkConfig.getServerUrl()) ? 
                barkConfig.getServerUrl() : "https://api.day.app";
            String pushUrl = String.format("%s/%s", serverUrl, barkConfig.getDeviceKey());
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("title", "配置测试");
            requestBody.put("body", "这是一条测试消息，您的Bark配置正常！");
            requestBody.put("group", "配置测试");
            
            String jsonBody = JSON.toJSONString(requestBody);
            RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                .url(pushUrl)
                .post(body)
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                boolean success = response.isSuccessful();
                log.info("Bark配置测试{}: deviceKey={}", success ? "成功" : "失败", 
                    maskDeviceKey(barkConfig.getDeviceKey()));
                return success;
            }
            
        } catch (Exception e) {
            log.error("Bark配置测试异常: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public String getPlatformCode() {
        return "bark";
    }
    
    /**
     * 掩码设备Key，用于日志输出
     * 
     * @param deviceKey 原始设备Key
     * @return 掩码后的设备Key
     */
    private String maskDeviceKey(String deviceKey) {
        if (StrUtil.isBlank(deviceKey) || deviceKey.length() <= 8) {
            return "****";
        }
        return deviceKey.substring(0, 4) + "****" + deviceKey.substring(deviceKey.length() - 4);
    }
}