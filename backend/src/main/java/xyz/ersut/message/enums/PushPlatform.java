package xyz.ersut.message.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 推送平台枚举
 * 
 * @author ersut
 */
@Getter
@AllArgsConstructor
public enum PushPlatform {
    
    /**
     * Bark推送
     */
    BARK("bark", "Bark", "https://bark.day.app"),
    
    /**
     * 邮箱推送
     */
    EMAIL("email", "邮箱", ""),
    
    /**
     * WxPusher推送
     */
    WXPUSHER("wxpusher", "WxPusher", "https://wxpusher.zjiecode.com"),
    
    /**
     * PushMe推送
     */
    PUSHME("pushme", "PushMe", "https://push.i-i.me");
    
    private final String code;
    private final String name;
    private final String url;
    
    /**
     * 根据code获取枚举
     */
    public static PushPlatform fromCode(String code) {
        for (PushPlatform platform : PushPlatform.values()) {
            if (platform.getCode().equals(code)) {
                return platform;
            }
        }
        return null;
    }
}