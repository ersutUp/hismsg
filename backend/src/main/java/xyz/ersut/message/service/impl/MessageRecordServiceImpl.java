package xyz.ersut.message.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import cn.hutool.core.util.IdUtil;
import xyz.ersut.message.entity.MessageRecord;
import xyz.ersut.message.entity.PushRecord;
import xyz.ersut.message.mapper.MessageRecordMapper;
import xyz.ersut.message.mapper.PushRecordMapper;
import xyz.ersut.message.service.MessageRecordService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 消息存储服务实现类
 * 
 * @author ersut
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DS("clickhouse")
public class MessageRecordServiceImpl implements MessageRecordService {
    
    private final MessageRecordMapper messageRecordMapper;
    private final PushRecordMapper pushRecordMapper;
    
    @Override
    public boolean saveMessageRecord(MessageRecord messageRecord) {
        if (messageRecord == null) {
            return false;
        }
        
        try {
            // 截断时间到秒级精度，避免 ClickHouse DateTime 类型不支持微秒的问题
            if (messageRecord.getCreateTime() != null) {
                messageRecord.setCreateTime(messageRecord.getCreateTime().truncatedTo(ChronoUnit.SECONDS));
            }
            if (messageRecord.getUpdateTime() != null) {
                messageRecord.setUpdateTime(messageRecord.getUpdateTime().truncatedTo(ChronoUnit.SECONDS));
            }
            
            int result = messageRecordMapper.insert(messageRecord);
            log.debug("保存消息记录，ID: {}, 结果: {}", messageRecord.getId(), result > 0);
            return result > 0;
        } catch (Exception e) {
            log.error("保存消息记录失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public MessageRecord getMessageById(Long id) {
        if (id == null) {
            return null;
        }
        
        try {
            return messageRecordMapper.selectById(id);
        } catch (Exception e) {
            log.error("根据ID查询消息记录失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public List<MessageRecord> getMessagesByUserId(Long userId, int offset, int limit) {
        if (userId == null) {
            return new ArrayList<>();
        }
        
        try {
            QueryWrapper<MessageRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                       .orderByDesc("create_time");
            
            Page<MessageRecord> page = new Page<>((offset / limit) + 1, limit);
            Page<MessageRecord> result = messageRecordMapper.selectPage(page, queryWrapper);
            return result.getRecords();
        } catch (Exception e) {
            log.error("根据用户ID查询消息记录失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<MessageRecord> getMessagesByCondition(Long userId, String messageType, 
                                                     LocalDateTime startTime, LocalDateTime endTime, 
                                                     int offset, int limit) {
        try {
            QueryWrapper<MessageRecord> queryWrapper = new QueryWrapper<>();
            
            if (userId != null) {
                queryWrapper.eq("user_id", userId);
            }
            if (messageType != null && !messageType.trim().isEmpty()) {
                queryWrapper.eq("message_type", messageType);
            }
            if (startTime != null) {
                queryWrapper.ge("create_time", startTime.truncatedTo(ChronoUnit.SECONDS));
            }
            if (endTime != null) {
                queryWrapper.le("create_time", endTime.truncatedTo(ChronoUnit.SECONDS));
            }
            
            queryWrapper.orderByDesc("create_time");
            
            Page<MessageRecord> page = new Page<>((offset / limit) + 1, limit);
            Page<MessageRecord> result = messageRecordMapper.selectPage(page, queryWrapper);
            return result.getRecords();
        } catch (Exception e) {
            log.error("根据条件查询消息记录失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public long countMessagesByCondition(Long userId, String messageType, 
                                        LocalDateTime startTime, LocalDateTime endTime) {
        try {
            QueryWrapper<MessageRecord> queryWrapper = new QueryWrapper<>();
            
            if (userId != null) {
                queryWrapper.eq("user_id", userId);
            }
            if (messageType != null && !messageType.trim().isEmpty()) {
                queryWrapper.eq("message_type", messageType);
            }
            if (startTime != null) {
                queryWrapper.ge("create_time", startTime.truncatedTo(ChronoUnit.SECONDS));
            }
            if (endTime != null) {
                queryWrapper.le("create_time", endTime.truncatedTo(ChronoUnit.SECONDS));
            }
            
            return messageRecordMapper.selectCount(queryWrapper);
        } catch (Exception e) {
            log.error("统计消息数量失败: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    @Override
    public boolean updateMessagePushStatus(Long messageId, String platform, boolean success) {
        if (messageId == null) {
            return false;
        }
        
        try {
            int successIncrement = success ? 1 : 0;
            int failIncrement = success ? 0 : 1;
            String[] platformArray = (platform != null && !platform.trim().isEmpty()) ? 
                                   new String[]{platform} : new String[0];
            
            int result = messageRecordMapper.updatePushStatus(messageId, successIncrement, failIncrement, platformArray);
            log.debug("更新消息推送状态，消息ID: {}, 平台: {}, 成功: {}, 结果: {}", messageId, platform, success, result > 0);
            return result > 0;
        } catch (Exception e) {
            log.error("更新消息推送状态失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean updateMessageRecord(MessageRecord messageRecord) {
        if (messageRecord == null || messageRecord.getId() == null) {
            return false;
        }
        
        try {
            // 截断时间到秒级精度
            if (messageRecord.getUpdateTime() != null) {
                messageRecord.setUpdateTime(messageRecord.getUpdateTime().truncatedTo(ChronoUnit.SECONDS));
            }
            
            // 只更新推送统计相关字段
            int result = messageRecordMapper.updatePushStats(
                messageRecord.getId(),
                messageRecord.getPushedPlatforms(),
                messageRecord.getPushSuccessCount(),
                messageRecord.getPushFailCount(),
                messageRecord.getUpdateTime()
            );
            
            log.debug("更新消息推送统计，ID: {}, 结果: {}", messageRecord.getId(), result > 0);
            return result > 0;
        } catch (Exception e) {
            log.error("更新消息推送统计失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean savePushRecord(PushRecord pushRecord) {
        if (pushRecord == null) {
            return false;
        }
        
        try {
            if (pushRecord.getId() == null) {
                pushRecord.setId(generatePushRecordId());
            }
            if (pushRecord.getCreateTime() == null) {
                pushRecord.setCreateTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
            } else {
                pushRecord.setCreateTime(pushRecord.getCreateTime().truncatedTo(ChronoUnit.SECONDS));
            }
            if (pushRecord.getPushTime() == null) {
                pushRecord.setPushTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
            } else {
                pushRecord.setPushTime(pushRecord.getPushTime().truncatedTo(ChronoUnit.SECONDS));
            }
            
            if (pushRecord.getRetryCount() == null) {
                pushRecord.setRetryCount(0);
            }
            
            int result = pushRecordMapper.insert(pushRecord);
            log.debug("保存推送记录，ID: {}, 结果: {}", pushRecord.getId(), result > 0);
            return result > 0;
        } catch (Exception e) {
            log.error("保存推送记录失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public List<PushRecord> getPushRecordsByMessageId(Long messageId) {
        if (messageId == null) {
            return new ArrayList<>();
        }
        
        try {
            QueryWrapper<PushRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("message_id", messageId)
                       .orderByDesc("create_time");
            
            return pushRecordMapper.selectList(queryWrapper);
        } catch (Exception e) {
            log.error("根据消息ID查询推送记录失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public Long generateMessageId() {
        return IdUtil.getSnowflakeNextId();
    }
    
    @Override
    public Long generatePushRecordId() {
        return IdUtil.getSnowflakeNextId();
    }
}