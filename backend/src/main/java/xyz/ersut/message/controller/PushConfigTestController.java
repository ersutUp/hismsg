package xyz.ersut.message.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import xyz.ersut.message.dto.Result;
import xyz.ersut.message.entity.UserPushConfig;
import xyz.ersut.message.service.SysUserService;
import xyz.ersut.message.service.UserPushConfigService;
import xyz.ersut.message.service.push.PushServiceManager;

/**
 * 推送配置测试控制器
 * 
 * @author ersut
 */
@Slf4j
@RestController
@RequestMapping("/api/user/push-config")
@RequiredArgsConstructor
@Tag(name = "推送配置测试", description = "推送配置的测试和平台管理")
public class PushConfigTestController {
    
    private final UserPushConfigService userPushConfigService;
    private final SysUserService userService;
    private final PushServiceManager pushServiceManager;
    
    /**
     * 测试推送配置
     * 
     * @param id 配置ID
     * @param authentication 认证信息
     * @return 测试结果
     */
    @Operation(summary = "测试推送配置", description = "对指定的推送配置发送测试消息")
    @PostMapping("/{id}/test")
    public Result<Void> testConfig(
            @Parameter(description = "推送配置ID", example = "1") @PathVariable Long id,
            @Parameter(hidden = true) Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            // 获取推送配置
            UserPushConfig config = userPushConfigService.getById(id);
            if (config == null || !config.getUserId().equals(userId)) {
                return Result.error("配置不存在或无权限操作");
            }
            
            // 执行测试
            boolean success = pushServiceManager.testPushConfig(config);
            
            if (success) {
                return Result.success("测试消息发送成功，请检查您的设备", null);
            } else {
                return Result.error("测试失败，请检查配置是否正确");
            }
            
        } catch (Exception e) {
            log.error("测试推送配置失败: configId={}, error={}", id, e.getMessage(), e);
            return Result.error("测试失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取支持的推送平台列表
     * 
     * @return 支持的平台列表
     */
    @Operation(summary = "获取支持的推送平台", description = "获取系统支持的所有推送平台列表")
    @GetMapping("/platforms")
    public Result<String[]> getSupportedPlatforms() {
        try {
            String[] platforms = pushServiceManager.getSupportedPlatforms();
            return Result.success(platforms);
        } catch (Exception e) {
            log.error("获取支持的平台列表失败: {}", e.getMessage(), e);
            return Result.error("获取失败");
        }
    }
    
    /**
     * 获取当前登录用户ID
     * 
     * @param authentication 认证信息
     * @return 用户ID
     */
    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        
        String username = authentication.getName();
        var user = userService.getUserByUsername(username);
        return user != null ? user.getId() : null;
    }
}