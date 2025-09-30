package xyz.ersut.message.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.ersut.message.entity.TagPushConfig;
import xyz.ersut.message.mapper.TagPushConfigMapper;
import xyz.ersut.message.service.TagPushConfigService;

import java.util.List;
import java.util.Map;

/**
 * 标签推送配置服务实现类
 * 
 * @author ersut
 */
@Slf4j
@Service
@DS("mysql")
@RequiredArgsConstructor
public class TagPushConfigServiceImpl implements TagPushConfigService {
    
    private final TagPushConfigMapper tagPushConfigMapper;
    
    @Override
    public List<TagPushConfig> getEnabledByUserId(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return tagPushConfigMapper.selectEnabledByUserId(userId);
    }
    
    @Override
    public TagPushConfig getByUserIdAndTagName(Long userId, String tagName) {
        if (userId == null || tagName == null) {
            return null;
        }
        return tagPushConfigMapper.selectByUserIdAndTagName(userId, tagName);
    }
    
    @Override
    public List<String> getTagNamesByUserId(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return tagPushConfigMapper.selectTagNamesByUserId(userId);
    }
    
    @Override
    public boolean saveOrUpdate(TagPushConfig tagPushConfig) {
        if (tagPushConfig == null) {
            return false;
        }
        
        try {
            if (tagPushConfig.getId() != null) {
                // 更新
                return tagPushConfigMapper.updateById(tagPushConfig) > 0;
            } else {
                // 新增
                return tagPushConfigMapper.insert(tagPushConfig) > 0;
            }
        } catch (Exception e) {
            log.error("保存标签推送配置失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public List<TagPushConfig> getByUserId(Long userId) {
        if (userId == null) {
            return List.of();
        }

        LambdaQueryWrapper<TagPushConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TagPushConfig::getUserId, userId)
               .orderByDesc(TagPushConfig::getCreateTime);

        return tagPushConfigMapper.selectList(wrapper);
    }
    
    @Override
    public boolean deleteById(Long id) {
        if (id == null) {
            return false;
        }
        
        try {
            return tagPushConfigMapper.deleteById(id) > 0;
        } catch (Exception e) {
            log.error("删除标签推送配置失败: id={}, error={}", id, e.getMessage(), e);
            return false;
        }
    }
}