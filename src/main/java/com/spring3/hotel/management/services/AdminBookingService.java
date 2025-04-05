package com.spring3.hotel.management.services;

import com.spring3.hotel.management.dtos.request.AdminBookingRequest;
import com.spring3.hotel.management.dtos.response.BookingResponseDTO;
import com.spring3.hotel.management.dtos.response.NewBookingResponse;
import com.spring3.hotel.management.dtos.response.RoomListResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface AdminBookingService {
    // Các phương thức quản lý đặt phòng cho admin
    List<NewBookingResponse> getRecentBookings();
    BookingResponseDTO getBookingById(Integer id);
    List<BookingResponseDTO> getAllBookings();
    List<BookingResponseDTO> getBookingsByStatus(String status);
    List<BookingResponseDTO> getBookingsByDateRange(LocalDate startDate, LocalDate endDate);
    List<RoomListResponseDTO> getBookedRoomsByDateRange(LocalDate startDate, LocalDate endDate);
    
    // Phương thức đặc thù cho admin
    BookingResponseDTO createBookingByAdmin(AdminBookingRequest request);
    BookingResponseDTO updateBookingByAdmin(AdminBookingRequest request, Integer id);
    BookingResponseDTO cancelBooking(Integer id);
    BookingResponseDTO confirmBooking(Integer id);
    BookingResponseDTO checkInBooking(Integer id);
    BookingResponseDTO checkOutBooking(Integer id);
    void deleteBooking(Integer id);
} 