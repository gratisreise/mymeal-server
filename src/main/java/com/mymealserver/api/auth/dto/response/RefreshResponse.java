package com.mymealserver.api.auth.dto.response;

import lombok.Builder;

@Builder
public record RefreshResponse(
    String accessToken, String refreshToken
) {

    public static RefreshResponse of(String newAT, String newRT) {
        return RefreshResponse.builder()
            .accessToken(newAT)
            .refreshToken(newRT)
            .build();
    }
}
