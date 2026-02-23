package com.mymealserver.domain.member;

import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.common.enums.ProviderType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberReader {

    private final MemberRepository memberRepository;

    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public Member findByEmail(String email) {
        return memberRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public boolean existsByEmail(String email) {
        return memberRepository.existsByEmailAndDeletedAtIsNull(email);
    }

    public Member findByProviderAndProviderId(ProviderType provider, String providerId) {
        return memberRepository.findByProviderAndProviderIdAndDeletedAtIsNull(provider, providerId)
                .orElse(null);
    }
}
