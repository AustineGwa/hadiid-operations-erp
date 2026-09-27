package com.hadiid.erp.labor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * §B.5 / §C: job-card-level rollup. Balance and % Paid are against the job's
 * Contract Amount — NOT against the sum of worker agreed amounts (those two
 * figures can legitimately differ). Never a stored table (Rule 17) — always
 * computed from fabrication_jobs.contract_amount + labor_payments.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentSummaryDto {
    private String jobCardNo;
    private String sectionLabel;
    private String vehicleId;
    private BigDecimal contractAmount;
    private BigDecimal totalWorkerWagesAgreed; // SUM(agreed_amount) — informational, not the Balance basis
    private BigDecimal totalPaid;              // SUM(amount_paid)
    private BigDecimal balance;                // contractAmount - totalPaid
    private Double percentPaid;                // totalPaid / contractAmount
}
