package xyz.ersut.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import xyz.ersut.message.entity.ScheduledTask;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务Mapper
 * 
 * @author ersut
 */
@Mapper
public interface ScheduledTaskMapper extends BaseMapper<ScheduledTask> {
    
    @Results(id = "scheduledTaskResultMap", value = {
        @Result(property = "tags", column = "tags", 
                typeHandler = xyz.ersut.message.typehandler.JsonStringListTypeHandler.class)
    })
    @Select("SELECT * FROM scheduled_task WHERE id = #{id} AND delete_time IS NULL")
    ScheduledTask selectByIdAndNotDeleted(@Param("id") Long id);
    
    /**
     * 分页查询用户的定时任务
     */
    @Select("""
        <script>
        SELECT * FROM scheduled_task 
        WHERE user_id = #{userId} AND delete_time IS NULL
        <if test="status != null and status != ''">
            AND status = #{status}
        </if>
        <if test="scheduleType != null and scheduleType != ''">
            AND schedule_type = #{scheduleType}
        </if>
        <if test="keyword != null and keyword != ''">
            AND (task_name LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%'))
        </if>
        ORDER BY create_time DESC
        </script>
        """)
    @Results(value = {
        @Result(property = "tags", column = "tags", 
                typeHandler = xyz.ersut.message.typehandler.JsonStringListTypeHandler.class)
    })
    IPage<ScheduledTask> selectPageByUserId(IPage<ScheduledTask> page, 
                                           @Param("userId") Long userId,
                                           @Param("status") String status,
                                           @Param("scheduleType") String scheduleType,
                                           @Param("keyword") String keyword);
    
    /**
     * 查询指定日期需要执行的任务
     */
    @Select("""
        SELECT * FROM scheduled_task 
        WHERE status = 'enabled' 
          AND delete_time IS NULL
          AND (start_date IS NULL OR start_date <= #{executeDate})
          AND (end_date IS NULL OR end_date >= #{executeDate})
          AND (max_executions = -1 OR executed_count < max_executions)
        ORDER BY create_time ASC
        """)
    @Results(value = {
        @Result(property = "tags", column = "tags", 
                typeHandler = xyz.ersut.message.typehandler.JsonStringListTypeHandler.class)
    })
    List<ScheduledTask> selectTasksForDate(@Param("executeDate") LocalDate executeDate);
    
    /**
     * 软删除任务
     */
    @Update("UPDATE scheduled_task SET delete_time = #{deleteTime}, status = 'deleted' WHERE id = #{id} AND user_id = #{userId}")
    int softDelete(@Param("id") Long id, @Param("userId") Long userId, @Param("deleteTime") LocalDateTime deleteTime);
    
    /**
     * 更新任务状态
     */
    @Update("UPDATE scheduled_task SET status = #{status}, update_time = #{updateTime} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("updateTime") LocalDateTime updateTime);
    
    /**
     * 增加执行次数
     */
    @Update("UPDATE scheduled_task SET executed_count = executed_count + 1, update_time = #{updateTime} WHERE id = #{id}")
    int incrementExecutedCount(@Param("id") Long id, @Param("updateTime") LocalDateTime updateTime);
    
    /**
     * 批量更新任务状态
     */
    @Update("""
        <script>
        UPDATE scheduled_task SET status = #{status}, update_time = #{updateTime} 
        WHERE user_id = #{userId} AND id IN
        <foreach collection="taskIds" item="taskId" open="(" close=")" separator=",">
            #{taskId}
        </foreach>
        </script>
        """)
    int batchUpdateStatus(@Param("userId") Long userId, 
                         @Param("taskIds") List<Long> taskIds, 
                         @Param("status") String status, 
                         @Param("updateTime") LocalDateTime updateTime);
    
    /**
     * 统计用户任务数量
     */
    @Select("""
        <script>
        SELECT COUNT(*) FROM scheduled_task 
        WHERE user_id = #{userId} AND delete_time IS NULL
        <if test="status != null and status != ''">
            AND status = #{status}
        </if>
        </script>
        """)
    Long countByUserId(@Param("userId") Long userId, @Param("status") String status);
}