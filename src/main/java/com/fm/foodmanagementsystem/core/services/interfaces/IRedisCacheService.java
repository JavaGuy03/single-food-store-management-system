package com.fm.foodmanagementsystem.core.services.interfaces;

import java.util.concurrent.TimeUnit;

public interface IRedisCacheService {
    void set(String key, Object value, long timeout, TimeUnit unit);
    <T> T get(String key, Class<T> targetClass);
    void delete(String key);
    boolean hasKey(String key);
}
