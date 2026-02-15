package com.mymealserver.auth.service;

import com.mymealserver.config.JwtTokenProvider;
import com.mymealserver.domain.member.MemberReader;
import com.mymealserver.auth.dto.AuthResponse;
import com.mymealserver.auth.dto.MemberResponse;
import com.mymealserver.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberReader memberReader;

    public AuthResponse generateTokens(Member member) {
        String accessToken = jwtTokenProvider.createAccessToken(member.getEmail(), member.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .member(MemberResponse.from(member))
                .build();
    }

    public AuthResponse refreshToken(String refreshToken) {
        // Validate refresh token and extract memberId
        Long memberId = jwtTokenProvider.validateRefreshTokenAndGetMemberId(refreshToken);

        // Find member
        Member member = memberReader.findById(memberId);

        // Generate new tokens
        return generateTokens(member);
    }

    public Long validateAccessToken(String token) {
        return jwtTokenProvider.validateAccessTokenAndGetMemberId(token);
    }

    public Long validateRefreshToken(String token) {
        return jwtTokenProvider.validateRefreshTokenAndGetMemberId(token);
    }
}
