package com.mymealserver.external.redis.key;

import org.springframework.stereotype.Component;

/**
 * 중앙화된 Redis 키 관리 컴포넌트
 *
 * <p>모든 Redis 키 패턴을 일관되게 관리하고 키 충돌을 방지합니다.
 * 키 접두사로 "mymeal"을 사용하여 다른 애플리케이션과의 충돌을 방지합니다.
 */
@Component
public class RedisKeyProvider {

    private static final String KEY_PREFIX = "mymeal";

    /**
     * 토큰 블랙리스트 키 생성
     *
     * @param token 블랙리스트에 추가할 토큰
     * @return Redis 키 (예: "mymeal:token:blacklist:{token}")
     */
    public String blacklistKey(String token) {
        return String.format("%s:token:blacklist:%s", KEY_PREFIX, token);
    }

    /**
     * 식사 추천 알림 스케줄 키
     *
     * @return Redis Sorted Set 키 (예: "mymeal:notifications:recommendation")
     */
    public String recommendationNotificationKey() {
        return String.format("%s:notifications:recommendation", KEY_PREFIX);
    }

    /**
     * 식후 반응 알림 큐 키
     *
     * @return Redis Sorted Set 키 (예: "mymeal:notifications:reaction")
     */
    public String reactionNotificationKey() {
        return String.format("%s:notifications:reaction", KEY_PREFIX);
    }
}
