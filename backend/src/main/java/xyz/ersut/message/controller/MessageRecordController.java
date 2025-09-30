package xyz.ersut.message.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import xyz.ersut.message.dto.Result;
import xyz.ersut.message.entity.MessageRecord;
import xyz.ersut.message.entity.PushRecord;
import xyz.ersut.message.service.MessageRecordService;
import xyz.ersut.message.service.SysUserService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息记录控制器
 * 
 * @author ersut
 */
@Slf4j
@RestController
@RequestMapping("/api/message/record")
@RequiredArgsConstructor
@Tag(name = "消息记录管理", description = "消息记录的查询、详情、统计等操作")
public class MessageRecordController {
    
    private final MessageRecordService messageRecordService;
    private final SysUserService userService;
    
    /**
     * 分页查询消息记录
     * 
     * @param page 页码
     * @param size 页大小
     * @param messageType 消息类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param tags 消息标签
     * @param authentication 认证信息
     * @return 分页结果
     */
    @Operation(summary = "分页查询消息记录", description = "根据条件分页查询当前用户的消息记录")
    @GetMapping("/list")
    public Result<Page<MessageRecord>> list(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "页大小", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "消息类型", example = "notification") @RequestParam(required = false) String messageType,
            @Parameter(description = "开始时间", example = "2024-01-01 00:00:00") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间", example = "2024-12-31 23:59:59") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @Parameter(description = "消息标签", example = "urgent,system") @RequestParam(required = false) List<String> tags,
            @Parameter(hidden = true) Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            int offset = (page - 1) * size;
            
            // 查询消息记录
            Page<MessageRecord> pageRes = messageRecordService.getMessagesByCondition(
                userId, messageType, startTime, endTime, tags, offset, size);
//
//            pageRes.get
//            // 构建分页响应
//            Map<String, Object> response = new HashMap<>();
//            response.put("records", pageRes.getRecords());
//            response.put("total", pageRes.getTotal());
//            response.put("page", page);
//            response.put("size", size);
//            response.put("pages", pageRes.getPages());
            
            return Result.success(pageRes);
        } catch (Exception e) {
            log.error("查询消息记录失败: {}", e.getMessage());
            return Result.error("查询失败");
        }
    }
    
    /**
     * 根据ID查询消息记录详情
     * 
     * @param id 消息ID
     * @param authentication 认证信息
     * @return 消息记录详情
     */
    @Operation(summary = "查询消息记录详情", description = "根据消息ID查询消息记录的详细信息")
    @GetMapping("/{id}")
    public Result<MessageRecord> getById(
            @Parameter(description = "消息ID", example = "1") @PathVariable Long id,
            @Parameter(hidden = true) Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            MessageRecord record = messageRecordService.getMessageById(id);
            if (record == null) {
                return Result.error("消息记录不存在");
            }
            
            // 检查权限：只能查看自己的消息
            if (!record.getUserId().equals(userId)) {
                return Result.error("无权限查看此消息");
            }
            
            return Result.success(record);
        } catch (Exception e) {
            log.error("查询消息记录详情失败: {}", e.getMessage());
            return Result.error("查询失败");
        }
    }
    
    /**
     * 查询消息的推送记录
     * 
     * @param messageId 消息ID
     * @param authentication 认证信息
     * @return 推送记录列表
     */
    @Operation(summary = "查询消息推送记录", description = "查询指定消息的所有推送记录")
    @GetMapping("/{messageId}/push-records")
    public Result<List<PushRecord>> getPushRecords(
            @Parameter(description = "消息ID", example = "1") @PathVariable Long messageId,
            @Parameter(hidden = true) Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            // 先检查消息是否存在且属于当前用户
            MessageRecord record = messageRecordService.getMessageById(messageId);
            if (record == null) {
                return Result.error("消息记录不存在");
            }
            
            if (!record.getUserId().equals(userId)) {
                return Result.error("无权限查看此消息的推送记录");
            }
            
            List<PushRecord> pushRecords = messageRecordService.getPushRecordsByMessageId(messageId);
            return Result.success(pushRecords);
        } catch (Exception e) {
            log.error("查询推送记录失败: {}", e.getMessage());
            return Result.error("查询失败");
        }
    }
    
    /**
     * 统计消息数据
     * 
     * @param days 统计天数（默认7天）
     * @param authentication 认证信息
     * @return 统计数据
     */
    @Operation(summary = "统计消息数据", description = "统计指定天数内的消息数据，包括总数和按类型分组统计")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(
            @Parameter(description = "统计天数", example = "7") @RequestParam(defaultValue = "7") int days,
            @Parameter(hidden = true) Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(days);
            
            // 统计总消息数
            long totalCount = messageRecordService.countMessagesByCondition(userId, null, startTime, endTime);
            
            // 按类型统计
            Map<String, Long> typeStats = new HashMap<>();
            String[] messageTypes = {"notification", "alert", "system", "custom"};
            for (String type : messageTypes) {
                long count = messageRecordService.countMessagesByCondition(userId, type, startTime, endTime);
                typeStats.put(type, count);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalCount", totalCount);
            response.put("typeStats", typeStats);
            response.put("days", days);
            response.put("startTime", startTime);
            response.put("endTime", endTime);
            
            return Result.success(response);
        } catch (Exception e) {
            log.error("统计消息数据失败: {}", e.getMessage());
            return Result.error("统计失败");
        }
    }
    
    /**
     * 获取当前登录用户ID
     * 
     * @param authentication 认证信息
     * @return 用户ID
     */
    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        
        String username = authentication.getName();
        var user = userService.getUserByUsername(username);
        return user != null ? user.getId() : null;
    }
}