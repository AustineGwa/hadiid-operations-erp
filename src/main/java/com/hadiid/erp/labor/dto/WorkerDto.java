package com.hadiid.erp.labor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class WorkerDto {
    private Long id;
    private String name;
    private String idNumber;
    private String phone;
    private Long contractorId;
    private String contractorName;
}
