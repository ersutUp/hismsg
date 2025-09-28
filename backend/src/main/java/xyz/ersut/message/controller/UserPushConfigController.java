package xyz.ersut.message.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import xyz.ersut.message.dto.Result;
import xyz.ersut.message.dto.UserPushConfigDto;
import xyz.ersut.message.entity.UserPushConfig;
import xyz.ersut.message.service.SysUserService;
import xyz.ersut.message.service.UserPushConfigService;
import xyz.ersut.message.utils.JwtUtils;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户推送配置控制器
 * 
 * @author ersut
 */
@Tag(name = "用户推送配置", description = "用户推送平台配置管理接口")
@Slf4j
@RestController
@RequestMapping("/api/user/push-config")
@RequiredArgsConstructor
public class UserPushConfigController {
    
    private final UserPushConfigService pushConfigService;
    private final SysUserService userService;
    private final JwtUtils jwtUtils;
    
    /**
     * 获取当前用户的推送配置列表
     * 
     * @param authentication 认证信息
     * @return 推送配置列表
     */
    @GetMapping("/list")
    public Result<List<UserPushConfigDto>> list(Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            List<UserPushConfig> configs = pushConfigService.getByUserId(userId);
            List<UserPushConfigDto> dtoList = new ArrayList<>();
            
            // 转换为DTO
            for (UserPushConfig config : configs) {
                UserPushConfigDto dto = pushConfigService.convertConfigDataToDto(config.getPlatform(), config.getConfigData());
                dto.setId(config.getId());
                dto.setPlatform(config.getPlatform());
                dto.setConfigName(config.getConfigName());
                dto.setIsEnabled(config.getIsEnabled());
                dto.setSortOrder(config.getSortOrder());
                dto.setRemark(config.getRemark());
                dtoList.add(dto);
            }
            
            return Result.success(dtoList);
        } catch (Exception e) {
            log.error("查询推送配置失败: {}", e.getMessage());
            return Result.error("查询失败");
        }
    }
    
    /**
     * 根据平台获取推送配置
     * 
     * @param platform 推送平台
     * @param authentication 认证信息
     * @return 推送配置列表
     */
    @GetMapping("/platform/{platform}")
    public Result<List<UserPushConfigDto>> getByPlatform(@PathVariable String platform, Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            List<UserPushConfig> configs = pushConfigService.getByUserIdAndPlatform(userId, platform);
            List<UserPushConfigDto> dtoList = new ArrayList<>();
            
            for (UserPushConfig config : configs) {
                UserPushConfigDto dto = pushConfigService.convertConfigDataToDto(config.getPlatform(), config.getConfigData());
                dto.setId(config.getId());
                dto.setPlatform(config.getPlatform());
                dto.setConfigName(config.getConfigName());
                dto.setIsEnabled(config.getIsEnabled());
                dto.setSortOrder(config.getSortOrder());
                dto.setRemark(config.getRemark());
                dtoList.add(dto);
            }
            
            return Result.success(dtoList);
        } catch (Exception e) {
            log.error("查询平台推送配置失败: {}", e.getMessage());
            return Result.error("查询失败");
        }
    }
    
    /**
     * 根据ID获取推送配置详情
     * 
     * @param id 配置ID
     * @param authentication 认证信息
     * @return 推送配置详情
     */
    @GetMapping("/{id}")
    public Result<UserPushConfigDto> getById(@PathVariable Long id, Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            UserPushConfig config = pushConfigService.getById(id);
            if (config == null || !config.getUserId().equals(userId)) {
                return Result.error("配置不存在");
            }
            
            UserPushConfigDto dto = pushConfigService.convertConfigDataToDto(config.getPlatform(), config.getConfigData());
            dto.setId(config.getId());
            dto.setPlatform(config.getPlatform());
            dto.setConfigName(config.getConfigName());
            dto.setIsEnabled(config.getIsEnabled());
            dto.setSortOrder(config.getSortOrder());
            dto.setRemark(config.getRemark());
            
            return Result.success(dto);
        } catch (Exception e) {
            log.error("查询推送配置详情失败: {}", e.getMessage());
            return Result.error("查询失败");
        }
    }
    
    /**
     * 新增推送配置
     * 
     * @param configDto 配置信息
     * @param authentication 认证信息
     * @return 操作结果
     */
    @PostMapping
    public Result<Void> save(@Valid @RequestBody UserPushConfigDto configDto, Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            boolean success = pushConfigService.save(userId, configDto);
            if (success) {
                return Result.success("新增成功", null);
            } else {
                return Result.error("新增失败");
            }
        } catch (Exception e) {
            log.error("新增推送配置失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 修改推送配置
     * 
     * @param configDto 配置信息
     * @param authentication 认证信息
     * @return 操作结果
     */
    @PutMapping
    public Result<Void> update(@Valid @RequestBody UserPushConfigDto configDto, Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            boolean success = pushConfigService.update(userId, configDto);
            if (success) {
                return Result.success("修改成功", null);
            } else {
                return Result.error("修改失败");
            }
        } catch (Exception e) {
            log.error("修改推送配置失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除推送配置
     * 
     * @param id 配置ID
     * @param authentication 认证信息
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            boolean success = pushConfigService.deleteById(userId, id);
            if (success) {
                return Result.success("删除成功", null);
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除推送配置失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 切换配置启用状态
     * 
     * @param id 配置ID
     * @param enabled 是否启用
     * @param authentication 认证信息
     * @return 操作结果
     */
    @PutMapping("/{id}/toggle")
    public Result<Void> toggleEnabled(@PathVariable Long id, @RequestParam boolean enabled, Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            boolean success = pushConfigService.toggleEnabled(userId, id, enabled);
            if (success) {
                return Result.success(enabled ? "启用成功" : "禁用成功", null);
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            log.error("切换配置状态失败: {}", e.getMessage());
            return Result.error(e.getMessage());
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