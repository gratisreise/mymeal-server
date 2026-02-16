package com.mymealserver.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record OAuthRequest(

        @NotBlank(message = "{validation.oauth.token.notblank}")
        String token,

        String fcmToken

) {
}
