package com.mymealserver.common.test.fixtures;

import com.mymealserver.domain.member.Member;
import com.mymealserver.common.enums.ProviderType;

import java.time.LocalDateTime;

/**
 * Test fixture for Member entities
 * Provides reusable Member instances for testing
 */
public class MemberFixture {

    /**
     * Creates a default active member with EMAIL provider
     */
    public static Member createDefaultMember() {
        return Member.builder()
                .id(1L)
                .email("test@example.com")
                .password("$2a$10$encodedPassword")
                .name("Test User")
                .profileImage(null)
                .provider(ProviderType.EMAIL)
                .providerId(null)
                .isActive(true)
                .lastLoginAt(null)
                .build();
    }

    /**
     * Creates a member with Google OAuth provider
     */
    public static Member createGoogleMember() {
        return Member.builder()
                .id(2L)
                .email("123456789@google.com")
                .password(null)
                .name("Google User")
                .profileImage("https://example.com/google-profile.jpg")
                .provider(ProviderType.GOOGLE)
                .providerId("123456789")
                .isActive(true)
                .lastLoginAt(null)
                .build();
    }

    /**
     * Creates a member with Kakao OAuth provider
     */
    public static Member createKakaoMember() {
        return Member.builder()
                .id(3L)
                .email("987654321@kakao.com")
                .password(null)
                .name("Kakao User")
                .profileImage("https://example.com/kakao-profile.jpg")
                .provider(ProviderType.KAKAO)
                .providerId("987654321")
                .isActive(true)
                .lastLoginAt(null)
                .build();
    }

    /**
     * Creates a member with Naver OAuth provider
     */
    public static Member createNaverMember() {
        return Member.builder()
                .id(4L)
                .email("456789123@naver.com")
                .password(null)
                .name("Naver User")
                .profileImage("https://example.com/naver-profile.jpg")
                .provider(ProviderType.NAVER)
                .providerId("456789123")
                .isActive(true)
                .lastLoginAt(null)
                .build();
    }

    /**
     * Creates an inactive member
     */
    public static Member createInactiveMember() {
        return Member.builder()
                .id(5L)
                .email("inactive@example.com")
                .password("$2a$10$encodedPassword")
                .name("Inactive User")
                .profileImage(null)
                .provider(ProviderType.EMAIL)
                .providerId(null)
                .isActive(false)
                .lastLoginAt(LocalDateTime.now().minusDays(30))
                .build();
    }

    /**
     * Creates a member with custom fields
     */
    public static Member createCustomMember(
            Long id,
            String email,
            String name,
            ProviderType provider,
            String providerId,
            Boolean isActive
    ) {
        return Member.builder()
                .id(id)
                .email(email)
                .password(provider == ProviderType.EMAIL ? "$2a$10$encodedPassword" : null)
                .name(name)
                .profileImage(null)
                .provider(provider)
                .providerId(providerId)
                .isActive(isActive)
                .lastLoginAt(null)
                .build();
    }

    /**
     * Creates a member with last login time
     */
    public static Member createMemberWithLastLogin() {
        return Member.builder()
                .id(6L)
                .email("logged@example.com")
                .password("$2a$10$encodedPassword")
                .name("Logged User")
                .profileImage(null)
                .provider(ProviderType.EMAIL)
                .providerId(null)
                .isActive(true)
                .lastLoginAt(LocalDateTime.now())
                .build();
    }
}
