package com.mymealserver.common.resolver;

import com.mymealserver.config.classes.MemberPrincipal;
import com.mymealserver.common.annotation.AuthenticatedMember;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticatedMemberArgumentResolver 단위 테스트")
class AuthenticatedMemberArgumentResolverTest {

    private final AuthenticatedMemberArgumentResolver resolver = new AuthenticatedMemberArgumentResolver();

    @Mock
    private Authentication authentication;

    @Mock
    private ModelAndViewContainer mavContainer;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("supportsParameter")
    class SupportsParameterTests {

        @Test
        @DisplayName("@AuthenticatedMember 어노테이션이 있고 Long 타입이면 true 반환")
        void testSupportsParameterWithValidAnnotation() throws NoSuchMethodException {
            // Given
            java.lang.reflect.Method method = TestController.class.getMethod("testMethod", Long.class);
            var parameter = new org.springframework.core.MethodParameter(method, 0);

            // When
            boolean result = resolver.supportsParameter(parameter);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("@AuthenticatedMember 어노테이션이 없으면 false 반환")
        void testSupportsParameterWithoutAnnotation() throws NoSuchMethodException {
            // Given
            java.lang.reflect.Method method = TestController.class.getMethod("methodWithoutAnnotation", Long.class);
            var parameter = new org.springframework.core.MethodParameter(method, 0);

            // When
            boolean result = resolver.supportsParameter(parameter);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Long 타입이 아니면 false 반환")
        void testSupportsParameterWithWrongType() throws NoSuchMethodException {
            // Given
            java.lang.reflect.Method method = TestController.class.getMethod("methodWithWrongType", String.class);
            var parameter = new org.springframework.core.MethodParameter(method, 0);

            // When
            boolean result = resolver.supportsParameter(parameter);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("어노테이션 없이 Long 타입만 있는 경우 false 반환")
        void testSupportsParameterWithLongButNoAnnotation() throws NoSuchMethodException {
            // Given
            java.lang.reflect.Method method = TestController.class.getMethod("methodWithLongOnly", Long.class);
            var parameter = new org.springframework.core.MethodParameter(method, 0);

            // When
            boolean result = resolver.supportsParameter(parameter);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("resolveArgument")
    class ResolveArgumentTests {

        @Test
        @DisplayName("MemberPrincipal에서 memberId를 정상적으로 추출")
        void testResolveArgumentWithMemberPrincipal() throws Exception {
            // Given
            Long expectedMemberId = 1L;
            MemberPrincipal principal = new MemberPrincipal(expectedMemberId);

            given(authentication.getPrincipal()).willReturn(principal);
            given(authentication.isAuthenticated()).willReturn(true);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            java.lang.reflect.Method method = TestController.class.getMethod("testMethod", Long.class);
            var parameter = new org.springframework.core.MethodParameter(method, 0);

            // When
            Long memberId = (Long) resolver.resolveArgument(parameter, mavContainer, null, null);

            // Then
            assertThat(memberId).isEqualTo(expectedMemberId);
        }

        @Test
        @DisplayName("다양한 memberId 값 추출")
        void testResolveArgumentWithVariousMemberIds() throws Exception {
            // Given
            Long[] testIds = {1L, 100L, 999L, Long.MAX_VALUE};

            java.lang.reflect.Method method = TestController.class.getMethod("testMethod", Long.class);
            var parameter = new org.springframework.core.MethodParameter(method, 0);

            for (Long expectedId : testIds) {
                // Setup
                MemberPrincipal principal = new MemberPrincipal(expectedId);
                given(authentication.getPrincipal()).willReturn(principal);
                given(authentication.isAuthenticated()).willReturn(true);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // When
                Long memberId = (Long) resolver.resolveArgument(parameter, mavContainer, null, null);

                // Then
                assertThat(memberId).isEqualTo(expectedId);
            }
        }

        @Test
        @DisplayName("Authentication이 null이면 예외 발생")
        void testResolveArgumentWithNullAuthentication() throws Exception {
            // Given
            SecurityContextHolder.getContext().setAuthentication(null);

            java.lang.reflect.Method method = TestController.class.getMethod("testMethod", Long.class);
            var parameter = new org.springframework.core.MethodParameter(method, 0);

            // When & Then
            assertThatThrownBy(() -> resolver.resolveArgument(parameter, mavContainer, null, null))
                    .isInstanceOf(AuthenticationCredentialsNotFoundException.class)
                    .hasMessageContaining("Authentication not found");
        }

        @Test
        @DisplayName("인증되지 않은 Authentication이면 예외 발생")
        void testResolveArgumentWithUnauthenticatedAuthentication() throws Exception {
            // Given
            MemberPrincipal principal = new MemberPrincipal(1L);
            given(authentication.getPrincipal()).willReturn(principal);
            given(authentication.isAuthenticated()).willReturn(false);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            java.lang.reflect.Method method = TestController.class.getMethod("testMethod", Long.class);
            var parameter = new org.springframework.core.MethodParameter(method, 0);

            // When & Then
            assertThatThrownBy(() -> resolver.resolveArgument(parameter, mavContainer, null, null))
                    .isInstanceOf(AuthenticationCredentialsNotFoundException.class)
                    .hasMessageContaining("Authentication not found");
        }

        @Test
        @DisplayName("Principal이 MemberPrincipal이 아니면 예외 발생")
        void testResolveArgumentWithWrongPrincipalType() throws Exception {
            // Given
            given(authentication.getPrincipal()).willReturn("wrong_principal");
            given(authentication.isAuthenticated()).willReturn(true);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            java.lang.reflect.Method method = TestController.class.getMethod("testMethod", Long.class);
            var parameter = new org.springframework.core.MethodParameter(method, 0);

            // When & Then
            assertThatThrownBy(() -> resolver.resolveArgument(parameter, mavContainer, null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Principal must be MemberPrincipal")
                    .hasMessageContaining("String");
        }

        @Test
        @DisplayName("Principal이 null이면 예외 발생")
        void testResolveArgumentWithNullPrincipal() throws Exception {
            // Given
            given(authentication.getPrincipal()).willReturn(null);
            given(authentication.isAuthenticated()).willReturn(true);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            java.lang.reflect.Method method = TestController.class.getMethod("testMethod", Long.class);
            var parameter = new org.springframework.core.MethodParameter(method, 0);

            // When & Then
            assertThatThrownBy(() -> resolver.resolveArgument(parameter, mavContainer, null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Principal must be MemberPrincipal")
                    .hasMessageContaining("null");
        }
    }

    // 테스트용 더미 컨트롤러
    static class TestController {
        void testMethod(@AuthenticatedMember Long memberId) {}

        void methodWithoutAnnotation(Long memberId) {}

        void methodWithWrongType(@AuthenticatedMember String value) {}

        void methodWithLongOnly(Long memberId) {}
    }
}
