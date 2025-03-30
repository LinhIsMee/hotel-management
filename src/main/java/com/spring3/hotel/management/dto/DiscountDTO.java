package com.spring3.hotel.management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountDTO {
    private Integer id;
    private String code;
    private String discountType;  // PERCENT hoặc FIXED
    private Double discountValue;
    private LocalDate validFrom;
    private LocalDate validTo;
    private int maxUses;
    private int usedCount;
    
    // Trường bổ sung chỉ có trong DTO để kiểm tra tính hợp lệ
    private boolean isValid;
} 