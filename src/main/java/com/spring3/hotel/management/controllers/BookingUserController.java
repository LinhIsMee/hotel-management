package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dtos.request.UpsertBookingRequest;
import com.spring3.hotel.management.dtos.response.BookingResponseDTO;
import com.spring3.hotel.management.dtos.response.PaymentLinkResponse;
import com.spring3.hotel.management.dtos.response.RoomListResponseDTO;
import com.spring3.hotel.management.models.Booking;
import com.spring3.hotel.management.services.BookingService;
import com.spring3.hotel.management.services.VNPayService;
import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping("/api/v1/user/bookings")
public class BookingUserController {

    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private VNPayService vnPayService;

    // Lấy thông tin booking theo ID (cho người dùng đã đăng nhập)
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDTO> getBookingById(@PathVariable Integer id) {
        BookingResponseDTO bookingResponseDTO = bookingService.getBookingById(id);
        
        // Đảm bảo người dùng chỉ có thể xem booking của chính họ
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        if (!bookingResponseDTO.getUserId().toString().equals(username) && 
            !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(bookingResponseDTO);
    }

    // Lấy danh sách booking của người dùng hiện tại
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/my-bookings")
    public ResponseEntity<List<BookingResponseDTO>> getCurrentUserBookings() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Integer userId = Integer.parseInt(username);
        
        List<BookingResponseDTO> bookings = bookingService.getBookingsByUserId(userId);
        return ResponseEntity.ok(bookings);
    }

    // Lấy danh sách phòng đã đặt trong khoảng thời gian (để kiểm tra trước khi đặt)
    @GetMapping("/booked-rooms")
    public ResponseEntity<List<RoomListResponseDTO>> getBookedRoomsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<RoomListResponseDTO> rooms = bookingService.getBookedRoomsByDateRange(startDate, endDate);
        return ResponseEntity.ok(rooms);
    }

    // Tạo mới booking và tạo liên kết thanh toán VNPay
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createBookingWithPayment(@RequestBody UpsertBookingRequest request) {
        // Tự động cập nhật userId từ thông tin người dùng đăng nhập
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Integer userId = Integer.parseInt(username);
        request.setUserId(userId);
        
        // Đặt trạng thái mặc định là PENDING (chờ thanh toán)
        request.setStatus("PENDING");
        
        // Tạo booking
        BookingResponseDTO bookingResponseDTO = bookingService.createBooking(request);
        
        // Tạo liên kết thanh toán VNPay
        String orderInfo = "Thanh toan dat phong khach san - Ma dat phong: " + bookingResponseDTO.getId();
        Long amount = bookingResponseDTO.getFinalPrice().longValue();
        String paymentUrl = vnPayService.createPayment(bookingResponseDTO.getId(), amount, orderInfo);
        
        // Trả về thông tin booking và liên kết thanh toán
        Map<String, Object> response = new HashMap<>();
        response.put("booking", bookingResponseDTO);
        response.put("payment", new PaymentLinkResponse(paymentUrl, orderInfo));
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Cập nhật thông tin booking (chỉ cho phép khi booking ở trạng thái PENDING)
    @PreAuthorize("hasRole('ROLE_USER')")
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateBooking(
            @PathVariable Integer id,
            @RequestBody UpsertBookingRequest request) {
        // Đảm bảo người dùng chỉ có thể cập nhật booking của chính họ
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Integer userId = Integer.parseInt(username);
        
        // Kiểm tra booking có thuộc về người dùng hiện tại không
        BookingResponseDTO existingBooking = bookingService.getBookingById(id);
        if (!existingBooking.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Bạn không có quyền cập nhật booking này"));
        }
        
        // Kiểm tra trạng thái booking
        if (!"PENDING".equals(existingBooking.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Chỉ có thể cập nhật booking có trạng thái PENDING"));
        }
        
        // Cập nhật booking
        request.setUserId(userId);
        BookingResponseDTO bookingResponseDTO = bookingService.updateBooking(request, id);
        
        // Tạo liên kết thanh toán mới nếu có thay đổi giá
        if (!existingBooking.getFinalPrice().equals(bookingResponseDTO.getFinalPrice())) {
            String orderInfo = "Thanh toan dat phong khach san - Ma dat phong: " + bookingResponseDTO.getId();
            Long amount = bookingResponseDTO.getFinalPrice().longValue();
            String paymentUrl = vnPayService.createPayment(bookingResponseDTO.getId(), amount, orderInfo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("booking", bookingResponseDTO);
            response.put("payment", new PaymentLinkResponse(paymentUrl, orderInfo));
            
            return ResponseEntity.ok(response);
        }
        
        return ResponseEntity.ok(bookingResponseDTO);
    }
    
    // Hủy booking (chỉ cho phép khi trạng thái là PENDING hoặc CONFIRMED)
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/cancel/{id}")
    public ResponseEntity<?> cancelBooking(@PathVariable Integer id) {
        // Đảm bảo người dùng chỉ có thể hủy booking của chính họ
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Integer userId = Integer.parseInt(username);
        
        // Kiểm tra booking có thuộc về người dùng hiện tại không
        BookingResponseDTO existingBooking = bookingService.getBookingById(id);
        if (!existingBooking.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Bạn không có quyền hủy booking này"));
        }
        
        // Kiểm tra trạng thái booking
        if (!"PENDING".equals(existingBooking.getStatus()) && !"CONFIRMED".equals(existingBooking.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Chỉ có thể hủy booking có trạng thái PENDING hoặc CONFIRMED"));
        }
        
        // Hủy booking
        BookingResponseDTO bookingResponseDTO = bookingService.cancelBooking(id);
        return ResponseEntity.ok(bookingResponseDTO);
    }
    
    // Kiểm tra trạng thái thanh toán của booking
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/payment-status/{id}")
    public ResponseEntity<?> getBookingPaymentStatus(@PathVariable Integer id) {
        // Đảm bảo người dùng chỉ có thể xem trạng thái thanh toán của booking của chính họ
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Integer userId = Integer.parseInt(username);
        
        // Kiểm tra booking có thuộc về người dùng hiện tại không
        BookingResponseDTO existingBooking = bookingService.getBookingById(id);
        if (!existingBooking.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Bạn không có quyền xem trạng thái thanh toán của booking này"));
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("booking", existingBooking);
        response.put("paymentStatus", existingBooking.getPaymentStatus());
        response.put("paymentMethod", existingBooking.getPaymentMethod());
        response.put("paymentDate", existingBooking.getPaymentDate());
        
        return ResponseEntity.ok(response);
    }
} 