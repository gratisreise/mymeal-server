package com.mymealserver.auth.service;


import com.mymealserver.auth.dto.request.OAuthRequest;
import com.mymealserver.auth.dto.response.AuthResponse;
import com.mymealserver.entity.enums.ProviderType;

/**
 * 제공업체별 OAuth 인증을 위한 서비스 인터페이스
 */
public interface OAuthService {

    /**
     * OAuth 인증 코드 흐름을 사용한 사용자 인증
     *
     * @param request 인증 코드와 리다이렉트 URI를 포함한 OAuth 요청
     * @return JWT 토큰과 회원 정보가 포함된 인증 응답
     */
    AuthResponse authenticate(OAuthRequest request);

    /**
     * 이 서비스의 제공업체 타입 반환
     *
     * @return 제공업체 타입
     */
    ProviderType getProvider();
}
