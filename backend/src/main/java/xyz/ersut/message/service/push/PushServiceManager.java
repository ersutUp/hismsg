package xyz.ersut.message.service.push;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.ersut.message.entity.MessageRecord;
import xyz.ersut.message.entity.PushRecord;
import xyz.ersut.message.entity.UserPushConfig;
import xyz.ersut.message.service.MessageRecordService;
import xyz.ersut.message.service.push.impl.BarkPushServiceImpl;
import xyz.ersut.message.service.push.impl.EmailPushServiceImpl;
import xyz.ersut.message.service.push.impl.PushMePushServiceImpl;
import xyz.ersut.message.service.push.impl.WxPusherPushServiceImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * 推送服务管理器
 * 统一管理各个平台的推送服务
 * 
 * @author ersut
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PushServiceManager {
    
    private final BarkPushServiceImpl barkPushService;
    private final EmailPushServiceImpl emailPushService;
    private final WxPusherPushServiceImpl wxpusherPushService;
    private final PushMePushServiceImpl pushmePushService;
    private final MessageRecordService messageRecordService;
    
    private Map<String, PushService> pushServices;
    
    /**
     * 初始化推送服务映射
     */
    private void initPushServices() {
        if (pushServices == null) {
            pushServices = new HashMap<>();
            pushServices.put("bark", barkPushService);
            pushServices.put("email", emailPushService);
            pushServices.put("wxpusher", wxpusherPushService);
            pushServices.put("pushme", pushmePushService);
        }
    }
    
    /**
     * 根据平台代码获取推送服务
     * 
     * @param platformCode 平台代码
     * @return 推送服务
     */
    public PushService getPushService(String platformCode) {
        initPushServices();
        return pushServices.get(platformCode);
    }
    
    /**
     * 推送消息到指定平台
     * 
     * @param messageRecord 消息记录
     * @param config 推送配置
     * @return 推送记录
     */
    public PushRecord pushToSpecificPlatform(MessageRecord messageRecord, UserPushConfig config) {
        PushService pushService = getPushService(config.getPlatform());
        if (pushService == null) {
            log.warn("不支持的推送平台: {}", config.getPlatform());
            
            // 创建失败的推送记录
            PushRecord pushRecord = new PushRecord();
            pushRecord.setId(messageRecordService.generatePushRecordId());
            pushRecord.setMessageId(messageRecord.getId());
            pushRecord.setUserId(messageRecord.getUserId());
            pushRecord.setPlatform(config.getPlatform());
            pushRecord.setConfigName(config.getConfigName());
            pushRecord.setPushStatus(0); // 推送失败
            pushRecord.setErrorMessage("不支持的推送平台: " + config.getPlatform());
            pushRecord.setPushTime(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
            pushRecord.setCreateTime(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
            
            return pushRecord;
        }
        
        // 执行推送
        PushRecord pushRecord = pushService.pushMessage(messageRecord, config);
        
        // 保存推送记录到ClickHouse
        try {
            messageRecordService.savePushRecord(pushRecord);
        } catch (Exception e) {
            log.error("保存推送记录失败: messageId={}, platform={}, error={}", 
                messageRecord.getId(), config.getPlatform(), e.getMessage());
        }
        
        return pushRecord;
    }
    
    /**
     * 测试推送配置
     * 
     * @param config 推送配置
     * @return 测试结果
     */
    public boolean testPushConfig(UserPushConfig config) {
        PushService pushService = getPushService(config.getPlatform());
        if (pushService == null) {
            log.warn("不支持的推送平台: {}", config.getPlatform());
            return false;
        }
        
        return pushService.testConfig(config);
    }
    
    /**
     * 获取所有支持的平台代码
     * 
     * @return 支持的平台代码数组
     */
    public String[] getSupportedPlatforms() {
        initPushServices();
        return pushServices.keySet().toArray(new String[0]);
    }
}