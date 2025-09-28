package xyz.ersut.message.listener;

import org.springframework.data.redis.connection.MessageListener;

/**
 * 消息队列处理器接口
 * 
 * @author ersut
 */
public interface MessageQueueProcessor extends MessageListener {
    
    /**
     * 从队列中拉取消息进行处理
     */
    void processQueueMessages();
}