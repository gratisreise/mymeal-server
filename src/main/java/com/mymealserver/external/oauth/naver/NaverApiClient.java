package com.mymealserver.external.oauth.naver;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

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

  public NaverTokenResponse exchangeCodeForToken(String code) {

    return restClient
        .post()
        .uri(tokenUrl)
        .body(
            Map.of(
                "code", code,
                "client_id", clientId,
                "client_secret", clientSecret,
                "redirect_uri", redirectUri,
                "grant_type", "authorization_code"))
        .retrieve()
        .body(NaverTokenResponse.class);
  }

  public NaverUserInfoResponse getUserInfo(String accessToken) {

    return restClient
        .get()
        .uri(userInfoUrl)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        .retrieve()
        .body(NaverUserInfoResponse.class);
  }
}
