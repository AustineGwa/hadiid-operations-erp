package com.hadiid.erp.job.calc;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Excel: Production % Complete =
 *   IF(Stage="Not Started", 0,
 *      IF(Stage="In Production", MIN(1,MAX(0,(TODAY()-StageEnteredDate)/PlannedProductionDays)), 1))
 * Business rule: progress through the production phase is a linear time ramp
 * from the moment the job entered "In Production," capped at the planned
 * duration; reads 100% once the job has moved past that stage (§C).
 */
@Component
public class ProductionProgressCalculator {

    public double calculate(String stageCode, LocalDateTime stageEnteredAt, int plannedProductionDays, LocalDate today) {
        if ("NOT_STARTED".equals(stageCode)) {
            return 0.0;
        }
        if ("IN_PRODUCTION".equals(stageCode)) {
            long elapsedDays = ChronoUnit.DAYS.between(stageEnteredAt.toLocalDate(), today);
            double ratio = plannedProductionDays == 0 ? 0 : (double) elapsedDays / plannedProductionDays;
            return Math.min(1.0, Math.max(0.0, ratio));
        }
        return 1.0; // already past this phase
    }
}
