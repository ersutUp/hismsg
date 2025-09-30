package xyz.ersut.message.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import xyz.ersut.message.typehandler.ListLongTypeHandler;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 标签推送配置实体
 * 
 * @author ersut
 */
@Data
@TableName("tag_push_config")
public class TagPushConfig {
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 标签名称
     */
    @TableField("tag_name")
    private String tagName;
    
    /**
     * 推送配置ID列表（JSON格式存储用户具体的推送配置ID）
     */
    @TableField(value = "push_config_ids", typeHandler = ListLongTypeHandler.class)
    private List<Long> pushConfigIds;
    
    /**
     * 是否启用（1-启用，0-禁用）
     */
    @TableField("is_enabled")
    private Integer isEnabled;
    
    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 删除标志（0-未删除，1-已删除）
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}