package com.mymealserver.external.redis;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisTokenService {

    private final StringRedisTemplate redisTemplate;

    private static final String RT_PREFIX = "RT:";
    private static final String BL_PREFIX = "BL:";
    private static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(7);

    // [ RefreshToken ]
    public void saveRefreshToken(Long memberId, String refreshToken) {
        String key = RT_PREFIX + memberId;
        redisTemplate.opsForValue().set(key, refreshToken, REFRESH_TOKEN_DURATION);
    }

    public String getRefreshToken(Long memberId) {
        return redisTemplate.opsForValue().get(RT_PREFIX + memberId);
    }

    public void deleteRefreshToken(Long memberId) {
        redisTemplate.delete(RT_PREFIX + memberId);
    }

    // [ Blacklist ]
    public void addBlacklist(String accessToken, long remainingTime) {
        String key = BL_PREFIX + accessToken;
        redisTemplate.opsForValue().set(key, "logout", Duration.ofMillis(remainingTime));
    }

    public boolean isBlacklisted(String accessToken) {
        return redisTemplate.hasKey(BL_PREFIX + accessToken);
    }
}
