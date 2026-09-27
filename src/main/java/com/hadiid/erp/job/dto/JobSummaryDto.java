package com.hadiid.erp.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One row of the Job List screen (§I) — cheap to compute for many rows at once. */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class JobSummaryDto {
    private Long id;
    private String vehicleId;
    private String jobCardNo;
    private String sectionCode;
    private String sectionLabel;
    private String bodyTypeLabel;
    private String vehicleModel;
    private String stageCode;
    private String stageLabel;
    private LocalDate plannedStartDate;
    private LocalDate jobCardDate;
    private String customerName;
    private BigDecimal contractAmount;

    // Computed at read time (§C) — never stored.
    private Double productionPercent;
    private Double finishingPercent;
    private Integer workingDaysRemaining; // null renders as "-" (Not Started)
    private boolean dueSoon;
    private boolean overdue;
}
