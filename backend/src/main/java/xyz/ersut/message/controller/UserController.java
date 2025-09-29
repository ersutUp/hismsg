package xyz.ersut.message.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import xyz.ersut.message.dto.ChangePasswordRequest;
import xyz.ersut.message.dto.Result;
import xyz.ersut.message.entity.SysUser;
import xyz.ersut.message.service.SysUserService;

/**
 * 用户信息控制器
 * 
 * @author ersut
 */
@Tag(name = "用户信息", description = "用户信息相关接口")
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    
    private final SysUserService sysUserService;
    
    /**
     * 获取当前登录用户信息
     * 
     * @param authentication 认证信息
     * @return 用户信息
     */
    @Operation(summary = "获取当前登录用户信息", description = "根据JWT token获取当前登录用户的详细信息")
    @GetMapping("/current")
    public Result<SysUser> getCurrentUser(Authentication authentication) {
        try {
            // 从JWT token中获取用户名
            String username = authentication.getName();
            log.debug("获取用户信息: {}", username);
            
            // 查询用户信息
            SysUser user = sysUserService.getUserByUsername(username);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 清除敏感信息
            user.setPassword(null);
            
            return Result.success("获取用户信息成功", user);
        } catch (Exception e) {
            log.error("获取当前用户信息失败: {}", e.getMessage(), e);
            return Result.error("获取用户信息失败");
        }
    }
    
    /**
     * 修改密码
     * 
     * @param request 修改密码请求
     * @param authentication 认证信息
     * @return 修改结果
     */
    @Operation(summary = "修改用户密码", description = "用户修改自己的登录密码")
    @PostMapping("/change-password")
    public Result<Void> changePassword(@Validated @RequestBody ChangePasswordRequest request,
                                     Authentication authentication) {
        try {
            // 从JWT token中获取用户名
            String username = authentication.getName();
            log.info("用户{}请求修改密码", username);
            
            // 调用服务层修改密码
            boolean success = sysUserService.changePassword(username, request.getCurrentPassword(), request.getNewPassword());
            
            if (success) {
                log.info("用户{}密码修改成功", username);
                return Result.success("密码修改成功", null);
            } else {
                return Result.error("密码修改失败");
            }
            
        } catch (Exception e) {
            log.error("修改密码失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 重置用户密钥
     * 
     * @param authentication 认证信息
     * @return 新的用户密钥
     */
    @Operation(summary = "重置用户密钥", description = "重新生成用户的推送密钥")
    @PostMapping("/reset-user-key")
    public Result<String> resetUserKey(Authentication authentication) {
        try {
            // 从JWT token中获取用户名
            String username = authentication.getName();
            log.info("用户{}请求重置用户密钥", username);
            
            // 调用服务层重置用户密钥
            String newUserKey = sysUserService.resetUserKey(username);
            
            if (newUserKey != null) {
                log.info("用户{}密钥重置成功，新密钥: {}", username, newUserKey);
                return Result.success("用户密钥重置成功", newUserKey);
            } else {
                return Result.error("用户密钥重置失败");
            }
            
        } catch (Exception e) {
            log.error("重置用户密钥失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
}