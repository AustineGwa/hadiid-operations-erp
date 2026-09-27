package com.hadiid.erp.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CustomerDto {
    private Long id;
    private String name;
    private String registrationNo;
    private String phone;
    private String address;
    private Integer jobCount;
}
