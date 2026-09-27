package com.hadiid.erp.planning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * §E.8: materials_budget/materials_actual/labor_budget are stored (manual entry
 * today, see spec §H.6). laborActual is always computed from labor_payments —
 * never a stored column — matching the "no Labor Actual column" note in V7.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class WeeklyBudgetDto {
    private Integer sectionId;
    private String sectionLabel;
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private BigDecimal materialsBudget;
    private BigDecimal materialsActual;
    private BigDecimal laborBudget;
    private BigDecimal laborActual; // computed: SUM(labor_payments.amount_paid) for this section/week
}
