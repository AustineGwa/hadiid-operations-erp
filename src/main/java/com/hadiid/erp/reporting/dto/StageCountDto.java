package com.hadiid.erp.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** §J Overview/Operations dashboard: live count of jobs per (section, stage). */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StageCountDto {
    private String sectionCode;
    private String stageCode;
    private long count;
}
