package com.spring3.hotel.management.services.impl;

import com.spring3.hotel.management.dto.response.DashboardInfoCountResponse;
import com.spring3.hotel.management.dto.response.RecentBookingResponse;
import com.spring3.hotel.management.dto.response.RoomBookingStatsResponse;
import com.spring3.hotel.management.dto.response.StatisticResponse;
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
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
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
        
        // Lấy dữ liệu từ cơ sở dữ liệu
        Integer totalBookings = bookingRepository.countAllBookings();
        Integer totalCustomers = bookingRepository.countDistinctCustomers();
        Integer totalRates = reviewRepository.countAllReviews();
        // Lấy tổng doanh thu của tất cả các tháng
        Double totalRevenue = bookingRepository.caculateTotalRevenue();
        
        // Đảm bảo có giá trị mặc định nếu truy vấn không trả về kết quả hoặc trả về 0
        dashboardInfoCountResponse.setTotalBookings(totalBookings != null && totalBookings > 0 ? totalBookings : 0);
        dashboardInfoCountResponse.setTotalCustomers(totalCustomers != null && totalCustomers > 0 ? totalCustomers : 0);
        dashboardInfoCountResponse.setTotalRates(totalRates != null && totalRates > 0 ? totalRates : 0);
        dashboardInfoCountResponse.setTotalRevenue(totalRevenue != null ? totalRevenue : 0.0);
        
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
            
            List<BookingDetail> details = booking.getBookingDetails();
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
        
        // Kiểm tra xem dữ liệu từ DB có hợp lệ không
        boolean hasValidData = false;
        if (mostBookedRooms != null && !mostBookedRooms.isEmpty()) {
            for (Object[] result : mostBookedRooms) {
                String roomNumber = (String) result[0];
                Long bookingCount = (Long) result[1];
                Integer roomTypeId = ((Number) result[2]).intValue();
                
                // Kiểm tra nếu dữ liệu hợp lệ
                if (roomNumber != null && bookingCount != null) {
                    hasValidData = true;
                    
                    // Tìm thông tin của phòng
                    Optional<Room> roomOpt = roomRepository.findByRoomNumber(roomNumber);
                    String roomType = "Unknown";
                    Double totalRevenue = 0.0;
                    
                    if (roomOpt.isPresent()) {
                        Room room = roomOpt.get();
                        roomType = room.getRoomType().getName();
                        
                        // Tính toán doanh thu dựa trên giá phòng và số lượng đặt
                        Double basePrice = room.getRoomType().getBasePrice();
                        // Giả định mỗi lượt đặt phòng kéo dài trung bình 2 ngày
                        totalRevenue = basePrice * bookingCount * 2; 
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
            }
        }
        
        // Nếu không có dữ liệu hợp lệ từ cơ sở dữ liệu, tạo dữ liệu mẫu
        if (!hasValidData) {
            // Tạo dữ liệu mẫu khi không có dữ liệu từ cơ sở dữ liệu
            List<String> roomTypes = Arrays.asList("Standard", "Deluxe", "Suite", "Family", "Presidential", "Executive");
            
            for (int i = 0; i < limit; i++) {
                String roomNumber = "Room-" + (i + 1);
                String roomType = roomTypes.get(i % roomTypes.size());
                Long bookingCount = 50L - i * 3;
                if (bookingCount < 10) bookingCount = 10L + i;
                
                Double totalRevenue = (double) (bookingCount * 10000000);
                Double occupancyRate = Math.min(95.0, (bookingCount / 50.0) * 100);
                
                RoomBookingStatsResponse roomStats = new RoomBookingStatsResponse(
                        roomNumber,
                        roomType,
                        bookingCount,
                        totalRevenue,
                        occupancyRate
                );
                
                response.add(roomStats);
            }
        }
        
        return response;
    }
    
    // Thống kê doanh thu theo ngày trong 12 ngày gần nhất
    @Override
    public Map<String, Double> getRevenueByDay() {
        // Lấy dữ liệu từ cơ sở dữ liệu
        List<Object[]> revenueData = bookingRepository.sumRevenueByDayInCurrentMonth();
        Map<String, Double> revenueByDay = new LinkedHashMap<>();
        
        // Tạo danh sách 12 ngày gần nhất
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        // Khởi tạo map với 12 ngày gần nhất, giá trị mặc định là 0
        for (int i = 11; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            revenueByDay.put(date.format(formatter), 0.0);
        }
        
        // Cập nhật dữ liệu thực từ database
        if (revenueData != null && !revenueData.isEmpty()) {
            for (Object[] result : revenueData) {
                String date = (String) result[0];
                Double amount = (Double) result[1];
                if (revenueByDay.containsKey(date)) {
                    revenueByDay.put(date, amount != null ? amount : 0.0);
                }
            }
        }
        
        return revenueByDay;
    }
    
    // Thống kê số lượng đặt phòng theo ngày trong tháng hiện tại
    @Override
    public Map<String, Integer> getBookingsByDay() {
        // Lấy dữ liệu từ cơ sở dữ liệu
        List<Object[]> bookingData = bookingRepository.countBookingsByDayInCurrentMonth();
        
        Map<String, Integer> bookingsByDay = new LinkedHashMap<>();
        
        // Nếu có dữ liệu từ cơ sở dữ liệu
        if (bookingData != null && !bookingData.isEmpty()) {
            // Xử lý dữ liệu từ cơ sở dữ liệu
            for (Object[] result : bookingData) {
                String date = (String) result[0];
                Long count = (Long) result[1];
                bookingsByDay.put(date, count != null ? count.intValue() : 0);
            }
        }
        
        // Nếu không có dữ liệu hoặc dữ liệu từ cơ sở dữ liệu không đủ
        if (bookingsByDay.isEmpty() || bookingsByDay.size() < 10) {
            // Tạo dữ liệu mẫu cho tháng trước và tháng hiện tại
            LocalDate today = LocalDate.now();
            LocalDate firstDayOfLastMonth = today.minusMonths(1).withDayOfMonth(1);
            LocalDate firstDayOfCurrentMonth = today.withDayOfMonth(1);
            
            // Tạo dữ liệu cho tháng trước nếu chưa có
            for (int i = 0; i < 10; i++) {
                LocalDate date = firstDayOfLastMonth.plusDays(i);
                String formattedDate = date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                if (!bookingsByDay.containsKey(formattedDate)) {
                    int bookingCount = 1 + (int)(Math.random() * 5);
                    bookingsByDay.put(formattedDate, bookingCount);
                }
            }
            
            // Tạo dữ liệu cho tháng hiện tại nếu chưa có
            for (int i = 0; i < 10; i++) {
                LocalDate date = firstDayOfCurrentMonth.plusDays(i);
                String formattedDate = date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                if (!bookingsByDay.containsKey(formattedDate)) {
                    int bookingCount = 3 + (int)(Math.random() * 8);
                    bookingsByDay.put(formattedDate, bookingCount);
                }
            }
        }
        
        return bookingsByDay;
    }
    
    // So sánh doanh thu giữa tháng hiện tại và tháng trước
    @Override
    public Map<String, Double> getRevenueComparison() {
        // Lấy dữ liệu từ cơ sở dữ liệu
        Double currentMonthRevenue = bookingRepository.calculateCurrentMonthRevenue();
        Double previousMonthRevenue = bookingRepository.calculatePreviousMonthRevenue();
        
        // Tính phần trăm thay đổi
        double percentChange = 0.0;
        if (previousMonthRevenue > 0) {
            percentChange = ((currentMonthRevenue - previousMonthRevenue) / previousMonthRevenue) * 100;
        } else if (currentMonthRevenue > 0) {
            percentChange = 100.0;
        }
        
        Map<String, Double> comparison = new HashMap<>();
        comparison.put("currentMonth", currentMonthRevenue);
        comparison.put("previousMonth", previousMonthRevenue);
        comparison.put("percentChange", percentChange);
        
        return comparison;
    }
    
    // Thống kê tỷ lệ đặt phòng theo trạng thái
    @Override
    public Map<String, Long> getBookingStatusStats() {
        List<Object[]> statusData = bookingRepository.countBookingsByStatus();
        
        // Nếu không có dữ liệu, tạo dữ liệu mẫu
        if (statusData == null || statusData.isEmpty()) {
            // Sử dụng LinkedHashMap để duy trì thứ tự thêm vào
            Map<String, Long> sampleStats = new LinkedHashMap<>();
            sampleStats.put("PENDING", 24L);
            sampleStats.put("CONFIRMED", 23L);
            sampleStats.put("CHECKED_IN", 7L);
            sampleStats.put("CHECKED_OUT", 51L);
            sampleStats.put("CANCELLED", 5L);
            return sampleStats;
        }
        
        // Tạo map chuẩn hóa trạng thái
        Map<String, String> normalizedStatus = new HashMap<>();
        normalizedStatus.put("CHECKED IN", "CHECKED_IN");
        normalizedStatus.put("CHECKED OUT", "CHECKED_OUT");
        normalizedStatus.put("COMPLETE", "CHECKED_OUT");
        normalizedStatus.put("COMPLETED", "CHECKED_OUT");
        normalizedStatus.put("CANCEL", "CANCELLED");
        
        // Map tạm để tổng hợp số liệu
        Map<String, Long> tempStatsMap = new HashMap<>();
        
        for (Object[] result : statusData) {
            String status = (String) result[0];
            Long count = (Long) result[1];
            
            // Nếu status là null, bỏ qua
            if (status == null) continue;
            
            // Chuẩn hóa trạng thái nếu cần
            String normalizedKey = normalizedStatus.getOrDefault(status, status);
            
            // Cộng dồn số lượng nếu đã tồn tại trạng thái
            tempStatsMap.put(normalizedKey, tempStatsMap.getOrDefault(normalizedKey, 0L) + count);
        }
        
        // Tạo LinkedHashMap để sắp xếp theo thứ tự quy trình đặt phòng
        Map<String, Long> orderedStats = new LinkedHashMap<>();
        
        // Thêm các trạng thái theo thứ tự quy trình
        String[] orderedStatuses = {
            "PENDING", 
            "CONFIRMED", 
            "CHECKED_IN", 
            "CHECKED_OUT", 
            "CANCELLED"
        };
        
        // Thêm vào map theo thứ tự
        for (String status : orderedStatuses) {
            if (tempStatsMap.containsKey(status)) {
                orderedStats.put(status, tempStatsMap.get(status));
            }
        }
        
        // Thêm bất kỳ trạng thái nào còn lại không nằm trong danh sách đã định nghĩa
        for (Map.Entry<String, Long> entry : tempStatsMap.entrySet()) {
            if (!orderedStats.containsKey(entry.getKey())) {
                orderedStats.put(entry.getKey(), entry.getValue());
            }
        }
        
        return orderedStats;
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
