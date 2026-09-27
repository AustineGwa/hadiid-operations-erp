package com.hadiid.erp.reference.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BodyTypeDto {
    private Integer id;
    private Integer sectionId;
    private String sectionCode;
    private String code;
    private String label;
    private boolean active;
}
