package com.mymealserver.api.auth.service.oauth;

import com.mymealserver.api.auth.dto.request.OAuthRequest;
import com.mymealserver.api.auth.dto.response.LoginResponse;
import com.mymealserver.common.enums.ProviderType;

public interface OAuthService {

  LoginResponse authenticate(OAuthRequest request);

  ProviderType getProvider();
}
