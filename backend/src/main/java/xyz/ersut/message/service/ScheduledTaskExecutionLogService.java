package xyz.ersut.message.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import xyz.ersut.message.entity.ScheduledTaskExecutionLog;
import xyz.ersut.message.mapper.ScheduledTaskExecutionLogMapper;

/**
 * 定时任务执行记录服务接口
 * 
 * @author ersut
 */
public interface ScheduledTaskExecutionLogService {
    
    /**
     * 保存执行记录
     * 
     * @param executionLog 执行记录
     * @return 是否成功
     */
    boolean saveExecutionLog(ScheduledTaskExecutionLog executionLog);
    
    /**
     * 分页查询任务执行记录
     * 
     * @param taskId 任务ID
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    IPage<ScheduledTaskExecutionLog> getExecutionLogsByPage(Long taskId, int page, int size);
    
    /**
     * 获取任务执行统计
     * 
     * @param taskId 任务ID
     * @return 执行统计
     */
    ScheduledTaskExecutionLogMapper.ScheduledTaskExecutionStats getExecutionStats(Long taskId);
}