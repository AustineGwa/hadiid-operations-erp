package com.hadiid.erp.job.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Write shape for editing a job's non-stage fields (§I "Job Edit"). Stage is
 * never editable here — that only ever happens through StageTransitionService.
 */
@Data
public class JobEditRequest {
    @NotNull
    private Integer bodyTypeId;
    @NotBlank
    private String vehicleModel;
    @NotNull
    private LocalDate plannedStartDate;
    @Positive
    private int plannedProductionDays;
    @Positive
    private int plannedFinishingDays;
    private BigDecimal contractAmount;
    private String customerName;
    private String customerPhone;
    private String customerAddress;
    private String notes;
}
