package xyz.ersut.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import xyz.ersut.message.entity.ScheduledTaskExecutionLog;

/**
 * 定时任务执行记录Mapper
 * 
 * @author ersut
 */
@Mapper
public interface ScheduledTaskExecutionLogMapper extends BaseMapper<ScheduledTaskExecutionLog> {
    
    /**
     * 分页查询任务执行记录
     */
    @Select("""
        SELECT * FROM scheduled_task_execution_log 
        WHERE task_id = #{taskId} 
        ORDER BY execution_time DESC
        """)
    IPage<ScheduledTaskExecutionLog> selectPageByTaskId(IPage<ScheduledTaskExecutionLog> page, @Param("taskId") Long taskId);
    
    /**
     * 统计任务执行情况
     */
    @Select("""
        SELECT 
            COUNT(*) as total,
            SUM(CASE WHEN status = 'success' THEN 1 ELSE 0 END) as success_count,
            SUM(CASE WHEN status = 'failed' THEN 1 ELSE 0 END) as failed_count,
            SUM(CASE WHEN status = 'skipped' THEN 1 ELSE 0 END) as skipped_count
        FROM scheduled_task_execution_log 
        WHERE task_id = #{taskId}
        """)
    ScheduledTaskExecutionStats selectExecutionStats(@Param("taskId") Long taskId);
    
    /**
     * 执行统计结果
     */
    class ScheduledTaskExecutionStats {
        private Long total;
        private Long successCount;
        private Long failedCount;
        private Long skippedCount;
        
        // getters and setters
        public Long getTotal() { return total; }
        public void setTotal(Long total) { this.total = total; }
        public Long getSuccessCount() { return successCount; }
        public void setSuccessCount(Long successCount) { this.successCount = successCount; }
        public Long getFailedCount() { return failedCount; }
        public void setFailedCount(Long failedCount) { this.failedCount = failedCount; }
        public Long getSkippedCount() { return skippedCount; }
        public void setSkippedCount(Long skippedCount) { this.skippedCount = skippedCount; }
    }
}