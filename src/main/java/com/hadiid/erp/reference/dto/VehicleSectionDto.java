package com.hadiid.erp.reference.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class VehicleSectionDto {
    private Integer id;
    private String code;
    private String label;
}
