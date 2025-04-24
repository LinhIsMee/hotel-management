package com.spring3.hotel.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoomRequest {
    @NotBlank(message = "Số phòng không được để trống")
    @Pattern(regexp = "^[0-9]{3,4}$", message = "Số phòng phải có 3-4 chữ số")
    private String roomNumber;

    @NotNull(message = "ID loại phòng không được để trống")
    private Integer roomTypeId;

    @NotBlank(message = "Trạng thái phòng không được để trống")
    private String status;

    @NotBlank(message = "Tầng không được để trống")
    @Pattern(regexp = "^[0-9]{1,2}$", message = "Tầng phải là số từ 1-99")
    private String floor;

    @NotNull(message = "Trạng thái hoạt động không được để trống")
    private Boolean isActive;

    private String notes;
    
    private List<Integer> serviceIds;
    
    private List<String> images;
} 