package com.spring3.hotel.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomBookingStatsResponse {
    private String roomNumber;
    private String roomType;
    private Long bookingCount;
    private Double totalRevenue;
    private Double occupancyRate; // Tỷ lệ lấp đầy (phần trăm)
} 
