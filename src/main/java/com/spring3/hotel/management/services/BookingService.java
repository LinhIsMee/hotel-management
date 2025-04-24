package com.spring3.hotel.management.services;

import com.spring3.hotel.management.dto.request.AdminBookingRequest;
import com.spring3.hotel.management.dto.request.UpsertBookingRequest;
import com.spring3.hotel.management.dto.response.BookingResponseDTO;
import com.spring3.hotel.management.dto.response.NewBookingResponse;
import com.spring3.hotel.management.dto.response.RoomListResponseDTO;
import com.spring3.hotel.management.enums.BookingStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface BookingService {
    BookingResponseDTO getBookingById(Integer id);
    
    List<BookingResponseDTO> getBookingsByUserId(Integer userId);
    
    List<BookingResponseDTO> getAllBookings();
    
    List<BookingResponseDTO> getAllBookings(int page, int size);
    
    List<BookingResponseDTO> getBookingsByStatus(BookingStatus status);
    
    List<BookingResponseDTO> getBookingsByDateRange(LocalDate startDate, LocalDate endDate);
    
    List<BookingResponseDTO> getConfirmedBookingsByDateRange(LocalDate startDate, LocalDate endDate);
    
    BookingResponseDTO createBooking(UpsertBookingRequest request);
    
    BookingResponseDTO createBooking(UpsertBookingRequest request, String username);
    
    BookingResponseDTO createBookingByAdmin(AdminBookingRequest request);
    
    BookingResponseDTO updateBooking(UpsertBookingRequest request, Integer id);
    
    BookingResponseDTO updateBookingByAdmin(AdminBookingRequest request, Integer id);
    
    BookingResponseDTO cancelBooking(Integer id);
    
    BookingResponseDTO confirmBooking(Integer id);
    
    BookingResponseDTO checkInBooking(Integer id);
    
    BookingResponseDTO checkOutBooking(Integer id);
    
    void deleteBooking(Integer id);
    
    List<RoomListResponseDTO> getBookedRoomsByDateRange(LocalDate startDate, LocalDate endDate);
    
    List<NewBookingResponse> getRecentBookings();

    Map<String, Object> getBookingPaymentInfo(Integer bookingId);
    
    BookingResponseDTO markBookingAsPaid(Integer bookingId, String paymentMethod);
    
    void updatePaymentAndBookingStatusAfterVNPay(Integer bookingId, String transactionNo, String responseCode);
}