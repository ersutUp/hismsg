package xyz.ersut.message.service;

import xyz.ersut.message.dto.UserPushConfigDto;
import xyz.ersut.message.entity.UserPushConfig;

import java.util.List;

/**
 * 用户推送配置服务接口
 * 
 * @author ersut
 */
public interface UserPushConfigService {
    
    /**
     * 根据用户ID查询推送配置
     * 
     * @param userId 用户ID
     * @return 推送配置列表
     */
    List<UserPushConfig> getByUserId(Long userId);
    
    /**
     * 根据用户ID和平台查询推送配置
     * 
     * @param userId 用户ID
     * @param platform 推送平台
     * @return 推送配置列表
     */
    List<UserPushConfig> getByUserIdAndPlatform(Long userId, String platform);
    
    /**
     * 根据用户ID查询启用的推送配置
     * 
     * @param userId 用户ID
     * @return 启用的推送配置列表
     */
    List<UserPushConfig> getEnabledByUserId(Long userId);
    
    /**
     * 根据ID查询推送配置
     * 
     * @param id 配置ID
     * @return 推送配置
     */
    UserPushConfig getById(Long id);
    
    /**
     * 新增推送配置
     * 
     * @param userId 用户ID
     * @param configDto 配置信息
     * @return 操作结果
     */
    boolean save(Long userId, UserPushConfigDto configDto);
    
    /**
     * 修改推送配置
     * 
     * @param userId 用户ID
     * @param configDto 配置信息
     * @return 操作结果
     */
    boolean update(Long userId, UserPushConfigDto configDto);
    
    /**
     * 删除推送配置
     * 
     * @param userId 用户ID
     * @param id 配置ID
     * @return 操作结果
     */
    boolean deleteById(Long userId, Long id);
    
    /**
     * 切换配置启用状态
     * 
     * @param userId 用户ID
     * @param id 配置ID
     * @param enabled 是否启用
     * @return 操作结果
     */
    boolean toggleEnabled(Long userId, Long id, boolean enabled);
    
    /**
     * 将DTO转换为配置数据JSON
     * 
     * @param configDto 配置DTO
     * @return JSON字符串
     */
    String convertDtoToConfigData(UserPushConfigDto configDto);
    
    /**
     * 将配置数据JSON转换为DTO
     * 
     * @param platform 推送平台
     * @param configData 配置数据JSON
     * @return 配置DTO
     */
    UserPushConfigDto convertConfigDataToDto(String platform, String configData);
}