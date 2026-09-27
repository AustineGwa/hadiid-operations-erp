package com.hadiid.erp.labor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentDto {
    private Long id;
    private Long assignmentId;
    private String workerName;
    private BigDecimal amountPaid;
    private LocalDate datePaid;
    private String paymentMethodLabel;
    private String paidByFullName;
    private String referenceNote;
}
