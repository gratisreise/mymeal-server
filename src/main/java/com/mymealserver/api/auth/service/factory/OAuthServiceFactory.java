package com.mymealserver.auth.service.factory;

import com.mymealserver.auth.service.OAuthService;
import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.entity.enums.ProviderType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory for retrieving provider-specific OAuth services
 */
@Slf4j
@Component
public class OAuthServiceFactory {

    private final Map<ProviderType, OAuthService> services;

    /**
     * Constructor injection with Map of all OAuthService beans
     * Spring automatically injects all beans implementing OAuthService interface
     * keyed by their ProviderType returned from getProvider() method
     */
    public OAuthServiceFactory(List<OAuthService> serviceList) {
        this.services = serviceList.stream()
                .collect(Collectors.toMap(
                        OAuthService::getProvider,
                        Function.identity()
                ));
        log.info("Initialized OAuthServiceFactory with {} providers: {}",
                services.size(), services.keySet());
    }

    /**
     * Get OAuth service for specified provider
     *
     * @param provider Provider type
     * @return OAuth service for the provider
     * @throws BusinessException if provider is not supported
     */
    public OAuthService getOAuthService(ProviderType provider) {
        OAuthService service = services.get(provider);
        if (service == null) {
            log.warn("Unsupported OAuth provider requested: {}", provider);
            throw new BusinessException(ErrorCode.OAUTH_UNSUPPORTED_PROVIDER);
        }
        return service;
    }
}
