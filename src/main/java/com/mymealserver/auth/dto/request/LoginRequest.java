package com.mymealserver.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record LoginRequest(

        @NotBlank(message = "{validation.email.notblank}")
        @Email(message = "{validation.email.invalid}")
        String email,

        @NotBlank(message = "{validation.password.notblank}")
        String password,

        String fcmToken

) {
}
