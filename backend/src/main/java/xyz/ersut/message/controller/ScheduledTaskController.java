package xyz.ersut.message.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import xyz.ersut.message.dto.Result;
import xyz.ersut.message.dto.TaskCreateDTO;
import xyz.ersut.message.dto.TaskQueryDTO;
import xyz.ersut.message.dto.TaskUpdateDTO;
import xyz.ersut.message.entity.ScheduledTask;
import xyz.ersut.message.entity.ScheduledTaskExecutionLog;
import xyz.ersut.message.mapper.ScheduledTaskExecutionLogMapper;
import xyz.ersut.message.service.ScheduledTaskService;
import xyz.ersut.message.service.SysUserService;
import xyz.ersut.message.service.ScheduledTaskExecutionLogService;
import xyz.ersut.message.utils.CronExpressionBuilder;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 定时任务控制器
 * 
 * @author ersut
 */
@Slf4j
@RestController
@RequestMapping("/api/scheduled-tasks")
@RequiredArgsConstructor
@Tag(name = "定时任务管理", description = "定时任务的创建、修改、查询、删除等操作")
public class ScheduledTaskController {
    
    private final ScheduledTaskService scheduledTaskService;
    private final ScheduledTaskExecutionLogService scheduledTaskExecutionLogService;
    private final SysUserService userService;
    
