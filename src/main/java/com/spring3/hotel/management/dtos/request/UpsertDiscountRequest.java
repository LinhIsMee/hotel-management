package com.spring3.hotel.management.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UpsertDiscountRequest {
    private String discountType;
    private Double discountValue;
    private String validFrom;
    private String validTo;
    private int maxUsage;
}
