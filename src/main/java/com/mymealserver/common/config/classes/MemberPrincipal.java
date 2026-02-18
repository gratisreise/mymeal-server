package com.mymealserver.common.config.classes;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security UserDetails implementation for JWT-based authentication.
 * Encapsulates the authenticated member's ID.
 */
@Getter
public class MemberPrincipal implements UserDetails {

    private final Long memberId;

    public MemberPrincipal(Long memberId) {
        this.memberId = memberId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // Can be extended for roles/permissions in the future
    }

    @Override
    public String getPassword() {
        return null; // Not applicable for JWT-based authentication
    }

    @Override
    public String getUsername() {
        return String.valueOf(memberId);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
