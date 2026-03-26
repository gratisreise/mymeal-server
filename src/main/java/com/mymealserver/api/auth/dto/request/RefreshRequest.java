package com.mymealserver.api.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record RefreshRequest(
    @NotBlank(message = "{validation.refreshtoken.notblank}") String refreshToken) {}
