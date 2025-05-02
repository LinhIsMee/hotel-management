package com.spring3.hotel.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticResponse {
    private Integer id;
    private LocalDateTime date;
    private Double totalRevenue;
    private Integer totalBookings;
    private Integer totalCustomers;
    private Integer totalRates;
}