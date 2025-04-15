package com.spring3.hotel.management.dtos.request;

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
public class UpsertRoomRequest {
    
    @NotBlank(message = "Số phòng không được để trống")
    private String roomNumber;
    
    @NotNull(message = "Loại phòng không được để trống")
    private Integer roomTypeId;
    
    private String status;
    
    private String floor;
    
    private Boolean isActive;
    
    private String notes;
    
    private List<String> images; // Danh sách các ảnh của phòng (Base64 encoded)
    
    private List<Integer> serviceIds; // Danh sách ID của các dịch vụ liên quan
}
