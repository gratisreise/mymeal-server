package com.mymealserver.auth.service.client.dto;

import lombok.Builder;

/**
 * Internal DTO for OAuth token request
 * Used for API client communication only (not exposed to API layer)
 */
@Builder
public record TokenRequest(
        String code,
        String clientId,
        String clientSecret,
        String redirectUri,
        String grantType
) {
}
