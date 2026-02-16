package com.mymealserver.auth.service;

import com.mymealserver.auth.dto.response.AuthResponse;
import com.mymealserver.auth.dto.response.MemberResponse;
import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.config.JwtTokenProvider;
import com.mymealserver.domain.member.MemberReader;
import com.mymealserver.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * JWT 토큰 생성 및 검증 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberReader memberReader;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * 액세스 토큰 및 리프레시 토큰 생성
     */
    public AuthResponse generateTokens(Member member) {
        String accessToken = jwtTokenProvider.createAccessToken(member.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .member(MemberResponse.from(member))
                .build();
    }

    /**
     * 리프레시 토큰으로 새로운 액세스 토큰 발급
     */
    public AuthResponse refreshToken(String refreshToken) {
        // 1. 리프레시 토큰 블랙리스트 확인
        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        // 2. 리프레시 토큰 유효성 검증 및 memberId 추출
        Long memberId = jwtTokenProvider.validateRefreshTokenAndGetMemberId(refreshToken);

        // 3. 회원 조회
        Member member = memberReader.findById(memberId);

        // 4. 활성 상태 확인
        if (!member.isActive()) {
            throw new BusinessException(ErrorCode.MEMBER_DEACTIVATED);
        }

        // 5. 새로운 토큰 생성
        return generateTokens(member);
    }

    /**
     * 액세스 토큰 유효성 검증
     */
    public Long validateAccessToken(String token) {
        return jwtTokenProvider.validateAccessTokenAndGetMemberId(token);
    }

    /**
     * 리프레시 토큰 유효성 검증
     */
    public Long validateRefreshToken(String token) {
        return jwtTokenProvider.validateRefreshTokenAndGetMemberId(token);
    }
}
