package com.spring3.hotel.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpsertServiceRequest {
    
    @NotBlank(message = "Tên dịch vụ không được để trống")
    private String name;
    
    @NotBlank(message = "Mã dịch vụ không được để trống")
    private String code;
    
    @NotBlank(message = "Loại dịch vụ không được để trống")
    private String type;
    
    private String description;
    
    @NotNull(message = "Giá dịch vụ không được để trống")
    @Positive(message = "Giá dịch vụ phải là số dương")
    private Double price;
    
    @NotBlank(message = "Đơn vị tính không được để trống")
    private String unit;
    
    private Boolean isAvailable;
} 