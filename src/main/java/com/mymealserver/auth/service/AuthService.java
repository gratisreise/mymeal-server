package com.mymealserver.auth.service;

import com.mymealserver.auth.dto.request.LoginRequest;
import com.mymealserver.auth.dto.request.RegisterRequest;
import com.mymealserver.auth.dto.request.WithdrawRequest;
import com.mymealserver.auth.dto.response.AuthResponse;
import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.domain.member.MemberReader;
import com.mymealserver.domain.member.MemberWriter;
import com.mymealserver.domain.member.MemberSettingsWriter;
import com.mymealserver.entity.Member;
import com.mymealserver.entity.MemberSettings;
import com.mymealserver.entity.MemberWithdrawal;
import com.mymealserver.repository.MemberWithdrawalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 서비스 (이메일 회원가입, 로그인, 로그아웃, 탈퇴)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberReader memberReader;
    private final MemberWriter memberWriter;
    private final MemberSettingsWriter memberSettingsWriter;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final MemberWithdrawalRepository memberWithdrawalRepository;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * 이메일 회원가입
     */
    @Transactional
    public void register(RegisterRequest request) {
        log.info("이메일 회원가입 시도: {}", request.email());

        // 1. 이메일 중복 체크
        if (memberReader.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.MEMBER_EMAIL_ALREADY_EXISTS);
        }

        // 2. 비밀번호 인코딩
        String encodedPassword = passwordEncoder.encode(request.password());

        // 3. Member 엔티티 생성 (DTO의 toEntity 메서드 사용)
        Member member = request.toEntity(encodedPassword);
        member = memberWriter.save(member);
        log.info("회원 생성 성공 - ID: {}", member.getId());

        // 4. 기본 설정 생성
        MemberSettings settings = MemberSettings.createDefault(member);
        if (request.fcmToken() != null) {
            settings.updateFcmToken(request.fcmToken());
        }
        memberSettingsWriter.save(settings);
        log.info("기본 설정 생성 완료 - 회원 ID: {}", member.getId());
    }

    /**
     * 이메일 로그인
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("이메일 로그인 시도: {}", request.email());

        // 1. 이메일로 회원 조회
        Member member = memberReader.findByEmail(request.email());

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 3. 활성 상태 확인
        if (!member.isActive()) {
            throw new BusinessException(ErrorCode.MEMBER_DEACTIVATED);
        }

        // 4. 마지막 로그인 시간 업데이트
        member.updateLastLoginAt();
        memberWriter.save(member);

        // 5. FCM 토큰 업데이트 (제공된 경우)
        if (request.fcmToken() != null) {
            memberSettingsWriter.updateFcmToken(member.getId(), request.fcmToken());
        }

        log.info("로그인 성공 - 회원 ID: {}", member.getId());

        // 6. 토큰 생성
        return tokenService.generateTokens(member);
    }

    /**
     * 로그아웃
     */
    @Transactional
    public void logout(Long memberId, String refreshToken) {
        log.info("로그아웃 - 회원 ID: {}", memberId);

        // 리프레시 토큰을 블랙리스트에 추가하여 즉시 무효화
        if (refreshToken != null) {
            tokenBlacklistService.addToBlacklist(refreshToken);
            log.info("리프레시 토큰 블랙리스트 추가 완료 - 회원 ID: {}", memberId);
        }

        log.info("로그아웃 완료 - 회원 ID: {}", memberId);
    }

    /**
     * 회원 탈퇴
     */
    @Transactional
    public void withdraw(Long memberId, WithdrawRequest request) {
        log.info("회원 탈퇴 - 회원 ID: {}", memberId);

        Member member = memberReader.findById(memberId);

        // 1. 탈퇴 기록 생성 (DTO의 toEntity 메서드 사용)
        MemberWithdrawal withdrawal = request.toEntity(memberId);

        memberWithdrawalRepository.save(withdrawal);
        log.info("탈퇴 기록 생성 완료 - 회원 ID: {}", memberId);

        // 2. 회원 비활성화 및 소프트 삭제
        member.deactivate();
        memberWriter.delete(member);

        log.info("회원 탈퇴 완료 - 회원 ID: {}", memberId);
    }
}
