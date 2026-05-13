package org.example.ash.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
@Slf4j
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Update (set) a Redis value by key.
     * If the key does not exist, it will be created.
     *
     * @param key
     * @param value
     */
    public void updateCache(String key, Serializable value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void evictCache(String key) {
        redisTemplate.delete(key);
    }
}
