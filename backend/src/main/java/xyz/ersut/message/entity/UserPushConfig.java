package xyz.ersut.message.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户推送配置实体类
 * 
 * @author ersut
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_push_config")
public class UserPushConfig {
    
    /**
     * 配置ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 推送平台（bark、email、wxpusher、pushme）
     */
    private String platform;
    
    /**
     * 配置名称
     */
    private String configName;
    
    /**
     * 配置数据（JSON格式）
     */
    private String configData;
    
    /**
     * 是否启用（0=禁用，1=启用）
     */
    private Integer isEnabled;
    
    /**
     * 排序
     */
    private Integer sortOrder;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 删除标记（0=正常，1=删除）
     */
    @TableLogic
    private Integer deleted;
}