package com.hadiid.erp.job.calc;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Excel: Total Fabrication Days = Internal Store Date - Planned Start Date (only
 * once Internal Store Date exists); Total Turnaround Days = Release Date -
 * Planned Start Date (only once Release Date exists). Two distinct business
 * metrics (§14 of the brief / §C of the spec) — never conflate them.
 */
@Component
public class DurationCalculator {

    public Integer totalFabricationDays(LocalDate plannedStartDate, LocalDate internalStoreDate) {
        return internalStoreDate == null ? null : (int) ChronoUnit.DAYS.between(plannedStartDate, internalStoreDate);
    }

    public Integer totalTurnaroundDays(LocalDate plannedStartDate, LocalDate releaseDate) {
        return releaseDate == null ? null : (int) ChronoUnit.DAYS.between(plannedStartDate, releaseDate);
    }
}
