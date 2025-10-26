package xyz.ersut.message.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.ersut.message.entity.ScheduledTaskExecutionLog;
import xyz.ersut.message.mapper.ScheduledTaskExecutionLogMapper;
import xyz.ersut.message.service.ScheduledTaskExecutionLogService;

/**
 * 定时任务执行记录服务实现类
 * 
 * @author ersut
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledTaskExecutionLogServiceImpl implements ScheduledTaskExecutionLogService {
    
    private final ScheduledTaskExecutionLogMapper scheduledTaskExecutionLogMapper;
    
    @Override
    public boolean saveExecutionLog(ScheduledTaskExecutionLog executionLog) {
        if (executionLog == null) {
            return false;
        }
        
        try {
            int result = scheduledTaskExecutionLogMapper.insert(executionLog);
            if (result > 0) {
                log.debug("保存任务执行记录成功: taskId={}, status={}", 
                    executionLog.getTaskId(), executionLog.getStatus());
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("保存任务执行记录失败: taskId={}, error={}", 
                executionLog.getTaskId(), e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public IPage<ScheduledTaskExecutionLog> getExecutionLogsByPage(Long taskId, int page, int size) {
        if (taskId == null || page < 1 || size < 1) {
            return new Page<>();
        }
        
        try {
            Page<ScheduledTaskExecutionLog> pageParam = new Page<>(page, size);
            return scheduledTaskExecutionLogMapper.selectPageByTaskId(pageParam, taskId);
        } catch (Exception e) {
            log.error("分页查询任务执行记录失败: taskId={}, error={}", taskId, e.getMessage(), e);
            return new Page<>();
        }
    }
    
    @Override
    public ScheduledTaskExecutionLogMapper.ScheduledTaskExecutionStats getExecutionStats(Long taskId) {
        if (taskId == null) {
            return null;
        }
        
        try {
            return scheduledTaskExecutionLogMapper.selectExecutionStats(taskId);
        } catch (Exception e) {
            log.error("查询任务执行统计失败: taskId={}, error={}", taskId, e.getMessage(), e);
            return null;
        }
    }
}