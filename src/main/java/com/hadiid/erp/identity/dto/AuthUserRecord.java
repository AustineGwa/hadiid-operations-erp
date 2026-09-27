package com.hadiid.erp.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Everything Spring Security needs to authenticate + authorize one user,
 * loaded in a single JOIN so login is one round trip, not N+1.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthUserRecord {
    private Long id;
    private String username;
    private String fullName;
    private String passwordHash;
    private String roleCode;
    private String status;
}
