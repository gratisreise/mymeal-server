package com.mymealserver.external.redis.service;

import com.mymealserver.external.redis.key.RedisKeyProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * JWT 토큰 블랙리스트 서비스
 *
 * <p>로그아웃한 리프레시 토큰을 Redis에 저장하여 재사용을 방지합니다.
 * 토큰 만료 기간 동안 블랙리스트에 유지됩니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisKeyProvider keyProvider;
    private static final long DEFAULT_TTL_DAYS = 7; // 리프레시 토큰 일반적인 만료 기간

    /**
     * 리프레시 토큰을 블랙리스트에 추가 (지정된 TTL)
     *
     * @param refreshToken 블랙리스트에 추가할 리프레시 토큰
     * @param ttlDays      블랙리스트 유지 기간 (일)
     */
    public void addToBlacklist(String refreshToken, long ttlDays) {
        String key = keyProvider.blacklistKey(refreshToken);
        redisTemplate.opsForValue().set(key, "blacklisted", ttlDays, TimeUnit.DAYS);
        log.info("리프레시 토큰 블랙리스트 추가 완료");
    }

    /**
     * 리프레시 토큰이 블랙리스트에 있는지 확인
     *
     * @param refreshToken 확인할 리프레시 토큰
     * @return 블랙리스트에 있으면 true, 없으면 false
     */
    public boolean isBlacklisted(String refreshToken) {
        String key = keyProvider.blacklistKey(refreshToken);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 리프레시 토큰을 블랙리스트에 추가 (기본 TTL)
     *
     * @param refreshToken 블랙리스트에 추가할 리프레시 토큰
     */
    public void addToBlacklist(String refreshToken) {
        addToBlacklist(refreshToken, DEFAULT_TTL_DAYS);
    }
}
