package xyz.ersut.message.service.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import xyz.ersut.message.entity.ScheduledTask;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * 动态任务管理器
 * 负责管理动态创建和取消的定时任务
 * 
 * @author ersut
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicTaskManager {
    
    private final TaskScheduler taskScheduler;
    private final TaskScheduleExecutor taskExecutor;
    
    // 存储已调度的任务
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void init() {
        log.info("动态任务管理器初始化完成");
    }
    
    @PreDestroy
    public void destroy() {
        log.info("正在关闭动态任务管理器...");
        clearAllTasks();
    }
    
    /**
     * 调度单个任务
     * 
     * @param task 定时任务
     * @param executeDate 执行日期
     */
    public void scheduleTask(ScheduledTask task, LocalDate executeDate) {
        if (task == null || !task.canExecute()) {
            log.warn("任务无法执行，跳过调度: taskId={}, status={}", 
                task != null ? task.getId() : null, 
                task != null ? task.getStatus() : null);
            return;
        }
        
        try {
            String taskKey = buildTaskKey(task.getId(), executeDate);
            
            // 如果任务已存在，先取消
            cancelTask(taskKey);

            // 解析Cron表达式并计算下次执行时间
            // Quartz支持7字段格式：秒 分 时 日 月 周 年
            CronExpression cronExpression = new CronExpression(task.getCronExpression());
            ZoneId zoneId = ZoneId.of(task.getTimezone() != null ? task.getTimezone() : "Asia/Shanghai");
            cronExpression.setTimeZone(java.util.TimeZone.getTimeZone(zoneId));

            Date now = new Date();
            Date nextExecutionDate = cronExpression.getNextValidTimeAfter(now);

            if (nextExecutionDate != null) {
                ZonedDateTime nextExecution = ZonedDateTime.ofInstant(nextExecutionDate.toInstant(), zoneId);

                // 创建任务执行器
                Runnable taskRunnable = () -> taskExecutor.executeTask(task, nextExecution.toLocalDateTime());
                
                // 调度任务
                ScheduledFuture<?> future = taskScheduler.schedule(taskRunnable, nextExecution.toInstant());
                
                if (future != null) {
                    scheduledTasks.put(taskKey, future);
                    log.info("任务调度成功: taskId={}, taskName={}, executeTime={}", 
                        task.getId(), task.getTaskName(), nextExecution.toLocalDateTime());
                } else {
                    log.error("任务调度失败: taskId={}, taskName={}", task.getId(), task.getTaskName());
                }
            } else {
                log.warn("无法计算下次执行时间: taskId={}, cronExpression={}", 
                    task.getId(), task.getCronExpression());
            }
            
        } catch (Exception e) {
            log.error("调度任务异常: taskId={}, taskName={}, error={}", 
                task.getId(), task.getTaskName(), e.getMessage(), e);
        }
    }
    
    /**
     * 取消指定任务
     * 
     * @param taskKey 任务键
     */
    public void cancelTask(String taskKey) {
        ScheduledFuture<?> future = scheduledTasks.remove(taskKey);
        if (future != null && !future.isCancelled()) {
            boolean cancelled = future.cancel(false);
            log.info("取消任务: taskKey={}, cancelled={}", taskKey, cancelled);
        }
    }
    
    /**
     * 取消指定任务的所有调度
     * 
     * @param taskId 任务ID
     */
    public void cancelTaskById(Long taskId) {
        String prefix = "task_" + taskId + "_";
        scheduledTasks.entrySet().removeIf(entry -> {
            String key = entry.getKey();
            if (key.startsWith(prefix)) {
                ScheduledFuture<?> future = entry.getValue();
                if (future != null && !future.isCancelled()) {
                    boolean cancelled = future.cancel(false);
                    log.info("取消任务: taskKey={}, cancelled={}", key, cancelled);
                }
                return true;
            }
            return false;
        });
    }
    
    /**
     * 重新调度任务
     * 
     * @param task 定时任务
     * @param executeDate 执行日期
     */
    public void rescheduleTask(ScheduledTask task, LocalDate executeDate) {
        cancelTaskById(task.getId());
        scheduleTask(task, executeDate);
    }
    
    /**
     * 清理所有任务
     */
    public void clearAllTasks() {
        log.info("清理所有动态任务，当前任务数量: {}", scheduledTasks.size());
        
        scheduledTasks.values().forEach(future -> {
            if (future != null && !future.isCancelled()) {
                future.cancel(false);
            }
        });
        
        scheduledTasks.clear();
        log.info("所有动态任务已清理完成");
    }
    
    /**
     * 获取当前调度的任务数量
     * 
     * @return 任务数量
     */
    public int getScheduledTaskCount() {
        return scheduledTasks.size();
    }
    
    /**
     * 检查任务是否已调度
     * 
     * @param taskId 任务ID
     * @param executeDate 执行日期
     * @return 是否已调度
     */
    public boolean isTaskScheduled(Long taskId, LocalDate executeDate) {
        String taskKey = buildTaskKey(taskId, executeDate);
        return scheduledTasks.containsKey(taskKey);
    }
    
    /**
     * 立即执行任务（用于手动触发）
     * 
     * @param task 任务
     */
    public void executeTaskNow(ScheduledTask task) {
        if (task == null) {
            return;
        }
        
        log.info("立即执行任务: taskId={}, taskName={}", task.getId(), task.getTaskName());
        
        // 异步执行任务
        taskScheduler.schedule(() -> {
            taskExecutor.executeTask(task, LocalDateTime.now());
        }, java.time.Instant.now());
    }

    /**
     * 构建任务键
     *
     * @param taskId 任务ID
     * @param executeDate 执行日期
     * @return 任务键
     */
    private String buildTaskKey(Long taskId, LocalDate executeDate) {
        return "task_" + taskId + "_" + executeDate.toString();
    }
}