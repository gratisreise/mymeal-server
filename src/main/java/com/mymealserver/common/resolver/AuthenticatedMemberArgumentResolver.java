package com.mymealserver.common.resolver;

import com.mymealserver.common.annotation.AuthenticatedMember;
import com.mymealserver.common.security.MemberPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Custom {@link HandlerMethodArgumentResolver} to resolve {@link AuthenticatedMember} annotated parameters.
 * Extracts the member ID from {@link MemberPrincipal} in the {@link SecurityContextHolder}.
 */
@Slf4j
@Component
public class AuthenticatedMemberArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthenticatedMember.class)
                && parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("Authentication not found in SecurityContext");
            throw new AuthenticationCredentialsNotFoundException("Authentication not found");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof MemberPrincipal memberPrincipal) {
            log.debug("Extracted memberId: {} from MemberPrincipal", memberPrincipal.getMemberId());
            return memberPrincipal.getMemberId();
        }

        log.error("Principal must be MemberPrincipal, but was: {}", principal.getClass());
        throw new IllegalArgumentException(
                "Principal must be MemberPrincipal, but was: " + principal.getClass()
        );
    }
}
