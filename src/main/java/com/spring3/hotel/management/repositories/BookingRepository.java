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

    // Tính tổng doanh thu trong khoảng thời gian (sử dụng finalPrice thay vì totalPrice)
    @Query("SELECT SUM(b.finalPrice) FROM Booking b WHERE b.createdAt BETWEEN :startDate AND :endDate")
    Double calculateTotalRevenueByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Đếm tổng số đặt phòng trong khoảng thời gian
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.createdAt BETWEEN :startDate AND :endDate")
    Integer countBookingsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Đếm tổng số khách hàng trong khoảng thời gian
    @Query("SELECT COUNT(DISTINCT b.user.id) FROM Booking b WHERE b.createdAt BETWEEN :startDate AND :endDate")
    Integer countDistinctCustomersByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Đếm tổng số khách hàng
    @Query("SELECT COUNT(DISTINCT b.user.id) FROM Booking b where b.user.role.name = 'ROLE_CUSTOMER'")
    Integer countDistinctCustomers();

    // Đếm tổng số đặt phòng
    @Query("SELECT COUNT(b) FROM Booking b")
    Integer countAllBookings();

    // Tính tổng doanh thu (sử dụng finalPrice)
    @Query("SELECT SUM(b.finalPrice) FROM Booking b")
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
    
    // Thống kê doanh thu theo ngày trong tháng hiện tại và tháng trước (sử dụng final_price)
    @Query(value = "SELECT DATE_FORMAT(created_at, '%d/%m/%Y') as booking_date, SUM(final_price) as daily_revenue " +
           "FROM bookings " +
           "WHERE DATE_FORMAT(created_at, '%Y%m') IN (DATE_FORMAT(CURDATE(), '%Y%m'), DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y%m')) " +
           "GROUP BY booking_date " +
           "ORDER BY MIN(created_at)", nativeQuery = true)
    List<Object[]> sumRevenueByDayInCurrentMonth();
    
    // Thống kê doanh thu tháng hiện tại (sử dụng final_price)
    @Query(value = "SELECT SUM(final_price) FROM bookings " +
           "WHERE DATE_FORMAT(created_at, '%Y%m') = DATE_FORMAT(CURDATE(), '%Y%m')", nativeQuery = true)
    Double calculateCurrentMonthRevenue();
    
    // Thống kê doanh thu tháng trước (sử dụng final_price)
    @Query(value = "SELECT SUM(final_price) FROM bookings " +
           "WHERE DATE_FORMAT(created_at, '%Y%m') = DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y%m')", nativeQuery = true)
    Double calculatePreviousMonthRevenue();
    
    // Đếm số booking theo trạng thái
    @Query("SELECT b.status, COUNT(b) FROM Booking b GROUP BY b.status")
    List<Object[]> countBookingsByStatus();
    
    // Thống kê các phòng được đặt nhiều nhất
    @Query(value = "SELECT r.room_number, COUNT(*) as count, SUM(r.room_type_id) as room_type_id " +
           "FROM booking_details bd " +
           "JOIN rooms r ON bd.room_id = r.id " +
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
