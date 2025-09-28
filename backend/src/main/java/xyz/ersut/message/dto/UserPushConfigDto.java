package xyz.ersut.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 用户推送配置DTO
 * 
 * @author ersut
 */
@Data
@Schema(description = "用户推送配置信息")
public class UserPushConfigDto {
    
    /**
     * 配置ID（新增时为null）
     */
    @Schema(description = "配置ID，新增时为null", example = "1")
    private Long id;
    
    /**
     * 推送平台
     */
    @NotBlank(message = "推送平台不能为空")
    @Schema(description = "推送平台类型", example = "bark", allowableValues = {"bark", "email", "wxpusher", "pushme"})
    private String platform;
    
    /**
     * 配置名称
     */
    @NotBlank(message = "配置名称不能为空")
    @Schema(description = "配置名称", example = "我的Bark配置")
    private String configName;
    
    /**
     * 是否启用
     */
    @NotNull(message = "启用状态不能为空")
    @Schema(description = "是否启用，1-启用，0-禁用", example = "1", allowableValues = {"0", "1"})
    private Integer isEnabled;
    
    /**
     * 排序
     */
    @Schema(description = "排序序号，数字越小排序越靠前", example = "1")
    private Integer sortOrder;
    
    /**
     * 备注
     */
    @Schema(description = "配置备注", example = "用于接收重要通知")
    private String remark;
    
    // 平台特定配置字段
    
    /**
     * Bark推送配置
     */
    @Schema(description = "Bark推送平台的配置参数")
    private BarkConfig bark;
    
    /**
     * 邮箱推送配置
     */
    @Schema(description = "邮箱推送平台的配置参数")
    private EmailConfig email;
    
    /**
     * WxPusher推送配置
     */
    @Schema(description = "WxPusher推送平台的配置参数")
    private WxPusherConfig wxpusher;
    
    /**
     * PushMe推送配置
     */
    @Schema(description = "PushMe推送平台的配置参数")
    private PushMeConfig pushme;
    
    @Data
    @Schema(description = "Bark推送配置")
    public static class BarkConfig {
        /**
         * Bark设备Key
         */
        @NotBlank(message = "Bark设备Key不能为空")
        @Schema(description = "Bark设备密钥", example = "abcd1234efgh5678", requiredMode = Schema.RequiredMode.REQUIRED)
        private String deviceKey;
        
        /**
         * 推送服务器地址（可选，默认使用官方服务器）
         */
        @Schema(description = "推送服务器地址，默认使用官方服务器", example = "https://api.day.app")
        private String serverUrl;
        
        /**
         * 默认推送声音
         */
        @Schema(description = "默认推送声音", example = "birdsong")
        private String sound;
        
        /**
         * 默认推送分组
         */
        @Schema(description = "默认推送分组", example = "工作通知")
        private String group;
        
        /**
         * 默认推送图标
         */
        @Schema(description = "默认推送图标URL", example = "https://example.com/icon.png")
        private String icon;
    }
    
    @Data
    @Schema(description = "邮箱推送配置")
    public static class EmailConfig {
        /**
         * 收件人邮箱地址
         */
        @NotBlank(message = "收件人邮箱不能为空")
        @Schema(description = "收件人邮箱地址", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        private String toEmail;
        
        /**
         * 邮件主题前缀
         */
        @Schema(description = "邮件主题前缀", example = "[系统通知]")
        private String subjectPrefix;
    }
    
    @Data
    @Schema(description = "WxPusher推送配置")
    public static class WxPusherConfig {
        /**
         * WxPusher应用Token
         */
        @NotBlank(message = "WxPusher应用Token不能为空")
        @Schema(description = "WxPusher应用Token", example = "AT_abcd1234efgh5678", requiredMode = Schema.RequiredMode.REQUIRED)
        private String appToken;
        
        /**
         * 用户UID（可选，与topicId二选一）
         */
        @Schema(description = "用户UID，与topicId二选一", example = "UID_abcd1234")
        private String uid;
        
        /**
         * 主题ID（可选，与uid二选一）
         */
        @Schema(description = "主题ID，与uid二选一", example = "123")
        private String topicId;
        
        /**
         * 消息摘要长度
         */
        @Schema(description = "消息摘要长度", example = "100")
        private Integer summaryLength;
        
        /**
         * 内容类型（1=文字，2=html，3=markdown）
         */
        @Schema(description = "内容类型", example = "1", allowableValues = {"1", "2", "3"}, 
                implementation = Integer.class,
                enumAsRef = true)
        private Integer contentType;
    }
    
    @Data
    @Schema(description = "PushMe推送配置")
    public static class PushMeConfig {
        /**
         * PushMe推送Key
         */
        @NotBlank(message = "PushMe推送Key不能为空")
        @Schema(description = "PushMe推送Key", example = "push_abcd1234efgh5678", requiredMode = Schema.RequiredMode.REQUIRED)
        private String pushKey;
        
        /**
         * 推送模板（可选）
         */
        @Schema(description = "推送模板", example = "html")
        private String template;
    }
}