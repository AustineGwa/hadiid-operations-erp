package com.hadiid.erp.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Read shape for the Users screen list/detail. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String roleCode;
    private String roleLabel;
    private String status;
    private String team;
    private LocalDateTime createdAt;
}
