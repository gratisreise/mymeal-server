package com.mymealserver.common.security;

import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT 토큰 생성 및 검증 provider
 */
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

    /**
     * 액세스 토큰 만료 시간 반환 (밀리초)
     */
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

    /**
     * 액세스 토큰 생성 (subject에 memberId 저장)
     */
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

    /**
     * 리프레시 토큰 생성 (claim에 memberId 저장)
     */
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

    /**
     * 리프레시 토큰에서 memberId 추출
     */
    public Long getRefreshMemberId(String token) {
        return Jwts.parser()
            .verifyWith(refreshKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .get("memberId", Long.class);
    }

    /**
     * 액세스 토큰에서 memberId 추출
     */
    public Long getMemberId(String token){
        return Jwts.parser()
            .verifyWith(accessKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .get("memberId", Long.class);
    }

    /**
     * 액세스 토큰 유효성 검증
     */
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

    /**
     * 액세스 토큰 유효성 검증 및 memberId 추출
     */
    public Long validateAccessTokenAndGetMemberId(String token) {
        try {
            Jwts.parser()
                .verifyWith(accessKey)
                .build()
                .parseSignedClaims(token);
            return getMemberId(token);
        } catch (RuntimeException e) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
    }

    /**
     * 리프레시 토큰 유효성 검증 및 memberId 추출
     */
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
