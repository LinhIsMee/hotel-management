package com.spring3.hotel.management.services;

import com.spring3.hotel.management.dtos.response.DashboardInfoCountResponse;
import com.spring3.hotel.management.dtos.response.RecentBookingResponse;
import com.spring3.hotel.management.dtos.response.RoomBookingStatsResponse;
import com.spring3.hotel.management.dtos.response.StatisticResponse;
import com.spring3.hotel.management.models.Statistic;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface StatisticService {
    List<StatisticResponse> getStatisticsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    DashboardInfoCountResponse getAllCountInfo();
    List<RecentBookingResponse> getRecentBookings(int days);
    Map<Integer, Long> getReviewsByRating();
    List<RoomBookingStatsResponse> getMostBookedRooms(int limit);
    Map<String, Double> getRevenueByDay();
    Map<String, Integer> getBookingsByDay();
    Map<String, Double> getRevenueComparison();
    Map<String, Long> getBookingStatusStats();
}
