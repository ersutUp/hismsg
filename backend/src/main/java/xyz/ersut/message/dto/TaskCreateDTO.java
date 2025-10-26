package xyz.ersut.message.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * 定时任务创建请求DTO
 *
 * @author ersut
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskCreateDTO {
    
    @NotBlank(message = "任务名称不能为空")
    private String taskName;
    
    private String description;
    
    @NotBlank(message = "通知标题不能为空")
    private String messageTitle;
    
    @NotBlank(message = "通知内容不能为空")
    private String messageContent;
    
    private String messageUrl;
    
    private List<String> tags;
    
    @NotBlank(message = "调度类型不能为空")
    private String scheduleType;
    
    @NotBlank(message = "Cron表达式不能为空")
    private String cronExpression;

    private String timezone = "Asia/Shanghai";

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer maxExecutions = -1;
}