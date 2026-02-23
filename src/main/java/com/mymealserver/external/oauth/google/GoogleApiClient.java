package com.mymealserver.external.oauth.google;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleApiClient {

    private final RestClient restClient;

    @Value("${oauth.google.client-id}")
    private String clientId;

    @Value("${oauth.google.client-secret}")
    private String clientSecret;

    @Value("${oauth.google.token-url}")
    private String tokenUrl;

    @Value("${oauth.google.user-info-url}")
    private String userInfoUrl;

    @Value("${oauth.google.redirect-uri}")
    private String redirectUri;

    public GoogleTokenResponse exchangeCodeForToken(String code) {
        log.info("Exchanging authorization code for access token with Google");
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
                .body(GoogleTokenResponse.class);
    }

    public GoogleUserInfoResponse getUserInfo(String accessToken) {
        log.info("Fetching user info from Google");

        return restClient.get()
                .uri(userInfoUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(GoogleUserInfoResponse.class);
    }
}
