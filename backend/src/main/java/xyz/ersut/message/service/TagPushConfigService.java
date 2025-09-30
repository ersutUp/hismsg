package xyz.ersut.message.service;

import xyz.ersut.message.entity.TagPushConfig;

import java.util.List;

/**
 * 标签推送配置服务接口
 * 
 * @author ersut
 */
public interface TagPushConfigService {
    
    /**
     * 根据用户ID查询启用的标签推送配置
     * 
     * @param userId 用户ID
     * @return 标签推送配置列表
     */
    List<TagPushConfig> getEnabledByUserId(Long userId);
    
    /**
     * 根据用户ID和标签名查询配置
     * 
     * @param userId 用户ID
     * @param tagName 标签名
     * @return 标签推送配置
     */
    TagPushConfig getByUserIdAndTagName(Long userId, String tagName);
    
    /**
     * 根据用户ID查询所有标签名称
     * 
     * @param userId 用户ID
     * @return 标签名称列表
     */
    List<String> getTagNamesByUserId(Long userId);
    
    /**
     * 保存或更新标签推送配置
     * 
     * @param tagPushConfig 标签推送配置
     * @return 保存结果
     */
    boolean saveOrUpdate(TagPushConfig tagPushConfig);
    
    /**
     * 根据用户ID查询所有标签推送配置
     * 
     * @param userId 用户ID
     * @return 标签推送配置列表
     */
    List<TagPushConfig> getByUserId(Long userId);
    
    /**
     * 删除标签推送配置
     * 
     * @param id 配置ID
     * @return 删除结果
     */
    boolean deleteById(Long id);
}