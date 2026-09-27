package com.hadiid.erp.security;

import com.hadiid.erp.identity.UserRepository;
import com.hadiid.erp.identity.dto.AuthUserRecord;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Loads a user for Spring Security and turns their role's permissions into
 * granted authorities — @PreAuthorize("hasAuthority('JOB_ADVANCE_STAGE')") etc.
 * throughout the app reads directly off this set. DISABLED users are rejected
 * here (account-status check), not just hidden in the UI.
 */
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AuthUserRecord record = userRepository.findAuthRecordByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No such user: " + username));

        List<String> permissionCodes = userRepository.findPermissionCodesForRole(record.getRoleCode());
        List<GrantedAuthority> authorities = permissionCodes.stream()
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();

        return new AppUserPrincipal(
                record.getId(), record.getUsername(), record.getPasswordHash(),
                record.getFullName(), record.getRoleCode(), authorities,
                "ACTIVE".equals(record.getStatus()));
    }
}
