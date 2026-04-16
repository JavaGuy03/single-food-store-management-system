package com.fm.foodmanagementsystem.core.services.imps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fm.foodmanagementsystem.core.services.interfaces.IRedisCacheService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisCacheService implements IRedisCacheService {
    RedisTemplate<String, Object> redisTemplate;
    ObjectMapper objectMapper;

    @Override
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        // Lưu giá trị vào Redis kèm thời gian sống (TTL)
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    @Override
    public <T> T get(String key, Class<T> targetClass) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        // Ép kiểu an toàn về class mong muốn
        return objectMapper.convertValue(value, targetClass);
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
