package com.hadiid.erp.reference.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One allowed (from_stage -> to_stage) move, and what it takes to perform it. Backs §H.1. */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StageTransitionRuleDto {
    private Integer fromStageId; // null = job creation
    private Integer toStageId;
    private String requiresPermissionCode;
    private boolean requiresReason;
}
