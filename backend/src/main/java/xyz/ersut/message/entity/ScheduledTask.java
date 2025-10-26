package xyz.ersut.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务实体
 * 
 * @author ersut
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("scheduled_task")
public class ScheduledTask {
    
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 任务名称
     */
    private String taskName;
    
    /**
     * 任务描述
     */
    private String description;
    
    /**
     * 通知标题
     */
    private String messageTitle;
    
    /**
     * 通知内容
     */
    private String messageContent;
    
    /**
     * 跳转链接
     */
    private String messageUrl;
    
    /**
     * 标签数组（MySQL JSON类型）
     */
    @TableField(typeHandler = xyz.ersut.message.typehandler.JsonStringListTypeHandler.class)
    private List<String> tags;
    
    /**
     * 调度类型：once-一次性, daily-每天, weekly-每周, monthly-每月, custom-自定义
     */
    private String scheduleType;
    
    /**
     * Cron表达式
     */
    private String cronExpression;
    
    /**
     * 时区
     */
    private String timezone;
    
    /**
     * 开始日期
     */
    private LocalDate startDate;
    
    /**
     * 结束日期（可选）
     */
    private LocalDate endDate;
    
    /**
     * 最大执行次数，-1表示无限制
     */
    private Integer maxExecutions;
    
    /**
     * 已执行次数
     */
    private Integer executedCount;
    
    /**
     * 任务状态：enabled-启用, disabled-停用, completed-完成, deleted-已删除
     */
    private String status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 软删除时间
     */
    private LocalDateTime deleteTime;
    
    /**
     * 检查任务是否可以执行
     */
    public boolean canExecute() {
        if (!"enabled".equals(status)) {
            return false;
        }
        
        if (deleteTime != null) {
            return false;
        }
        
        LocalDate today = LocalDate.now();
        if (startDate != null && today.isBefore(startDate)) {
            return false;
        }
        
        if (endDate != null && today.isAfter(endDate)) {
            return false;
        }
        
        if (maxExecutions > 0 && executedCount >= maxExecutions) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 是否为一次性任务
     */
    public boolean isOnceTask() {
        return "once".equals(scheduleType);
    }
    
    /**
     * 增加执行次数
     */
    public void incrementExecutedCount() {
        this.executedCount = (this.executedCount == null ? 0 : this.executedCount) + 1;
    }
    
    /**
     * 检查是否已完成
     */
    public boolean isCompleted() {
        if (isOnceTask()) {
            return executedCount != null && executedCount > 0;
        }
        
        if (maxExecutions > 0 && executedCount != null && executedCount >= maxExecutions) {
            return true;
        }
        
        if (endDate != null && LocalDate.now().isAfter(endDate)) {
            return true;
        }
        
        return false;
    }
}