package com.spring3.hotel.management.services;

import com.spring3.hotel.management.dto.request.UpsertBookingRequest;
import com.spring3.hotel.management.dto.request.AdminBookingRequest;
import com.spring3.hotel.management.dto.response.BookingResponseDTO;
import com.spring3.hotel.management.dto.response.NewBookingResponse;
import com.spring3.hotel.management.dto.response.RoomListResponseDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Unified BookingService interface combining user and admin functionalities
 */
public interface BookingService {
    // Phương thức lấy thông tin
    List<NewBookingResponse> getRecentBookings();
    BookingResponseDTO getBookingById(Integer id);
    List<BookingResponseDTO> getBookingsByUserId(Integer userId);
    List<BookingResponseDTO> getAllBookings();
    List<BookingResponseDTO> getAllBookings(int page, int size); // Phân trang
    List<BookingResponseDTO> getAllBookingsNoPage(); // Cho admin
    List<BookingResponseDTO> getBookingsByStatus(String status);
    List<BookingResponseDTO> getBookingsByDateRange(LocalDate startDate, LocalDate endDate);
    List<BookingResponseDTO> getConfirmedBookingsByDateRange(LocalDate startDate, LocalDate endDate);
    List<RoomListResponseDTO> getBookedRoomsByDateRange(LocalDate startDate, LocalDate endDate);
    
    // Phương thức thêm booking
    BookingResponseDTO createBooking(UpsertBookingRequest request); // Cho user thường
    BookingResponseDTO createBooking(UpsertBookingRequest request, String username); // Cho user đã đăng nhập
    BookingResponseDTO createBookingByAdmin(AdminBookingRequest request); // Cho admin
    BookingResponseDTO createBookingTest(UpsertBookingRequest request); // Cho testing
    
    // Phương thức cập nhật
    BookingResponseDTO updateBooking(UpsertBookingRequest request, Integer id); // Cho user thường
    BookingResponseDTO updateBookingByAdmin(AdminBookingRequest request, Integer id); // Cho admin
    
    // Phương thức quản lý trạng thái
    BookingResponseDTO cancelBooking(Integer id);
    BookingResponseDTO confirmBooking(Integer id);
    BookingResponseDTO checkInBooking(Integer id); // Chỉ admin
    BookingResponseDTO checkOutBooking(Integer id); // Chỉ admin
    void deleteBooking(Integer id); // Chỉ admin
    
    // Phương thức thanh toán
    Map<String, Object> getBookingPaymentInfo(Integer bookingId);
    BookingResponseDTO markBookingAsPaid(Integer bookingId, String paymentMethod);
    void updatePaymentAndBookingStatusAfterVNPay(Integer bookingId, String transactionNo, String responseCode);
}
