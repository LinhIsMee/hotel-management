package com.spring3.hotel.management.repositories;

import com.spring3.hotel.management.dtos.response.BookingDetailResponse;
import com.spring3.hotel.management.models.BookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface BookingDetailRepository extends JpaRepository<BookingDetail, Integer> {

    List<BookingDetailResponse> findBookingDetailsByBooking_Id(Integer bookingId);

    List<BookingDetail> findAllByBooking_Id(Integer bookingId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM BookingDetail bd WHERE bd.booking.id = :bookingId")
    void deleteAllByBookingId(@Param("bookingId") Integer bookingId);

    // Tính doanh thu theo loại phòng trong khoảng thời gian
    @Query("SELECT SUM(bd.pricePerNight) FROM BookingDetail bd WHERE bd.room.roomType.id = :roomTypeId AND bd.booking.createdAt BETWEEN :startDate AND :endDate")
    Double calculateRevenueByRoomTypeAndDateRange(@Param("roomTypeId") Integer roomTypeId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
