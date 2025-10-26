package xyz.ersut.message.service.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xyz.ersut.message.dto.MessagePushRequest;
import xyz.ersut.message.entity.ScheduledTask;
import xyz.ersut.message.entity.ScheduledTaskExecutionLog;
import xyz.ersut.message.service.MessageForwardService;
import xyz.ersut.message.service.ScheduledTaskService;
import xyz.ersut.message.service.ScheduledTaskExecutionLogService;
import xyz.ersut.message.service.SysUserService;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 任务执行器
 * 负责实际执行定时任务
 * 
 * @author ersut
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskScheduleExecutor {
    
    private static final String NODE_ID = UUID.randomUUID().toString();
    
    private final DistributedTaskLock distributedTaskLock;
    private final MessageForwardService messageForwardService;
    private final ScheduledTaskService scheduledTaskService;
    private final ScheduledTaskExecutionLogService scheduledTaskExecutionLogService;
    private final SysUserService userService;
    
    /**
     * 执行任务
     * 
     * @param task 定时任务
     * @param executeTime 执行时间
     */
    public void executeTask(ScheduledTask task, LocalDateTime executeTime) {
        if (task == null) {
            log.warn("任务为空，无法执行");
            return;
        }
        
        Long taskId = task.getId();
        String nodeId = NODE_ID;
        
        // 尝试获取分布式锁
        if (!distributedTaskLock.tryLock(taskId, executeTime)) {
            log.info("获取任务执行锁失败，任务可能已被其他节点执行: taskId={}, executeTime={}", 
                taskId, executeTime);
            
            // 记录跳过执行日志
            ScheduledTaskExecutionLog skipLog = ScheduledTaskExecutionLog.skipped(taskId, executeTime, 
                "获取分布式锁失败，任务已被其他节点执行", nodeId);
            scheduledTaskExecutionLogService.saveExecutionLog(skipLog);
            return;
        }
        
        long startTime = System.currentTimeMillis();
        ScheduledTaskExecutionLog executionLog = null;
        
        try {
            log.info("开始执行任务: taskId={}, taskName={}, executeTime={}, nodeId={}", 
                taskId, task.getTaskName(), executeTime, nodeId);
            
            // 检查任务是否仍然可以执行
            if (!task.canExecute()) {
                String reason = "任务状态不允许执行: status=" + task.getStatus();
                log.warn("任务执行检查失败: taskId={}, reason={}", taskId, reason);
                
                executionLog = ScheduledTaskExecutionLog.skipped(taskId, executeTime, reason, nodeId);
                return;
            }
            
            // 构建消息推送请求
            MessagePushRequest pushRequest = buildMessagePushRequest(task);
            
            // 执行消息推送
            Long messageId = messageForwardService.pushMessage(pushRequest);
            
            if (messageId != null) {
                // 更新任务执行次数
                scheduledTaskService.incrementExecutedCount(taskId);
                
                // 如果是一次性任务或已达到最大执行次数，标记为完成
                if (task.isCompleted()) {
                    scheduledTaskService.markTaskCompleted(taskId);
                }
                
                int duration = (int) (System.currentTimeMillis() - startTime);
                executionLog = ScheduledTaskExecutionLog.success(taskId, executeTime, messageId, duration, nodeId);
                
                log.info("任务执行成功: taskId={}, messageId={}, duration={}ms", 
                    taskId, messageId, duration);
            } else {
                throw new RuntimeException("消息推送失败，返回的消息ID为空");
            }
            
        } catch (Exception e) {
            log.error("任务执行失败: taskId={}, taskName={}, error={}", 
                taskId, task.getTaskName(), e.getMessage(), e);
            
            int duration = (int) (System.currentTimeMillis() - startTime);
            executionLog = ScheduledTaskExecutionLog.failed(taskId, executeTime, e.getMessage(), duration, nodeId);
            
        } finally {
            // 释放分布式锁
            distributedTaskLock.releaseLock(taskId, executeTime);
            
            // 保存执行日志
            if (executionLog != null) {
                try {
                    scheduledTaskExecutionLogService.saveExecutionLog(executionLog);
                } catch (Exception e) {
                    log.error("保存任务执行日志失败: taskId={}, error={}", taskId, e.getMessage());
                }
            }
        }
    }
    
    /**
     * 构建消息推送请求
     * 
     * @param task 定时任务
     * @return 消息推送请求
     */
    private MessagePushRequest buildMessagePushRequest(ScheduledTask task) {
        MessagePushRequest request = new MessagePushRequest();
        request.setTitle(task.getMessageTitle());
        request.setContent(task.getMessageContent());
        request.setUrl(task.getMessageUrl());
        request.setTags(task.getTags());
        request.setMessageType("notification");
        request.setSource("scheduled_task");
        request.setLevel("normal");
        
        // 根据用户ID获取用户密钥
        var user = userService.getUserById(task.getUserId());
        if (user != null) {
            request.setUserKey(user.getUserKey());
        } else {
            throw new RuntimeException("用户不存在: " + task.getUserId());
        }
        
        return request;
    }
}