    /**
     * 创建定时任务
     */
    @Operation(summary = "创建定时任务", description = "创建新的定时提醒任务")
    @PostMapping
    public Result<Long> createTask(@Valid @RequestBody TaskCreateDTO createDTO,
                                   @Parameter(hidden = true) Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            Long taskId = scheduledTaskService.createTask(createDTO, userId);
            return Result.success(taskId);
        } catch (Exception e) {
            log.error("创建定时任务失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新定时任务
     */
    @Operation(summary = "更新定时任务", description = "更新指定的定时任务信息")
    @PutMapping("/{id}")
    public Result<Void> updateTask(@PathVariable Long id, 
                                   @RequestBody TaskUpdateDTO updateDTO,
                                   @Parameter(hidden = true) Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            boolean success = scheduledTaskService.updateTask(id, updateDTO, userId);
            return success ? Result.success() : Result.error("更新失败");
        } catch (Exception e) {
            log.error("更新定时任务失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取任务详情
     */
    @Operation(summary = "获取任务详情", description = "根据任务ID获取任务详细信息")
    @GetMapping("/{id}")
    public Result<ScheduledTask> getTask(@PathVariable Long id,
                                        @Parameter(hidden = true) Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            ScheduledTask task = scheduledTaskService.getTaskById(id, userId);
            if (task == null) {
                return Result.error("任务不存在或无权限访问");
            }
            
            return Result.success(task);
        } catch (Exception e) {
            log.error("获取任务详情失败: {}", e.getMessage(), e);
            return Result.error("获取任务详情失败");
        }
    }
    
    /**
     * 分页查询任务列表
     */
    @Operation(summary = "分页查询任务列表", description = "根据条件分页查询当前用户的定时任务")
    @GetMapping("/list")
    public Result<IPage<ScheduledTask>> listTasks(@Parameter(description = "查询条件") TaskQueryDTO queryDTO,
                                                  @Parameter(hidden = true) Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            IPage<ScheduledTask> pageResult = scheduledTaskService.getTasksByPage(queryDTO, userId);
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("查询任务列表失败: {}", e.getMessage(), e);
            return Result.error("查询任务列表失败");
        }
    }
    
    /**
     * 更新任务状态
     */
    @Operation(summary = "更新任务状态", description = "启用、停用指定的定时任务")
    @PutMapping("/{id}/status")
    public Result<Void> updateTaskStatus(@PathVariable Long id,
                                        @RequestParam String status,
                                        @Parameter(hidden = true) Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            boolean success = scheduledTaskService.updateTaskStatus(id, status, userId);
            return success ? Result.success() : Result.error("更新状态失败");
        } catch (Exception e) {
            log.error("更新任务状态失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除任务
     */
    @Operation(summary = "删除任务", description = "删除指定的定时任务")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTask(@PathVariable Long id,
                                   @Parameter(hidden = true) Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            boolean success = scheduledTaskService.deleteTask(id, userId);
            return success ? Result.success() : Result.error("删除失败");
        } catch (Exception e) {
            log.error("删除任务失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 批量更新任务状态
     */
    @Operation(summary = "批量更新任务状态", description = "批量启用或停用多个定时任务")
    @PutMapping("/batch/status")
    public Result<Map<String, Object>> batchUpdateStatus(@RequestParam List<Long> taskIds,
                                                         @RequestParam String status,
                                                         @Parameter(hidden = true) Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            int count = scheduledTaskService.batchUpdateStatus(taskIds, status, userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("updatedCount", count);
            result.put("totalCount", taskIds.size());
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("批量更新任务状态失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 立即执行任务
     */
    @Operation(summary = "立即执行任务", description = "手动触发指定任务立即执行一次")
    @PostMapping("/{id}/execute")
    public Result<Void> executeTaskNow(@PathVariable Long id,
                                       @Parameter(hidden = true) Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            boolean success = scheduledTaskService.executeTaskNow(id, userId);
            return success ? Result.success() : Result.error("执行失败");
        } catch (Exception e) {
            log.error("立即执行任务失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取任务执行记录
     */
    @Operation(summary = "获取任务执行记录", description = "分页查询指定任务的执行记录")
    @GetMapping("/{id}/executions")
    public Result<IPage<ScheduledTaskExecutionLog>> getTaskExecutions(@PathVariable Long id,
                                                            @RequestParam(defaultValue = "1") int page,
                                                            @RequestParam(defaultValue = "20") int size,
                                                            @Parameter(hidden = true) Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            // 验证任务权限
            ScheduledTask task = scheduledTaskService.getTaskById(id, userId);
            if (task == null) {
                return Result.error("任务不存在或无权限访问");
            }
            
            IPage<ScheduledTaskExecutionLog> pageResult = scheduledTaskExecutionLogService.getExecutionLogsByPage(id, page, size);
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("获取任务执行记录失败: {}", e.getMessage(), e);
            return Result.error("获取执行记录失败");
        }
    }
    
    /**
     * 获取任务执行统计
     */
    @Operation(summary = "获取任务执行统计", description = "获取指定任务的执行统计信息")
    @GetMapping("/{id}/stats")
    public Result<ScheduledTaskExecutionLogMapper.ScheduledTaskExecutionStats> getTaskStats(@PathVariable Long id,
                                                                          @Parameter(hidden = true) Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            // 验证任务权限
            ScheduledTask task = scheduledTaskService.getTaskById(id, userId);
            if (task == null) {
                return Result.error("任务不存在或无权限访问");
            }
            
            ScheduledTaskExecutionLogMapper.ScheduledTaskExecutionStats stats = scheduledTaskExecutionLogService.getExecutionStats(id);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取任务执行统计失败: {}", e.getMessage(), e);
            return Result.error("获取执行统计失败");
        }
    }
    
    /**
     * 预览Cron表达式执行时间
     */
    @Operation(summary = "预览Cron表达式", description = "预览Cron表达式的未来执行时间")
    @PostMapping("/cron/preview")
    public Result<Map<String, Object>> previewCronExpression(@RequestBody Map<String, String> request) {
        try {
            String cronExpression = request.get("cronExpression");
            if (cronExpression == null || cronExpression.trim().isEmpty()) {
                return Result.error("Cron表达式不能为空");
            }
            
            boolean isValid = CronExpressionBuilder.isValidCronExpression(cronExpression);
            String description = CronExpressionBuilder.getCronDescription(cronExpression);
            
            Map<String, Object> result = new HashMap<>();
            result.put("isValid", isValid);
            result.put("description", description);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("预览Cron表达式失败: {}", e.getMessage(), e);
            return Result.error("预览失败");
        }
    }
    
    /**
     * 获取用户任务统计
     */
    @Operation(summary = "获取用户任务统计", description = "获取当前用户的任务统计信息")
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getUserTaskDashboard(@Parameter(hidden = true) Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("totalTasks", scheduledTaskService.countUserTasks(userId, null));
            dashboard.put("enabledTasks", scheduledTaskService.countUserTasks(userId, "enabled"));
            dashboard.put("disabledTasks", scheduledTaskService.countUserTasks(userId, "disabled"));
            dashboard.put("completedTasks", scheduledTaskService.countUserTasks(userId, "completed"));
            
            return Result.success(dashboard);
        } catch (Exception e) {
            log.error("获取用户任务统计失败: {}", e.getMessage(), e);
            return Result.error("获取统计失败");
        }
    }
    
    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        
        String username = authentication.getName();
        var user = userService.getUserByUsername(username);
        return user != null ? user.getId() : null;
    }
}