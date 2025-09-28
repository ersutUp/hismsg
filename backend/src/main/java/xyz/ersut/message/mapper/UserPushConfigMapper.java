package xyz.ersut.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import xyz.ersut.message.entity.UserPushConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户推送配置Mapper接口
 * 
 * @author ersut
 */
@Mapper
public interface UserPushConfigMapper extends BaseMapper<UserPushConfig> {
    
    /**
     * 根据用户ID查询推送配置
     * 
     * @param userId 用户ID
     * @return 推送配置列表
     */
    @Select("SELECT * FROM user_push_config WHERE user_id = #{userId} AND deleted = 0 ORDER BY sort_order ASC")
    List<UserPushConfig> selectByUserId(Long userId);
    
    /**
     * 根据用户ID和平台查询推送配置
     * 
     * @param userId 用户ID
     * @param platform 推送平台
     * @return 推送配置列表
     */
    @Select("SELECT * FROM user_push_config WHERE user_id = #{userId} AND platform = #{platform} AND deleted = 0 ORDER BY sort_order ASC")
    List<UserPushConfig> selectByUserIdAndPlatform(Long userId, String platform);
    
    /**
     * 根据用户ID查询启用的推送配置
     * 
     * @param userId 用户ID
     * @return 启用的推送配置列表
     */
    @Select("SELECT * FROM user_push_config WHERE user_id = #{userId} AND is_enabled = 1 AND deleted = 0 ORDER BY sort_order ASC")
    List<UserPushConfig> selectEnabledByUserId(Long userId);
}