package com.mymealserver.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    private static final String[] WHITE_LIST = {
        "/", "/actuator/**", "/swagger-ui/**",
        "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**",
        "/api/v1/auth/register", "/api/v1/auth/login", "/api/v1/auth/oauth"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler))
            .authorizeHttpRequests(auth->
                auth.anyRequest().permitAll());
//            .authorizeHttpRequests(auth -> auth
//                .requestMatchers(WHITE_LIST).permitAll()
//                .anyRequest().authenticated()
//            )
//            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
//                UsernamePasswordAuthenticationFilter.class)
//            .addFilterBefore(new ExceptionHandlerFilter(objectMapper),
//                JwtAuthenticationFilter.class);
        return http.build();
    }


}
