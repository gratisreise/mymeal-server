package com.mymealserver.api.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record LogoutRequest(
    @NotBlank(message = "{validation.accesstoken.notblank}") String accessToken) {}
