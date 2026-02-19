package com.mymealserver.auth.service;

import com.mymealserver.api.auth.service.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenBlacklistService 단위 테스트")
class TokenBlacklistServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Nested
    @DisplayName("블랙리스트에 추가")
    class AddToBlacklistTests {

        @Test
        @DisplayName("리프레시 토큰을 블랙리스트에 추가한다 - 기본 TTL")
        void addToBlacklist_WithDefaultTtl_ShouldAddToBlacklist() {
            // Given
            String refreshToken = "refresh_token_abc123";

            // When
            tokenBlacklistService.addToBlacklist(refreshToken);

            // Then
            then(redisTemplate).should().opsForValue();
            then(valueOperations).should().set(
                    eq("token:blacklist:" + refreshToken),
                    eq("blacklisted"),
                    eq(7L),
                    eq(TimeUnit.DAYS)
            );
        }

        @Test
        @DisplayName("리프레시 토큰을 블랙리스트에 추가한다 - 커스텀 TTL")
        void addToBlacklist_WithCustomTtl_ShouldAddToBlacklist() {
            // Given
            String refreshToken = "refresh_token_xyz789";
            long customTtlDays = 14L;

            // When
            tokenBlacklistService.addToBlacklist(refreshToken, customTtlDays);

            // Then
            then(redisTemplate).should().opsForValue();
            then(valueOperations).should().set(
                    eq("token:blacklist:" + refreshToken),
                    eq("blacklisted"),
                    eq(customTtlDays),
                    eq(TimeUnit.DAYS)
            );
        }

        @Test
        @DisplayName("여러 토큰을 블랙리스트에 추가한다")
        void addToBlacklist_MultipleTokens_ShouldAddAllToBlacklist() {
            // Given
            String token1 = "refresh_token_1";
            String token2 = "refresh_token_2";
            String token3 = "refresh_token_3";

            // When
            tokenBlacklistService.addToBlacklist(token1);
            tokenBlacklistService.addToBlacklist(token2);
            tokenBlacklistService.addToBlacklist(token3);

            // Then
            then(redisTemplate).should(times(3)).opsForValue();
            then(valueOperations).should().set(eq("token:blacklist:" + token1), eq("blacklisted"), eq(7L), eq(TimeUnit.DAYS));
            then(valueOperations).should().set(eq("token:blacklist:" + token2), eq("blacklisted"), eq(7L), eq(TimeUnit.DAYS));
            then(valueOperations).should().set(eq("token:blacklist:" + token3), eq("blacklisted"), eq(7L), eq(TimeUnit.DAYS));
        }

        @Test
        @DisplayName("동일한 토큰을 여러 번 블랙리스트에 추가해도 덮어쓴다")
        void addToBlacklist_SameTokenMultipleTimes_ShouldOverwrite() {
            // Given
            String refreshToken = "refresh_token_mno678";

            // When
            tokenBlacklistService.addToBlacklist(refreshToken, 7L);
            tokenBlacklistService.addToBlacklist(refreshToken, 14L);

            // Then
            then(redisTemplate).should(times(2)).opsForValue();
            then(valueOperations).should(times(2)).set(
                    eq("token:blacklist:" + refreshToken),
                    eq("blacklisted"),
                    anyLong(),
                    eq(TimeUnit.DAYS)
            );
        }
    }

    @Nested
    @DisplayName("블랙리스트 확인")
    class IsBlacklistedTests {

        @Test
        @DisplayName("블랙리스트에 있는 토큰인지 확인한다 - 블랙리스트에 있는 경우")
        void isBlacklisted_WithBlacklistedToken_ShouldReturnTrue() {
            // Given
            String refreshToken = "blacklisted_token_def456";
            given(redisTemplate.hasKey("token:blacklist:" + refreshToken)).willReturn(true);

            // When
            boolean result = tokenBlacklistService.isBlacklisted(refreshToken);

            // Then
            assertThat(result).isTrue();
            then(redisTemplate).should().hasKey("token:blacklist:" + refreshToken);
        }

        @Test
        @DisplayName("블랙리스트에 있는 토큰인지 확인한다 - 블랙리스트에 없는 경우")
        void isBlacklisted_WithNonBlacklistedToken_ShouldReturnFalse() {
            // Given
            String refreshToken = "valid_token_ghi012";
            given(redisTemplate.hasKey("token:blacklist:" + refreshToken)).willReturn(false);

            // When
            boolean result = tokenBlacklistService.isBlacklisted(refreshToken);

            // Then
            assertThat(result).isFalse();
            then(redisTemplate).should().hasKey("token:blacklist:" + refreshToken);
        }

        @Test
        @DisplayName("블랙리스트에 있는 토큰인지 확인한다 - Redis가 null을 반환하는 경우")
        void isBlacklisted_WithNullResponse_ShouldReturnFalse() {
            // Given
            String refreshToken = "unknown_token_jkl345";
            given(redisTemplate.hasKey("token:blacklist:" + refreshToken)).willReturn(null);

            // When
            boolean result = tokenBlacklistService.isBlacklisted(refreshToken);

            // Then
            assertThat(result).isFalse();
            then(redisTemplate).should().hasKey("token:blacklist:" + refreshToken);
        }
    }
}
