package xyz.ersut.message.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import xyz.ersut.message.entity.MessageRecord;
import xyz.ersut.message.service.MessageForwardService;

/**
 * Redis消息队列监听器
 * 
 * @author ersut
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessagePushQueueListener implements MessageListener {

    @Autowired
    @Lazy
    private MessageForwardService messageForwardService;
    
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            if (message == null || message.getBody() == null) {
                log.warn("收到空消息，忽略");
                return;
            }
            String messageBody = new String(message.getBody());
            log.debug("收到推送队列消息: {}", messageBody);

            // 解析消息
            MessageRecord messageRecord = objectMapper.readValue(messageBody, MessageRecord.class);
            
            // 异步处理消息推送
            processMessageAsync(messageRecord);
            
        } catch (Exception e) {
            log.error("处理推送队列消息失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 异步处理消息推送
     * 
     * @param messageRecord 消息记录
     */
    @Async
    public void processMessageAsync(MessageRecord messageRecord) {
        messageForwardService.processMessagePush(messageRecord);
    }
}




