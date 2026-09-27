package com.hadiid.erp.planning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * §E.8 Production Planning: Planned to Start/Complete and Actual Released are
 * computed at read time from fabrication_jobs — never stored (Rule 17).
 * Only monthlyCapacityTarget is a real column (production_plan_targets).
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PlanTargetDto {
    private Integer sectionId;
    private String sectionLabel;
    private LocalDate planMonth; // first day of month
    private int monthlyCapacityTarget;
    private long plannedToStart;   // jobs whose planned_start_date falls in this month
    private long actualReleased;   // jobs whose release_date falls in this month
    private long variance;         // actualReleased - monthlyCapacityTarget
}
