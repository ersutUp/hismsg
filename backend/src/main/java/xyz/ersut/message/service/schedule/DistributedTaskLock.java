package xyz.ersut.message.service.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * 基于Redisson的分布式任务锁管理器
 * 
 * @author ersut
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedTaskLock {

    private final RedissonClient redissonClient;
    
    private static final String LOCK_KEY_PREFIX = "task_lock:";
    private static final int LOCK_WAIT_TIME = 1; // 等待获取锁的时间（秒）
    private static final int LOCK_LEASE_TIME = 300; // 锁自动释放时间（秒，5分钟）
    
    /**
     * 尝试获取任务执行锁
     * 
     * @param taskId 任务ID
     * @param executeTime 执行时间
     * @return 是否获取锁成功
     */
    public boolean tryLock(Long taskId, LocalDateTime executeTime) {
        String lockKey = buildLockKey(taskId, executeTime);
        
        try {
            RLock lock = redissonClient.getLock(lockKey);
            boolean acquired = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);
            
            if (acquired) {
                log.debug("获取任务锁成功: taskId={}, executeTime={}, lockKey={}", 
                    taskId, executeTime, lockKey);
            } else {
                log.debug("获取任务锁失败，任务可能已被其他节点执行: taskId={}, executeTime={}", 
                    taskId, executeTime);
            }
            
            return acquired;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取任务锁被中断: taskId={}, executeTime={}", taskId, executeTime);
            return false;
        } catch (Exception e) {
            log.error("获取任务锁异常: taskId={}, executeTime={}, error={}", 
                taskId, executeTime, e.getMessage());
            return false;
        }
    }
    
    /**
     * 释放任务执行锁
     * 
     * @param taskId 任务ID
     * @param executeTime 执行时间
     */
    public void releaseLock(Long taskId, LocalDateTime executeTime) {
        String lockKey = buildLockKey(taskId, executeTime);
        
        try {
            RLock lock = redissonClient.getLock(lockKey);
            
            // 只有当前线程持有锁时才能释放
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("释放任务锁成功: taskId={}, executeTime={}, lockKey={}", 
                    taskId, executeTime, lockKey);
            } else {
                log.debug("当前线程未持有锁，无需释放: taskId={}, executeTime={}", 
                    taskId, executeTime);
            }
            
        } catch (Exception e) {
            log.error("释放任务锁异常: taskId={}, executeTime={}, error={}", 
                taskId, executeTime, e.getMessage());
        }
    }
    
    /**
     * 检查任务是否已被锁定
     * 
     * @param taskId 任务ID
     * @param executeTime 执行时间
     * @return 是否已被锁定
     */
    public boolean isLocked(Long taskId, LocalDateTime executeTime) {
        String lockKey = buildLockKey(taskId, executeTime);
        
        try {
            RLock lock = redissonClient.getLock(lockKey);
            return lock.isLocked();
        } catch (Exception e) {
            log.error("检查任务锁状态异常: taskId={}, executeTime={}, error={}", 
                taskId, executeTime, e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取锁的剩余生存时间
     * 
     * @param taskId 任务ID
     * @param executeTime 执行时间
     * @return 剩余时间（毫秒），-1表示没有设置过期时间，-2表示锁不存在
     */
    public long getRemainingTimeToLive(Long taskId, LocalDateTime executeTime) {
        String lockKey = buildLockKey(taskId, executeTime);
        
        try {
            RLock lock = redissonClient.getLock(lockKey);
            return lock.remainTimeToLive();
        } catch (Exception e) {
            log.error("获取锁剩余时间异常: taskId={}, executeTime={}, error={}", 
                taskId, executeTime, e.getMessage());
            return -2;
        }
    }
    
    /**
     * 构建锁的键名
     * 
     * @param taskId 任务ID
     * @param executeTime 执行时间
     * @return 锁键名
     */
    private String buildLockKey(Long taskId, LocalDateTime executeTime) {
        // 精确到分钟级别，避免同一分钟内重复执行
        String timeKey = executeTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        return LOCK_KEY_PREFIX + taskId + ":" + timeKey;
    }
}