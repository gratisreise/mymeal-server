package com.mymealserver.common.security;

import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtProvider {

    private final SecretKey accessKey;
    private final SecretKey refreshKey;
    private final long accessValidity;
    private final long refreshValidity;

    public JwtProvider(
        @Value("${jwt.access-secret}") String accessKey,
        @Value("${jwt.refresh-secret}") String refreshKey,
        @Value("${jwt.access_expiration}") long accessValidity,
        @Value("${jwt.refresh-expiration}") long refreshValidity
    ) {
        // HMAC SHA-512 알고리즘을 사용하는 키 생성
        this.accessKey = Keys.hmacShaKeyFor(accessKey.getBytes());
        this.refreshKey = Keys.hmacShaKeyFor(refreshKey.getBytes());
        this.accessValidity = accessValidity;
        this.refreshValidity = refreshValidity;
    }



    // == Access ==
    public String createAccessToken(Long memberId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessValidity);

        return Jwts.builder()
            .subject(String.valueOf(memberId))
            .claim("memberId", memberId)
            .issuedAt(now)
            .expiration(validity)
            .signWith(accessKey, Jwts.SIG.HS512)
            .compact();
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

    public Long validateAccessTokenAndGetMemberId(String token) {
        try {
            Jwts.parser()
                .verifyWith(accessKey)
                .build()
                .parseSignedClaims(token);
            return getMemberId(token);
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        } catch (Exception e){
            log.error("유효하지 않은 토큰{}, 이유:{}", token, e.getMessage());
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
    }

    public long getExpiration(String accessToken) {
        Date expiration = Jwts.parser()
            .verifyWith(accessKey)
            .build()
            .parseSignedClaims(accessToken)
            .getPayload()
            .getExpiration();

        // 현재 시간과의 차이를 계산
        long now = new Date().getTime();
        return (expiration.getTime() - now);
    }




    // == Refresh ==
    public String createRefreshToken(Long memberId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshValidity);

        return Jwts.builder()
            .claim("memberId", memberId)
            .issuedAt(now)
            .expiration(validity)
            .signWith(refreshKey, SIG.HS512)
            .compact();
    }


    public Long getRefreshMemberId(String token) {
        return Jwts.parser()
            .verifyWith(refreshKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .get("memberId", Long.class);
    }

    public Long validateRefreshTokenAndGetMemberId(String token) {
        try {
            Jwts.parser()
                .verifyWith(refreshKey)
                .build()
                .parseSignedClaims(token);
            return getRefreshMemberId(token);
        } catch (RuntimeException e) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }
    }
}
