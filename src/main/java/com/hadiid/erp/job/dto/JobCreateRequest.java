package com.hadiid.erp.job.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Write shape for job intake (§I "Job Create"). */
@Data
public class JobCreateRequest {

    @NotBlank
    private String vehicleId;
    @NotBlank
    private String jobCardNo;
    @NotNull
    private LocalDate jobCardDate;
    @NotNull
    private Integer sectionId;
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

    // Customer: either an existing customerId, or enough fields to find-or-create one (Rule 15).
    private Long customerId;
    @NotBlank
    private String customerName;
    private String customerRegistrationNo;
    private String customerPhone;
    private String customerAddress;

    private String notes;
}
