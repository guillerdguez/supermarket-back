package com.supermarket.supermarket.service.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "token_blacklist:";
    private final RedisTemplate<String, Object> redisTemplate;

    public void blacklistToken(String token, LocalDateTime expirationTime) {
        String redisKey = BLACKLIST_PREFIX + token;
        Duration ttl = Duration.between(LocalDateTime.now(), expirationTime);

        if (ttl.isNegative() || ttl.isZero()) {
            log.debug("Token already expired, not adding to blacklist");
            return;
        }

        redisTemplate.opsForValue().set(redisKey, "blacklisted", ttl);
        log.info("Token blacklisted until: {} (TTL: {} seconds)", expirationTime, ttl.getSeconds());
    }

    public boolean isBlacklisted(String token) {
        String redisKey = BLACKLIST_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(redisKey);
        if (exists) {
            log.debug("Token found in blacklist");
            return true;
        }
        return false;
    }

    public void removeFromBlacklist(String token) {
        String redisKey = BLACKLIST_PREFIX + token;
        redisTemplate.delete(redisKey);
        log.debug("Token manually removed from blacklist");
    }

    public Long getBlacklistTimeRemaining(String token) {
        String redisKey = BLACKLIST_PREFIX + token;
        return redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
    }
}