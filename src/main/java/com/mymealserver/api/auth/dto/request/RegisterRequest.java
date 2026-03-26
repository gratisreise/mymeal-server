package com.mymealserver.api.auth.dto.request;

import com.mymealserver.common.annotation.Password;
import com.mymealserver.common.enums.ProviderType;
import com.mymealserver.domain.member.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

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
    String fcmToken) {
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
