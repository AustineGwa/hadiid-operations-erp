package com.hadiid.erp.common.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDto {
    private Long id;
    private String userFullName;
    private String action;
    private String module;
    private Long recordId;
    private LocalDateTime occurredAt;
    private String oldValue;
    private String newValue;
    private String reason;
}
