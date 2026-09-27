package com.hadiid.erp.reference.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentMethodDto {
    private Integer id;
    private String code;
    private String label;
    private boolean active;
}
