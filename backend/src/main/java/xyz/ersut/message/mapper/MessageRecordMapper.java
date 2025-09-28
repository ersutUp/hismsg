package xyz.ersut.message.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import xyz.ersut.message.entity.MessageRecord;

/**
 * 消息记录Mapper - ClickHouse
 * 
 * @author ersut
 */
@Mapper
@DS("clickhouse")
public interface MessageRecordMapper extends BaseMapper<MessageRecord> {
    
    @Results(id = "messageRecordResultMap", value = {
        @Result(property = "tags", column = "tags", 
                typeHandler = xyz.ersut.message.typehandler.StringListTypeHandler.class),
        @Result(property = "pushedPlatforms", column = "pushed_platforms", 
                typeHandler = xyz.ersut.message.typehandler.StringListTypeHandler.class)
    })
    @Select("SELECT * FROM message_record WHERE id = #{id} LIMIT 1")
    MessageRecord selectById(@Param("id") Long id);

    @Insert("""
        INSERT INTO message_record (
            id, user_id, user_code, message_type, title, subtitle, content, group,
            url, source, level, tags, extra_data, status,
            pushed_platforms, push_success_count, push_fail_count,
            create_time, update_time
        ) VALUES (
            #{id}, #{userId}, #{userCode}, #{messageType}, #{title}, #{subtitle}, #{content}, #{group},
            #{url}, #{source}, #{level},
            #{tags,typeHandler=xyz.ersut.message.typehandler.StringListTypeHandler},
            #{extraData}, #{status},
            #{pushedPlatforms,typeHandler=xyz.ersut.message.typehandler.StringListTypeHandler},
            #{pushSuccessCount}, #{pushFailCount}, #{createTime}, #{updateTime}
        )
        """)
    int insert(MessageRecord messageRecord);


    /**
     * 更新消息推送状态
     */
    @Update("""
        ALTER TABLE message_record 
        UPDATE 
            push_success_count = push_success_count + #{successIncrement},
            push_fail_count = push_fail_count + #{failIncrement},
            pushed_platforms = arrayConcat(pushed_platforms, #{platformArray}),
            update_time = now()
        WHERE id = #{messageId}
        """)
    int updatePushStatus(@Param("messageId") Long messageId, 
                        @Param("successIncrement") int successIncrement,
                        @Param("failIncrement") int failIncrement,
                        @Param("platformArray") String[] platformArray);
    
    /**
     * 更新消息推送统计信息
     */
    @Update("""
        ALTER TABLE message_record 
        UPDATE 
            pushed_platforms = #{pushedPlatforms,typeHandler=xyz.ersut.message.typehandler.StringListTypeHandler},
            push_success_count = #{pushSuccessCount},
            push_fail_count = #{pushFailCount},
            update_time = #{updateTime}
        WHERE id = #{id}
        """)
    int updatePushStats(@Param("id") Long id,
                       @Param("pushedPlatforms") java.util.List<String> pushedPlatforms,
                       @Param("pushSuccessCount") Integer pushSuccessCount,
                       @Param("pushFailCount") Integer pushFailCount,
                       @Param("updateTime") java.time.LocalDateTime updateTime);
    
    /**
     * 更新消息记录（排除 create_time）
     */
    @Update("""
        ALTER TABLE message_record 
        UPDATE 
            user_id = #{userId},
            user_code = #{userCode},
            message_type = #{messageType},
            title = #{title},
            subtitle = #{subtitle},
            content = #{content},
            group = #{group},
            url = #{url},
            source = #{source},
            level = #{level},
            tags = #{tags,typeHandler=xyz.ersut.message.typehandler.StringListTypeHandler},
            extra_data = #{extraData},
            status = #{status},
            pushed_platforms = #{pushedPlatforms,typeHandler=xyz.ersut.message.typehandler.StringListTypeHandler},
            push_success_count = #{pushSuccessCount},
            push_fail_count = #{pushFailCount},
            update_time = #{updateTime}
        WHERE id = #{id}
        """)
    int updateMessageRecord(@Param("id") Long id,
                           @Param("userId") Long userId,
                           @Param("userCode") String userCode,
                           @Param("messageType") String messageType,
                           @Param("title") String title,
                           @Param("subtitle") String subtitle,
                           @Param("content") String content,
                           @Param("group") String group,
                           @Param("url") String url,
                           @Param("source") String source,
                           @Param("level") String level,
                           @Param("tags") java.util.List<String> tags,
                           @Param("extraData") String extraData,
                           @Param("status") Integer status,
                           @Param("pushedPlatforms") java.util.List<String> pushedPlatforms,
                           @Param("pushSuccessCount") Integer pushSuccessCount,
                           @Param("pushFailCount") Integer pushFailCount,
                           @Param("updateTime") java.time.LocalDateTime updateTime);
}