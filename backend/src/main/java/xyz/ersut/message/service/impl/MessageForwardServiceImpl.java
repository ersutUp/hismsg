package xyz.ersut.message.service.impl;

import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.baomidou.dynamic.datasource.annotation.DS;
import cn.hutool.core.util.StrUtil;
import xyz.ersut.message.constant.CacheConstants;
import xyz.ersut.message.dto.MessagePushRequest;
import xyz.ersut.message.entity.MessageRecord;
import xyz.ersut.message.entity.PushRecord;
import xyz.ersut.message.entity.SysUser;
import xyz.ersut.message.entity.UserPushConfig;
import xyz.ersut.message.entity.TagPushConfig;
import xyz.ersut.message.service.*;
import xyz.ersut.message.service.push.PushServiceManager;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 消息转发服务实现类
 * 
 * @author ersut
 */
@Slf4j
@Service
@DS("mysql")
@RequiredArgsConstructor
public class MessageForwardServiceImpl implements MessageForwardService {
    
    private final MessageRecordService messageRecordService;
    private final UserPushConfigService userPushConfigService;
    private final SysUserService userService;
    private final TagPushConfigService tagPushConfigService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PushServiceManager pushServiceManager;
    
    // Redis队列键名
    private static final String MESSAGE_RETRY_QUEUE = "message:retry:queue";
    
    @Override
    public Long pushMessage(MessagePushRequest pushRequest) {
        // 参数验证
        if (pushRequest == null) {
            throw new RuntimeException("推送请求不能为空");
        }

        if (StrUtil.isBlank(pushRequest.getTitle()) && StrUtil.isBlank(pushRequest.getContent())) {
            throw new RuntimeException("消息标题和内容不能同时为空");
        }

        if (StrUtil.isBlank(pushRequest.getUserKey())) {
            throw new RuntimeException("用户标识不能为空");
        }

        // 根据用户编号、用户ID或用户密钥获取用户信息
        SysUser user = userService.getUserByUserKey(pushRequest.getUserKey());

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 构建消息记录
        MessageRecord messageRecord = buildMessageRecord(pushRequest, user);
        
        // 保存消息记录到ClickHouse
        boolean saved = messageRecordService.saveMessageRecord(messageRecord);
        if (!saved) {
            throw new RuntimeException("保存消息记录失败");
        }
        
        // 发送到Redis队列进行异步推送
        try {
            redisTemplate.convertAndSend(CacheConstants.MESSAGE_PUSH_QUEUE, messageRecord);
            log.info("消息已发送到推送队列: messageId={}", messageRecord.getId());
        } catch (Exception e) {
            log.error("发送消息到推送队列失败: {}", e.getMessage(), e);
            throw new RuntimeException("发送消息到推送队列失败");
        }
        
        return messageRecord.getId();
    }
    
    @Override
    public void processMessagePush(MessageRecord messageRecord) {
        if (messageRecord == null || messageRecord.getUserId() == null) {
            log.warn("消息记录为空或用户ID为空，跳过推送");
            return;
        }
        
        try {
            // 获取用户启用的推送配置
            List<UserPushConfig> configs = userPushConfigService.getEnabledByUserId(messageRecord.getUserId());
            if (configs == null || configs.isEmpty()) {
                log.warn("用户{}没有启用的推送配置，跳过推送", messageRecord.getUserId());
                return;
            }
            
            // 基于标签过滤推送配置
            configs = filterConfigsByTags(messageRecord, configs);
            
            // 如果指定了推送平台，则只推送指定的平台
            if (messageRecord.getPushedPlatforms() != null && !messageRecord.getPushedPlatforms().isEmpty()) {
                configs = configs.stream()
                    .filter(config -> messageRecord.getPushedPlatforms().contains(config.getPlatform()))
                    .toList();
            }
            
            log.info("开始推送消息: messageId={}, 推送配置数量={}", messageRecord.getId(), configs.size());
            
            // 遍历推送配置，逐个推送
            for (UserPushConfig config : configs) {
                try {
                    // 调用推送服务管理器进行推送
                    PushRecord pushRecord = pushServiceManager.pushToSpecificPlatform(messageRecord, config);

                    // 更新推送成功计数和平台列表
                    if (pushRecord.getPushStatus() == 1) {
                        log.info("推送到平台{}成功: messageId={}, configName={}, tags={}",
                                config.getPlatform(), messageRecord.getId(), config.getConfigName(), messageRecord.getTags());
                        updatePushSuccess(messageRecord, config.getPlatform());
                    } else {
                        log.warn("推送到平台{}失败: messageId={}, configName={}",
                                config.getPlatform(), messageRecord.getId(), config.getConfigName());
                    }
                    
                } catch (Exception e) {
                    log.error("推送到平台{}失败: messageId={}, configName={}, error={}", 
                        config.getPlatform(), messageRecord.getId(), config.getConfigName(), e.getMessage());
                    
                    // 更新推送失败计数
                    updatePushFailure(messageRecord);
                    
                    // 推送失败，可以考虑加入重试队列
                    addToRetryQueue(messageRecord, config);
                }
            }
            
        } catch (Exception e) {
            log.error("处理消息推送失败: messageId={}, error={}", messageRecord.getId(), e.getMessage(), e);
        }
    }
    
