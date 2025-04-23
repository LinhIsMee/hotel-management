package com.spring3.hotel.management.controllers;

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
import org.springframework.security.access.prepost.PreAuthorize;
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
     * Lấy thông tin đặt phòng theo ID
     * - Người dùng chỉ có thể xem các đặt phòng của họ
     * - Admin có thể xem tất cả đặt phòng
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> getBookingById(@PathVariable Integer id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            User currentUser = userRepository.findByEmail(currentUsername)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng"));

            // Lấy booking
            BookingResponseDTO bookingResponse = bookingService.getBookingById(id);
            
            // Kiểm tra quyền truy cập
            if (bookingResponse == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Không tìm thấy booking."));
            }
            
            // Admin có thể xem tất cả, user chỉ xem được booking của mình
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                    
            if (isAdmin || bookingResponse.getUserId().equals(currentUser.getId())) {
                return ResponseEntity.ok(bookingResponse);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Bạn không có quyền truy cập booking này."));
            }
        } catch (Exception e) {
            log.error("Lỗi lấy booking {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi máy chủ nội bộ."));
        }
    }

    /**
     * Lấy danh sách booking của người dùng hiện tại
     */
    @GetMapping("/my-bookings")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<List<BookingResponseDTO>> getMyBookings() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("Không tìm thấy người dùng với username: " + username);
        }
        
        List<BookingResponseDTO> bookings = bookingService.getBookingsByUserId(user.getId());
        return ResponseEntity.ok(bookings);
    }
    
    /**
     * Lấy danh sách đặt phòng theo userId (chỉ cho admin)
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<BookingResponseDTO>> getBookingsByUserId(@PathVariable Integer userId) {
        List<BookingResponseDTO> bookings = bookingService.getBookingsByUserId(userId);
        return ResponseEntity.ok(bookings);
    }
    
    /**
     * Lấy tất cả đặt phòng (chỉ cho admin)
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<BookingResponseDTO>> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<BookingResponseDTO> bookings = bookingService.getAllBookings(page, size);
        return ResponseEntity.ok(bookings);
    }
    
    /**
     * Lấy tất cả đặt phòng (chỉ cho admin) - không phân trang
     */
    @GetMapping("/all/no-page")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<BookingResponseDTO>> getAllBookingsNoPage() {
        List<BookingResponseDTO> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }
    
    /**
     * Lấy danh sách đặt phòng theo trạng thái (chỉ cho admin)
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<BookingResponseDTO>> getBookingsByStatus(@PathVariable String status) {
        List<BookingResponseDTO> bookings = bookingService.getBookingsByStatus(status);
        return ResponseEntity.ok(bookings);
    }
    
    /**
     * Lấy danh sách đặt phòng theo khoảng thời gian (chỉ cho admin)
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
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
     * Tạo mới đơn đặt phòng
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Map<String, Object>> createBooking(@Valid @RequestBody UpsertBookingRequest request) {
        // Tự động cập nhật userId từ thông tin người dùng đăng nhập
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("Không tìm thấy người dùng với username: " + username);
        }
        
        request.setUserId(user.getId());
        request.setStatus("PENDING");
        
        // Tạo booking
        BookingResponseDTO bookingResponseDTO = bookingService.createBooking(request);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "booking", bookingResponseDTO,
            "message", "Đặt phòng thành công. Vui lòng tiến hành thanh toán."
        ));
    }

    /**
     * Cập nhật thông tin đặt phòng
     */
    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> updateBooking(
            @PathVariable Integer id,
            @Valid @RequestBody UpsertBookingRequest request) {
        try {
            // Xác thực người dùng
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Người dùng không hợp lệ"));
            }
            
            // Kiểm tra booking có thuộc về người dùng không
            BookingResponseDTO existingBooking = bookingService.getBookingById(id);
            if (existingBooking == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Không tìm thấy booking."));
            }
            
            if (!existingBooking.getUserId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Bạn không có quyền cập nhật booking này."));
            }
            
            // Chỉ cho phép cập nhật các booking có trạng thái PENDING hoặc CONFIRMED
            if (!existingBooking.getStatus().equals("PENDING") && !existingBooking.getStatus().equals("CONFIRMED")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                            "success", false,
                            "message", "Chỉ có thể cập nhật booking có trạng thái PENDING hoặc CONFIRMED."
                        ));
            }
            
            // Cập nhật booking
            request.setUserId(user.getId());
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
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> cancelBooking(@PathVariable Integer id) {
        try {
            // Xác thực người dùng
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Người dùng không hợp lệ"));
            }
            
            // Kiểm tra booking có thuộc về người dùng không
            BookingResponseDTO existingBooking = bookingService.getBookingById(id);
            if (existingBooking == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Không tìm thấy booking."));
            }
            
            if (!existingBooking.getUserId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Bạn không có quyền hủy booking này."));
            }
            
            // Hủy booking
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
     * Xác nhận đặt phòng (chỉ cho admin)
     */
    @PostMapping("/confirm/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<BookingResponseDTO> confirmBooking(@PathVariable Integer id) {
        BookingResponseDTO bookingResponseDTO = bookingService.confirmBooking(id);
        return ResponseEntity.ok(bookingResponseDTO);
    }

    /**
     * Kiểm tra trạng thái đặt phòng
     */
    @GetMapping("/check-status/{bookingId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> checkBookingStatus(@PathVariable Integer bookingId) {
        try {
            // Xác thực người dùng
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Người dùng không hợp lệ"));
            }
            
            // Kiểm tra booking có tồn tại không
            BookingResponseDTO booking = bookingService.getBookingById(bookingId);
            
            if (booking == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Không tìm thấy booking."));
            }
            
            // Kiểm tra nếu booking thuộc về người dùng hiện tại
            if (!booking.getUserId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Bạn không có quyền kiểm tra booking này."));
            }
            
            // Lấy thông tin thanh toán
            Map<String, Object> paymentInfo = bookingService.getBookingPaymentInfo(bookingId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("booking", booking);
            response.put("payment", paymentInfo);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi kiểm tra trạng thái booking {}: {}", bookingId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }

    /**
     * Lấy thông tin thanh toán của booking
     */
    @GetMapping("/{id}/payment-info")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getBookingPaymentInfo(@PathVariable("id") Integer bookingId) {
        Map<String, Object> paymentInfo = bookingService.getBookingPaymentInfo(bookingId);
        return ResponseEntity.ok(paymentInfo);
    }
    
    /**
     * Lấy chi tiết booking (bao gồm thông tin phòng, dịch vụ và thanh toán)
     */
    @GetMapping("/detail/{id}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> getBookingDetail(@PathVariable Integer id) {
        try {
            // Xác thực quyền truy cập
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng"));
            
            // Lấy chi tiết booking
            BookingResponseDTO booking = bookingService.getBookingById(id);
            
            if (booking == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Không tìm thấy booking."));
            }
            
            // Kiểm tra quyền truy cập (admin có thể xem tất cả, người dùng chỉ xem được booking của mình)
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            if (!isAdmin && !booking.getUserId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Bạn không có quyền truy cập booking này."));
            }
            
            // Lấy thông tin payment
            Map<String, Object> paymentInfo = bookingService.getBookingPaymentInfo(id);
            
            // Kết hợp thông tin booking và payment
            Map<String, Object> result = new HashMap<>();
            result.put("booking", booking);
            result.put("payment", paymentInfo);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Lỗi lấy chi tiết booking {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "ERROR",
                "message", "Không thể lấy thông tin đặt phòng: " + e.getMessage()
            ));
        }
    }
    
    /**
     * API check-in booking (chỉ cho admin)
     */
    @PostMapping("/check-in/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<BookingResponseDTO> checkInBooking(@PathVariable Integer id) {
        BookingResponseDTO bookingResponseDTO = bookingService.checkInBooking(id);
        return ResponseEntity.ok(bookingResponseDTO);
    }
    
    /**
     * API check-out booking (chỉ cho admin)
     */
    @PostMapping("/check-out/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<BookingResponseDTO> checkOutBooking(@PathVariable Integer id) {
        BookingResponseDTO bookingResponseDTO = bookingService.checkOutBooking(id);
        return ResponseEntity.ok(bookingResponseDTO);
    }
    
    /**
     * API xóa booking (chỉ cho admin)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteBooking(@PathVariable Integer id) {
        bookingService.deleteBooking(id);
        
        Map<String, Object> response = Map.of(
            "message", "Booking đã được xóa thành công",
            "bookingId", id
        );
        
        return ResponseEntity.ok(response);
    }
}
