package com.mymealserver.api.auth.dto.request;

import com.mymealserver.common.enums.ProviderType;
import com.mymealserver.common.validation.Password;
import com.mymealserver.domain.member.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * 회원가입 요청 DTO
 */
@Builder
public record RegisterRequest(

        @NotBlank(message = "{validation.email.notblank}")
        @Email(message = "{validation.email.invalid}")
        String email,

        @NotBlank(message = "{validation.password.notblank}")
        @Password(message = "{validation.password.pattern}")
        String password,

        @NotBlank(message = "{validation.name.notblank}")
        @Size(max = 50, message = "{validation.name.size.max}")
        String name,

        String fcmToken
) {
    /**
     * Member 엔티티로 변환
     *
     * @param encodedPassword 인코딩된 비밀번호
     * @return Member 엔티티
     */
    public Member toEntity(String encodedPassword) {
        return Member.builder()
                .email(email)
                .password(encodedPassword)
                .name(name)
                .provider(ProviderType.EMAIL)
                .isActive(true)
                .build();
    }
}
