package xyz.ersut.message.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息类型枚举
 * 
 * @author ersut
 */
@Getter
@AllArgsConstructor
public enum MessageType {
    
    /**
     * 通知类型消息
     */
    NOTIFICATION("notification", "通知"),
    
    /**
     * 告警类型消息
     */
    ALERT("alert", "告警"),
    
    /**
     * 系统类型消息
     */
    SYSTEM("system", "系统"),
    
    /**
     * 自定义类型消息
     */
    CUSTOM("custom", "自定义");
    
    private final String code;
    private final String description;
    
    /**
     * 根据code获取枚举
     */
    public static MessageType fromCode(String code) {
        for (MessageType type : MessageType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return NOTIFICATION; // 默认返回通知类型
    }
}