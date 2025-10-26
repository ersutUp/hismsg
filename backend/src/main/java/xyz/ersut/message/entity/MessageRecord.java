package xyz.ersut.message.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息记录实体类（ClickHouse）
 * 
 * @author ersut
 */
@Data
public class MessageRecord {
    
    /**
     * 消息ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户编号
     */
    private String userCode;
    
    /**
     * 消息类型
     */
    private String messageType;
    
    /**
     * 消息标题
     */
    private String title;
    
    /**
     * 副标题
     */
    private String subtitle;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息分组
     */
    private String group;
    
    /**
     * 消息链接
     */
    private String url;
    
    /**
     * 数据来源
     */
    private String source;
    
    /**
     * 消息级别
     */
    private String level;
    
    /**
     * 消息标签（ClickHouse Array类型）
     */
    @TableField(typeHandler = xyz.ersut.message.typehandler.StringListTypeHandler.class)
    private List<String> tags;
    
    /**
     * 额外数据（JSON格式）
     */
    private String extraData;
    
    /**
     * 消息状态
     */
    private Integer status;
    
    /**
     * 已推送的平台列表（ClickHouse Array类型）
     */
    @TableField(typeHandler = xyz.ersut.message.typehandler.StringListTypeHandler.class)
    private List<String> pushedPlatforms;
    
    /**
     * 推送成功次数
     */
    private Integer pushSuccessCount;
    
    /**
     * 推送失败次数
     */
    private Integer pushFailCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}