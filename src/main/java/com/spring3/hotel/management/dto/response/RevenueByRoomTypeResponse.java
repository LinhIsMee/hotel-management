package com.spring3.hotel.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueByRoomTypeResponse {
    private Integer id;
    private Integer roomTypeId;
    private String roomTypeName;
    private Double totalRevenue;
}
