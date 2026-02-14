package com.mymealserver.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:mymeal-server}")
    private String applicationName;

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI openAPI() {
        String serverUrl = "http://localhost:" + serverPort;

        return new OpenAPI()
                .info(new Info()
                        .title(applicationName + " API")
                        .description("mymeal-server API Documentation")
                        .version("1.0.0"))
                .servers(List.of(
                        new Server().url(serverUrl).description("Local Server")
                ));
    }
}
