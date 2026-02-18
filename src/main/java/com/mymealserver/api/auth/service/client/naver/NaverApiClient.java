package com.mymealserver.auth.service.client.naver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Naver OAuth API Client
 * Naver OAuth 설정값을 @Value로 직접 주입받아 강하게 결합
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NaverApiClient {

    private final RestClient restClient;

    @Value("${oauth.naver.client-id}")
    private String clientId;

    @Value("${oauth.naver.client-secret}")
    private String clientSecret;

    @Value("${oauth.naver.token-url}")
    private String tokenUrl;

    @Value("${oauth.naver.user-info-url}")
    private String userInfoUrl;

    @Value("${oauth.naver.redirect-uri}")
    private String redirectUri;

    /**
     * Exchange authorization code for access token
     * 내부적으로 @Value로 주입받은 redirectUri 사용
     *
     * @param code Authorization code from Naver
     * @return Naver token response
     */
    public NaverTokenResponse exchangeCodeForToken(String code) {
        log.info("Exchanging authorization code for access token with Naver");
        log.debug("Using redirect URI: {}", redirectUri);

        return restClient.post()
                .uri(tokenUrl)
                .body(Map.of(
                        "code", code,
                        "client_id", clientId,
                        "client_secret", clientSecret,
                        "redirect_uri", redirectUri,
                        "grant_type", "authorization_code"
                ))
                .retrieve()
                .body(NaverTokenResponse.class);
    }

    /**
     * Get user information from Naver using access token
     *
     * @param accessToken Access token from Naver
     * @return Naver user info response
     */
    public NaverUserInfoResponse getUserInfo(String accessToken) {
        log.info("Fetching user info from Naver");

        return restClient.get()
                .uri(userInfoUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(NaverUserInfoResponse.class);
    }
}
