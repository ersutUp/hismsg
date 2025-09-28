package xyz.ersut.message.service;

import xyz.ersut.message.dto.MessagePushRequest;
import xyz.ersut.message.entity.MessageRecord;

/**
 * 消息转发服务接口
 * 
 * @author ersut
 */
public interface MessageForwardService {
    
    /**
     * 推送消息
     * 处理流程：
     * 1. 验证用户和消息数据
     * 2. 保存消息记录到ClickHouse
     * 3. 发送到Redis队列进行异步推送
     * 
     * @param pushRequest 推送请求
     * @return 消息ID
     */
    Long pushMessage(MessagePushRequest pushRequest);
    
    /**
     * 处理消息推送（异步）
     * 从Redis队列中获取消息并推送到各个平台
     * 
     * @param messageRecord 消息记录
     */
    void processMessagePush(MessageRecord messageRecord);
    
    /**
     * 重试失败的消息推送
     * 
     * @param messageId 消息ID
     * @param platform 推送平台
     * @return 重试结果
     */
    boolean retryMessagePush(Long messageId, String platform);
}