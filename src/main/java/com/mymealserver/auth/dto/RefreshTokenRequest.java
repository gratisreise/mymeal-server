package com.mymealserver.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record RefreshTokenRequest(

        @NotBlank(message = "리프레시 토큰은 필수 항목입니다.")
        String refreshToken

) {
}
