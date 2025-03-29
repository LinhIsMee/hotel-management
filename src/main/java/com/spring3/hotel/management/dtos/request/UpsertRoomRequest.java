package com.spring3.hotel.management.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpsertRoomRequest {
    
    @NotBlank(message = "Số phòng không được để trống")
    private String roomNumber;
    
    @NotNull(message = "Loại phòng không được để trống")
    private Integer roomTypeId;
    
    private String status;
    
    private String floor;
    
    private Boolean isActive;
    
    private String notes;
}
