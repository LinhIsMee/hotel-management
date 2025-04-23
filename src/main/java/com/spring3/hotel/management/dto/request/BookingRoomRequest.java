package com.spring3.hotel.management.dto.request;

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
public class BookingRoomRequest {
    @NotNull(message = "ID phòng không được để trống")
    private Integer roomId;
    
    private List<Integer> serviceIds;
    
    private Integer adults;
    
    private Integer children;
} 