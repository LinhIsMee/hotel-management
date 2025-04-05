package com.spring3.hotel.management.services.impl;

import com.spring3.hotel.management.dtos.response.DashboardInfoCountResponse;
import com.spring3.hotel.management.dtos.response.RecentBookingResponse;
import com.spring3.hotel.management.dtos.response.RoomBookingStatsResponse;
import com.spring3.hotel.management.dtos.response.StatisticResponse;
import com.spring3.hotel.management.models.Booking;
import com.spring3.hotel.management.models.BookingDetail;
import com.spring3.hotel.management.models.RevenueByRoomType;
import com.spring3.hotel.management.models.Room;
import com.spring3.hotel.management.models.RoomType;
import com.spring3.hotel.management.models.Statistic;
import com.spring3.hotel.management.repositories.*;
import com.spring3.hotel.management.services.StatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private RoomRepository roomRepository;

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
    
    // Lấy thông tin đặt phòng gần đây
    @Override
    public List<RecentBookingResponse> getRecentBookings(int days) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        
        List<Booking> recentBookings = bookingRepository.findRecentBookings(startDate, endDate);
        
        List<RecentBookingResponse> response = new ArrayList<>();
        for (Booking booking : recentBookings) {
            RecentBookingResponse bookingResponse = new RecentBookingResponse();
            bookingResponse.setId(booking.getId());
            bookingResponse.setCustomerName(booking.getUser() != null ? booking.getUser().getFullName() : "N/A");
            
            // Xử lý thông tin về phòng
            String roomNumber = "N/A";
            String roomType = "N/A";
            
            List<BookingDetail> details = booking.getBookingDetail();
            if (details != null && !details.isEmpty()) {
                BookingDetail detail = details.get(0);
                roomNumber = detail.getRoomNumber();
                roomType = detail.getRoomType();
            }
            
            bookingResponse.setRoomNumber(roomNumber);
            bookingResponse.setRoomType(roomType);
            bookingResponse.setTotalPrice(booking.getTotalPrice());
            bookingResponse.setCheckInDate(booking.getCheckInDate());
            bookingResponse.setCheckOutDate(booking.getCheckOutDate());
            bookingResponse.setStatus(booking.getStatus());
            bookingResponse.setCreatedAt(booking.getCreatedAt());
            
            response.add(bookingResponse);
        }
        
        return response;
    }
    
    // Lấy thống kê đánh giá theo số sao
    @Override
    public Map<Integer, Long> getReviewsByRating() {
        Map<Integer, Long> reviewsCount = new HashMap<>();
        
        // Thống kê số lượng đánh giá cho mỗi mức sao từ 1-5
        for (int star = 1; star <= 5; star++) {
            long count = reviewRepository.countByRating(star);
            reviewsCount.put(star, count);
        }
        
        return reviewsCount;
    }
    
    // Lấy số liệu phòng được đặt nhiều nhất
    @Override
    public List<RoomBookingStatsResponse> getMostBookedRooms(int limit) {
        List<Object[]> mostBookedRooms = bookingRepository.findMostBookedRooms(PageRequest.of(0, limit));
        
        List<RoomBookingStatsResponse> response = new ArrayList<>();
        for (Object[] result : mostBookedRooms) {
            String roomNumber = (String) result[0];
            Long bookingCount = (Long) result[1];
            Double totalRevenue = (Double) result[2];
            
            // Tìm thông tin của phòng
            Optional<Room> roomOpt = roomRepository.findByRoomNumber(roomNumber);
            String roomType = "Unknown";
            if (roomOpt.isPresent()) {
                Room room = roomOpt.get();
                roomType = room.getRoomType().getName(); 
            }
            
            // Tính tỷ lệ lấp đầy
            Double occupancyRate = 0.0;
            if (roomOpt.isPresent()) {
                // Lấy tổng số ngày trong năm hiện tại (đơn giản hóa)
                LocalDate startOfYear = LocalDate.now().withDayOfYear(1);
                LocalDate endOfYear = LocalDate.now();
                long totalDays = ChronoUnit.DAYS.between(startOfYear, endOfYear) + 1;
                
                // Đếm số booking cho phòng này (giả định chúng ta có tổng số)
                long bookingsCount = bookingCount != null ? bookingCount : 0;
                
                // Tính toán tỷ lệ lấp đầy dựa trên giả định đơn giản
                // Giả định rằng mỗi booking kéo dài trung bình 2 ngày
                long estimatedDaysBooked = bookingsCount * 2;
                occupancyRate = Math.min(100.0, (estimatedDaysBooked / (double) totalDays) * 100);
            }
            
            RoomBookingStatsResponse roomStats = new RoomBookingStatsResponse(
                    roomNumber,
                    roomType,
                    bookingCount,
                    totalRevenue,
                    occupancyRate
            );
            
            response.add(roomStats);
        }
        
        return response;
    }
    
    // Thống kê doanh thu theo ngày trong tháng hiện tại
    @Override
    public Map<String, Double> getRevenueByDay() {
        List<Object[]> revenueData = bookingRepository.sumRevenueByDayInCurrentMonth();
        
        Map<String, Double> revenueByDay = new LinkedHashMap<>();
        for (Object[] result : revenueData) {
            String date = (String) result[0];
            Double amount = (Double) result[1];
            revenueByDay.put(date, amount != null ? amount : 0.0);
        }
        
        return revenueByDay;
    }
    
    // Thống kê số lượng đặt phòng theo ngày trong tháng hiện tại
    @Override
    public Map<String, Integer> getBookingsByDay() {
        List<Object[]> bookingData = bookingRepository.countBookingsByDayInCurrentMonth();
        
        Map<String, Integer> bookingsByDay = new LinkedHashMap<>();
        for (Object[] result : bookingData) {
            String date = (String) result[0];
            Long count = (Long) result[1];
            bookingsByDay.put(date, count != null ? count.intValue() : 0);
        }
        
        return bookingsByDay;
    }
    
    // So sánh doanh thu giữa tháng hiện tại và tháng trước
    @Override
    public Map<String, Double> getRevenueComparison() {
        Double currentMonthRevenue = bookingRepository.calculateCurrentMonthRevenue();
        Double previousMonthRevenue = bookingRepository.calculatePreviousMonthRevenue();
        
        Map<String, Double> comparison = new HashMap<>();
        comparison.put("currentMonth", currentMonthRevenue != null ? currentMonthRevenue : 0.0);
        comparison.put("previousMonth", previousMonthRevenue != null ? previousMonthRevenue : 0.0);
        
        // Tính phần trăm thay đổi
        if (previousMonthRevenue != null && previousMonthRevenue > 0) {
            double changePercent = ((currentMonthRevenue != null ? currentMonthRevenue : 0) - previousMonthRevenue) / previousMonthRevenue * 100;
            comparison.put("percentChange", Math.round(changePercent * 100) / 100.0);
        } else {
            comparison.put("percentChange", 100.0); // Nếu tháng trước không có doanh thu
        }
        
        return comparison;
    }
    
    // Thống kê tỷ lệ đặt phòng theo trạng thái
    @Override
    public Map<String, Long> getBookingStatusStats() {
        List<Object[]> statusData = bookingRepository.countBookingsByStatus();
        
        Map<String, Long> statusStats = new HashMap<>();
        for (Object[] result : statusData) {
            String status = (String) result[0];
            Long count = (Long) result[1];
            statusStats.put(status, count);
        }
        
        return statusStats;
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
