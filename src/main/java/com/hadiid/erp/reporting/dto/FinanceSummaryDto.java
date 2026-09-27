package com.hadiid.erp.reporting.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * §J Finance dashboard rollup, per section. Balance basis is Contract Amount,
 * matching PaymentSummaryDto's job-card-level rule — never worker wages agreed.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FinanceSummaryDto {
    private String sectionLabel;
    private BigDecimal totalContractAmount;
    private BigDecimal totalPaid;
    private BigDecimal totalBalance;
    private long jobCount;
}
