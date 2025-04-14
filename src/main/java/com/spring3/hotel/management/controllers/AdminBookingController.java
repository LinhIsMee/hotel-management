package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dtos.request.AdminBookingRequest;
import com.spring3.hotel.management.dtos.response.BookingResponseDTO;
import com.spring3.hotel.management.dtos.response.NewBookingResponse;
import com.spring3.hotel.management.services.AdminBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/bookings")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminBookingController {

    @Autowired
    private AdminBookingService adminBookingService;

    /**
     * Lấy tất cả booking (cho admin)
     */
    @GetMapping("/")
    public ResponseEntity<List<BookingResponseDTO>> getAllBookings() {
        List<BookingResponseDTO> bookings = adminBookingService.getAllBookingsNoPage(); // Sử dụng phương thức mới
        return ResponseEntity.ok(bookings);
    }

    /**
     * Lấy tất cả booking (cho admin) - có phân trang
     */
    @GetMapping("/paged")
    public ResponseEntity<List<BookingResponseDTO>> getPagedBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<BookingResponseDTO> bookings = adminBookingService.getAllBookings(page, size);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Lấy booking theo ID (cho admin)
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDTO> getBookingById(@PathVariable Integer id) {
        BookingResponseDTO booking = adminBookingService.getBookingById(id);
        return ResponseEntity.ok(booking);
    }

    /**
     * Lấy danh sách booking mới nhất (cho admin)
     */
    @GetMapping("/recent")
    public ResponseEntity<List<NewBookingResponse>> getRecentBookings() {
        List<NewBookingResponse> bookings = adminBookingService.getRecentBookings();
        return ResponseEntity.ok(bookings);
    }

    /**
     * Lấy danh sách booking theo trạng thái (cho admin)
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<BookingResponseDTO>> getBookingsByStatus(@PathVariable String status) {
        List<BookingResponseDTO> bookings = adminBookingService.getBookingsByStatus(status);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Lấy danh sách booking theo khoảng thời gian (cho admin)
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<BookingResponseDTO>> getBookingsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<BookingResponseDTO> bookings = adminBookingService.getBookingsByDateRange(startDate, endDate);
        return ResponseEntity.ok(bookings);
    }
    
    /**
     * Tạo mới booking (dành riêng cho admin)
     */
    @PostMapping("/create")
    public ResponseEntity<BookingResponseDTO> createBookingByAdmin(@RequestBody AdminBookingRequest request) {
        BookingResponseDTO bookingResponseDTO = adminBookingService.createBookingByAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingResponseDTO);
    }
    
    /**
     * Cập nhật booking (dành riêng cho admin)
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<BookingResponseDTO> updateBookingByAdmin(
            @PathVariable Integer id,
            @RequestBody AdminBookingRequest request) {
        BookingResponseDTO bookingResponseDTO = adminBookingService.updateBookingByAdmin(request, id);
        return ResponseEntity.ok(bookingResponseDTO);
    }
    
    /**
     * Hủy booking (dành riêng cho admin)
     */
    @PostMapping("/cancel/{id}")
    public ResponseEntity<BookingResponseDTO> cancelBookingByAdmin(@PathVariable Integer id) {
        BookingResponseDTO bookingResponseDTO = adminBookingService.cancelBooking(id);
        return ResponseEntity.ok(bookingResponseDTO);
    }
    
    /**
     * Xác nhận booking (dành riêng cho admin)
     */
    @PostMapping("/confirm/{id}")
    public ResponseEntity<BookingResponseDTO> confirmBookingByAdmin(@PathVariable Integer id) {
        BookingResponseDTO bookingResponseDTO = adminBookingService.confirmBooking(id);
        return ResponseEntity.ok(bookingResponseDTO);
    }
    
    /**
     * Check-in booking (dành riêng cho admin)
     */
    @PostMapping("/check-in/{id}")
    public ResponseEntity<BookingResponseDTO> checkInBookingByAdmin(@PathVariable Integer id) {
        BookingResponseDTO bookingResponseDTO = adminBookingService.checkInBooking(id);
        return ResponseEntity.ok(bookingResponseDTO);
    }
    
    /**
     * Check-out booking (dành riêng cho admin)
     */
    @PostMapping("/check-out/{id}")
    public ResponseEntity<BookingResponseDTO> checkOutBookingByAdmin(@PathVariable Integer id) {
        BookingResponseDTO bookingResponseDTO = adminBookingService.checkOutBooking(id);
        return ResponseEntity.ok(bookingResponseDTO);
    }
    
    /**
     * Xóa booking (dành riêng cho admin)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteBookingByAdmin(@PathVariable Integer id) {
        adminBookingService.deleteBooking(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Booking đã được xóa thành công");
        response.put("bookingId", id);
        
        return ResponseEntity.ok(response);
    }
} 