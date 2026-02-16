package com.mymealserver.auth.service.client.kakao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Kakao OAuth API Client
 * Kakao OAuth 설정값을 @Value로 직접 주입받아 강하게 결합
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoApiClient {

    private final RestClient restClient;

    @Value("${oauth.kakao.client-id}")
    private String clientId;

    @Value("${oauth.kakao.client-secret}")
    private String clientSecret;

    @Value("${oauth.kakao.token-url}")
    private String tokenUrl;

    @Value("${oauth.kakao.user-info-url}")
    private String userInfoUrl;

    @Value("${oauth.kakao.redirect-uri}")
    private String redirectUri;

    /**
     * Exchange authorization code for access token
     * 내부적으로 @Value로 주입받은 redirectUri 사용
     *
     * @param code Authorization code from Kakao
     * @return Kakao token response
     */
    public KakaoTokenResponse exchangeCodeForToken(String code) {
        log.info("Exchanging authorization code for access token with Kakao");
        log.debug("Using redirect URI: {}", redirectUri);

        return restClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(Map.of(
                        "code", code,
                        "client_id", clientId,
                        "client_secret", clientSecret,
                        "redirect_uri", redirectUri,
                        "grant_type", "authorization_code"
                ))
                .retrieve()
                .body(KakaoTokenResponse.class);
    }

    /**
     * Get user information from Kakao using access token
     *
     * @param accessToken Access token from Kakao
     * @return Kakao user info response
     */
    public KakaoUserInfoResponse getUserInfo(String accessToken) {
        log.info("Fetching user info from Kakao");

        return restClient.get()
                .uri(userInfoUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(KakaoUserInfoResponse.class);
    }
}
