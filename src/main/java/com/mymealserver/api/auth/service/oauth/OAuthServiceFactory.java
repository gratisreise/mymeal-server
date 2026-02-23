package com.mymealserver.api.auth.service.oauth.factory;

import com.mymealserver.api.auth.dto.request.OAuthRequest;
import com.mymealserver.api.auth.service.oauth.OAuthService;
import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.common.enums.ProviderType;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class OAuthServiceFactory {

    private final Map<ProviderType, OAuthService> services;

    public OAuthServiceFactory(List<OAuthService> serviceList) {
        Map<ProviderType, OAuthService> tempMap = new EnumMap<>(ProviderType.class);

        for (OAuthService service : serviceList) {
            tempMap.put(service.getProvider(), service);
        }

        this.services = Collections.unmodifiableMap(tempMap);
    }

    public OAuthService getOAuthService(OAuthRequest request) {
        // Get provider from request body
        ProviderType provider = request.provider();
        OAuthService service = services.get(provider);
        if (service == null) {
            log.warn("지원되지 않는 플랫폼입니다. : {}", provider);
            throw new BusinessException(ErrorCode.OAUTH_UNSUPPORTED_PROVIDER);
        }
        return service;
    }
}
