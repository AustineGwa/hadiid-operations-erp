package com.hadiid.erp.labor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * One worker's assignment to a job/stage, with amount paid and balance
 * computed from labor_payments (§C: Balance = MAX(0, Agreed - Paid), a
 * worker-level figure — distinct from the job-card-level Balance in
 * PaymentSummaryDto, which is against Contract Amount).
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AssignmentDto {
    private Long id;
    private Long jobId;
    private String vehicleId;
    private String stageCode;
    private String stageLabel;
    private Long workerId;
    private String workerName;
    private String workerIdNumber;
    private String contractorName;
    private BigDecimal agreedAmount;
    private BigDecimal amountPaid;   // SUM(labor_payments.amount_paid), computed
    private BigDecimal balance;      // MAX(0, agreed - paid), computed
}
