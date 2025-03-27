package com.spring3.hotel.management.services.impl;

import com.spring3.hotel.management.dtos.response.DashboardInfoCountResponse;
import com.spring3.hotel.management.dtos.response.StatisticResponse;
import com.spring3.hotel.management.models.RevenueByRoomType;
import com.spring3.hotel.management.models.RoomType;
import com.spring3.hotel.management.models.Statistic;
import com.spring3.hotel.management.repositories.*;
import com.spring3.hotel.management.services.StatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatisticServiceImpl implements StatisticService {

    @Autowired
    private StatisticRepository statisticRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RevenueByRoomTypeRepository revenueByRoomTypeRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private BookingDetailRepository bookingDetailRepository;
    @Autowired
    private ReviewRepository reviewRepository;


    // Lấy thống kê theo khoảng thời gian
    @Override
    public List<StatisticResponse> getStatisticsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return statisticRepository.findByDateBetween(startDate, endDate).stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    // Chuyển đổi từ Statistic sang StatisticResponse
    private StatisticResponse convertToResponse(Statistic statistic) {
        return new StatisticResponse(
            statistic.getId(),
            statistic.getDate(),
            statistic.getTotalRevenue(),
            statistic.getTotalBookings(),
            statistic.getTotalCustomers(),
            statistic.getTotalRates()
        );
    }

    // Lấy thông tin tổng số đặt phòng, tổng số khách hàng, tổng số đánh giá, tổng doanh thu
    @Override
    public DashboardInfoCountResponse getAllCountInfo() {
        DashboardInfoCountResponse dashboardInfoCountResponse = new DashboardInfoCountResponse();
        Integer totalBookings = bookingRepository.countAllBookings();
        Integer totalCustomers = bookingRepository.countDistinctCustomers();
        Integer totalRates = reviewRepository.countAllReviews();
        Double totalRevenue = bookingRepository.caculateTotalRevenue();
        dashboardInfoCountResponse.setTotalBookings(totalBookings);
        dashboardInfoCountResponse.setTotalCustomers(totalCustomers);
        dashboardInfoCountResponse.setTotalRates(totalRates);
        dashboardInfoCountResponse.setTotalRevenue(totalRevenue);
        return dashboardInfoCountResponse;
    }

    // Tác vụ chạy vào lúc 23:59 mỗi ngày
    @Scheduled(cron = "0 59 23 * * ?") // Cron expression: Giây Phút Giờ Ngày Tháng Thứ
    public void generateDailyStatistics() {
        // Lấy ngày hiện tại
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay(); // 00:00:00 của ngày hiện tại
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX); // 23:59:59.999999999 của ngày hiện tại

        // Tính toán tổng doanh thu, tổng số đặt phòng, tổng số khách hàng trong ngày
        Double totalRevenue = bookingRepository.calculateTotalRevenueByDateRange(startOfDay, endOfDay);
        Integer totalBookings = bookingRepository.countBookingsByDateRange(startOfDay, endOfDay);
        Integer totalCustomers = bookingRepository.countDistinctCustomersByDateRange(startOfDay, endOfDay);
        Integer totalRates = reviewRepository.countDistinctRatesByDateRange(startOfDay, endOfDay);
        // Lưu thống kê vào bảng Statistic
        Statistic statistic = new Statistic();
        statistic.setDate(endOfDay); // Lưu thời điểm kết thúc ngày
        statistic.setTotalRevenue(totalRevenue != null ? totalRevenue : 0.0);
        statistic.setTotalBookings(totalBookings != null ? totalBookings : 0);
        statistic.setTotalCustomers(totalCustomers != null ? totalCustomers : 0);
        statistic.setTotalRates(totalRates != null ? totalRates : 0);
        statisticRepository.save(statistic);

        // Tính toán doanh thu theo từng loại phòng
        List<RoomType> roomTypes = roomTypeRepository.findAll();
        for (RoomType roomType : roomTypes) {
            Double revenueByType = bookingDetailRepository.calculateRevenueByRoomTypeAndDateRange(roomType.getId(), startOfDay, endOfDay);

            // Lưu doanh thu theo loại phòng vào bảng RevenueByRoomType
            RevenueByRoomType revenueByRoomType = new RevenueByRoomType();
            revenueByRoomType.setStatistic(statistic);
            revenueByRoomType.setRoomType(roomType);
            revenueByRoomType.setTotalRevenue(revenueByType != null ? revenueByType : 0.0);
            revenueByRoomTypeRepository.save(revenueByRoomType);
        }
    }
}
