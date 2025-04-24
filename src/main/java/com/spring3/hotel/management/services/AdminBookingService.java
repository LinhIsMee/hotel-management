package com.spring3.hotel.management.services;

import com.spring3.hotel.management.dto.request.AdminBookingRequest;
import com.spring3.hotel.management.dto.response.BookingResponseDTO;
import com.spring3.hotel.management.dto.response.NewBookingResponse;
import com.spring3.hotel.management.dto.response.RoomListResponseDTO;
import com.spring3.hotel.management.enums.BookingStatus;

import java.time.LocalDate;
import java.util.List;

public interface AdminBookingService {
    // Phương thức chung
    List<NewBookingResponse> getRecentBookings();
    BookingResponseDTO getBookingById(Integer id);
    List<BookingResponseDTO> getAllBookings(int page, int size);
    List<BookingResponseDTO> getBookingsByStatus(BookingStatus status);
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