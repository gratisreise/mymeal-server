package com.mymealserver.api.auth.service.factory;

import com.mymealserver.api.auth.dto.request.OAuthRequest;
import com.mymealserver.api.auth.service.OAuthService;
import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.entity.enums.ProviderType;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OAuthServiceFactory {

    private final Map<ProviderType, OAuthService> services;

    public OAuthServiceFactory(List<OAuthService> serviceList) {
        this.services = serviceList.stream()
                .collect(Collectors.toMap(
                        OAuthService::getProvider,
                        Function.identity()
                ));
        log.info("Initialized OAuthServiceFactory with {} providers: {}",
                services.size(), services.keySet());
    }

    public OAuthService getOAuthService(OAuthRequest request) {
        // Get provider from request body
        ProviderType provider = request.provider();
        OAuthService service = services.get(provider);
        if (service == null) {
            log.warn("Unsupported OAuth provider requested: {}", provider);
            throw new BusinessException(ErrorCode.OAUTH_UNSUPPORTED_PROVIDER);
        }
        return service;
    }
}
