package com.hadiid.erp.labor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ContractorDto {
    private Long id;
    private String name;
    private String registrationNo;
    private String phone;
}
