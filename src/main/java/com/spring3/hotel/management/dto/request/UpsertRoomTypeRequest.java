package com.spring3.hotel.management.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpsertRoomTypeRequest {
    
    @NotBlank(message = "Tên loại phòng không được để trống")
    private String name;
    
    @NotBlank(message = "Mã loại phòng không được để trống")
    private String code;
    
    private String description;
    
    @NotNull(message = "Giá theo đêm không được để trống")
    @Min(value = 0, message = "Giá theo đêm phải lớn hơn hoặc bằng 0")
    private Double pricePerNight;
    
    @NotNull(message = "Số người tối đa không được để trống")
    @Min(value = 1, message = "Số người tối đa phải lớn hơn 0")
    private Integer maxOccupancy;
    
    private List<String> amenities;
    
    private String imageUrl;
    
    private Boolean isActive;
} 