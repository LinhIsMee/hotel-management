package com.spring3.hotel.management.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardInfoCountResponse { // Dữ liệu trả về cho dashboard về số lượng thông tin
    private Integer totalBookings;
    private Integer totalCustomers;
    private Integer totalRates;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Double totalRevenue;
}
