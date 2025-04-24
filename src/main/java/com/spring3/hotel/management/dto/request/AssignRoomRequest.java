package com.spring3.hotel.management.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignRoomRequest {
    
    @NotNull(message = "ID đặt phòng không được để trống")
    private Integer bookingId;
    
    @NotNull(message = "ID phòng không được để trống")
    private Integer roomId;
} 