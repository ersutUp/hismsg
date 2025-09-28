package xyz.ersut.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 通用响应结果类
 * 
 * @author ersut
 * @param <T> 响应数据类型
 */
@Data
@Schema(description = "通用响应结果")
public class Result<T> {
    
    /**
     * 状态码
     */
    @Schema(description = "HTTP状态码", example = "200")
    private Integer code;
    
    /**
     * 响应消息
     */
    @Schema(description = "响应消息", example = "操作成功")
    private String message;
    
    /**
     * 响应数据
     */
    @Schema(description = "响应数据")
    private T data;
    
    /**
     * 成功响应
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        return result;
    }
    
    /**
     * 成功响应，带数据
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = success();
        result.setData(data);
        return result;
    }
    
    /**
     * 成功响应，自定义消息
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }
    
    /**
     * 失败响应
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }
    
    /**
     * 失败响应，自定义状态码
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}