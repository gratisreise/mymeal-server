package com.mymealserver.auth.service;

import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.domain.member.MemberReader;
import com.mymealserver.domain.member.MemberWriter;
import com.mymealserver.auth.dto.AuthResponse;
import com.mymealserver.auth.dto.LoginRequest;
import com.mymealserver.auth.dto.RegisterRequest;
import com.mymealserver.auth.dto.WithdrawRequest;
import com.mymealserver.entity.Member;
import com.mymealserver.entity.MemberSettings;
import com.mymealserver.entity.MemberWithdrawal;
import com.mymealserver.entity.enums.ProviderType;
import com.mymealserver.entity.enums.WithdrawalReason;
import com.mymealserver.repository.MemberSettingsRepository;
import com.mymealserver.repository.MemberWithdrawalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberReader memberReader;
    private final MemberWriter memberWriter;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final MemberSettingsRepository memberSettingsRepository;
    private final MemberWithdrawalRepository memberWithdrawalRepository;

    @Transactional
    public void register(RegisterRequest request) {
        log.info("Registering new member with email: {}", request.email());

        // 1. Validate email not already registered
        if (memberReader.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.MEMBER_EMAIL_ALREADY_EXISTS);
        }

        // 2. Validate password (already validated by @Pattern annotation)
        // Additional validation can be added here if needed

        // 3. Create member
        Member member = Member.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .provider(ProviderType.EMAIL)
                .isActive(true)
                .build();

        member = memberWriter.save(member);
        log.info("Member created successfully with id: {}", member.getId());

        // 4. Create default settings
        MemberSettings settings = MemberSettings.createDefault(member);
        if (request.fcmToken() != null) {
            settings.updateFcmToken(request.fcmToken());
        }
        memberSettingsRepository.save(settings);
        log.info("Default settings created for member: {}", member.getId());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.email());

        // 1. Find member by email
        Member member = memberReader.findByEmail(request.email());

        // 2. Verify password
        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 3. Check if active
        if (!member.isActive()) {
            throw new BusinessException(ErrorCode.MEMBER_DEACTIVATED);
        }

        // 4. Update last login
        member.updateLastLoginAt();
        memberWriter.save(member);

        // 5. Update FCM token if provided
        if (request.fcmToken() != null) {
            updateFcmToken(member.getId(), request.fcmToken());
        }

        log.info("Member logged in successfully: {}", member.getId());

        // 6. Generate tokens
        return tokenService.generateTokens(member);
    }

    @Transactional
    public void logout(Long memberId) {
        log.info("Member logging out: {}", memberId);

        // For now, client-side token removal is sufficient
        // In a production environment, you might want to:
        // - Add the token to a blacklist in Redis
        // - Remove the refresh token from database
        // - etc.

        log.info("Member logged out successfully: {}", memberId);
    }

    @Transactional
    public void withdraw(Long memberId, WithdrawRequest request) {
        log.info("Member withdrawing: {}", memberId);

        Member member = memberReader.findById(memberId);

        // 1. Parse withdrawal reason (defaults to OTHER if invalid)
        WithdrawalReason reason = WithdrawalReason.fromString(request.reason());

        // 2. Create withdrawal record
        MemberWithdrawal withdrawal = MemberWithdrawal.builder()
                .memberId(memberId)
                .reason(reason)
                .reasonDetail(request.reasonDetail())
                .build();

        memberWithdrawalRepository.save(withdrawal);
        log.info("Withdrawal record created for member: {}", memberId);

        // 3. Deactivate and soft delete member
        member.deactivate();
        memberWriter.delete(member);

        log.info("Member withdrawn successfully: {}", memberId);
    }

    private void updateFcmToken(Long memberId, String fcmToken) {
        MemberSettings settings = memberSettingsRepository.findByMemberId(memberId)
                .orElse(null);

        if (settings != null) {
            settings.updateFcmToken(fcmToken);
            memberSettingsRepository.save(settings);
        } else {
            // Create settings if not exists
            Member member = memberReader.findById(memberId);
            MemberSettings newSettings = MemberSettings.createDefault(member);
            newSettings.updateFcmToken(fcmToken);
            memberSettingsRepository.save(newSettings);
        }
    }
}
