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
}