package com.hadiid.erp.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** The full Job Detail / "Vehicle Job Screen" read model (§39 of the brief / §I of the spec). */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class JobDetailDto {
    private Long id;
    private String vehicleId;
    private String jobCardNo;
    private LocalDate jobCardDate;
    private Integer sectionId;
    private String sectionCode;
    private String sectionLabel;
    private Integer bodyTypeId;
    private String bodyTypeLabel;
    private String vehicleModel;

    private LocalDate plannedStartDate;
    private Integer plannedProductionDays;
    private Integer plannedFinishingDays;

    private Integer currentStageId;
    private String stageCode;
    private String stageLabel;
    private LocalDateTime stageEnteredAt;

    private LocalDate internalStoreDate;
    private LocalDate releaseDate;

    private BigDecimal contractAmount;

    private Long customerId;
    private String customerNameSnapshot;
    private String customerRegSnapshot;
    private String customerPhoneSnapshot;
    private String customerAddressSnapshot;

    private String notes;

    // Computed (§C) — never stored.
    private Double productionPercent;
    private Double finishingPercent;
    private Integer workingDaysRemaining;
    private Integer totalFabricationDays; // null until internalStoreDate is set
    private Integer totalTurnaroundDays;  // null until releaseDate is set
}
