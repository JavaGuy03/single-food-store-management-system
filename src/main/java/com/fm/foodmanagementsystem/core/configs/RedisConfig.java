package com.fm.foodmanagementsystem.core.configs;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@Configuration
public class RedisConfig implements CachingConfigurer {

    private ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return objectMapper;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper());

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper());

        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer)
                );
    }

    /**
     * Khi đọc cache cũ (không có @class, ví dụ do đổi serializer), tự động evict
     * entry đó và trả về null — @Cacheable sẽ tiếp tục gọi DB và ghi lại đúng định dạng.
     * Điều này ngăn lỗi 500 SerializationException ném ra cho client.
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new SimpleCacheErrorHandler() {

            @Override
            public void handleCacheGetError(RuntimeException ex, Cache cache, Object key) {
                if (isSerializationProblem(ex)) {
                    log.warn("[CACHE] Stale/corrupt entry — evicting. cache='{}' key='{}': {}",
                            cache.getName(), key, ex.getMessage());
                    try {
                        cache.evict(key);
                    } catch (Exception ignored) {
                        // Evict best-effort; ignore if Redis is temporarily unavailable
                    }
                    // Return normally → @Cacheable falls through to the real method (DB)
                    return;
                }
                super.handleCacheGetError(ex, cache, key);
            }

            @Override
            public void handleCachePutError(RuntimeException ex, Cache cache, Object key, Object value) {
                log.warn("[CACHE] Put error — cache='{}' key='{}': {}",
                        cache.getName(), key, ex.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException ex, Cache cache, Object key) {
                log.warn("[CACHE] Evict error — cache='{}' key='{}': {}",
                        cache.getName(), key, ex.getMessage());
            }

            private boolean isSerializationProblem(RuntimeException ex) {
                if (ex instanceof SerializationException) return true;
                Throwable cause = ex.getCause();
                if (cause instanceof SerializationException) return true;
                // Catch the Jackson InvalidTypeIdException wrapped inside
                return cause != null && cause.getMessage() != null
                        && cause.getMessage().contains("missing type id property");
            }
        };
    }
}
