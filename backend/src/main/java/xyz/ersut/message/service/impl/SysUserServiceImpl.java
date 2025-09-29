package xyz.ersut.message.service.impl;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.baomidou.dynamic.datasource.annotation.DS;
import xyz.ersut.message.dto.LoginRequest;
import xyz.ersut.message.dto.LoginResponse;
import xyz.ersut.message.entity.SysUser;
import xyz.ersut.message.mapper.SysUserMapper;
import xyz.ersut.message.service.SysUserService;
import xyz.ersut.message.utils.JwtUtils;
import xyz.ersut.message.utils.Md5Utils;

/**
 * 用户服务实现类
 * 
 * @author ersut
 */
@Slf4j
@Service
@DS("mysql")
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {
    
    private final SysUserMapper sysUserMapper;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        // 参数校验
        if (StrUtil.isBlank(loginRequest.getUsername()) || StrUtil.isBlank(loginRequest.getPassword())) {
            throw new RuntimeException("用户名或密码不能为空");
        }
        
        // 查询用户信息
        SysUser user = sysUserMapper.selectByUsername(loginRequest.getUsername());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("用户已被禁用");
        }
        
        // 验证密码
        if (!verifyPassword(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        // 生成JWT Token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        
        // 构建响应
        LoginResponse response = new LoginResponse();
        BeanUtils.copyProperties(user, response);
        response.setToken(token);
        response.setUserId(user.getId());
        response.setExpiresIn(86400L); // 24小时
        
        log.info("用户登录成功: {}", user.getUsername());
        return response;
    }
    
    @Override
    public SysUser getUserByUsername(String username) {
        if (StrUtil.isBlank(username)) {
            return null;
        }
        return sysUserMapper.selectByUsername(username);
    }
    
    @Override
    public SysUser getUserById(Long userId) {
        if (userId == null) {
            return null;
        }
        return sysUserMapper.selectById(userId);
    }
    
    @Override
    public SysUser getUserByUserKey(String userKey) {
        if (StrUtil.isBlank(userKey)) {
            return null;
        }
        return sysUserMapper.selectByUserKey(userKey);
    }
    
    @Override
    public boolean createUser(SysUser user) {
        if (user == null) {
            return false;
        }
        
        // 检查用户名是否已存在
        if (getUserByUsername(user.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 加密密码
        user.setPassword(encodePassword(user.getPassword()));
        
        // 设置默认值
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        
        return sysUserMapper.insert(user) > 0;
    }
    
    @Override
    public boolean updateUser(SysUser user) {
        if (user == null || user.getId() == null) {
            return false;
        }
        
        // 如果更新密码，需要加密
        if (StrUtil.isNotBlank(user.getPassword())) {
            user.setPassword(encodePassword(user.getPassword()));
        }
        
        return sysUserMapper.updateById(user) > 0;
    }
    
    @Override
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        if (StrUtil.isBlank(rawPassword) || StrUtil.isBlank(encodedPassword)) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    
    @Override
    public String encodePassword(String rawPassword) {
        if (StrUtil.isBlank(rawPassword)) {
            throw new RuntimeException("密码不能为空");
        }
        return passwordEncoder.encode(rawPassword);
    }
    
    @Override
    public boolean changePassword(String username, String currentPassword, String newPassword) {
        // 参数验证
        if (StrUtil.isBlank(username) || StrUtil.isBlank(currentPassword) || StrUtil.isBlank(newPassword)) {
            throw new RuntimeException("参数不能为空");
        }
        
        // 新密码长度验证
        if (newPassword.length() < 6 || newPassword.length() > 20) {
            throw new RuntimeException("新密码长度必须为6-20位字符");
        }
        
        // 查询用户信息
        SysUser user = sysUserMapper.selectByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 验证当前密码
        if (!verifyPassword(currentPassword, user.getPassword())) {
            throw new RuntimeException("当前密码错误");
        }
        
        // 检查新密码是否与当前密码相同
        if (verifyPassword(newPassword, user.getPassword())) {
            throw new RuntimeException("新密码不能与当前密码相同");
        }
        
        // 加密新密码
        String encodedNewPassword = encodePassword(newPassword);
        
        // 更新密码
        user.setPassword(encodedNewPassword);
        int result = sysUserMapper.updateById(user);
        
        if (result > 0) {
            log.info("用户{}密码修改成功", username);
            return true;
        } else {
            log.error("用户{}密码修改失败", username);
            return false;
        }
    }
    
    @Override
    public String resetUserKey(String username) {
        // 参数验证
        if (StrUtil.isBlank(username)) {
            throw new RuntimeException("用户名不能为空");
        }
        
        // 查询用户信息
        SysUser user = sysUserMapper.selectByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 生成新的用户密钥，确保全表唯一
        String newUserKey = generateUniqueUserKey(username);
        
        // 更新用户密钥
        user.setUserKey(newUserKey);
        int result = sysUserMapper.updateById(user);
        
        if (result > 0) {
            log.info("用户{}密钥重置成功，新密钥: {}", username, newUserKey);
            return newUserKey;
        } else {
            log.error("用户{}密钥重置失败", username);
            return null;
        }
    }
    
    @Override
    public boolean isUserKeyExists(String userKey) {
        if (StrUtil.isBlank(userKey)) {
            return false;
        }
        SysUser user = sysUserMapper.selectByUserKey(userKey);
        return user != null;
    }
    
    /**
     * 生成唯一的用户密钥
     * 
     * @param username 用户名
     * @return 唯一的用户密钥
     */
    private String generateUniqueUserKey(String username) {
        String newUserKey;
        int attempts = 0;
        int maxAttempts = 10; // 最大尝试次数
        
        do {
            // 生成新密钥
            String source = username + System.currentTimeMillis() + (attempts > 0 ? "_" + attempts : "");
            newUserKey = Md5Utils.md5Short(source);
            attempts++;
            
            // 防止无限循环
            if (attempts >= maxAttempts) {
                throw new RuntimeException("生成唯一用户密钥失败，请稍后重试");
            }
            
            // 短暂延迟确保时间戳不同
            if (attempts > 1) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("生成用户密钥被中断");
                }
            }
            
        } while (isUserKeyExists(newUserKey));
        
        log.debug("生成唯一用户密钥成功，尝试次数: {}, 新密钥: {}", attempts, newUserKey);
        return newUserKey;
    }
}