package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dtos.request.UpsertBookingRequest;
import com.spring3.hotel.management.dtos.response.BookingResponseDTO;
import com.spring3.hotel.management.dtos.response.NewBookingResponse;
import com.spring3.hotel.management.dtos.response.RoomListResponseDTO;
import com.spring3.hotel.management.services.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // Lấy thông tin booking theo ID (cho cả user và admin)
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDTO> getBookingById(@PathVariable Integer id) {
        BookingResponseDTO bookingResponseDTO = bookingService.getBookingById(id);
        return ResponseEntity.ok(bookingResponseDTO);
    }

    // Lấy danh sách booking theo userId (cho user đã đăng nhập)
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponseDTO>> getBookingsByUserId(@PathVariable Integer userId) {
        List<BookingResponseDTO> bookings = bookingService.getBookingsByUserId(userId);
        return ResponseEntity.ok(bookings);
    }

    // API chỉ dành cho admin - được chuyển sang AdminBookingController
    // Ẩn toàn bộ danh sách booking
    /*
    @GetMapping("/")
    public ResponseEntity<List<BookingResponseDTO>> getAllBookings() {
        List<BookingResponseDTO> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<NewBookingResponse>> getRecentBookings() {
        List<NewBookingResponse> bookings = bookingService.getRecentBookings();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<BookingResponseDTO>> getBookingsByStatus(@PathVariable String status) {
        List<BookingResponseDTO> bookings = bookingService.getBookingsByStatus(status);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<BookingResponseDTO>> getBookingsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<BookingResponseDTO> bookings = bookingService.getBookingsByDateRange(startDate, endDate);
        return ResponseEntity.ok(bookings);
    }
    */
    
    // Lấy danh sách phòng đã đặt trong khoảng thời gian (cho cả user và admin)
    @GetMapping("/booked-rooms")
    public ResponseEntity<List<RoomListResponseDTO>> getBookedRoomsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<RoomListResponseDTO> rooms = bookingService.getBookedRoomsByDateRange(startDate, endDate);
        return ResponseEntity.ok(rooms);
    }

    // Tạo mới booking (cho user thông thường - sẽ có quy trình thanh toán VNPay)
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/create")
    public ResponseEntity<BookingResponseDTO> createBooking(@RequestBody UpsertBookingRequest request) {
        BookingResponseDTO bookingResponseDTO = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingResponseDTO);
    }

    // Cập nhật thông tin booking (cho user thông thường - chỉ cho phép trong trạng thái PENDING)
    @PreAuthorize("hasRole('ROLE_USER')")
    @PutMapping("/update/{id}")
    public ResponseEntity<BookingResponseDTO> updateBooking(
            @PathVariable Integer id,
            @RequestBody UpsertBookingRequest request) {
        BookingResponseDTO bookingResponseDTO = bookingService.updateBooking(request, id);
        return ResponseEntity.ok(bookingResponseDTO);
    }
    
    // Hủy booking (cho user thông thường - chỉ cho phép trong trạng thái PENDING hoặc CONFIRMED)
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/cancel/{id}")
    public ResponseEntity<BookingResponseDTO> cancelBooking(@PathVariable Integer id) {
        BookingResponseDTO bookingResponseDTO = bookingService.cancelBooking(id);
        return ResponseEntity.ok(bookingResponseDTO);
    }
    
    // Xác nhận booking sau khi thanh toán (gọi từ callback của VNPay)
    @PostMapping("/confirm/{id}")
    public ResponseEntity<BookingResponseDTO> confirmBooking(@PathVariable Integer id) {
        BookingResponseDTO bookingResponseDTO = bookingService.confirmBooking(id);
        return ResponseEntity.ok(bookingResponseDTO);
    }
}
