package com.supermarket.supermarket.service.security;

import com.supermarket.supermarket.exception.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int BLOCK_TIME_MINUTES = 5;
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    private final RedisTemplate<String, Object> redisTemplate;

    public void checkRateLimit(String key) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        Integer currentAttempts = (Integer) redisTemplate.opsForValue().get(redisKey);

        if (currentAttempts == null) {
            redisTemplate.opsForValue().set(redisKey, 1, Duration.ofMinutes(BLOCK_TIME_MINUTES));
            log.debug("First attempt for key: {}", key);
            return;
        }

        if (currentAttempts >= MAX_ATTEMPTS) {
            Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
            log.warn("Rate limit exceeded for key: {}. Blocked for {} seconds", key, ttl);
            throw new RateLimitExceededException(
                String.format("Too many attempts. Please try again in %d seconds", 
                    ttl != null ? ttl : BLOCK_TIME_MINUTES * 60)
            );
        }

        redisTemplate.opsForValue().increment(redisKey);
        log.debug("Attempt {} of {} for key: {}", currentAttempts + 1, MAX_ATTEMPTS, key);
    }

    public void resetRateLimit(String key) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        redisTemplate.delete(redisKey);
        log.debug("Rate limit reset for key: {}", key);
    }

    public int getRemainingAttempts(String key) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        Integer currentAttempts = (Integer) redisTemplate.opsForValue().get(redisKey);
        if (currentAttempts == null) {
            return MAX_ATTEMPTS;
        }
        return Math.max(0, MAX_ATTEMPTS - currentAttempts);
    }

    public Long getBlockTimeRemaining(String key) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        return redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
    }
}