    @Override
    public boolean retryMessagePush(Long messageId, String platform) {
        if (messageId == null || StrUtil.isBlank(platform)) {
            return false;
        }
        
        try {
            // 获取消息记录
            MessageRecord messageRecord = messageRecordService.getMessageById(messageId);
            if (messageRecord == null) {
                log.warn("消息记录不存在: messageId={}", messageId);
                return false;
            }
            
            // 获取指定平台的推送配置
            List<UserPushConfig> configs = userPushConfigService.getByUserIdAndPlatform(
                messageRecord.getUserId(), platform);
            
            if (configs == null || configs.isEmpty()) {
                log.warn("用户{}没有{}平台的推送配置", messageRecord.getUserId(), platform);
                return false;
            }
            
            // 重试推送
            for (UserPushConfig config : configs) {
                if (config.getIsEnabled() == 1) {
                    // 调用推送服务管理器进行重试推送
                    pushServiceManager.pushToSpecificPlatform(messageRecord, config);
                    log.info("重试推送成功: messageId={}, platform={}, configName={}", 
                        messageId, platform, config.getConfigName());
                }
            }
            
            return true;
        } catch (Exception e) {
            log.error("重试消息推送失败: messageId={}, platform={}, error={}", messageId, platform, e.getMessage());
            return false;
        }
    }
    
    /**
     * 构建消息记录对象
     * 
     * @param pushRequest 推送请求
     * @param user 用户信息
     * @return 消息记录
     */
    private MessageRecord buildMessageRecord(MessagePushRequest pushRequest, SysUser user) {
        MessageRecord record = new MessageRecord();
        record.setId(messageRecordService.generateMessageId());
        record.setUserId(user.getId());
        record.setUserCode(pushRequest.getUserCode() != null ? pushRequest.getUserCode() : user.getUsername());
        record.setMessageType(pushRequest.getMessageType() != null ? pushRequest.getMessageType() : "notification");
        record.setTitle(pushRequest.getTitle());
        record.setSubtitle(pushRequest.getSubtitle());
        record.setContent(Optional.ofNullable(pushRequest.getContent()).orElse(""));
        record.setGroup(pushRequest.getGroup());
        record.setUrl(Optional.ofNullable(pushRequest.getUrl()).orElse(""));
        record.setSource(pushRequest.getSource() != null ? pushRequest.getSource() : "");
        record.setLevel(pushRequest.getLevel() != null ? pushRequest.getLevel() : "normal");
        record.setTags(pushRequest.getTags() != null ? pushRequest.getTags() : new ArrayList<>());
        record.setExtraData(pushRequest.getExtraData() != null ? JSON.toJSONString(pushRequest.getExtraData()) : "{}");
        record.setStatus(1);
        record.setPushedPlatforms(pushRequest.getPlatforms() != null ? pushRequest.getPlatforms() : new ArrayList<>());
        record.setPushSuccessCount(0);
        record.setPushFailCount(0);
        record.setCreateTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        record.setUpdateTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        
        return record;
    }
    
