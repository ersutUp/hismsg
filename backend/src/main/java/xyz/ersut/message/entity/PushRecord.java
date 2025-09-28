package xyz.ersut.message.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 推送记录实体类（ClickHouse）
 * 
 * @author ersut
 */
@Data
public class PushRecord {
    
    /**
     * 推送记录ID
     */
    private Long id;
    
    /**
     * 消息ID
     */
    private Long messageId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 推送平台
     */
    private String platform;
    
    /**
     * 配置名称
     */
    private String configName;
    
    /**
     * 推送状态（0=失败，1=成功，2=进行中）
     */
    private Integer pushStatus;
    
    /**
     * 请求数据
     */
    private String requestData;
    
    /**
     * 响应数据
     */
    private String responseData;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 重试次数
     */
    private Integer retryCount;
    
    /**
     * 推送时间
     */
    private LocalDateTime pushTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}