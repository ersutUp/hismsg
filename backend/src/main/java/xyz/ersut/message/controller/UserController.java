package xyz.ersut.message.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
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
}