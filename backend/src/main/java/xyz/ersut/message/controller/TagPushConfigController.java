package xyz.ersut.message.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import xyz.ersut.message.dto.Result;
import xyz.ersut.message.entity.SysUser;
import xyz.ersut.message.entity.TagPushConfig;
import xyz.ersut.message.entity.UserPushConfig;
import xyz.ersut.message.service.SysUserService;
import xyz.ersut.message.service.TagPushConfigService;
import xyz.ersut.message.service.UserPushConfigService;

import java.util.List;

/**
 * 标签推送配置控制器
 * 
 * @author ersut
 */
@Tag(name = "标签推送配置", description = "标签推送配置相关接口")
@Slf4j
@RestController
@RequestMapping("/api/tag-push-config")
@RequiredArgsConstructor
public class TagPushConfigController {
    
    private final TagPushConfigService tagPushConfigService;
    private final SysUserService sysUserService;
    private final UserPushConfigService userPushConfigService;
    
    /**
     * 获取当前用户的标签推送配置列表
     * 
     * @param authentication 认证信息
     * @return 标签推送配置列表
     */
    @Operation(summary = "获取标签推送配置列表", description = "获取当前用户的所有标签推送配置")
    @GetMapping("/list")
    public Result<List<TagPushConfig>> getTagPushConfigs(Authentication authentication) {
        try {
            String username = authentication.getName();
            SysUser user = sysUserService.getUserByUsername(username);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            List<TagPushConfig> configs = tagPushConfigService.getByUserId(user.getId());
            return Result.success("获取标签推送配置成功", configs);
            
        } catch (Exception e) {
            log.error("获取标签推送配置失败: {}", e.getMessage(), e);
            return Result.error("获取标签推送配置失败");
        }
    }
    
    /**
     * 获取当前用户的所有标签名称
     * 
     * @param authentication 认证信息
     * @return 标签名称列表
     */
    @Operation(summary = "获取标签名称列表", description = "获取当前用户的所有标签名称")
    @GetMapping("/tag-names")
    public Result<List<String>> getTagNames(Authentication authentication) {
        try {
            String username = authentication.getName();
            SysUser user = sysUserService.getUserByUsername(username);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            List<String> tagNames = tagPushConfigService.getTagNamesByUserId(user.getId());
            return Result.success("获取标签名称成功", tagNames);
            
        } catch (Exception e) {
            log.error("获取标签名称失败: {}", e.getMessage(), e);
            return Result.error("获取标签名称失败");
        }
    }
    
    /**
     * 获取当前用户的推送配置列表（用于标签配置选择）
     * 
     * @param authentication 认证信息
     * @return 用户推送配置列表
     */
    @Operation(summary = "获取用户推送配置列表", description = "获取当前用户的所有推送配置，用于标签配置选择")
    @GetMapping("/user-push-configs")
    public Result<List<UserPushConfig>> getUserPushConfigs(Authentication authentication) {
        try {
            String username = authentication.getName();
            SysUser user = sysUserService.getUserByUsername(username);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            List<UserPushConfig> configs = userPushConfigService.getByUserId(user.getId());
            return Result.success("获取用户推送配置成功", configs);
            
        } catch (Exception e) {
            log.error("获取用户推送配置失败: {}", e.getMessage(), e);
            return Result.error("获取用户推送配置失败");
        }
    }
    
    /**
     * 保存或更新标签推送配置
     * 
     * @param tagPushConfig 标签推送配置
     * @param authentication 认证信息
     * @return 保存结果
     */
    @Operation(summary = "保存标签推送配置", description = "新增或更新标签推送配置")
    @PostMapping("/save")
    public Result<Void> saveTagPushConfig(@Validated @RequestBody TagPushConfig tagPushConfig,
                                        Authentication authentication) {
        try {
            String username = authentication.getName();
            SysUser user = sysUserService.getUserByUsername(username);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            tagPushConfig.setUserId(user.getId());
            
            boolean success = tagPushConfigService.saveOrUpdate(tagPushConfig);
            if (success) {
                return Result.success("保存标签推送配置成功", null);
            } else {
                return Result.error("保存标签推送配置失败");
            }
            
        } catch (Exception e) {
            log.error("保存标签推送配置失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除标签推送配置
     * 
     * @param id 配置ID
     * @param authentication 认证信息
     * @return 删除结果
     */
    @Operation(summary = "删除标签推送配置", description = "删除指定的标签推送配置")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTagPushConfig(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication.getName();
            SysUser user = sysUserService.getUserByUsername(username);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 验证配置是否属于当前用户
            TagPushConfig config = tagPushConfigService.getByUserId(user.getId()).stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
                
            if (config == null) {
                return Result.error("配置不存在或无权限删除");
            }
            
            boolean success = tagPushConfigService.deleteById(id);
            if (success) {
                return Result.success("删除标签推送配置成功", null);
            } else {
                return Result.error("删除标签推送配置失败");
            }
            
        } catch (Exception e) {
            log.error("删除标签推送配置失败: {}", e.getMessage(), e);
            return Result.error("删除标签推送配置失败");
        }
    }
}