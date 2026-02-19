package com.mymealserver.api.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "token:blacklist:";
    private static final long DEFAULT_TTL_DAYS = 7; // 리프레시 토큰 일반적인 만료 기간

    public void addToBlacklist(String refreshToken, long ttlDays) {
        String key = BLACKLIST_PREFIX + refreshToken;
        redisTemplate.opsForValue().set(key, "blacklisted", ttlDays, TimeUnit.DAYS);
        log.info("리프레시 토큰 블랙리스트 추가 완료");
    }

    public boolean isBlacklisted(String refreshToken) {
        String key = BLACKLIST_PREFIX + refreshToken;
        Boolean isBlacklisted = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(isBlacklisted);
    }

    public void addToBlacklist(String refreshToken) {
        addToBlacklist(refreshToken, DEFAULT_TTL_DAYS);
    }
}
