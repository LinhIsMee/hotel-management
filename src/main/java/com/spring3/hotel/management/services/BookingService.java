package com.spring3.hotel.management.services;

import com.spring3.hotel.management.dto.request.UpsertBookingRequest;
import com.spring3.hotel.management.dto.response.BookingResponseDTO;
import com.spring3.hotel.management.dto.response.NewBookingResponse;
import com.spring3.hotel.management.dto.response.RoomListResponseDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface BookingService {
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
    List<NewBookingResponse> getRecentBookings();
    // Đã loại bỏ phương thức createBookingTest không cần thiết
    BookingResponseDTO markBookingAsPaid(Integer bookingId, String paymentMethod);
    void updatePaymentAndBookingStatusAfterVNPay(Integer bookingId, String transactionNo, String responseCode);
    BookingResponseDTO createBooking(UpsertBookingRequest request, String username);
    List<BookingResponseDTO> getConfirmedBookingsByDateRange(LocalDate startDate, LocalDate endDate);
    Map<String, Object> getBookingPaymentInfo(Integer bookingId);
}