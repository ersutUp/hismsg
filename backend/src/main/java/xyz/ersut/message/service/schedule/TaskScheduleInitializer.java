package xyz.ersut.message.service.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import xyz.ersut.message.entity.ScheduledTask;
import xyz.ersut.message.service.ScheduledTaskService;

import java.time.LocalDate;
import java.util.List;

/**
 * 定时任务调度器
 * 负责每日初始化和管理动态定时任务
 * 
 * @author ersut
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskScheduleInitializer implements ApplicationRunner {
    
    private final ScheduledTaskService scheduledTaskService;
    private final DynamicTaskManager dynamicTaskManager;
    
    /**
     * 应用启动时初始化今天的任务
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("应用启动，初始化定时任务...");
        initializeTasksForDate(LocalDate.now());
    }
    
    /**
     * 每天00:00执行，初始化第二天的任务
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void initializeDailyTasks() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        log.info("开始初始化明天的定时任务: {}", tomorrow);
        
        try {
            // 清理昨天的动态任务
            dynamicTaskManager.clearAllTasks();
            
            // 初始化明天的任务
            initializeTasksForDate(tomorrow);
            
            log.info("明天的定时任务初始化完成，当前调度任务数: {}", 
                dynamicTaskManager.getScheduledTaskCount());
        } catch (Exception e) {
            log.error("初始化每日定时任务失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 每小时清理一次过期的动态任务（容错处理）
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanupExpiredTasks() {
        log.debug("执行定时任务清理检查，当前任务数: {}", dynamicTaskManager.getScheduledTaskCount());
        
        // 这里可以添加更详细的清理逻辑，比如检查已完成的任务等
        // 目前主要依靠每日00:00的全量清理
    }
    
    /**
     * 每5分钟检查一次任务调度状态（监控用）
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void monitorTaskSchedule() {
        int scheduledCount = dynamicTaskManager.getScheduledTaskCount();
        log.debug("定时任务调度监控 - 当前调度任务数: {}", scheduledCount);
        
        // 如果调度任务数量异常，可以发送告警
        if (scheduledCount > 1000) {
            log.warn("调度任务数量过多，可能存在内存泄漏: {}", scheduledCount);
        }
    }
    
    /**
     * 为指定日期初始化任务
     * 
     * @param executeDate 执行日期
     */
    private void initializeTasksForDate(LocalDate executeDate) {
        try {
            // 查询指定日期需要执行的任务
            List<ScheduledTask> tasks = scheduledTaskService.getTasksForDate(executeDate);
            log.info("找到{}个需要在{}执行的任务", tasks.size(), executeDate);
            
            // 为每个任务创建动态定时任务
            int scheduledCount = 0;
            for (ScheduledTask task : tasks) {
                try {
                    if (task.canExecute()) {
                        dynamicTaskManager.scheduleTask(task, executeDate);
                        scheduledCount++;
                    } else {
                        log.debug("任务无法执行，跳过调度: taskId={}, status={}", 
                            task.getId(), task.getStatus());
                    }
                } catch (Exception e) {
                    log.error("调度单个任务失败: taskId={}, taskName={}, error={}", 
                        task.getId(), task.getTaskName(), e.getMessage());
                }
            }
            
            log.info("{}的定时任务初始化完成，成功调度{}个任务", executeDate, scheduledCount);
        } catch (Exception e) {
            log.error("初始化{}的定时任务失败: {}", executeDate, e.getMessage(), e);
        }
    }
}