package xyz.ersut.message.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import xyz.ersut.message.entity.SysUser;
import xyz.ersut.message.service.SysUserService;

import java.util.ArrayList;

/**
 * 用户详情服务实现
 * 
 * @author ersut
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final SysUserService sysUserService;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 根据用户名查询用户信息
        SysUser sysUser = sysUserService.getUserByUsername(username);
        if (sysUser == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        
        // 检查用户状态
        if (sysUser.getStatus() == 0) {
            throw new UsernameNotFoundException("用户已被禁用: " + username);
        }
        
        // 构建UserDetails对象
        return User.builder()
                .username(sysUser.getUsername())
                .password(sysUser.getPassword())
                .authorities(new ArrayList<>()) // 暂时不设置权限
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(sysUser.getStatus() == 0)
                .build();
    }
}