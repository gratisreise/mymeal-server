package com.mymealserver.api.auth.service.oauth;

import com.mymealserver.api.auth.dto.request.OAuthRequest;
import com.mymealserver.common.enums.ProviderType;
import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
        ProviderType provider = request.provider();
        OAuthService service = services.get(provider);
        if (service == null) {
            log.warn("지원되지 않는 플랫폼입니다. : {}", provider);
            throw new BusinessException(ErrorCode.OAUTH_UNSUPPORTED_PROVIDER);
        }
        return service;
    }
}
