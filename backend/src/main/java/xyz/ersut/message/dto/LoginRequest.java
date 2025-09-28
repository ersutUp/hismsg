package xyz.ersut.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 用户登录请求DTO
 * 
 * @author ersut
 */
@Data
@Schema(description = "用户登录请求信息")
public class LoginRequest {
    
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户登录名", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Schema(description = "用户登录密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}