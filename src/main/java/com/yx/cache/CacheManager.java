package com.yx.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存管理工具类
 */
@Slf4j
@Component
public class CacheManager {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置缓存（无过期时间）
     */
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            log.debug("缓存设置成功: key={}", key);
        } catch (Exception e) {
            log.error("缓存设置失败: key={}", key, e);
        }
    }

    /**
     * 设置缓存（指定过期时间，单位秒）
     */
    public void set(String key, Object value, long timeout) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
            log.debug("缓存设置成功: key={}, timeout={}", key, timeout);
        } catch (Exception e) {
            log.error("缓存设置失败: key={}", key, e);
        }
    }

    /**
     * 获取缓存
     */
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("缓存获取失败: key={}", key, e);
            return null;
        }
    }

    /**
     * 删除缓存
     */
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("缓存删除成功: key={}", key);
        } catch (Exception e) {
            log.error("缓存删除失败: key={}", key, e);
        }
    }

    /**
     * 批量删除缓存
     */
    public void deletePattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            log.debug("批量删除缓存成功: pattern={}", pattern);
        } catch (Exception e) {
            log.error("批量删除缓存失败: pattern={}", pattern, e);
        }
    }

    /**
     * 检查缓存是否存在
     */
    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("检查缓存失败: key={}", key, e);
            return false;
        }
    }

    /**
     * 获取缓存过期时间（秒）
     */
    public long getExpire(String key) {
        try {
            Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return expire != null ? expire : -1;
        } catch (Exception e) {
            log.error("获取过期时间失败: key={}", key, e);
            return -1;
        }
    }

    /**
     * 设置缓存过期时间
     */
    public void expire(String key, long timeout) {
        try {
            redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
            log.debug("设置过期时间成功: key={}, timeout={}", key, timeout);
        } catch (Exception e) {
            log.error("设置过期时间失败: key={}", key, e);
        }
    }

    /**
     * 清空所有缓存
     */
    public void flushAll() {
        try {
            redisTemplate.getConnectionFactory().getConnection().flushAll();
            log.info("清空所有缓存成功");
        } catch (Exception e) {
            log.error("清空缓存失败", e);
        }
    }
}
