package com.mymealserver.domain.MemberSettings;

import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberSettingsReader {

    private final MemberSettingsRepository memberSettingsRepository;

    public MemberSettings findByMemberId(Long memberId) {
        return memberSettingsRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SETTINGS_NOT_FOUND));
    }

    public MemberSettings findByMemberIdOrNull(Long memberId) {
        return memberSettingsRepository.findByMemberId(memberId).orElse(null);
    }

    public boolean existsByMemberId(Long memberId) {
        return memberSettingsRepository.findByMemberId(memberId).isPresent();
    }
}
