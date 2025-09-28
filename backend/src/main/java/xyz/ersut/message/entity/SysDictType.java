package xyz.ersut.message.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 字典类型实体类
 * 
 * @author ersut
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_dict_type")
public class SysDictType {
    
    /**
     * 字典类型ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 字典名称
     */
    private String dictName;
    
    /**
     * 字典类型
     */
    private String dictType;
    
    /**
     * 状态（0=禁用，1=启用）
     */
    private Integer status;
    
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