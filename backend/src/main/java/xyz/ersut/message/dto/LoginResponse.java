package xyz.ersut.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户登录响应DTO
 * 
 * @author ersut
 */
@Data
@Schema(description = "用户登录响应信息")
public class LoginResponse {
    
    /**
     * JWT Token
     */
    @Schema(description = "JWT访问令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
    
    /**
     * 用户ID
     */
    @Schema(description = "用户唯一标识", example = "1")
    private Long userId;
    
    /**
     * 用户名
     */
    @Schema(description = "用户登录名", example = "admin")
    private String username;
    
    /**
     * 昵称
     */
    @Schema(description = "用户显示名称", example = "管理员")
    private String nickname;
    
    /**
     * 邮箱
     */
    @Schema(description = "用户邮箱地址", example = "admin@example.com")
    private String email;
    
    /**
     * 头像
     */
    @Schema(description = "用户头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;
    
    /**
     * 用户密钥
     */
    @Schema(description = "用户推送密钥", example = "abc123def456")
    private String userKey;
    
    /**
     * Token过期时间（秒）
     */
    @Schema(description = "Token过期时间（秒）", example = "3600")
    private Long expiresIn;
}