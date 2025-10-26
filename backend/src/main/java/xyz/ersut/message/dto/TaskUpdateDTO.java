package xyz.ersut.message.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 定时任务更新请求DTO
 * 
 * @author ersut
 */
@Data
public class TaskUpdateDTO {
    
    private String taskName;
    
    private String description;
    
    private String messageTitle;
    
    private String messageContent;
    
    private String messageUrl;
    
    private List<String> tags;
    
    private String scheduleType;
    
    private String cronExpression;
    
    private String timezone;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private Integer maxExecutions;
    
    private String status;
}