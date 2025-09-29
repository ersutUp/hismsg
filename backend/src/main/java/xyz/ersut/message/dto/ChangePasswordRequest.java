package xyz.ersut.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 修改密码请求DTO
 * 
 * @author ersut
 */
@Data
@Schema(description = "修改密码请求信息")
public class ChangePasswordRequest {
    
    /**
     * 当前密码
     */
    @NotBlank(message = "当前密码不能为空")
    @Schema(description = "当前密码", example = "oldPassword123")
    private String currentPassword;
    
    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度为6-20位字符")
    @Schema(description = "新密码", example = "newPassword123")
    private String newPassword;
}