    /**
     * 更新推送成功状态
     * 
     * @param messageRecord 消息记录
     * @param platform 推送平台
     */
    private void updatePushSuccess(MessageRecord messageRecord, String platform) {
        try {
            // 增加成功计数
            messageRecord.setPushSuccessCount(messageRecord.getPushSuccessCount() + 1);
            
            // 添加到已推送平台列表
            List<String> pushedPlatforms = messageRecord.getPushedPlatforms();
            if (pushedPlatforms == null) {
                pushedPlatforms = new ArrayList<>();
                messageRecord.setPushedPlatforms(pushedPlatforms);
            }
            if (!pushedPlatforms.contains(platform)) {
                pushedPlatforms.add(platform);
            }
            
            // 更新时间
            messageRecord.setUpdateTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
            
            // 更新到数据库
            messageRecordService.updateMessageRecord(messageRecord);
            
        } catch (Exception e) {
            log.error("更新推送成功状态失败: messageId={}, platform={}, error={}", 
                messageRecord.getId(), platform, e.getMessage());
        }
    }
    
    /**
     * 更新推送失败状态
     * 
     * @param messageRecord 消息记录
     */
    private void updatePushFailure(MessageRecord messageRecord) {
        try {
            // 增加失败计数
            messageRecord.setPushFailCount(messageRecord.getPushFailCount() + 1);
            
            // 更新时间
            messageRecord.setUpdateTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
            
            // 更新到数据库
            messageRecordService.updateMessageRecord(messageRecord);
            
        } catch (Exception e) {
            log.error("更新推送失败状态失败: messageId={}, error={}", 
                messageRecord.getId(), e.getMessage());
        }
    }
    
    /**
     * 将失败的推送任务加入重试队列
     * 
     * @param messageRecord 消息记录
     * @param config 推送配置
     */
    private void addToRetryQueue(MessageRecord messageRecord, UserPushConfig config) {
        try {
            // 构建重试任务数据
            var retryTask = new Object() {
                public final Long messageId = messageRecord.getId();
                public final String platform = config.getPlatform();
                public final String configName = config.getConfigName();
                public final LocalDateTime retryTime = LocalDateTime.now().plusMinutes(5); // 5分钟后重试
            };
            
            redisTemplate.opsForList().leftPush(MESSAGE_RETRY_QUEUE, JSON.toJSONString(retryTask));
            log.info("推送失败任务已加入重试队列: messageId={}, platform={}", 
                messageRecord.getId(), config.getPlatform());
        } catch (Exception e) {
            log.error("加入重试队列失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 基于标签过滤推送配置
     * 
     * @param messageRecord 消息记录
     * @param configs 原始推送配置列表
     * @return 过滤后的推送配置列表
     */
    private List<UserPushConfig> filterConfigsByTags(MessageRecord messageRecord, List<UserPushConfig> configs) {
        // 如果消息没有标签，返回所有配置
        if (messageRecord.getTags() == null || messageRecord.getTags().isEmpty()) {
            return configs;
        }
        
        // 获取用户的标签推送配置
        List<TagPushConfig> tagConfigs = tagPushConfigService.getEnabledByUserId(messageRecord.getUserId());
        if (tagConfigs == null || tagConfigs.isEmpty()) {
            // 没有标签配置，返回所有推送配置
            return configs;
        }
        
        // 检查消息标签是否匹配任何标签配置，收集允许的推送配置ID
        Set<Long> allowedConfigIds = new HashSet<>();
        for (String tag : messageRecord.getTags()) {
            for (TagPushConfig tagConfig : tagConfigs) {
                if (tag.equals(tagConfig.getTagName()) && tagConfig.getPushConfigIds() != null) {
                    allowedConfigIds.addAll(tagConfig.getPushConfigIds());
                }
            }
        }
        
        // 如果没有匹配的标签配置，使用所有推送配置
        if (allowedConfigIds.isEmpty()) {
            return configs;
        }
        
        // 过滤推送配置，只保留标签配置中指定的配置ID
        return configs.stream()
            .filter(config -> allowedConfigIds.contains(config.getId()))
            .toList();
    }
}