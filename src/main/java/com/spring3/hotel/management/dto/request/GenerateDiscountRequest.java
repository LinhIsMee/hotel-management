package com.spring3.hotel.management.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateDiscountRequest {
    private String prefix;  // Tiền tố cho mã (tùy chọn)
    private String discountType;  // PERCENT hoặc FIXED
    private Double discountValue;
    private LocalDate validFrom;
    private LocalDate validTo;
    private int maxUses;
    private int count;  // Số lượng mã cần tạo
} 