package com.mymealserver.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 토큰 블랙리스트 서비스
 * 로그아웃 시 리프레시 토큰을 Redis에 저장하여 무효화
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "token:blacklist:";
    private static final long DEFAULT_TTL_DAYS = 7; // 리프레시 토큰 일반적인 만료 기간

    /**
     * 리프레시 토큰을 블랙리스트에 추가
     *
     * @param refreshToken 블랙리스트에 추가할 리프레시 토큰
     * @param ttlDays     만료 기간 (일)
     */
    public void addToBlacklist(String refreshToken, long ttlDays) {
        String key = BLACKLIST_PREFIX + refreshToken;
        redisTemplate.opsForValue().set(key, "blacklisted", ttlDays, TimeUnit.DAYS);
        log.info("리프레시 토큰 블랙리스트 추가 완료");
    }

    /**
     * 리프레시 토큰이 블랙리스트에 있는지 확인
     *
     * @param refreshToken 확인할 리프레시 토큰
     * @return 블랙리스트에 있으면 true
     */
    public boolean isBlacklisted(String refreshToken) {
        String key = BLACKLIST_PREFIX + refreshToken;
        Boolean isBlacklisted = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(isBlacklisted);
    }

    /**
     * 기본 TTL로 블랙리스트에 추가
     *
     * @param refreshToken 블랙리스트에 추가할 리프레시 토큰
     */
    public void addToBlacklist(String refreshToken) {
        addToBlacklist(refreshToken, DEFAULT_TTL_DAYS);
    }
}
