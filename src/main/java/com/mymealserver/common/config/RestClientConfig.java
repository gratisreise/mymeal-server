package com.mymealserver.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * OAuth 설정 (RestClient Bean)
 * OAuth 설정은 YAML에서 직접 @Value로 주입받으므로 별도의 Properties 클래스 불필요
 */
@Slf4j
@Configuration
public class RestClientConfig {

    /**
     * OAuth API 호출을 위한 RestClient Bean
     */
    @Bean
    public RestClient oauthRestClient(RestClient.Builder builder) {
        return builder
                .requestFactory(new JdkClientHttpRequestFactory())
                .build();
    }
}
