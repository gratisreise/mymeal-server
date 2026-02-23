package com.mymealserver.external.s3.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "aws.s3")
public class S3Config {

    private String bucket;
    private String endpoint;
    private String region;

    // 중첩 클래스로 YAML aws.credentials.* 구조 매핑
    private Credentials credentials;

    @Getter
    @Setter
    public static class Credentials {
        private String accessKey;
        private String secretKey;
    }

    @Bean
    public S3Client s3Client() {
        log.info("Initializing S3 client for bucket: {}, region: {}", bucket, region);

        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        credentials.getAccessKey(),
                                        credentials.getSecretKey()
                                )
                        )
                )
                .build();
    }
}
