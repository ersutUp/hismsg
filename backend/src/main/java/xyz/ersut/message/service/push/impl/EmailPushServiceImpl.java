package xyz.ersut.message.service.push.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import cn.hutool.core.util.StrUtil;
import xyz.ersut.message.dto.UserPushConfigDto;
import xyz.ersut.message.entity.MessageRecord;
import xyz.ersut.message.entity.PushRecord;
import xyz.ersut.message.entity.UserPushConfig;
import xyz.ersut.message.service.MessageRecordService;
import xyz.ersut.message.service.UserPushConfigService;
import xyz.ersut.message.service.push.PushService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;

/**
 * 邮箱推送服务实现
 * 
 * @author ersut
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailPushServiceImpl implements PushService {
    
    private final MessageRecordService messageRecordService;
    private final UserPushConfigService userPushConfigService;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromMall;

    @Override
    public PushRecord pushMessage(MessageRecord messageRecord, UserPushConfig config) {
        PushRecord pushRecord = new PushRecord();
        pushRecord.setId(messageRecordService.generatePushRecordId());
        pushRecord.setMessageId(messageRecord.getId());
        pushRecord.setUserId(messageRecord.getUserId());
        pushRecord.setPlatform("email");
        pushRecord.setConfigName(config.getConfigName());
        pushRecord.setPushStatus(2); // 推送中
        pushRecord.setPushTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        pushRecord.setCreateTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        
        try {
            // 解析邮箱配置
            UserPushConfigDto configDto = userPushConfigService.convertConfigDataToDto("email", config.getConfigData());
            UserPushConfigDto.EmailConfig emailConfig = configDto.getEmail();
            
            if (emailConfig == null || StrUtil.isBlank(emailConfig.getToEmail())) {
                throw new RuntimeException("收件人邮箱未配置");
            }
            
            // 构建邮件
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromMall);
            message.setTo(emailConfig.getToEmail());
            
            // 构建邮件主题
            String subject = messageRecord.getTitle();
            if (StrUtil.isNotBlank(emailConfig.getSubjectPrefix())) {
                subject = emailConfig.getSubjectPrefix() + " " + subject;
            }
            message.setSubject(subject);
            
            // 构建邮件内容
            StringBuilder contentBuilder = new StringBuilder();
            contentBuilder.append("消息内容：\n").append(messageRecord.getContent()).append("\n\n");
            
            if (StrUtil.isNotBlank(messageRecord.getUrl())) {
                contentBuilder.append("相关链接：").append(messageRecord.getUrl()).append("\n");
            }
            
            contentBuilder.append("消息类型：").append(getMessageTypeText(messageRecord.getMessageType())).append("\n");
            contentBuilder.append("消息级别：").append(getLevelText(messageRecord.getLevel())).append("\n");
            
            if (StrUtil.isNotBlank(messageRecord.getSource())) {
                contentBuilder.append("数据来源：").append(messageRecord.getSource()).append("\n");
            }
            
            contentBuilder.append("hismsg转发时间：").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            message.setText(contentBuilder.toString());
            
            pushRecord.setRequestData(String.format("To: %s, Subject: %s", 
                maskEmail(emailConfig.getToEmail()), subject));
            
            // 发送邮件
            mailSender.send(message);
            
            pushRecord.setPushStatus(1); // 推送成功
            pushRecord.setResponseData("邮件发送成功");
            log.info("邮箱推送成功: messageId={}, toEmail={}", messageRecord.getId(), 
                maskEmail(emailConfig.getToEmail()));
            
        } catch (Exception e) {
            pushRecord.setPushStatus(0); // 推送失败
            pushRecord.setErrorMessage(e.getMessage());
            log.error("邮箱推送异常: messageId={}, error={}", messageRecord.getId(), e.getMessage(), e);
        }
        
        return pushRecord;
    }
    
    @Override
    public boolean testConfig(UserPushConfig config) {
        try {
            // 解析配置
            UserPushConfigDto configDto = userPushConfigService.convertConfigDataToDto("email", config.getConfigData());
            UserPushConfigDto.EmailConfig emailConfig = configDto.getEmail();
            
            if (emailConfig == null || StrUtil.isBlank(emailConfig.getToEmail())) {
                return false;
            }
            
            // 发送测试邮件
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromMall);
            message.setTo(emailConfig.getToEmail());
            
            String subject = "配置测试";
            if (StrUtil.isNotBlank(emailConfig.getSubjectPrefix())) {
                subject = emailConfig.getSubjectPrefix() + " " + subject;
            }
            message.setSubject(subject);
            message.setText("这是一条测试邮件，您的邮箱推送配置正常！\n\n测试时间：" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            mailSender.send(message);
            
            log.info("邮箱配置测试成功: toEmail={}", maskEmail(emailConfig.getToEmail()));
            return true;
            
        } catch (Exception e) {
            log.error("邮箱配置测试异常: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public String getPlatformCode() {
        return "email";
    }
    
    /**
     * 获取消息类型文本
     */
    private String getMessageTypeText(String messageType) {
        return switch (messageType) {
            case "notification" -> "通知";
            case "alert" -> "告警";
            case "system" -> "系统";
            case "custom" -> "自定义";
            default -> "未知";
        };
    }
    
    /**
     * 获取级别文本
     */
    private String getLevelText(String level) {
        return switch (level) {
            case "low" -> "低";
            case "normal" -> "普通";
            case "high" -> "高";
            case "critical" -> "紧急";
            default -> "普通";
        };
    }
    
    /**
     * 掩码邮箱地址，用于日志输出
     */
    private String maskEmail(String email) {
        if (StrUtil.isBlank(email) || !email.contains("@")) {
            return "****";
        }
        
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];
        
        if (localPart.length() <= 2) {
            return "**@" + domain;
        }
        
        return localPart.substring(0, 2) + "****@" + domain;
    }
}