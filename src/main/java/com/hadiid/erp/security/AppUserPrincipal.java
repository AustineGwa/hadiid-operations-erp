package com.hadiid.erp.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * The authenticated principal. Extends Spring Security's User so the standard
 * form-login machinery works unchanged, while carrying the extra fields the
 * UI/services need (id, full name, role code) without a second lookup per request.
 */
@Getter
public class AppUserPrincipal extends User {

    private final Long userId;
    private final String fullName;
    private final String roleCode;

    public AppUserPrincipal(Long userId, String username, String passwordHash, String fullName,
                             String roleCode, Collection<? extends GrantedAuthority> authorities,
                             boolean enabled) {
        super(username, passwordHash, enabled, true, true, true, authorities);
        this.userId = userId;
        this.fullName = fullName;
        this.roleCode = roleCode;
    }
}
