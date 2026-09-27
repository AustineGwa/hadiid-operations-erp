package com.hadiid.erp.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** §J Overview dashboard KPI strip — all computed at read time, nothing stored. */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OverviewSummaryDto {
    private long totalJobs;
    private long releasedJobs;
    private long dueSoonCount;
    private long overdueCount;
}
