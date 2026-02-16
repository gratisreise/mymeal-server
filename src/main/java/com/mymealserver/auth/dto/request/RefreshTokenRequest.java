package com.mymealserver.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record RefreshTokenRequest(

        @NotBlank(message = "{validation.refreshtoken.notblank}")
        String refreshToken

) {
}
