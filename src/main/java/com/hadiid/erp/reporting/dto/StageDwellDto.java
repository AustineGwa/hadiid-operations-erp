package com.hadiid.erp.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** §K Stage Movement Analysis: how long jobs dwell in a stage before moving on. */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StageDwellDto {
    private String fromStageLabel;
    private double avgDaysHeld;
    private int maxDaysHeld;
    private long transitionCount;
}
