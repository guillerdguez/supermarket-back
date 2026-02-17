package com.supermarket.supermarket.service.security;

import com.supermarket.supermarket.exception.RateLimitExceededException;
import com.supermarket.supermarket.exception.RateLimitServiceException;
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
        try {
             Long currentAttempts = redisTemplate.opsForValue().increment(redisKey);

            if (currentAttempts == 1) {
                 redisTemplate.expire(redisKey, Duration.ofMinutes(BLOCK_TIME_MINUTES));
            }

            if (currentAttempts > MAX_ATTEMPTS) {
                Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
                throw new RateLimitExceededException(
                        String.format("Too many attempts. Retry in %d seconds", ttl)
                );
            }
        } catch (Exception e) {
         throw new RateLimitServiceException("Error checking rate limit", e);
        }
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