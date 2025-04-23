package com.spring3.hotel.management.dto.request;

import com.spring3.hotel.management.enums.ServiceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpsertServiceRequest {
    
    @NotBlank(message = "Tên dịch vụ không được để trống")
    private String name;
    
    @NotBlank(message = "Mã dịch vụ không được để trống")
    private String code;
    
    @NotNull(message = "Loại dịch vụ không được để trống")
    private ServiceType type;
    
    private String description;
    
    @NotNull(message = "Giá dịch vụ không được để trống")
    @Positive(message = "Giá dịch vụ phải là số dương")
    private BigDecimal price;
    
    private Boolean isAvailable;
    
    private String imageUrl;
} 
