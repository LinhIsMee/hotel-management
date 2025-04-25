package com.spring3.hotel.management.services;

import com.spring3.hotel.management.dtos.request.UpsertBookingRequest;
import com.spring3.hotel.management.dtos.response.BookingResponseDTO;
import com.spring3.hotel.management.dtos.response.NewBookingResponse;
import com.spring3.hotel.management.dtos.response.RoomListResponseDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
    
    // Phương thức mới
    Map<String, Object> getBookingPaymentInfo(Integer bookingId);
    List<BookingResponseDTO> getConfirmedBookingsByDateRange(LocalDate startDate, LocalDate endDate);
    BookingResponseDTO createBookingTest(UpsertBookingRequest request);
    BookingResponseDTO markBookingAsPaid(Integer bookingId, String paymentMethod);
    
    // Phương thức mới để xử lý callback VNPay
    void updatePaymentAndBookingStatusAfterVNPay(Integer bookingId, String transactionNo, String responseCode);
    
    // Phương thức mới để lấy các lịch đặt của một phòng cụ thể
    List<Map<String, LocalDate>> getBookedDatesByRoomId(Integer roomId);
}
