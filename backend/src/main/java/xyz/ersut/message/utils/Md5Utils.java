package xyz.ersut.message.utils;

import cn.hutool.crypto.digest.DigestUtil;

/**
 * MD5工具类
 * 
 * @author ersut
 */
public class Md5Utils {
    
    /**
     * 计算字符串的MD5值
     * 
     * @param input 输入字符串
     * @return MD5值（32位小写）
     */
    public static String md5(String input) {
        if (input == null) {
            return null;
        }
        return DigestUtil.md5Hex(input);
    }
    
    /**
     * 计算字符串的MD5值并截取前16位
     * 
     * @param input 输入字符串
     * @return MD5值前16位（小写）
     */
    public static String md5Short(String input) {
        String fullMd5 = md5(input);
        return fullMd5 != null ? fullMd5.substring(0, 16) : null;
    }
    
    /**
     * 生成用户密钥
     * 规则：用户名+时间戳，然后计算16位MD5值
     * 
     * @param username 用户名
     * @return 用户密钥
     */
    public static String generateUserKey(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        
        String source = username + System.currentTimeMillis();
        return md5Short(source);
    }
}