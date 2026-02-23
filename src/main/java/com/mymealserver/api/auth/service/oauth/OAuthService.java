package com.mymealserver.api.auth.service.oauth;


import com.mymealserver.api.auth.dto.request.OAuthRequest;
import com.mymealserver.api.auth.dto.response.AuthResponse;
import com.mymealserver.common.enums.ProviderType;


public interface OAuthService {

    AuthResponse authenticate(OAuthRequest request);
    ProviderType getProvider();
}
