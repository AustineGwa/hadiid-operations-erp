package com.hadiid.erp.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** §K Stage Movement Analysis: how many stage-history rows ever arrived at each stage. */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StageArrivalDto {
    private String toStageLabel;
    private long arrivalCount;
}
