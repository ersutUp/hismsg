package xyz.ersut.message.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import xyz.ersut.message.entity.MessageRecord;
import xyz.ersut.message.entity.PushRecord;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息存储服务接口
 * 
 * @author ersut
 */
public interface MessageRecordService {
    
    /**
     * 保存消息记录
     * 
     * @param messageRecord 消息记录
     * @return 保存结果
     */
    boolean saveMessageRecord(MessageRecord messageRecord);
    
    /**
     * 根据ID查询消息记录
     * 
     * @param id 消息ID
     * @return 消息记录
     */
    MessageRecord getMessageById(Long id);
    
    /**
     * 根据用户ID分页查询消息记录
     * 
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 消息记录列表
     */
    List<MessageRecord> getMessagesByUserId(Long userId, int offset, int limit);
    
    /**
     * 根据条件查询消息记录
     * 
     * @param userId 用户ID
     * @param messageType 消息类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param tags 消息标签列表
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 消息记录列表
     */
    Page<MessageRecord> getMessagesByCondition(Long userId, String messageType,
                                               LocalDateTime startTime, LocalDateTime endTime,
                                               List<String> tags, int offset, int limit);
    
    /**
     * 统计用户消息数量
     * 
     * @param userId 用户ID
     * @param messageType 消息类型（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 消息数量
     */
    long countMessagesByCondition(Long userId, String messageType, 
                                LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 更新消息推送状态
     * 
     * @param messageId 消息ID
     * @param platform 推送平台
     * @param success 是否成功
     * @return 更新结果
     */
    boolean updateMessagePushStatus(Long messageId, String platform, boolean success);
    
    /**
     * 更新消息记录
     * 
     * @param messageRecord 消息记录
     * @return 更新结果
     */
    boolean updateMessageRecord(MessageRecord messageRecord);
    
    /**
     * 保存推送记录
     * 
     * @param pushRecord 推送记录
     * @return 保存结果
     */
    boolean savePushRecord(PushRecord pushRecord);
    
    /**
     * 根据消息ID查询推送记录
     * 
     * @param messageId 消息ID
     * @return 推送记录列表
     */
    List<PushRecord> getPushRecordsByMessageId(Long messageId);
    
    /**
     * 生成消息ID
     * 
     * @return 唯一消息ID
     */
    Long generateMessageId();
    
    /**
     * 生成推送记录ID
     * 
     * @return 唯一推送记录ID
     */
    Long generatePushRecordId();
}