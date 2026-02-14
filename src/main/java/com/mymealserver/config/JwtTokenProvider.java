package com.mymealserver.config;

import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final SecretKey accessKey;
    private final SecretKey refreshKey;
    private final long accessValidity;
    private final long refreshValidity;

    public JwtTokenProvider(
        @Value("${jwt.access_secret}") String accessKey,
        @Value("${jwt.refresh_secret}") String refreshKey,
        @Value("${jwt.access_expiration}") long accessValidity,
        @Value("${jwt.refresh_expiration}") long refreshValidity
    ) {
        // HMAC SHA-512 알고리즘을 사용하는 키 생성
        this.accessKey = Keys.hmacShaKeyFor(accessKey.getBytes());
        this.refreshKey = Keys.hmacShaKeyFor(refreshKey.getBytes());
        this.accessValidity = accessValidity;
        this.refreshValidity = refreshValidity;
    }

    public long getExpiration(String accessToken) {
        Date expiration = Jwts.parser()
            .verifyWith(accessKey)
            .build()
            .parseSignedClaims(accessToken)
            .getPayload()
            .getExpiration();

        // 2. 현재 시간과의 차이를 계산
        long now = new Date().getTime();
        return (expiration.getTime() - now);
    }


    public String createAccessToken(String subject, long memberId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessValidity);

        return Jwts.builder()
            .subject(subject)
            .claim("memberId", memberId)
            .issuedAt(now)
            .expiration(validity)
            .signWith(accessKey, Jwts.SIG.HS512)
            .compact();
    }

    public String createRefreshToken(String username) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshValidity);

        return Jwts.builder()
            .subject(username)
            .issuedAt(now)
            .expiration(validity)
            .signWith(refreshKey, SIG.HS512)
            .compact();
    }
    public String getRefreshUsername(String token) {
        return Jwts.parser()
            .verifyWith(refreshKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }

    public String getUsername(String token) {
        return Jwts.parser()
            .verifyWith(accessKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }

    public Long getMemberId(String token){
        return Jwts.parser()
            .verifyWith(accessKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .get("memberId", Long.class);
    }


    public boolean validateAccessToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(accessKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (RuntimeException e) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
    }
}
