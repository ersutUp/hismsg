package xyz.ersut.message.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import xyz.ersut.message.dto.LoginRequest;
import xyz.ersut.message.dto.LoginResponse;
import xyz.ersut.message.dto.Result;
import xyz.ersut.message.service.SysUserService;

/**
 * 用户认证控制器
 * 
 * @author ersut
 */
@Tag(name = "用户认证", description = "用户登录登出相关接口")
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final SysUserService sysUserService;
    
    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    @Operation(summary = "用户登录", description = "用户通过用户名密码登录系统")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Validated @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = sysUserService.login(loginRequest);
            return Result.success("登录成功", response);
        } catch (Exception e) {
            log.error("用户登录失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 用户登出
     * 
     * @return 登出结果
     */
    @Operation(summary = "用户登出", description = "用户退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        // JWT Token是无状态的，登出只需要前端删除token即可
        return Result.success("登出成功", null);
    }
}