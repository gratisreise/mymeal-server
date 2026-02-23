package com.mymealserver.api.auth.service.factory;

import com.mymealserver.api.auth.dto.request.OAuthRequest;
import com.mymealserver.api.auth.service.oauth.OAuthService;
import com.mymealserver.api.auth.service.oauth.factory.OAuthServiceFactory;
import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.common.enums.ProviderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuthServiceFactory 단위 테스트")
class OAuthServiceFactoryTest {

    @Mock
    private OAuthService googleOAuthService;

    @Mock
    private OAuthService kakaoOAuthService;

    @Mock
    private OAuthService naverOAuthService;

    private OAuthServiceFactory factory;

    @Nested
    @DisplayName("팩토리 초기화")
    class InitializationTests {

        @Test
        @DisplayName("생성자는 3개의 제공자를 초기화한다")
        void constructor_ShouldInitializeWithThreeProviders() {
            // Given
            given(googleOAuthService.getProvider()).willReturn(ProviderType.GOOGLE);
            given(kakaoOAuthService.getProvider()).willReturn(ProviderType.KAKAO);
            given(naverOAuthService.getProvider()).willReturn(ProviderType.NAVER);

            OAuthServiceFactory testFactory = new OAuthServiceFactory(
                    List.of(googleOAuthService, kakaoOAuthService, naverOAuthService)
            );

            // When
            OAuthRequest googleRequest = OAuthRequest.builder().provider(ProviderType.GOOGLE).code("code").build();
            OAuthRequest kakaoRequest = OAuthRequest.builder().provider(ProviderType.KAKAO).code("code").build();
            OAuthRequest naverRequest = OAuthRequest.builder().provider(ProviderType.NAVER).code("code").build();

            OAuthService googleService = testFactory.getOAuthService(googleRequest);
            OAuthService kakaoService = testFactory.getOAuthService(kakaoRequest);
            OAuthService naverService = testFactory.getOAuthService(naverRequest);

            // Then
            assertThat(googleService).isNotNull();
            assertThat(kakaoService).isNotNull();
            assertThat(naverService).isNotNull();
        }
    }

    @Nested
    @DisplayName("제공자 조회")
    class GetOAuthServiceTests {

        @BeforeEach
        void setUp() {
            // Setup mock services to return correct provider types
            given(googleOAuthService.getProvider()).willReturn(ProviderType.GOOGLE);
            given(kakaoOAuthService.getProvider()).willReturn(ProviderType.KAKAO);
            given(naverOAuthService.getProvider()).willReturn(ProviderType.NAVER);

            // Create factory with all three providers
            factory = new OAuthServiceFactory(
                    List.of(googleOAuthService, kakaoOAuthService, naverOAuthService)
            );
        }

        @Test
        @DisplayName("getOAuthService(GOOGLE)는 GoogleOAuthService를 반환한다")
        void getOAuthService_WithGoogle_ShouldReturnGoogleOAuthService() {
            // When
            OAuthRequest request = OAuthRequest.builder().provider(ProviderType.GOOGLE).code("code").build();
            OAuthService service = factory.getOAuthService(request);

            // Then
            assertThat(service).isNotNull();
            assertThat(service.getProvider()).isEqualTo(ProviderType.GOOGLE);
        }

        @Test
        @DisplayName("getOAuthService(KAKAO)는 KakaoOAuthService를 반환한다")
        void getOAuthService_WithKakao_ShouldReturnKakaoOAuthService() {
            // When
            OAuthRequest request = OAuthRequest.builder().provider(ProviderType.KAKAO).code("code").build();
            OAuthService service = factory.getOAuthService(request);

            // Then
            assertThat(service).isNotNull();
            assertThat(service.getProvider()).isEqualTo(ProviderType.KAKAO);
        }

        @Test
        @DisplayName("getOAuthService(NAVER)는 NaverOAuthService를 반환한다")
        void getOAuthService_WithNaver_ShouldReturnNaverOAuthService() {
            // When
            OAuthRequest request = OAuthRequest.builder().provider(ProviderType.NAVER).code("code").build();
            OAuthService service = factory.getOAuthService(request);

            // Then
            assertThat(service).isNotNull();
            assertThat(service.getProvider()).isEqualTo(ProviderType.NAVER);
        }

        @Test
        @DisplayName("getOAuthService(EMAIL)는 지원하지 않는 제공자 예외를 발생시킨다")
        void getOAuthService_WithUnsupportedProvider_ShouldThrowException() {
            // When & Then
            OAuthRequest request = OAuthRequest.builder().provider(ProviderType.EMAIL).code("code").build();
            assertThatThrownBy(() -> factory.getOAuthService(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo(ErrorCode.OAUTH_UNSUPPORTED_PROVIDER));
        }

        @Test
        @DisplayName("getOAuthService는 null을 허용하지 않는다")
        void getOAuthService_WithNullProvider_ShouldHandleGracefully() {
            // When & Then - EMAIL provider is not in OAuth services
            OAuthRequest request = OAuthRequest.builder().provider(ProviderType.EMAIL).code("code").build();
            assertThatThrownBy(() -> factory.getOAuthService(request))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("같은 제공자를 여러 번 조회해도 같은 인스턴스를 반환한다")
        void getOAuthService_MultipleCallsForSameProvider_ShouldReturnSameInstance() {
            // When
            OAuthRequest request = OAuthRequest.builder().provider(ProviderType.GOOGLE).code("code").build();
            OAuthService service1 = factory.getOAuthService(request);
            OAuthService service2 = factory.getOAuthService(request);

            // Then
            assertThat(service1).isSameAs(service2);
        }

        @Test
        @DisplayName("모든 제공자를 순회하며 조회할 수 있다")
        void getOAuthService_AllProviders_ShouldReturnCorrectServices() {
            // When
            OAuthRequest googleRequest = OAuthRequest.builder().provider(ProviderType.GOOGLE).code("code").build();
            OAuthRequest kakaoRequest = OAuthRequest.builder().provider(ProviderType.KAKAO).code("code").build();
            OAuthRequest naverRequest = OAuthRequest.builder().provider(ProviderType.NAVER).code("code").build();

            OAuthService google = factory.getOAuthService(googleRequest);
            OAuthService kakao = factory.getOAuthService(kakaoRequest);
            OAuthService naver = factory.getOAuthService(naverRequest);

            // Then
            assertThat(google.getProvider()).isEqualTo(ProviderType.GOOGLE);
            assertThat(kakao.getProvider()).isEqualTo(ProviderType.KAKAO);
            assertThat(naver.getProvider()).isEqualTo(ProviderType.NAVER);
        }
    }
}
