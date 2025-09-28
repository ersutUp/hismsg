package xyz.ersut.message.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.baomidou.dynamic.datasource.annotation.DS;
import cn.hutool.core.util.StrUtil;
import xyz.ersut.message.dto.UserPushConfigDto;
import xyz.ersut.message.entity.UserPushConfig;
import xyz.ersut.message.enums.PushPlatform;
import xyz.ersut.message.mapper.UserPushConfigMapper;
import xyz.ersut.message.service.UserPushConfigService;

import java.util.List;

/**
 * 用户推送配置服务实现类
 * 
 * @author ersut
 */
@Slf4j
@Service
@DS("mysql")
@RequiredArgsConstructor
public class UserPushConfigServiceImpl implements UserPushConfigService {
    
    private final UserPushConfigMapper pushConfigMapper;
    
    @Override
    public List<UserPushConfig> getByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return pushConfigMapper.selectByUserId(userId);
    }
    
    @Override
    public List<UserPushConfig> getByUserIdAndPlatform(Long userId, String platform) {
        if (userId == null || StrUtil.isBlank(platform)) {
            return null;
        }
        return pushConfigMapper.selectByUserIdAndPlatform(userId, platform);
    }
    
    @Override
    public List<UserPushConfig> getEnabledByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return pushConfigMapper.selectEnabledByUserId(userId);
    }
    
    @Override
    public UserPushConfig getById(Long id) {
        if (id == null) {
            return null;
        }
        return pushConfigMapper.selectById(id);
    }
    
    @Override
    public boolean save(Long userId, UserPushConfigDto configDto) {
        if (userId == null || configDto == null) {
            return false;
        }
        
        // 验证推送平台
        PushPlatform platform = PushPlatform.fromCode(configDto.getPlatform());
        if (platform == null) {
            throw new RuntimeException("不支持的推送平台");
        }
        
        // 构建实体对象
        UserPushConfig config = new UserPushConfig();
        config.setUserId(userId);
        config.setPlatform(configDto.getPlatform());
        config.setConfigName(configDto.getConfigName());
        config.setConfigData(convertDtoToConfigData(configDto));
        config.setIsEnabled(configDto.getIsEnabled());
        config.setSortOrder(configDto.getSortOrder() != null ? configDto.getSortOrder() : 0);
        config.setRemark(configDto.getRemark());
        
        return pushConfigMapper.insert(config) > 0;
    }
    
    @Override
    public boolean update(Long userId, UserPushConfigDto configDto) {
        if (userId == null || configDto == null || configDto.getId() == null) {
            return false;
        }
        
        // 检查配置是否属于当前用户
        UserPushConfig existConfig = getById(configDto.getId());
        if (existConfig == null || !existConfig.getUserId().equals(userId)) {
            throw new RuntimeException("配置不存在或无权限操作");
        }
        
        // 验证推送平台
        PushPlatform platform = PushPlatform.fromCode(configDto.getPlatform());
        if (platform == null) {
            throw new RuntimeException("不支持的推送平台");
        }
        
        // 更新配置
        UserPushConfig config = new UserPushConfig();
        config.setId(configDto.getId());
        config.setConfigName(configDto.getConfigName());
        config.setConfigData(convertDtoToConfigData(configDto));
        config.setIsEnabled(configDto.getIsEnabled());
        config.setSortOrder(configDto.getSortOrder());
        config.setRemark(configDto.getRemark());
        
        return pushConfigMapper.updateById(config) > 0;
    }
    
    @Override
    public boolean deleteById(Long userId, Long id) {
        if (userId == null || id == null) {
            return false;
        }
        
        // 检查配置是否属于当前用户
        UserPushConfig existConfig = getById(id);
        if (existConfig == null || !existConfig.getUserId().equals(userId)) {
            throw new RuntimeException("配置不存在或无权限操作");
        }
        
        return pushConfigMapper.deleteById(id) > 0;
    }
    
    @Override
    public boolean toggleEnabled(Long userId, Long id, boolean enabled) {
        if (userId == null || id == null) {
            return false;
        }
        
        // 检查配置是否属于当前用户
        UserPushConfig existConfig = getById(id);
        if (existConfig == null || !existConfig.getUserId().equals(userId)) {
            throw new RuntimeException("配置不存在或无权限操作");
        }
        
        UserPushConfig config = new UserPushConfig();
        config.setId(id);
        config.setIsEnabled(enabled ? 1 : 0);
        
        return pushConfigMapper.updateById(config) > 0;
    }
    
    @Override
    public String convertDtoToConfigData(UserPushConfigDto configDto) {
        if (configDto == null) {
            return "{}";
        }
        
        String platform = configDto.getPlatform();
        Object configData = null;
        
        switch (platform) {
            case "bark":
                configData = configDto.getBark();
                break;
            case "email":
                configData = configDto.getEmail();
                break;
            case "wxpusher":
                configData = configDto.getWxpusher();
                break;
            case "pushme":
                configData = configDto.getPushme();
                break;
            default:
                return "{}";
        }
        
        return configData != null ? JSON.toJSONString(configData) : "{}";
    }
    
    @Override
    public UserPushConfigDto convertConfigDataToDto(String platform, String configData) {
        UserPushConfigDto dto = new UserPushConfigDto();
        dto.setPlatform(platform);
        
        if (StrUtil.isBlank(configData) || "{}".equals(configData)) {
            return dto;
        }
        
        try {
            switch (platform) {
                case "bark":
                    UserPushConfigDto.BarkConfig barkConfig = JSON.parseObject(configData, UserPushConfigDto.BarkConfig.class);
                    dto.setBark(barkConfig);
                    break;
                case "email":
                    UserPushConfigDto.EmailConfig emailConfig = JSON.parseObject(configData, UserPushConfigDto.EmailConfig.class);
                    dto.setEmail(emailConfig);
                    break;
                case "wxpusher":
                    UserPushConfigDto.WxPusherConfig wxpusherConfig = JSON.parseObject(configData, UserPushConfigDto.WxPusherConfig.class);
                    dto.setWxpusher(wxpusherConfig);
                    break;
                case "pushme":
                    UserPushConfigDto.PushMeConfig pushmeConfig = JSON.parseObject(configData, UserPushConfigDto.PushMeConfig.class);
                    dto.setPushme(pushmeConfig);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.warn("解析配置数据失败: {}", e.getMessage());
        }
        
        return dto;
    }
}