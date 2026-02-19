package com.mymealserver.api.auth.dto.request;

import com.mymealserver.entity.enums.ProviderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * OAuth login request using authorization code flow
 */
@Builder
public record OAuthRequest(

        @NotNull(message = "{validation.oauth.provider.notnull}")
        ProviderType provider,

        @NotBlank(message = "{validation.oauth.code.notblank}")
        String code,

        String fcmToken

) {
}
