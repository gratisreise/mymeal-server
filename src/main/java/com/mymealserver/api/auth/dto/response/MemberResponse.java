package com.mymealserver.api.auth.dto.response;

import com.mymealserver.domain.member.Member;
import com.mymealserver.common.enums.ProviderType;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record MemberResponse(

        Long id,

        String email,

        String name,

        String profileImage,

        ProviderType provider,

        boolean isActive,

        LocalDateTime lastLoginAt

) {
    public static MemberResponse from(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .profileImage(member.getProfileImage())
                .provider(member.getProvider())
                .isActive(member.isActive())
                .lastLoginAt(member.getLastLoginAt())
                .build();
    }
}
