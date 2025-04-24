package openerp.openerpresourceserver.wms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final long DEFAULT_TIMEOUT = 60 * 24 * 60; // 1 day
    private final ObjectMapper objectMapper;
    public void save(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value, DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save(String key, Object value, long timeout) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
            log.info("Saved key: {}, value: {}, timeout: {} seconds", key, value, timeout);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public <T> T get(String key, Class<T> type) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) return null;

            if (value instanceof java.util.Map) {
                return objectMapper.convertValue(value, type);
            }
            return type.cast(value);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
