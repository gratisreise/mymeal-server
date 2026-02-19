package com.mymealserver.config.classes;

import com.mymealserver.common.security.MemberPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MemberPrincipal 단위 테스트")
class MemberPrincipalTest {

    @Nested
    @DisplayName("생성 및 조회")
    class CreationTests {

        @Test
        @DisplayName("MemberPrincipal 생성 및 memberId 조회")
        void testMemberPrincipalCreation() {
            // Given
            Long memberId = 1L;

            // When
            MemberPrincipal principal = new MemberPrincipal(memberId);

            // Then
            assertThat(principal.getMemberId()).isEqualTo(1L);
            assertThat(principal.getUsername()).isEqualTo("1");
            assertThat(principal.getAuthorities()).isEmpty();
            assertThat(principal.getPassword()).isNull();
        }

        @Test
        @DisplayName("여러 memberId로 MemberPrincipal 생성")
        void testMultipleMemberIds() {
            // Given & When
            MemberPrincipal principal1 = new MemberPrincipal(1L);
            MemberPrincipal principal2 = new MemberPrincipal(100L);
            MemberPrincipal principal3 = new MemberPrincipal(999L);

            // Then
            assertThat(principal1.getMemberId()).isEqualTo(1L);
            assertThat(principal2.getMemberId()).isEqualTo(100L);
            assertThat(principal3.getMemberId()).isEqualTo(999L);
        }

        @Test
        @DisplayName("null memberId로 MemberPrincipal 생성")
        void testNullMemberId() {
            // Given
            Long memberId = null;

            // When
            MemberPrincipal principal = new MemberPrincipal(memberId);

            // Then
            assertThat(principal.getMemberId()).isNull();
            assertThat(principal.getUsername()).isEqualTo("null");
        }
    }

    @Nested
    @DisplayName("UserDetails 계약 준수")
    class UserDetailsContractTests {

        @Test
        @DisplayName("계정 상태 관련 메서드는 모두 true 반환")
        void testAccountStatusMethods() {
            // Given
            MemberPrincipal principal = new MemberPrincipal(1L);

            // When & Then
            assertThat(principal.isAccountNonExpired()).isTrue();
            assertThat(principal.isAccountNonLocked()).isTrue();
            assertThat(principal.isCredentialsNonExpired()).isTrue();
            assertThat(principal.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("getAuthorities는 빈 컬렉션 반환")
        void testEmptyAuthorities() {
            // Given
            MemberPrincipal principal = new MemberPrincipal(1L);

            // When
            var authorities = principal.getAuthorities();

            // Then
            assertThat(authorities).isNotNull();
            assertThat(authorities).isEmpty();
        }

        @Test
        @DisplayName("getPassword는 null 반환")
        void testNullPassword() {
            // Given
            MemberPrincipal principal = new MemberPrincipal(1L);

            // When
            String password = principal.getPassword();

            // Then
            assertThat(password).isNull();
        }

        @Test
        @DisplayName("getUsername은 memberId를 문자열로 반환")
        void testUsername() {
            // Given
            Long memberId = 12345L;
            MemberPrincipal principal = new MemberPrincipal(memberId);

            // When
            String username = principal.getUsername();

            // Then
            assertThat(username).isEqualTo("12345");
        }
    }

    @Nested
    @DisplayName("불변성 테스트")
    class ImmutabilityTests {

        @Test
        @DisplayName("MemberPrincipal은 불변 객체")
        void testImmutability() {
            // Given
            Long memberId = 1L;
            MemberPrincipal principal = new MemberPrincipal(memberId);

            // When & Then - final 필드이므로 변경 불가능
            assertThat(principal.getMemberId()).isEqualTo(1L);
            // principal.setMemberId(2L); // 컴파일 에러 (setter 없음)
        }
    }
}
