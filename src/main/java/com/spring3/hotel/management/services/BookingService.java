package com.spring3.hotel.management.services;

import com.spring3.hotel.management.dtos.request.UpsertBookingRequest;
import com.spring3.hotel.management.dtos.response.BookingResponseDTO;
import com.spring3.hotel.management.dtos.response.NewBookingResponse;
import com.spring3.hotel.management.dtos.response.RoomListResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {
    List<NewBookingResponse> getRecentBookings();
    BookingResponseDTO getBookingById(Integer id);
    List<BookingResponseDTO> getBookingsByUserId(Integer userId);
    List<BookingResponseDTO> getAllBookings();
    List<BookingResponseDTO> getBookingsByStatus(String status);
    BookingResponseDTO createBooking(UpsertBookingRequest request);
    BookingResponseDTO updateBooking(UpsertBookingRequest request, Integer id);
    BookingResponseDTO cancelBooking(Integer id);
    List<BookingResponseDTO> getBookingsByDateRange(LocalDate startDate, LocalDate endDate);
    BookingResponseDTO confirmBooking(Integer id);
    List<RoomListResponseDTO> getBookedRoomsByDateRange(LocalDate startDate, LocalDate endDate);
}
