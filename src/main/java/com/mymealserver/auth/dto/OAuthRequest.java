package com.mymealserver.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record OAuthRequest(

        @NotBlank(message = "OAuth 토큰은 필수 항목입니다.")
        String token,

        String fcmToken

) {
}
