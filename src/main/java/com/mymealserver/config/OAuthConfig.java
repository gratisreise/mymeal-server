package com.mymealserver.config;

import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * OAuth 설정 (RestClient Bean)
 * OAuth 설정은 YAML에서 직접 @Value로 주입받으므로 별도의 Properties 클래스 불필요
 */
@Slf4j
@Configuration
public class OAuthConfig {

    /**
     * OAuth API 호출을 위한 RestClient Bean
     */
    @org.springframework.context.annotation.Bean
    public RestClient oauthRestClient(RestClient.Builder builder) {
        return builder
                .requestFactory(new JdkClientHttpRequestFactory())
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    log.error("OAuth API 호출 실패: {} {}", response.getStatusCode(), request.getURI());
                    throw new BusinessException(ErrorCode.OAUTH_API_FAILED);
                })
                .build();
    }
}
