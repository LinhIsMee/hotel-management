package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dto.response.DashboardInfoCountResponse;
import com.spring3.hotel.management.dto.response.RecentBookingResponse;
import com.spring3.hotel.management.dto.response.RoomBookingStatsResponse;
import com.spring3.hotel.management.dto.response.StatisticResponse;
import com.spring3.hotel.management.services.StatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/statistics")
public class StatisticController {

    @Autowired
    private StatisticService statisticService;

    /**
     * Lấy số liệu tổng hợp của 4 bảng thống kê của dashboard
     */
    @GetMapping("/count-info")
    public ResponseEntity<DashboardInfoCountResponse> getAllStatistics() {
        DashboardInfoCountResponse infoCountResponse = statisticService.getAllCountInfo();
        return ResponseEntity.ok(infoCountResponse);
    }

    /**
     * Lấy thống kê theo khoảng thời gian
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<StatisticResponse>> getStatisticsByDateRange(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<StatisticResponse> statistics = statisticService.getStatisticsByDateRange(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Lấy thông tin đặt phòng gần đây
     */
    @GetMapping("/recent-bookings")
    public ResponseEntity<List<RecentBookingResponse>> getRecentBookings(
            @RequestParam(defaultValue = "7") int days) {
        List<RecentBookingResponse> recentBookings = statisticService.getRecentBookings(days);
        return ResponseEntity.ok(recentBookings);
    }
    
    /**
     * Lấy thống kê đánh giá theo số sao
     */
    @GetMapping("/reviews-by-rating")
    public ResponseEntity<Map<Integer, Long>> getReviewsByRating() {
        Map<Integer, Long> reviewsCount = statisticService.getReviewsByRating();
        return ResponseEntity.ok(reviewsCount);
    }
    
    /**
     * Lấy số liệu phòng được đặt nhiều nhất
     */
    @GetMapping("/most-booked-rooms")
    public ResponseEntity<List<RoomBookingStatsResponse>> getMostBookedRooms(
            @RequestParam(defaultValue = "5") int limit) {
        List<RoomBookingStatsResponse> mostBookedRooms = statisticService.getMostBookedRooms(limit);
        return ResponseEntity.ok(mostBookedRooms);
    }
    
    /**
     * Thống kê doanh thu theo ngày trong tháng hiện tại
     */
    @GetMapping("/revenue-by-day")
    public ResponseEntity<Map<String, Double>> getRevenueByDay() {
        Map<String, Double> revenueByDay = statisticService.getRevenueByDay();
        return ResponseEntity.ok(revenueByDay);
    }
    
    /**
     * Thống kê số lượng đặt phòng theo ngày trong tháng hiện tại
     */
    @GetMapping("/bookings-by-day")
    public ResponseEntity<Map<String, Integer>> getBookingsByDay() {
        Map<String, Integer> bookingsByDay = statisticService.getBookingsByDay();
        return ResponseEntity.ok(bookingsByDay);
    }
    
    /**
     * So sánh doanh thu giữa tháng hiện tại và tháng trước
     */
    @GetMapping("/revenue-comparison")
    public ResponseEntity<Map<String, Double>> getRevenueComparison() {
        Map<String, Double> revenueComparison = statisticService.getRevenueComparison();
        return ResponseEntity.ok(revenueComparison);
    }

    /**
     * Thống kê tỷ lệ đặt phòng theo trạng thái
     */
    @GetMapping("/booking-status")
    public ResponseEntity<Map<String, Long>> getBookingStatusStats() {
        Map<String, Long> bookingStatusStats = statisticService.getBookingStatusStats();
        return ResponseEntity.ok(bookingStatusStats);
    }
}
