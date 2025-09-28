package xyz.ersut.message.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import xyz.ersut.message.entity.PushRecord;

import java.util.List;

/**
 * 推送记录Mapper - ClickHouse
 * 
 * @author ersut
 */
@Mapper
@DS("clickhouse")
public interface PushRecordMapper extends BaseMapper<PushRecord> {
    
    /**
     * 保存推送记录
     */
    @Insert("""
        INSERT INTO push_record (
            id, message_id, user_id, platform, config_name, push_status, 
            request_data, response_data, error_message, retry_count, push_time, create_time
        ) VALUES (
            #{id}, #{messageId}, #{userId}, #{platform}, #{configName}, #{pushStatus}, 
            #{requestData}, #{responseData}, #{errorMessage}, #{retryCount}, #{pushTime}, #{createTime}
        )
        """)
    int insert(PushRecord pushRecord);
    
    /**
     * 根据消息ID查询推送记录
     */
    @Select("SELECT * FROM push_record WHERE message_id = #{messageId} ORDER BY create_time DESC")
    List<PushRecord> selectByMessageId(@Param("messageId") Long messageId);
    
    /**
     * 根据用户ID查询推送记录
     */
    @Select("SELECT * FROM push_record WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<PushRecord> selectByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 查询失败的推送记录（用于重试）
     */
    @Select("SELECT * FROM push_record WHERE push_status = 0 AND retry_count < #{maxRetryCount} ORDER BY create_time ASC LIMIT #{limit}")
    List<PushRecord> selectFailedRecords(@Param("maxRetryCount") int maxRetryCount, @Param("limit") int limit);
}