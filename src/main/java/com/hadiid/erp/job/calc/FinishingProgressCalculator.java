package com.hadiid.erp.job.calc;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Excel: Finishing % Complete — same linear-ramp shape as Production %, keyed
 * off PlannedFinishingDays, only ramping while Stage = "At Finishing Stage";
 * 0 while earlier than that, 1 once past it (§C).
 */
@Component
public class FinishingProgressCalculator {

    public double calculate(String stageCode, LocalDateTime stageEnteredAt, int plannedFinishingDays, LocalDate today) {
        if ("NOT_STARTED".equals(stageCode) || "IN_PRODUCTION".equals(stageCode)) {
            return 0.0;
        }
        if ("AT_FINISHING".equals(stageCode)) {
            long elapsedDays = ChronoUnit.DAYS.between(stageEnteredAt.toLocalDate(), today);
            double ratio = plannedFinishingDays == 0 ? 0 : (double) elapsedDays / plannedFinishingDays;
            return Math.min(1.0, Math.max(0.0, ratio));
        }
        return 1.0;
    }
}
