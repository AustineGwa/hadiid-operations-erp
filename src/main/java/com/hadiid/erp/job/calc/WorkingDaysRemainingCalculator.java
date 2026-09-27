package com.hadiid.erp.job.calc;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Excel: Working Days Remaining (current stage) =
 *   IF(Stage="In Production", MAX(0, NETWORKDAYS(TODAY(), StageEnteredDate+PlannedProductionDays) - 1),
 *      IF(Stage="At Finishing Stage", MAX(0, NETWORKDAYS(TODAY(), StageEnteredDate+PlannedFinishingDays) - 1),
 *         IF(Stage="Not Started", "-", 0)))
 * Uses working days (§H.2), not calendar days — deliberately a different clock
 * than Days Held on the stage-history log, which is calendar days (§C).
 * Returns null for "Not Started" (renders as "-" in the UI, matching Excel).
 */
@Component
public class WorkingDaysRemainingCalculator {

    private final WorkingDayCalendar calendar;

    public WorkingDaysRemainingCalculator(WorkingDayCalendar calendar) {
        this.calendar = calendar;
    }

    public Integer calculate(String stageCode, LocalDateTime stageEnteredAt,
                              int plannedProductionDays, int plannedFinishingDays, LocalDate today) {
        if ("NOT_STARTED".equals(stageCode)) {
            return null;
        }
        if ("IN_PRODUCTION".equals(stageCode)) {
            LocalDate windowEnd = stageEnteredAt.toLocalDate().plusDays(plannedProductionDays);
            return Math.max(0, calendar.networkDays(today, windowEnd) - 1);
        }
        if ("AT_FINISHING".equals(stageCode)) {
            LocalDate windowEnd = stageEnteredAt.toLocalDate().plusDays(plannedFinishingDays);
            return Math.max(0, calendar.networkDays(today, windowEnd) - 1);
        }
        return 0; // At Delivery/In Store or Released
    }
}
