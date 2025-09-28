package xyz.ersut.message.service.push;

import xyz.ersut.message.entity.MessageRecord;
import xyz.ersut.message.entity.PushRecord;
import xyz.ersut.message.entity.UserPushConfig;

/**
 * 推送服务接口
 * 
 * @author ersut
 */
public interface PushService {
    
    /**
     * 推送消息
     * 
     * @param messageRecord 消息记录
     * @param config 推送配置
     * @return 推送记录
     */
    PushRecord pushMessage(MessageRecord messageRecord, UserPushConfig config);
    
    /**
     * 测试推送配置
     * 
     * @param config 推送配置
     * @return 测试结果
     */
    boolean testConfig(UserPushConfig config);
    
    /**
     * 获取支持的平台代码
     * 
     * @return 平台代码
     */
    String getPlatformCode();
}