package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dto.request.AdminBookingRequest;
import com.spring3.hotel.management.dto.request.UpsertBookingRequest;
import com.spring3.hotel.management.dto.response.BookingResponseDTO;
import com.spring3.hotel.management.dto.response.RoomListResponseDTO;
import com.spring3.hotel.management.models.User;
import com.spring3.hotel.management.repositories.UserRepository;
import com.spring3.hotel.management.services.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller quản lý đặt phòng cho cả người dùng và admin
 */
@RestController
@RequestMapping("/api/v1/bookings")
@Slf4j
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;

    /**
     * Lấy thông tin đặt phòng theo ID (không cần đăng nhập)
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingById(@PathVariable Integer id) {
        try {
            // Lấy booking
            BookingResponseDTO bookingResponse = bookingService.getBookingById(id);
            
            // Kiểm tra booking có tồn tại không
            if (bookingResponse == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Không tìm thấy booking."));
            }
            
            return ResponseEntity.ok(bookingResponse);
        } catch (Exception e) {
            log.error("Lỗi lấy booking {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi máy chủ nội bộ."));
        }
    }

    /**
     * API tổng hợp lấy danh sách đặt phòng với nhiều tiêu chí lọc
     * Hỗ trợ lọc theo: người dùng, trạng thái, khoảng thời gian, phân trang
     */
    @GetMapping
    public ResponseEntity<?> getBookings(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "false") boolean noPage) {
        try {
            List<BookingResponseDTO> bookings;
            String message = "Lấy danh sách đặt phòng thành công";
            
            // Lọc theo userId
            if (userId != null) {
                bookings = bookingService.getBookingsByUserId(userId);
                message = "Lấy danh sách đặt phòng theo người dùng thành công";
            }
            // Lọc theo trạng thái
            else if (status != null && !status.isEmpty()) {
                bookings = bookingService.getBookingsByStatus(status);
                message = "Lấy danh sách đặt phòng theo trạng thái thành công";
            }
            // Lọc theo khoảng thời gian
            else if (startDate != null && endDate != null) {
                bookings = bookingService.getBookingsByDateRange(startDate, endDate);
                message = "Lấy danh sách đặt phòng theo khoảng thời gian thành công";
            }
            // Lấy tất cả đặt phòng
            else {
                if (noPage) {
                    bookings = bookingService.getAllBookings();
                } else {
                    bookings = bookingService.getAllBookings(page, size);
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", message,
                "bookings", bookings
            ));
        } catch (Exception e) {
            log.error("Lỗi lấy danh sách đặt phòng: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy danh sách booking của người dùng hiện tại
     */
    @GetMapping("/my-bookings")
    public ResponseEntity<?> getMyBookings() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            User user = userRepository.findByUsername(username);
            if (user == null) {
                throw new RuntimeException("Không tìm thấy người dùng với username: " + username);
            }
            
            List<BookingResponseDTO> bookings = bookingService.getBookingsByUserId(user.getId());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Lấy danh sách đặt phòng của bạn thành công",
                "bookings", bookings
            ));
        } catch (Exception e) {
            log.error("Lỗi lấy danh sách booking: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy danh sách đặt phòng theo userId
     * @deprecated Sử dụng API GET ?userId={userId} thay thế
     */
    @GetMapping("/user/{userId}")
    @Deprecated
    public ResponseEntity<List<BookingResponseDTO>> getBookingsByUserId(@PathVariable Integer userId) {
        List<BookingResponseDTO> bookings = bookingService.getBookingsByUserId(userId);
        return ResponseEntity.ok(bookings);
    }
    
    /**
     * Lấy tất cả đặt phòng
     * @deprecated Sử dụng API GET ?page={page}&size={size} thay thế
     */
    @GetMapping("/all")
    @Deprecated
    public ResponseEntity<List<BookingResponseDTO>> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<BookingResponseDTO> bookings = bookingService.getAllBookings(page, size);
        return ResponseEntity.ok(bookings);
    }
    
    /**
     * Lấy tất cả đặt phòng - không phân trang
     * @deprecated Sử dụng API GET ?noPage=true thay thế
     */
    @GetMapping("/all/no-page")
    @Deprecated
    public ResponseEntity<List<BookingResponseDTO>> getAllBookingsNoPage() {
        List<BookingResponseDTO> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }
    
    /**
     * Lấy danh sách đặt phòng theo trạng thái
     * @deprecated Sử dụng API GET ?status={status} thay thế
     */
    @GetMapping("/status/{status}")
    @Deprecated
    public ResponseEntity<List<BookingResponseDTO>> getBookingsByStatus(@PathVariable String status) {
        List<BookingResponseDTO> bookings = bookingService.getBookingsByStatus(status);
        return ResponseEntity.ok(bookings);
    }
    
    /**
     * Lấy danh sách đặt phòng theo khoảng thời gian
     * @deprecated Sử dụng API GET ?startDate={startDate}&endDate={endDate} thay thế
     */
    @GetMapping("/date-range")
    @Deprecated
    public ResponseEntity<List<BookingResponseDTO>> getBookingsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<BookingResponseDTO> bookings = bookingService.getBookingsByDateRange(startDate, endDate);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Lấy danh sách phòng đã đặt trong khoảng thời gian
     * (Cho mục đích kiểm tra phòng trống)
     */
    @GetMapping("/booked-rooms")
    public ResponseEntity<List<RoomListResponseDTO>> getBookedRoomsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<RoomListResponseDTO> rooms = bookingService.getBookedRoomsByDateRange(startDate, endDate);
        return ResponseEntity.ok(rooms);
    }

    /**
     * Tạo mới đơn đặt phòng (không cần đăng nhập, dành cho cả user và admin)
     */
    @PostMapping("/admin-create")
    public ResponseEntity<Map<String, Object>> createBookingAdmin(@Valid @RequestBody AdminBookingRequest request) {
        try {
            // Tạo booking với thông tin từ request
            BookingResponseDTO bookingResponseDTO = bookingService.createBookingByAdmin(request);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "booking", bookingResponseDTO,
                "message", "Đặt phòng thành công."
            ));
        } catch (Exception e) {
            log.error("Lỗi tạo booking: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "success", false, 
                        "message", "Lỗi khi đặt phòng: " + e.getMessage()
                    ));
        }
    }

    /**
     * Tạo mới đơn đặt phòng (không cần đăng nhập)
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createBooking(@Valid @RequestBody UpsertBookingRequest request) {
        try {
            // Tạo booking với thông tin từ request
            BookingResponseDTO bookingResponseDTO = bookingService.createBooking(request);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "booking", bookingResponseDTO,
                "message", "Đặt phòng thành công. Vui lòng tiến hành thanh toán."
            ));
        } catch (Exception e) {
            log.error("Lỗi tạo booking: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "success", false, 
                        "message", "Lỗi khi đặt phòng: " + e.getMessage()
                    ));
        }
    }

    /**
     * Cập nhật thông tin đặt phòng
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateBooking(
            @PathVariable Integer id,
            @Valid @RequestBody UpsertBookingRequest request) {
        try {
            BookingResponseDTO updatedBooking = bookingService.updateBooking(request, id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "booking", updatedBooking,
                "message", "Cập nhật booking thành công."
            ));
        } catch (Exception e) {
            log.error("Lỗi cập nhật booking {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }

    /**
     * Hủy đặt phòng
     */
    @PostMapping("/cancel/{id}")
    public ResponseEntity<?> cancelBooking(@PathVariable Integer id) {
        try {
            BookingResponseDTO cancelledBooking = bookingService.cancelBooking(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "booking", cancelledBooking,
                "message", "Đã hủy booking thành công."
            ));
        } catch (Exception e) {
            log.error("Lỗi hủy booking {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }

    /**
     * Xác nhận đặt phòng
     */
    @PostMapping("/confirm/{id}")
    public ResponseEntity<BookingResponseDTO> confirmBooking(@PathVariable Integer id) {
        BookingResponseDTO bookingResponseDTO = bookingService.confirmBooking(id);
        return ResponseEntity.ok(bookingResponseDTO);
    }

    /**
     * Lấy chi tiết booking (bao gồm thông tin phòng, dịch vụ và thanh toán)
     * API này thay thế cho các API trùng lặp: /check-status/{bookingId}, /{id}/payment-info
     */
    @GetMapping({"/detail/{id}", "/check-status/{id}"})
    public ResponseEntity<?> getBookingDetail(@PathVariable Integer id) {
        try {
            // Lấy chi tiết booking
            BookingResponseDTO booking = bookingService.getBookingById(id);
            
            if (booking == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Không tìm thấy booking."));
            }
            
            // Lấy thông tin payment
            Map<String, Object> paymentInfo = bookingService.getBookingPaymentInfo(id);
            
            // Kết hợp thông tin booking và payment
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("booking", booking);
            result.put("payment", paymentInfo);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Lỗi lấy chi tiết booking {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy thông tin thanh toán của booking
     * @deprecated Sử dụng API GET /detail/{id} thay thế
     */
    @GetMapping("/{id}/payment-info")
    @Deprecated
    public ResponseEntity<Map<String, Object>> getBookingPaymentInfo(@PathVariable("id") Integer bookingId) {
        Map<String, Object> paymentInfo = bookingService.getBookingPaymentInfo(bookingId);
        return ResponseEntity.ok(paymentInfo);
    }
    
    /**
     * API check-in booking
     */
    @PostMapping("/check-in/{id}")
    public ResponseEntity<BookingResponseDTO> checkInBooking(@PathVariable Integer id) {
        BookingResponseDTO bookingResponseDTO = bookingService.checkInBooking(id);
        return ResponseEntity.ok(bookingResponseDTO);
    }
    
    /**
     * API check-out booking
     */
    @PostMapping("/check-out/{id}")
    public ResponseEntity<BookingResponseDTO> checkOutBooking(@PathVariable Integer id) {
        BookingResponseDTO bookingResponseDTO = bookingService.checkOutBooking(id);
        return ResponseEntity.ok(bookingResponseDTO);
    }
    
    /**
     * API xóa booking
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteBooking(@PathVariable Integer id) {
        bookingService.deleteBooking(id);
        
        Map<String, Object> response = Map.of(
            "message", "Booking đã được xóa thành công",
            "bookingId", id
        );
        
        return ResponseEntity.ok(response);
    }
}
