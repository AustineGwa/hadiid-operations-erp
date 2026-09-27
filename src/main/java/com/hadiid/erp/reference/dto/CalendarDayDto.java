package com.hadiid.erp.reference.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** One row of the production calendar (§B.10 / §H.4 — "Day Status" from the Daily Schedule tab). */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CalendarDayDto {
    private LocalDate date;
    private String status; // WORKING (default when no row exists) / NO_WORK / ON_HOLD
    private String note;
}
