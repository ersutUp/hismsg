package xyz.ersut.message.dto;

import lombok.Data;

/**
 * 定时任务查询请求DTO
 * 
 * @author ersut
 */
@Data
public class TaskQueryDTO {
    
    private int page = 1;
    
    private int size = 20;
    
    private String status;
    
    private String scheduleType;
    
    private String keyword;
}