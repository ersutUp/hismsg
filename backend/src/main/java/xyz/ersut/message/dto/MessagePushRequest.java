package xyz.ersut.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import xyz.ersut.message.enums.MessageType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 消息推送请求DTO
 * 
 * @author ersut
 */
@Data
@Schema(description = "消息推送请求信息")
public class MessagePushRequest {
    
    /**
     * 用户ID
     */
    @Schema(description = "目标用户ID", example = "1")
    private Long userId;
    
    /**
     * 用户编号（外部系统标识）
     */
    @Schema(description = "用户编号（外部系统标识）", example = "user001")
    private String userCode;
    
    /**
     * 用户密钥（推送消息的用户标识）
     */
    @Schema(description = "用户密钥（推送消息的用户标识）", example = "abc123def456")
    private String userKey;
    
    /**
     * 消息类型
     */
    @Schema(description = "消息类型", example = "notification", allowableValues = {"notification", "alert", "system", "custom"})
    private String messageType;
    
    /**
     * 消息标题
     */
    @Schema(description = "消息标题", example = "系统通知")
    private String title;
    
    /**
     * 副标题
     */
    @Schema(description = "消息副标题", example = "订单状态更新")
    private String subtitle;
    
    /**
     * 消息内容
     */
    @Schema(description = "消息正文内容", example = "您有一条新的通知消息")
    private String content;
    
    /**
     * 消息分组
     */
    @Schema(description = "消息分组", example = "order")
    private String group;
    
    /**
     * 消息链接（可选）
     */
    @Schema(description = "消息链接URL", example = "https://example.com/detail/123")
    private String url;
    
    /**
     * 数据来源
     */
    @Schema(description = "消息来源系统", example = "order-system")
    private String source;
    
    /**
     * 消息级别（low、normal、high、critical）
     */
    @Schema(description = "消息级别", example = "normal", allowableValues = {"low", "normal", "high", "critical"})
    private String level;
    
    /**
     * 消息标签
     */
    @Schema(description = "消息标签列表", example = "[\"订单\", \"通知\"]")
    private List<String> tags;
    
    /**
     * 额外数据
     */
    @Schema(description = "额外数据字典", example = "{\"orderId\": \"12345\", \"amount\": 100.00}")
    private Map<String, Object> extraData;
    
    /**
     * 指定推送平台（可选，不指定则使用用户配置的所有启用平台）
     */
    @Schema(description = "指定推送平台列表", example = "[\"bark\", \"email\"]")
    private List<String> platforms;
    
    public MessageType getMessageTypeEnum() {
        return MessageType.fromCode(this.messageType);
    }
}