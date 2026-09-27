package com.hadiid.erp.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** One row of a job's stage-history timeline (§I "History" tab). */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StageHistoryEntryDto {
    private Long id;
    private String fromStageLabel; // null for the creation row
    private String toStageLabel;
    private LocalDateTime changedAt;
    private int daysHeld;
    private String changedByFullName;
    private boolean correction;
    private String note;
}
