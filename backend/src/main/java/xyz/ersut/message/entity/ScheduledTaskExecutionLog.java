package xyz.ersut.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 定时任务执行记录实体
 * 
 * @author ersut
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("scheduled_task_execution_log")
public class ScheduledTaskExecutionLog {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 任务ID
     */
    private Long taskId;
    
    /**
     * 执行时间
     */
    private LocalDateTime executionTime;
    
    /**
     * 执行状态：success-成功, failed-失败, skipped-跳过
     */
    private String status;
    
    /**
     * 推送的消息ID
     */
    private Long messageId;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 执行节点标识
     */
    private String nodeId;
    
    /**
     * 执行耗时（毫秒）
     */
    private Integer executionDuration;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 创建成功执行记录
     */
    public static ScheduledTaskExecutionLog success(Long taskId, LocalDateTime executionTime, 
                                          Long messageId, Integer duration, String nodeId) {
        ScheduledTaskExecutionLog log = new ScheduledTaskExecutionLog();
        log.setTaskId(taskId);
        log.setExecutionTime(executionTime);
        log.setStatus("success");
        log.setMessageId(messageId);
        log.setExecutionDuration(duration);
        log.setNodeId(nodeId);
        log.setCreateTime(LocalDateTime.now());
        return log;
    }
    
    /**
     * 创建失败执行记录
     */
    public static ScheduledTaskExecutionLog failed(Long taskId, LocalDateTime executionTime, 
                                         String errorMessage, Integer duration, String nodeId) {
        ScheduledTaskExecutionLog log = new ScheduledTaskExecutionLog();
        log.setTaskId(taskId);
        log.setExecutionTime(executionTime);
        log.setStatus("failed");
        log.setErrorMessage(errorMessage);
        log.setExecutionDuration(duration);
        log.setNodeId(nodeId);
        log.setCreateTime(LocalDateTime.now());
        return log;
    }
    
    /**
     * 创建跳过执行记录
     */
    public static ScheduledTaskExecutionLog skipped(Long taskId, LocalDateTime executionTime, 
                                          String reason, String nodeId) {
        ScheduledTaskExecutionLog log = new ScheduledTaskExecutionLog();
        log.setTaskId(taskId);
        log.setExecutionTime(executionTime);
        log.setStatus("skipped");
        log.setErrorMessage(reason);
        log.setNodeId(nodeId);
        log.setCreateTime(LocalDateTime.now());
        return log;
    }
}