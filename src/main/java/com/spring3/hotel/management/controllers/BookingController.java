package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dtos.request.UpsertBookingRequest;
import com.spring3.hotel.management.dtos.response.BookingResponseDTO;
import com.spring3.hotel.management.dtos.response.NewBookingResponse;
import com.spring3.hotel.management.services.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // Lấy thông tin booking theo ID
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDTO> getBookingById(@PathVariable Integer id) {
        BookingResponseDTO bookingResponseDTO = bookingService.getBookingById(id);
        return ResponseEntity.ok(bookingResponseDTO);
    }

    // Lấy danh sách booking theo userId
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponseDTO>> getBookingsByUserId(@PathVariable Integer userId) {
        List<BookingResponseDTO> bookings = bookingService.getBookingsByUserId(userId);
        return ResponseEntity.ok(bookings);
    }

    // Lấy tất cả booking
    @GetMapping("/")
    public ResponseEntity<List<BookingResponseDTO>> getAllBookings() {
        List<BookingResponseDTO> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    //lấy danh sách booking mới nhất trong 7 ngày
    @GetMapping("/recent")
    public ResponseEntity<List<NewBookingResponse>> getRecentBookings() {
        List<NewBookingResponse> bookings = bookingService.getRecentBookings();
        return ResponseEntity.ok(bookings);
    }

    // Lấy danh sách booking theo trạng thái
    @GetMapping("/status/{status}")
    public ResponseEntity<List<BookingResponseDTO>> getBookingsByStatus(@PathVariable String status) {
        List<BookingResponseDTO> bookings = bookingService.getBookingsByStatus(status);
        return ResponseEntity.ok(bookings);
    }

    // Tạo mới booking
    @PostMapping("/create")
    public ResponseEntity<BookingResponseDTO> createBooking(@RequestBody UpsertBookingRequest request) {
        BookingResponseDTO bookingResponseDTO = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingResponseDTO);
    }

    // Cập nhật thông tin booking
    @PutMapping("/update/{id}")
    public ResponseEntity<BookingResponseDTO> updateBooking(
            @PathVariable Integer id,
            @RequestBody UpsertBookingRequest request) {
        BookingResponseDTO bookingResponseDTO = bookingService.updateBooking(request, id);
        return ResponseEntity.ok(bookingResponseDTO);
    }
}
