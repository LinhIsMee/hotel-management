package com.spring3.hotel.management.services;

import com.spring3.hotel.management.dto.response.BookingResponseDTO;
import com.spring3.hotel.management.dto.response.NewBookingResponse;
import com.spring3.hotel.management.dto.response.RoomListResponseDTO;
import com.spring3.hotel.management.dto.request.AdminBookingRequest;

import java.time.LocalDate;
import java.util.List;

public interface AdminBookingService {
    // Các phương thức quản lý đặt phòng cho admin
    List<NewBookingResponse> getRecentBookings();
    BookingResponseDTO getBookingById(Integer id);
    List<BookingResponseDTO> getAllBookings(int page, int size);
    List<BookingResponseDTO> getAllBookingsNoPage();
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