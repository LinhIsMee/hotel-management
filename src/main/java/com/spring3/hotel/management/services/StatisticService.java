package com.spring3.hotel.management.services;

import com.spring3.hotel.management.dtos.response.DashboardInfoCountResponse;
import com.spring3.hotel.management.dtos.response.StatisticResponse;
import com.spring3.hotel.management.models.Statistic;

import java.time.LocalDateTime;
import java.util.List;

public interface StatisticService {
    List<StatisticResponse> getStatisticsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    DashboardInfoCountResponse getAllCountInfo();
}
