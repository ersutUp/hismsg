package xyz.ersut.message.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import xyz.ersut.message.dto.TaskCreateDTO;
import xyz.ersut.message.dto.TaskQueryDTO;
import xyz.ersut.message.dto.TaskUpdateDTO;
import xyz.ersut.message.entity.ScheduledTask;

import java.time.LocalDate;
import java.util.List;

/**
 * 定时任务服务接口
 * 
 * @author ersut
 */
public interface ScheduledTaskService {
    
    /**
     * 创建定时任务
     * 
     * @param createDTO 创建请求DTO
     * @param userId 用户ID
     * @return 任务ID
     */
    Long createTask(TaskCreateDTO createDTO, Long userId);
    
    /**
     * 更新定时任务
     * 
     * @param taskId 任务ID
     * @param updateDTO 更新请求DTO
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean updateTask(Long taskId, TaskUpdateDTO updateDTO, Long userId);
    
    /**
     * 根据ID获取任务详情
     * 
     * @param taskId 任务ID
     * @param userId 用户ID（权限验证）
     * @return 任务详情
     */
    ScheduledTask getTaskById(Long taskId, Long userId);
    
    /**
     * 分页查询用户的定时任务
     * 
     * @param queryDTO 查询条件
     * @param userId 用户ID
     * @return 分页结果
     */
    IPage<ScheduledTask> getTasksByPage(TaskQueryDTO queryDTO, Long userId);
    
    /**
     * 更新任务状态
     * 
     * @param taskId 任务ID
     * @param status 新状态
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean updateTaskStatus(Long taskId, String status, Long userId);
    
    /**
     * 删除任务（软删除）
     * 
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteTask(Long taskId, Long userId);
    
    /**
     * 批量更新任务状态
     * 
     * @param taskIds 任务ID列表
     * @param status 新状态
     * @param userId 用户ID
     * @return 更新成功的数量
     */
    int batchUpdateStatus(List<Long> taskIds, String status, Long userId);
    
    /**
     * 获取指定日期需要执行的任务
     * 
     * @param executeDate 执行日期
     * @return 任务列表
     */
    List<ScheduledTask> getTasksForDate(LocalDate executeDate);
    
    /**
     * 增加任务执行次数
     * 
     * @param taskId 任务ID
     * @return 是否成功
     */
    boolean incrementExecutedCount(Long taskId);
    
    /**
     * 标记任务为已完成
     * 
     * @param taskId 任务ID
     * @return 是否成功
     */
    boolean markTaskCompleted(Long taskId);
    
    /**
     * 立即执行任务
     * 
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean executeTaskNow(Long taskId, Long userId);
    
    /**
     * 统计用户任务数量
     * 
     * @param userId 用户ID
     * @param status 状态筛选（可选）
     * @return 任务数量
     */
    Long countUserTasks(Long userId, String status);
}