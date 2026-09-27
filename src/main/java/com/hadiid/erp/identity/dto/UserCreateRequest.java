package com.hadiid.erp.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String rawPassword;
    private Integer roleId;
    private String team;
}
