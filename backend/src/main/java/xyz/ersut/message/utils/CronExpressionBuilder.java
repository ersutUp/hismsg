package xyz.ersut.message.utils;

import cn.hutool.core.util.StrUtil;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * Cron表达式构建工具类
 * 
 * @author ersut
 */
public class CronExpressionBuilder {
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    /**
     * 构建一次性任务的Cron表达式
     * 
     * @param executeTime 执行时间
     * @return Cron表达式
     */
    public static String buildOnceCron(LocalDateTime executeTime) {
        if (executeTime == null) {
            throw new IllegalArgumentException("执行时间不能为空");
        }
        
        return String.format("0 %d %d %d %d ? %d",
                executeTime.getMinute(),
                executeTime.getHour(),
                executeTime.getDayOfMonth(),
                executeTime.getMonthValue(),
                executeTime.getYear());
    }
    
    /**
     * 构建每日任务的Cron表达式
     * 
     * @param time 执行时间
     * @param weekdays 指定工作日（可选），为空则表示每天
     * @return Cron表达式
     */
    public static String buildDailyCron(LocalTime time, Set<DayOfWeek> weekdays) {
        if (time == null) {
            throw new IllegalArgumentException("执行时间不能为空");
        }
        
        String dayOfWeek = "*";
        if (weekdays != null && !weekdays.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (DayOfWeek day : weekdays) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(getDayOfWeekValue(day));
            }
            dayOfWeek = sb.toString();
        }
        
        return String.format("0 %d %d * * %s",
                time.getMinute(),
                time.getHour(),
                dayOfWeek);
    }
    
    /**
     * 构建每周任务的Cron表达式
     * 
     * @param dayOfWeek 星期几
     * @param time 执行时间
     * @return Cron表达式
     */
    public static String buildWeeklyCron(DayOfWeek dayOfWeek, LocalTime time) {
        if (dayOfWeek == null || time == null) {
            throw new IllegalArgumentException("星期几和执行时间不能为空");
        }
        
        return String.format("0 %d %d ? * %s",
                time.getMinute(),
                time.getHour(),
                getDayOfWeekValue(dayOfWeek));
    }
    
    /**
     * 构建每月任务的Cron表达式
     * 
     * @param dayOfMonth 月份中的第几天（1-31）
     * @param time 执行时间
     * @return Cron表达式
     */
    public static String buildMonthlyCron(int dayOfMonth, LocalTime time) {
        if (dayOfMonth < 1 || dayOfMonth > 31) {
            throw new IllegalArgumentException("月份中的天数必须在1-31之间");
        }
        if (time == null) {
            throw new IllegalArgumentException("执行时间不能为空");
        }
        
        return String.format("0 %d %d %d * ?",
                time.getMinute(),
                time.getHour(),
                dayOfMonth);
    }
    
    /**
     * 验证Cron表达式格式是否正确
     * 
     * @param cronExpression Cron表达式
     * @return 是否有效
     */
    public static boolean isValidCronExpression(String cronExpression) {
        if (StrUtil.isBlank(cronExpression)) {
            return false;
        }

        try {
            String[] parts = cronExpression.trim().split("\\s+");
            // 支持6字段（标准）或7字段（带年份）格式
            if (parts.length != 6 && parts.length != 7) {
                return false;
            }

            // 这里可以添加更详细的验证逻辑
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 解析简化配置生成Cron表达式
     * 
     * @param scheduleType 调度类型
     * @param config 配置JSON字符串
     * @return Cron表达式
     */
    public static String buildCronFromConfig(String scheduleType, String config) {
        if (StrUtil.isBlank(scheduleType)) {
            throw new IllegalArgumentException("调度类型不能为空");
        }
        
        try {
            switch (scheduleType.toLowerCase()) {
                case "once":
                    return buildOnceFromConfig(config);
                case "daily":
                    return buildDailyFromConfig(config);
                case "weekly":
                    return buildWeeklyFromConfig(config);
                case "monthly":
                    return buildMonthlyFromConfig(config);
                case "custom":
                    return config; // 直接返回自定义的Cron表达式
                default:
                    throw new IllegalArgumentException("不支持的调度类型: " + scheduleType);
            }
        } catch (Exception e) {
            throw new RuntimeException("构建Cron表达式失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 生成人类可读的Cron表达式描述
     * 
     * @param cronExpression Cron表达式
     * @return 描述文本
     */
    public static String getCronDescription(String cronExpression) {
        if (StrUtil.isBlank(cronExpression)) {
            return "无效的Cron表达式";
        }
        
        try {
            String[] parts = cronExpression.trim().split("\\s+");
            if (parts.length != 6) {
                return "无效的Cron表达式格式";
            }
            
            String minute = parts[1];
            String hour = parts[2];
            String day = parts[3];
            String month = parts[4];
            String dayOfWeek = parts[5];
            String year = parts.length > 6 ? parts[6] : "*";
            
            StringBuilder desc = new StringBuilder();
            
            if (!"*".equals(year) && !"?".equals(year)) {
                desc.append(year).append("年");
            }
            
            if (!"*".equals(month)) {
                desc.append(month).append("月");
            }
            
            if (!"*".equals(day) && !"?".equals(day)) {
                desc.append(day).append("日");
            }
            
            if (!"*".equals(dayOfWeek) && !"?".equals(dayOfWeek)) {
                desc.append(" 星期").append(getDayOfWeekName(dayOfWeek));
            }
            
            desc.append(" ").append(hour).append(":").append(String.format("%02d", Integer.parseInt(minute)));
            
            return desc.toString();
        } catch (Exception e) {
            return "解析Cron表达式失败";
        }
    }
    
    // 私有辅助方法
    
    private static String buildOnceFromConfig(String config) {
        // 解析一次性任务配置，这里简化处理
        // 实际项目中应该使用JSON解析
        return config;
    }
    
    private static String buildDailyFromConfig(String config) {
        // 解析每日任务配置
        return config;
    }
    
    private static String buildWeeklyFromConfig(String config) {
        // 解析每周任务配置
        return config;
    }
    
    private static String buildMonthlyFromConfig(String config) {
        // 解析每月任务配置
        return config;
    }
    
    private static String getDayOfWeekValue(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY: return "MON";
            case TUESDAY: return "TUE";
            case WEDNESDAY: return "WED";
            case THURSDAY: return "THU";
            case FRIDAY: return "FRI";
            case SATURDAY: return "SAT";
            case SUNDAY: return "SUN";
            default: throw new IllegalArgumentException("无效的星期几: " + dayOfWeek);
        }
    }
    
    private static String getDayOfWeekName(String dayOfWeek) {
        switch (dayOfWeek.toUpperCase()) {
            case "MON": case "1": return "一";
            case "TUE": case "2": return "二";
            case "WED": case "3": return "三";
            case "THU": case "4": return "四";
            case "FRI": case "5": return "五";
            case "SAT": case "6": return "六";
            case "SUN": case "0": case "7": return "日";
            default: return dayOfWeek;
        }
    }
}