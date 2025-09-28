package xyz.ersut.message.service;

import xyz.ersut.message.dto.LoginRequest;
import xyz.ersut.message.dto.LoginResponse;
import xyz.ersut.message.entity.SysUser;

/**
 * 用户服务接口
 * 
 * @author ersut
 */
public interface SysUserService {
    
    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    LoginResponse login(LoginRequest loginRequest);
    
    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户信息
     */
    SysUser getUserByUsername(String username);
    
    /**
     * 根据用户ID查询用户
     * 
     * @param userId 用户ID
     * @return 用户信息
     */
    SysUser getUserById(Long userId);
    
    /**
     * 根据用户密钥查询用户
     * 
     * @param userKey 用户密钥
     * @return 用户信息
     */
    SysUser getUserByUserKey(String userKey);
    
    /**
     * 创建用户
     * 
     * @param user 用户信息
     * @return 创建结果
     */
    boolean createUser(SysUser user);
    
    /**
     * 更新用户信息
     * 
     * @param user 用户信息
     * @return 更新结果
     */
    boolean updateUser(SysUser user);
    
    /**
     * 验证密码
     * 
     * @param rawPassword 原始密码
     * @param encodedPassword 加密密码
     * @return 验证结果
     */
    boolean verifyPassword(String rawPassword, String encodedPassword);
    
    /**
     * 加密密码
     * 
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    String encodePassword(String rawPassword);
}