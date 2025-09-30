package xyz.ersut.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import xyz.ersut.message.entity.TagPushConfig;

import java.util.List;
import java.util.Map;

/**
 * 标签推送配置Mapper接口
 * 
 * @author ersut
 */
@Mapper
public interface TagPushConfigMapper extends BaseMapper<TagPushConfig> {
    
    /**
     * 根据用户ID查询启用的标签推送配置
     * 
     * @param userId 用户ID
     * @return 标签推送配置列表
     */
    @Select("SELECT * FROM tag_push_config WHERE user_id = #{userId} AND is_enabled = 1 AND deleted = 0")
    List<TagPushConfig> selectEnabledByUserId(Long userId);
    
    /**
     * 根据用户ID和标签名查询配置
     * 
     * @param userId 用户ID
     * @param tagName 标签名
     * @return 标签推送配置
     */
    @Select("SELECT * FROM tag_push_config WHERE user_id = #{userId} AND tag_name = #{tagName} AND deleted = 0")
    TagPushConfig selectByUserIdAndTagName(Long userId, String tagName);
    
    /**
     * 根据用户ID查询所有标签名称
     * 
     * @param userId 用户ID
     * @return 标签名称列表
     */
    @Select("SELECT DISTINCT tag_name FROM tag_push_config WHERE user_id = #{userId} AND deleted = 0")
    List<String> selectTagNamesByUserId(Long userId);
    
    /**
     * 原生SQL查询，用于调试JSON字段
     * 
     * @param userId 用户ID
     * @return 原始数据Map列表
     */
    @Select("SELECT id, user_id, tag_name, push_config_ids, is_enabled, remark, create_time, update_time, deleted FROM tag_push_config WHERE user_id = #{userId} AND deleted = 0")
    List<Map<String, Object>> selectRawByUserId(Long userId);
}