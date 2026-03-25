package com.mymealserver.api.auth.dto.request;


import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
    @NotBlank(message = "{validation.refreshtoken.notblank}")
    String accessToken
) { }
