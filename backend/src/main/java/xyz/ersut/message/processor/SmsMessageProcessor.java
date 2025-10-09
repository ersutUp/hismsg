package xyz.ersut.message.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import cn.hutool.core.util.StrUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 短信消息处理器
 * 用于提取短信中的验证码和签名，并重新格式化消息内容
 * 
 * @author ersut
 */
@Slf4j
@Component
public class SmsMessageProcessor {
    
    /**
     * 验证码正则表达式模式
     * 匹配各种常见的验证码格式：4-8位数字/字母组合
     */
    private static final Pattern[] CODE_PATTERNS = {
        Pattern.compile("验证码[:：]?\\s*([A-Za-z0-9]{4,8})"),                    // 验证码: 123456
        Pattern.compile("验证码为[:：]?\\s*([A-Za-z0-9]{4,8})"),                  // 验证码为: 123456
        Pattern.compile("码[:：]?\\s*([A-Za-z0-9]{4,8})"),                       // 码: 123456
        Pattern.compile("动态码[:：]?\\s*([A-Za-z0-9]{4,8})"),                   // 动态码: 123456
        Pattern.compile("校验码[:：]?\\s*([A-Za-z0-9]{4,8})"),                   // 校验码: 123456
        Pattern.compile("([A-Za-z0-9]{4,8})\\s*[,，]?\\s*[^。]*?有效"),            // 123456，30分钟内有效
        Pattern.compile("\\b([A-Za-z0-9]{6})\\b(?=.*验证)"),                     // 独立的6位验证码
        Pattern.compile("\\b([A-Za-z0-9]{4})\\b(?=.*验证)"),                     // 独立的4位验证码
        Pattern.compile("\\[([A-Za-z0-9]{4,8})\\]"),                            // [123456]
        Pattern.compile("【([A-Za-z0-9]{4,8})】")                                // 【123456】
    };
    
    /**
     * 签名正则表达式模式
     * 匹配常见的短信签名格式
     */
    private static final Pattern[] SIGNATURE_PATTERNS = {
        Pattern.compile("^\\[([^\\]]+)\\]"),                                     // 开头的[签名]
        Pattern.compile("^【([^】]+)】"),                                         // 开头的【签名】
        Pattern.compile("\\[([^\\]]+)\\]$"),                                     // 结尾的[签名]
        Pattern.compile("【([^】]+)】$")                                         // 结尾的【签名】
//        Pattern.compile("^([A-Za-z0-9\\u4e00-\\u9fa5]{2,10})[:：]"),            // 开头的签名: 内容
//        Pattern.compile("([A-Za-z0-9\\u4e00-\\u9fa5]{2,10})\\s*$")              // 结尾的签名
    };
    
    /**
     * 处理短信消息，提取验证码和签名并重新格式化
     * 
     * @param content 原始消息内容
     * @return 处理后的消息内容
     */
    public String processSmsMessage(String content) {
        if (StrUtil.isBlank(content)) {
            return null;
        }
        
        try {
            String verificationCode = extractVerificationCode(content);
            String signature = extractSignature(content);
            
            // 如果提取到验证码或签名，重新格式化消息
            if (StrUtil.isNotBlank(verificationCode)) {
                return formatSmsMessage(signature, verificationCode, content);
            }
            
            return null;
        } catch (Exception e) {
            log.error("处理短信消息失败: {}", e.getMessage(), e);
            return null; // 处理失败时返回原内容
        }
    }
    
    /**
     * 提取验证码
     * 
     * @param content 消息内容
     * @return 提取到的验证码，如果没有则返回null
     */
    private String extractVerificationCode(String content) {
        for (Pattern pattern : CODE_PATTERNS) {
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                String code = matcher.group(1);
                log.debug("提取到验证码: {}", code);
                return code;
            }
        }
        return null;
    }
    
    /**
     * 提取签名
     * 
     * @param content 消息内容
     * @return 提取到的签名，如果没有则返回null
     */
    private String extractSignature(String content) {
        for (Pattern pattern : SIGNATURE_PATTERNS) {
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                String signature = matcher.group(1);
                // 过滤掉一些不太可能是签名的内容
                if (isValidSignature(signature)) {
                    log.debug("提取到签名: {}", signature);
                    return signature;
                }
            }
        }
        return null;
    }
    
    /**
     * 验证签名是否有效
     * 
     * @param signature 签名内容
     * @return 是否为有效签名
     */
    private boolean isValidSignature(String signature) {
        if (StrUtil.isBlank(signature)) {
            return false;
        }
        
        // 长度检查
        if (signature.length() < 2 || signature.length() > 15) {
            return false;
        }
        
        // 排除纯数字（很可能是验证码）
        if (signature.matches("^\\d+$")) {
            return false;
        }
        
        // 排除一些常见的非签名内容
        String[] excludeWords = {"验证码", "动态码", "校验码", "有效期", "分钟", "小时", "请勿", "泄露"};
        for (String word : excludeWords) {
            if (signature.contains(word)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 格式化短信消息
     * 
     * @param signature 签名
     * @param verificationCode 验证码
     * @param originalContent 原始内容
     * @return 格式化后的消息
     */
    private String formatSmsMessage(String signature, String verificationCode, String originalContent) {
        StringBuilder formatted = new StringBuilder();
        
        // 添加签名（如果存在）
        if (StrUtil.isNotBlank(signature)) {
            formatted.append("【").append(signature).append("】");
        }
        
        // 添加验证码（如果存在）
        if (StrUtil.isNotBlank(verificationCode)) {
            if (StrUtil.isBlank(signature)){
                formatted.append("验证码");
            }
            formatted.append(": ").append(verificationCode);
        }
        
        // 添加分隔符
        if (!formatted.isEmpty()) {
            formatted.append("\n");
        }
        
        String result = formatted.toString();
        log.debug("格式化短信消息: 原始={}, 处理后={}", originalContent, result);
        
        return result;
    }
    
    /**
     * 检查消息是否为短信消息
     * 
     * @param tags 消息标签列表
     * @return 是否为短信消息
     */
    public boolean isSmsMessage(java.util.List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return false;
        }
        return tags.contains("短信");
    }

    public static void main(String[] args) {
        // 测试各种验证码格式
        String[] testCases = {
                "验证码：880891，短信验证码2分钟内有效，请勿泄漏给他人。【中国联通】",
                "【支付宝】验证码，请在15分钟内输入",
                "验证码为123456，30分钟内有效",
                "动态码: ABC123，请勿泄露",
                "校验码：789012，有效期10分钟",
                "您的验证码[456789]，请及时使用",
                "【微信】验证码【XYZ789】，请妥善保管",
                "888999，30分钟内有效，请勿泄露",
                "短信验证码1234，请尽快使用",
                "这是一条普通的短信消息，没有验证码或签名"
        };

        for (String testCase : testCases) {
            String result = new SmsMessageProcessor().processSmsMessage(testCase);
            System.out.println("原始: " + testCase);
            System.out.println("处理后: " + result);
            System.out.println("---");
        }
    }
}