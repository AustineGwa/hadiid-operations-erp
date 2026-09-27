package com.hadiid.erp.job.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/** Server-side search/filter criteria for the Job List screen (§40, §41). */
@Data
@Builder
public class JobSearchCriteria {
    private String searchText; // matches Vehicle ID, Job Card No., Customer Name/Reg, Telephone, Model
    private Integer sectionId;
    private Integer bodyTypeId;
    private Integer stageId;
    private LocalDate plannedStartFrom;
    private LocalDate plannedStartTo;
    private LocalDate jobCardDateFrom;
    private LocalDate jobCardDateTo;
    private Boolean dueSoon;
    private Boolean overdue;
    private Boolean releasedOnly;
}
