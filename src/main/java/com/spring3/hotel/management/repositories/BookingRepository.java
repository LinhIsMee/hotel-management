package com.spring3.hotel.management.repositories;

import com.spring3.hotel.management.dtos.response.BookingResponseDTO;
import com.spring3.hotel.management.dtos.response.NewBookingResponse;
import com.spring3.hotel.management.models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

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

    // Tính tổng doanh thu trong khoảng thời gian
    @Query("SELECT SUM(b.totalPrice) FROM Booking b WHERE b.createdAt BETWEEN :startDate AND :endDate")
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

    // Tính tổng doanh thu
    @Query("SELECT SUM(b.totalPrice) FROM Booking b")
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
}
