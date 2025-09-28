package xyz.ersut.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import xyz.ersut.message.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 系统用户Mapper接口
 * 
 * @author ersut
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    
    /**
     * 根据用户名查询用户信息
     * 
     * @param username 用户名
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    SysUser selectByUsername(String username);
    
    /**
     * 根据邮箱查询用户信息
     * 
     * @param email 邮箱
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE email = #{email} AND deleted = 0")
    SysUser selectByEmail(String email);
    
    /**
     * 根据用户密钥查询用户信息
     * 
     * @param userKey 用户密钥
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE user_key = #{userKey} AND deleted = 0")
    SysUser selectByUserKey(String userKey);
}