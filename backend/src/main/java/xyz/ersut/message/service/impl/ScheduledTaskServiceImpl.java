package xyz.ersut.message.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.ersut.message.dto.TaskCreateDTO;
import xyz.ersut.message.dto.TaskQueryDTO;
import xyz.ersut.message.dto.TaskUpdateDTO;
import xyz.ersut.message.entity.ScheduledTask;
import xyz.ersut.message.mapper.ScheduledTaskMapper;
import xyz.ersut.message.service.ScheduledTaskService;
import xyz.ersut.message.service.schedule.DynamicTaskManager;
import xyz.ersut.message.utils.CronExpressionBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务服务实现类
 * 
 * @author ersut
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledTaskServiceImpl implements ScheduledTaskService {
    
    private final ScheduledTaskMapper scheduledTaskMapper;
    @Autowired
    @Lazy
    private DynamicTaskManager dynamicTaskManager;
    
    @Override
    @Transactional
    public Long createTask(TaskCreateDTO createDTO, Long userId) {
        if (createDTO == null || userId == null) {
            throw new IllegalArgumentException("创建参数不能为空");
        }
        
        // 验证Cron表达式
        if (!CronExpressionBuilder.isValidCronExpression(createDTO.getCronExpression())) {
            throw new IllegalArgumentException("无效的Cron表达式: " + createDTO.getCronExpression());
        }
        
        try {
            // 构建任务实体
            ScheduledTask task = new ScheduledTask();
            BeanUtil.copyProperties(createDTO, task);
            task.setId(IdUtil.getSnowflakeNextId());
            task.setUserId(userId);
            task.setExecutedCount(0);
            task.setStatus("enabled");
            task.setCreateTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());
            
            // 保存到数据库
            int result = scheduledTaskMapper.insert(task);
            if (result > 0) {
                log.info("创建定时任务成功: taskId={}, taskName={}, userId={}", 
                    task.getId(), task.getTaskName(), userId);
                
                // 如果是今天或以后开始的任务，立即调度
                LocalDate today = LocalDate.now();
                if (task.getStartDate() == null || !task.getStartDate().isAfter(today)) {
                    dynamicTaskManager.scheduleTask(task, today.plusDays(1));
                }
                
                return task.getId();
            } else {
                throw new RuntimeException("保存任务到数据库失败");
            }
        } catch (Exception e) {
            log.error("创建定时任务失败: taskName={}, userId={}, error={}", 
                createDTO.getTaskName(), userId, e.getMessage(), e);
            throw new RuntimeException("创建定时任务失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean updateTask(Long taskId, TaskUpdateDTO updateDTO, Long userId) {
        if (taskId == null || updateDTO == null || userId == null) {
            return false;
        }
        
        try {
            // 验证任务存在且属于当前用户
            ScheduledTask existingTask = scheduledTaskMapper.selectByIdAndNotDeleted(taskId);
            if (existingTask == null || !existingTask.getUserId().equals(userId)) {
                log.warn("任务不存在或无权限更新: taskId={}, userId={}", taskId, userId);
                return false;
            }
            
            // 验证Cron表达式（如果有更新）
            if (StrUtil.isNotBlank(updateDTO.getCronExpression()) &&
                !CronExpressionBuilder.isValidCronExpression(updateDTO.getCronExpression())) {
                throw new IllegalArgumentException("无效的Cron表达式: " + updateDTO.getCronExpression());
            }
            
            // 更新任务属性
            BeanUtil.copyProperties(updateDTO, existingTask, "id", "userId", "executedCount", "createTime");
            existingTask.setUpdateTime(LocalDateTime.now());
            
            // 保存更新
            int result = scheduledTaskMapper.updateById(existingTask);
            if (result > 0) {
                log.info("更新定时任务成功: taskId={}, taskName={}, userId={}", 
                    taskId, existingTask.getTaskName(), userId);
                
                // 重新调度任务
                dynamicTaskManager.rescheduleTask(existingTask, LocalDate.now().plusDays(1));
                
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("更新定时任务失败: taskId={}, userId={}, error={}", 
                taskId, userId, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public ScheduledTask getTaskById(Long taskId, Long userId) {
        if (taskId == null || userId == null) {
            return null;
        }
        
        try {
            ScheduledTask task = scheduledTaskMapper.selectByIdAndNotDeleted(taskId);
            if (task != null && task.getUserId().equals(userId)) {
                return task;
            } else {
                log.warn("任务不存在或无权限查看: taskId={}, userId={}", taskId, userId);
                return null;
            }
        } catch (Exception e) {
            log.error("查询任务详情失败: taskId={}, userId={}, error={}", 
                taskId, userId, e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public IPage<ScheduledTask> getTasksByPage(TaskQueryDTO queryDTO, Long userId) {
        if (queryDTO == null || userId == null) {
            return new Page<>();
        }
        
        try {
            Page<ScheduledTask> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
            return scheduledTaskMapper.selectPageByUserId(page, userId, 
                queryDTO.getStatus(), queryDTO.getScheduleType(), queryDTO.getKeyword());
        } catch (Exception e) {
            log.error("分页查询任务失败: userId={}, error={}", userId, e.getMessage(), e);
            return new Page<>();
        }
    }
    
    @Override
    @Transactional
    public boolean updateTaskStatus(Long taskId, String status, Long userId) {
        if (taskId == null || StrUtil.isBlank(status) || userId == null) {
            return false;
        }
        
        // 验证状态值
        if (!"enabled".equals(status) && !"disabled".equals(status)) {
            log.warn("无效的任务状态: {}", status);
            return false;
        }
        
        try {
            // 验证任务存在且属于当前用户
            ScheduledTask task = scheduledTaskMapper.selectByIdAndNotDeleted(taskId);
            if (task == null || !task.getUserId().equals(userId)) {
                log.warn("任务不存在或无权限更新状态: taskId={}, userId={}", taskId, userId);
                return false;
            }
            
            int result = scheduledTaskMapper.updateStatus(taskId, status, LocalDateTime.now());
            if (result > 0) {
                log.info("更新任务状态成功: taskId={}, status={}, userId={}", taskId, status, userId);
                
                // 根据状态更新任务调度
                if ("enabled".equals(status)) {
                    task.setStatus(status);
                    dynamicTaskManager.scheduleTask(task, LocalDate.now().plusDays(1));
                } else {
                    dynamicTaskManager.cancelTaskById(taskId);
                }
                
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("更新任务状态失败: taskId={}, status={}, userId={}, error={}", 
                taskId, status, userId, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean deleteTask(Long taskId, Long userId) {
        if (taskId == null || userId == null) {
            return false;
        }
        
        try {
            int result = scheduledTaskMapper.softDelete(taskId, userId, LocalDateTime.now());
            if (result > 0) {
                log.info("删除任务成功: taskId={}, userId={}", taskId, userId);
                
                // 取消任务调度
                dynamicTaskManager.cancelTaskById(taskId);
                
                return true;
            } else {
                log.warn("删除任务失败，任务不存在或无权限: taskId={}, userId={}", taskId, userId);
                return false;
            }
        } catch (Exception e) {
            log.error("删除任务失败: taskId={}, userId={}, error={}", 
                taskId, userId, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public int batchUpdateStatus(List<Long> taskIds, String status, Long userId) {
        if (taskIds == null || taskIds.isEmpty() || StrUtil.isBlank(status) || userId == null) {
            return 0;
        }
        
        // 验证状态值
        if (!"enabled".equals(status) && !"disabled".equals(status)) {
            log.warn("无效的任务状态: {}", status);
            return 0;
        }
        
        try {
            int result = scheduledTaskMapper.batchUpdateStatus(userId, taskIds, status, LocalDateTime.now());
            
            if (result > 0) {
                log.info("批量更新任务状态成功: count={}, status={}, userId={}", result, status, userId);
                
                // 根据状态更新任务调度
                for (Long taskId : taskIds) {
                    if ("enabled".equals(status)) {
                        ScheduledTask task = scheduledTaskMapper.selectByIdAndNotDeleted(taskId);
                        if (task != null && task.getUserId().equals(userId)) {
                            task.setStatus(status);
                            dynamicTaskManager.scheduleTask(task, LocalDate.now().plusDays(1));
                        }
                    } else {
                        dynamicTaskManager.cancelTaskById(taskId);
                    }
                }
            }
            
            return result;
        } catch (Exception e) {
            log.error("批量更新任务状态失败: taskIds={}, status={}, userId={}, error={}", 
                taskIds, status, userId, e.getMessage(), e);
            return 0;
        }
    }
    
    @Override
    public List<ScheduledTask> getTasksForDate(LocalDate executeDate) {
        if (executeDate == null) {
            return List.of();
        }
        
        try {
            return scheduledTaskMapper.selectTasksForDate(executeDate);
        } catch (Exception e) {
            log.error("查询指定日期的任务失败: executeDate={}, error={}", executeDate, e.getMessage(), e);
            return List.of();
        }
    }
    
    @Override
    @Transactional
    public boolean incrementExecutedCount(Long taskId) {
        if (taskId == null) {
            return false;
        }
        
        try {
            int result = scheduledTaskMapper.incrementExecutedCount(taskId, LocalDateTime.now());
            return result > 0;
        } catch (Exception e) {
            log.error("增加任务执行次数失败: taskId={}, error={}", taskId, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean markTaskCompleted(Long taskId) {
        if (taskId == null) {
            return false;
        }
        
        try {
            int result = scheduledTaskMapper.updateStatus(taskId, "completed", LocalDateTime.now());
            if (result > 0) {
                log.info("标记任务为完成: taskId={}", taskId);
                
                // 取消任务调度
                dynamicTaskManager.cancelTaskById(taskId);
                
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("标记任务为完成失败: taskId={}, error={}", taskId, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean executeTaskNow(Long taskId, Long userId) {
        if (taskId == null || userId == null) {
            return false;
        }
        
        try {
            ScheduledTask task = getTaskById(taskId, userId);
            if (task != null && task.canExecute()) {
                dynamicTaskManager.executeTaskNow(task);
                log.info("立即执行任务: taskId={}, userId={}", taskId, userId);
                return true;
            } else {
                log.warn("任务不存在或无法执行: taskId={}, userId={}", taskId, userId);
                return false;
            }
        } catch (Exception e) {
            log.error("立即执行任务失败: taskId={}, userId={}, error={}", 
                taskId, userId, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public Long countUserTasks(Long userId, String status) {
        if (userId == null) {
            return 0L;
        }
        
        try {
            return scheduledTaskMapper.countByUserId(userId, status);
        } catch (Exception e) {
            log.error("统计用户任务数量失败: userId={}, status={}, error={}", 
                userId, status, e.getMessage(), e);
            return 0L;
        }
    }
}