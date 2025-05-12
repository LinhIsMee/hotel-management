package com.spring3.hotel.management.repositories;

import com.spring3.hotel.management.dto.response.BookingResponseDTO;
import com.spring3.hotel.management.dto.response.NewBookingResponse;
import com.spring3.hotel.management.models.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    List<Booking> findByUserId(Integer userId);

    List<Booking> findByStatus(String status);

    // Tìm các booking có checkInDate bằng ngày hiện tại và trạng thái là 'pending' hoặc 'confirmed'
    @Query("SELECT b FROM Booking b WHERE b.checkInDate = :today AND b.status IN ('PENDING', 'CONFIRMED')")
    List<Booking> findBookingsToCheckIn(@Param("today") LocalDate today);

    // Tìm các booking có checkOutDate bằng ngày hiện tại và trạng thái là 'CHECKED_IN'
    @Query("SELECT b FROM Booking b WHERE b.checkOutDate = :today AND b.status = 'CHECKED_IN'")
    List<Booking> findBookingsToCheckOut(@Param("today") LocalDate today);

    // Tính tổng doanh thu trong khoảng thời gian (chỉ tính các đơn đã xác nhận và thanh toán)
    @Query("SELECT SUM(b.finalPrice) FROM Booking b WHERE b.status IN ('CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT') AND b.paymentStatus = 'PAID' AND b.createdAt BETWEEN :startDate AND :endDate")
    Double calculateTotalRevenueByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Đếm tổng số đặt phòng trong khoảng thời gian
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.createdAt BETWEEN :startDate AND :endDate")
    Integer countBookingsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Đếm tổng số khách hàng trong khoảng thời gian
    @Query("SELECT COUNT(DISTINCT b.user.id) FROM Booking b WHERE b.createdAt BETWEEN :startDate AND :endDate")
    Integer countDistinctCustomersByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Đếm số lượng khách hàng duy nhất
    @Query(value = "SELECT COUNT(DISTINCT user_id) FROM bookings WHERE user_id IS NOT NULL", nativeQuery = true)
    Integer countDistinctCustomers();

    // Đếm tổng số đặt phòng
    @Query("SELECT COUNT(b) FROM Booking b")
    Integer countAllBookings();

    // Tính tổng doanh thu (tính các đơn đã hoàn thành hoặc đang thực hiện)
    @Query("SELECT COALESCE(SUM(b.finalPrice), 0) FROM Booking b WHERE b.status IN ('COMPLETED', 'CHECKED_OUT', 'CONFIRMED', 'CHECKED_IN')")
    Double caculateTotalRevenue();

    // Lấy ra danh sách booking mới nhất trong 7 ngày
    @Query("SELECT b FROM Booking b WHERE b.createdAt >= :startDate AND b.createdAt <= :endDate ORDER BY b.createdAt DESC")
    List<Booking> findRecentBookings(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Tìm các booking trong khoảng thời gian
    @Query("SELECT b FROM Booking b WHERE " +
           "(b.checkInDate >= :startDate AND b.checkInDate <= :endDate) OR " +
           "(b.checkOutDate >= :startDate AND b.checkOutDate <= :endDate) OR " +
           "(b.checkInDate <= :startDate AND b.checkOutDate >= :endDate)")
    List<Booking> findBookingsBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // Tìm các booking không bị hủy trong khoảng thời gian (để kiểm tra phòng đã đặt)
    @Query("SELECT b FROM Booking b WHERE b.status != 'CANCELLED' AND " +
           "((b.checkInDate >= :startDate AND b.checkInDate <= :endDate) OR " +
           "(b.checkOutDate >= :startDate AND b.checkOutDate <= :endDate) OR " +
           "(b.checkInDate <= :startDate AND b.checkOutDate >= :endDate))")
    List<Booking> findActiveBookingsBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // Thống kê số lượng đặt phòng theo ngày trong tháng hiện tại và tháng trước
    @Query(value = "SELECT DATE_FORMAT(created_at, '%d/%m/%Y') as booking_date, COUNT(*) as booking_count " +
           "FROM bookings " +
           "WHERE DATE_FORMAT(created_at, '%Y%m') IN (DATE_FORMAT(CURDATE(), '%Y%m'), DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y%m')) " +
           "GROUP BY booking_date " +
           "ORDER BY MIN(created_at)", nativeQuery = true)
    List<Object[]> countBookingsByDayInCurrentMonth();
    
    // Thống kê doanh thu theo ngày trong 12 ngày gần nhất
    @Query(value = "WITH RECURSIVE dates AS (\n" +
           "    SELECT CURDATE() as date\n" +
           "    UNION ALL\n" +
           "    SELECT DATE_SUB(date, INTERVAL 1 DAY)\n" +
           "    FROM dates\n" +
           "    WHERE DATE_SUB(date, INTERVAL 1 DAY) >= DATE_SUB(CURDATE(), INTERVAL 11 DAY)\n" +
           "),\n" +
           "booking_days AS (\n" +
           "    SELECT b.id,\n" +
           "           b.final_price,\n" +
           "           COALESCE(b.check_in_date, CURDATE()) as check_in_date,\n" +
           "           COALESCE(b.check_out_date, DATE_ADD(CURDATE(), INTERVAL 1 DAY)) as check_out_date,\n" +
           "           GREATEST(DATEDIFF(COALESCE(b.check_out_date, DATE_ADD(CURDATE(), INTERVAL 1 DAY)), \n" +
           "                            COALESCE(b.check_in_date, CURDATE())), 1) as total_days\n" +
           "    FROM bookings b\n" +
           "    WHERE b.status IN ('COMPLETED', 'CHECKED_OUT', 'CONFIRMED', 'CHECKED_IN')\n" +
           "    AND b.final_price > 0\n" +
           "),\n" +
           "daily_revenue AS (\n" +
           "    SELECT d.date,\n" +
           "           COALESCE(SUM(\n" +
           "               CASE\n" +
           "                   WHEN d.date >= bd.check_in_date AND d.date < bd.check_out_date\n" +
           "                   THEN bd.final_price / bd.total_days\n" +
           "                   ELSE 0\n" +
           "               END\n" +
           "           ), 0) as revenue\n" +
           "    FROM dates d\n" +
           "    LEFT JOIN booking_days bd ON d.date >= bd.check_in_date AND d.date < bd.check_out_date\n" +
           "    GROUP BY d.date\n" +
           ")\n" +
           "SELECT DATE_FORMAT(date, '%d/%m/%Y') as booking_date,\n" +
           "       revenue\n" +
           "FROM daily_revenue\n" +
           "ORDER BY date ASC", nativeQuery = true)
    List<Object[]> sumRevenueByDayInCurrentMonth();
    
    // Thống kê doanh thu tháng hiện tại (tính từ đầu tháng đến ngày hiện tại)
    @Query(value = 
           "WITH monthly_bookings AS (\n" +
           "    SELECT\n" +
           "        b.id,\n" +
           "        b.final_price,\n" +
           "        b.check_in_date,\n" +
           "        b.check_out_date,\n" +
           "        GREATEST(DATEDIFF(b.check_out_date, b.check_in_date), 1) as total_days\n" +
           "    FROM bookings b\n" +
           "    WHERE b.status IN ('COMPLETED', 'CHECKED_OUT', 'CONFIRMED', 'CHECKED_IN')\n" +
           "      AND b.final_price > 0\n" +
           "      AND b.check_out_date > DATE_FORMAT(CURDATE() ,\'%Y-%m-01\')\n" +
           "      AND b.check_in_date <= CURDATE()\n" +
           "),\n" +
           "daily_revenue AS (\n" +
           "    SELECT\n" +
           "        id,\n" +
           "        final_price / total_days as daily_price,\n" +
           "        GREATEST(check_in_date, DATE_FORMAT(CURDATE(), \'%Y-%m-01\')) as effective_start_date,\n" +
           "        LEAST(check_out_date, DATE_ADD(CURDATE(), INTERVAL 1 DAY)) as effective_end_date\n" +
           "    FROM monthly_bookings\n" +
           ")\n" +
           "SELECT COALESCE(SUM(\n" +
           "           daily_price * GREATEST(DATEDIFF(effective_end_date, effective_start_date), 0)\n" +
           "       ), 0) as current_month_revenue_to_date\n" +
           "FROM daily_revenue\n" +
           "WHERE effective_end_date > effective_start_date\n", nativeQuery = true)
    Double calculateCurrentMonthRevenue();
    
    // Thống kê doanh thu tháng trước
    @Query(value = "WITH daily_revenue AS (\n" +
           "    SELECT b.id,\n" +
           "           b.final_price,\n" +
           "           COALESCE(b.check_in_date, CURDATE()) as check_in_date,\n" +
           "           COALESCE(b.check_out_date, DATE_ADD(CURDATE(), INTERVAL 1 DAY)) as check_out_date,\n" +
           "           GREATEST(DATEDIFF(COALESCE(b.check_out_date, DATE_ADD(CURDATE(), INTERVAL 1 DAY)), \n" +
           "                            COALESCE(b.check_in_date, CURDATE())), 1) as total_days\n" +
           "    FROM bookings b\n" +
           "    WHERE b.status IN ('COMPLETED', 'CHECKED_OUT', 'CONFIRMED', 'CHECKED_IN')\n" +
           "    AND b.final_price > 0\n" +
           ")\n" +
           "SELECT COALESCE(SUM(\n" +
           "    CASE\n" +
           "        WHEN DATE_FORMAT(check_in_date, '%Y%m') = DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y%m')\n" +
           "        AND DATE_FORMAT(check_out_date, '%Y%m') = DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y%m')\n" +
           "        THEN final_price\n" +
           "        WHEN DATE_FORMAT(check_in_date, '%Y%m') = DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y%m')\n" +
           "        THEN (final_price / total_days) * (DAY(LAST_DAY(check_in_date)) - DAY(check_in_date) + 1)\n" +
           "        WHEN DATE_FORMAT(check_out_date, '%Y%m') = DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y%m')\n" +
           "        THEN (final_price / total_days) * DAY(check_out_date)\n" +
           "        ELSE 0\n" +
           "    END\n" +
           "), 0) as monthly_revenue\n" +
           "FROM daily_revenue", nativeQuery = true)
    Double calculatePreviousMonthRevenue();
    
    // Đếm số booking theo trạng thái
    @Query("SELECT b.status, COUNT(b) FROM Booking b GROUP BY b.status")
    List<Object[]> countBookingsByStatus();
    
    // Thống kê các phòng được đặt nhiều nhất (chỉ tính các đơn đã xác nhận và thanh toán)
    @Query(value = "SELECT r.room_number, COUNT(*) as count, SUM(r.room_type_id) as room_type_id " +
           "FROM booking_details bd " +
           "JOIN rooms r ON bd.room_id = r.id " +
           "JOIN bookings b ON bd.booking_id = b.id " +
           "WHERE b.status IN ('CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT') " +
           "AND b.payment_status = 'PAID' " +
           "GROUP BY r.room_number " +
           "ORDER BY count DESC", nativeQuery = true)
    List<Object[]> findMostBookedRooms(Pageable pageable);

    // Tìm các booking đã được xác nhận (CONFIRMED) trong khoảng thời gian
    List<Booking> findByStatusAndCheckInDateBetween(String status, LocalDate startDate, LocalDate endDate);
    
    // Tìm tất cả booking với eager loading của các entities liên quan để tối ưu hiệu suất
    @Query("SELECT DISTINCT b FROM Booking b " +
           "LEFT JOIN FETCH b.user u " +
           "LEFT JOIN FETCH b.discount d " +
           "ORDER BY b.createdAt DESC")
    Page<Booking> findAllWithDetails(Pageable pageable);
    
    // Tìm tất cả booking không phân trang (lấy ALL)
    @Query("SELECT DISTINCT b FROM Booking b " +
           "LEFT JOIN FETCH b.user u " +
           "LEFT JOIN FETCH b.discount d " +
           "ORDER BY b.createdAt DESC")
    List<Booking> findAllWithDetailsNoPage();
    
    // Truy vấn bổ sung để lấy booking details
    @Query("SELECT DISTINCT b FROM Booking b " +
           "LEFT JOIN FETCH b.bookingDetails bd " +
           "LEFT JOIN FETCH bd.room r " +
           "LEFT JOIN FETCH r.roomType " +
           "WHERE b.id IN :ids")
    List<Booking> findBookingsWithDetails(List<Integer> ids);
    
    // Truy vấn bổ sung để lấy payments
    @Query("SELECT DISTINCT b FROM Booking b " +
           "LEFT JOIN FETCH b.payments p " +
           "WHERE b.id IN :ids " +
           "ORDER BY p.id DESC")
    List<Booking> findBookingsWithPayments(List<Integer> ids);
}
