package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dto.request.UpsertBookingRequest;
import com.spring3.hotel.management.dto.response.BookingResponseDTO;
import com.spring3.hotel.management.dto.response.PaymentResponse;
import com.spring3.hotel.management.dto.response.RoomListResponseDTO;
import com.spring3.hotel.management.models.Booking;
import com.spring3.hotel.management.models.User;
import com.spring3.hotel.management.repositories.BookingRepository;
import com.spring3.hotel.management.repositories.PaymentRepository;
import com.spring3.hotel.management.repositories.UserRepository;
import com.spring3.hotel.management.services.BookingService;
import com.spring3.hotel.management.services.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

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
    private final VNPayService vnPayService;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

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
     * Tạo mới đơn đặt phòng và tạo liên kết thanh toán VNPay
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
        
        // Tạo liên kết thanh toán VNPay
        String orderInfo = "Thanh toan dat phong khach san - Ma dat phong: " + bookingResponseDTO.getId();
        Long amount = bookingResponseDTO.getFinalPrice().longValue();
        String returnUrl = "http://localhost:9000/api/v1/payments/callback";
        String ipAddress = "127.0.0.1";
        
        PaymentResponse paymentResponse = vnPayService.createPayment(orderInfo, amount, ipAddress, returnUrl);
        
        // Trả về thông tin booking và liên kết thanh toán
        Map<String, Object> response = new HashMap<>();
        response.put("booking", bookingResponseDTO);
        response.put("payment", paymentResponse);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Cập nhật thông tin đặt phòng
     * (Chỉ cho phép người dùng cập nhật trong trạng thái PENDING)
     */
    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> updateBooking(
            @PathVariable Integer id,
            @Valid @RequestBody UpsertBookingRequest request) {
        // Đảm bảo người dùng chỉ có thể cập nhật booking của chính họ
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("Không tìm thấy người dùng với username: " + username);
        }
        
        // Kiểm tra booking có thuộc về người dùng hiện tại không
        BookingResponseDTO existingBooking = bookingService.getBookingById(id);
        if (!existingBooking.getUserId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Bạn không có quyền cập nhật booking này"));
        }
        
        // Kiểm tra trạng thái booking
        if (!"PENDING".equals(existingBooking.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Chỉ có thể cập nhật booking có trạng thái PENDING"));
        }
        
        // Cập nhật booking
        request.setUserId(user.getId());
        BookingResponseDTO bookingResponseDTO = bookingService.updateBooking(request, id);
        
        // Tạo liên kết thanh toán mới nếu có thay đổi giá
        if (!existingBooking.getFinalPrice().equals(bookingResponseDTO.getFinalPrice())) {
            String orderInfo = "Thanh toan dat phong khach san - Ma dat phong: " + bookingResponseDTO.getId();
            Long amount = bookingResponseDTO.getFinalPrice().longValue();
            String returnUrl = "http://localhost:9000/api/v1/payments/callback";
            String ipAddress = "127.0.0.1";
            
            PaymentResponse paymentResponse = vnPayService.createPayment(orderInfo, amount, ipAddress, returnUrl);
            
            Map<String, Object> response = new HashMap<>();
            response.put("booking", bookingResponseDTO);
            response.put("payment", paymentResponse);
            
            return ResponseEntity.ok(response);
        }
        
        return ResponseEntity.ok(bookingResponseDTO);
    }
    
    /**
     * Hủy booking (chỉ cho phép khi trạng thái là PENDING hoặc CONFIRMED)
     */
    @PostMapping("/cancel/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> cancelBooking(@PathVariable Integer id) {
        // Đảm bảo người dùng chỉ có thể hủy booking của chính họ
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("Không tìm thấy người dùng với username: " + username);
        }
        
        // Kiểm tra booking có thuộc về người dùng hiện tại không
        BookingResponseDTO existingBooking = bookingService.getBookingById(id);
        if (!existingBooking.getUserId().equals(user.getId())) {
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
    
    /**
     * Xác nhận đơn đặt phòng sau khi thanh toán thành công
     * (Gọi từ VNPay callback hoặc từ admin)
     */
    @PostMapping("/confirm/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<BookingResponseDTO> confirmBooking(@PathVariable Integer id) {
        BookingResponseDTO bookingResponseDTO = bookingService.confirmBooking(id);
        return ResponseEntity.ok(bookingResponseDTO);
    }
    
    /**
     * Kiểm tra trạng thái thanh toán của booking
     */
    @GetMapping("/check-status/{bookingId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> checkBookingStatus(@PathVariable Integer bookingId) {
        try {
            // Lấy thông tin user đang đăng nhập
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentPrincipalName = authentication.getName();
            User currentUser = userRepository.findByEmail(currentPrincipalName)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng"));

            // Lấy DTO từ service
            BookingResponseDTO booking = bookingService.getBookingById(bookingId);
            
            if (booking != null && booking.getUserId().equals(currentUser.getId())) {
                String bookingStatus = booking.getStatus();
                String paymentStatus = booking.getPaymentStatus();
                
                if ("CONFIRMED".equals(bookingStatus) && "PAID".equals(paymentStatus)) { 
                    return ResponseEntity.ok(Map.of(
                            "success", true, 
                            "message", "Đơn đặt phòng đã được xác nhận và thanh toán."
                    ));
                } else {
                    return ResponseEntity.ok(Map.of(
                            "success", false, 
                            "message", "Trạng thái booking: " + bookingStatus + ", Trạng thái thanh toán: " + (paymentStatus != null ? paymentStatus : "PENDING")
                    ));
                }
            } else if (booking == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", "Không tìm thấy đơn đặt phòng."));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", "Bạn không có quyền truy cập booking này."));
            }
        } catch (Exception e) {
            log.error("Lỗi kiểm tra trạng thái booking {}: {}", bookingId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "Lỗi kiểm tra trạng thái booking: " + e.getMessage()));
        }
    }
    
    /**
     * API thông tin thanh toán
     * (Sử dụng để lấy thông tin trước khi thanh toán)
     */
    @GetMapping("/{id}/payment-info")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getBookingPaymentInfo(@PathVariable("id") Integer bookingId) {
        Map<String, Object> paymentInfo = bookingService.getBookingPaymentInfo(bookingId);
        return ResponseEntity.ok(paymentInfo);
    }
    
    /**
     * Lấy chi tiết booking kèm thông tin thanh toán
     */
    @GetMapping("/detail/{id}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> getBookingDetail(@PathVariable Integer id) {
        try {
            // Kiểm tra quyền truy cập
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentPrincipalName = authentication.getName();
            User currentUser = userRepository.findByEmail(currentPrincipalName)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng"));
            
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                    
            // Lấy thông tin booking DTO
            BookingResponseDTO booking = bookingService.getBookingById(id);
            
            if (booking == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                       .body(Map.of("success", false, "message", "Không tìm thấy booking."));
            }
            
            // Kiểm tra quyền truy cập
            if (!isAdmin && !booking.getUserId().equals(currentUser.getId())) {
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
     * API tạo URL thanh toán cho booking đã tồn tại
     */
    @PostMapping("/create-payment")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> createPaymentUrl(@RequestParam Integer bookingId, 
                                              @RequestParam(required = false) String bankCode, 
                                              HttpServletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentPrincipalName = authentication.getName();
            User currentUser = userRepository.findByEmail(currentPrincipalName)
                  .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng"));
            Integer userId = currentUser.getId();

            // Lấy BookingResponseDTO để kiểm tra và lấy giá tiền
            BookingResponseDTO bookingResponse = bookingService.getBookingById(bookingId);

            if (bookingResponse == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy booking với id: " + bookingId);
            }
             
            if (!bookingResponse.getUserId().equals(userId)) { 
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không có quyền truy cập booking này.");
            }
            
            // Lấy giá tiền 
            long amount = (bookingResponse.getFinalPrice() != null ? bookingResponse.getFinalPrice().longValue() : bookingResponse.getTotalPrice().longValue());
            if (amount <= 0) {
                return ResponseEntity.badRequest().body("Số tiền thanh toán không hợp lệ.");
            }
            
            String orderInfo = "Thanh toan don hang #" + bookingId;
            String ipAddress = getClientIp(request);
            String returnUrl = "http://localhost:3000/payment/return";

            // Gọi VNPayService
            PaymentResponse paymentResponse = vnPayService.createPayment(
                orderInfo, 
                amount,
                ipAddress, 
                returnUrl, 
                bookingId
            );
            
            // Tìm booking gốc để cập nhật paymentStatus
            Booking booking = bookingRepository.findById(bookingId).orElse(null);
            if (booking != null) {
                booking.setPaymentStatus("PENDING");
                bookingRepository.save(booking); 
            } else {
                log.warn("Không tìm thấy booking gốc để cập nhật paymentStatus sau khi tạo payment cho bookingId: {}", bookingId);
            }

            return ResponseEntity.ok(paymentResponse);
        } catch (Exception e) {
            log.error("Lỗi tạo URL thanh toán cho booking {}: {}", bookingId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Lỗi tạo URL thanh toán: " + e.getMessage());
        }
    }

    /**
     * Xử lý callback từ VNPay
     */
    @GetMapping("/payment/vnpay_return")
    public RedirectView vnpayReturn(@RequestParam Map<String, String> queryParams) {
        // URL của frontend để chuyển hướng đến
        String frontendReturnUrlBase = "http://localhost:3000/payment-result";
        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromHttpUrl(frontendReturnUrlBase);

        try {
            log.info("VNPay return data: {}", queryParams);
            
            String transactionNo = queryParams.get("vnp_TransactionNo");
            String responseCode = queryParams.get("vnp_ResponseCode");
            String orderInfo = queryParams.get("vnp_OrderInfo"); 

            // 1. Kiểm tra chữ ký
            if (!vnPayService.verifyReturnSignature(queryParams)) { 
                log.warn("Chữ ký VNPay return không hợp lệ cho transactionNo: {}", transactionNo);
                urlBuilder.queryParam("success", "false");
                urlBuilder.queryParam("message", "invalid_signature");
                return new RedirectView(urlBuilder.toUriString());
            }
            
            log.info("Chữ ký VNPay return hợp lệ cho transactionNo: {}", transactionNo);

            // 2. Lấy bookingId
            Integer bookingId = null;
            if (orderInfo != null && orderInfo.contains("#")) {
                try {
                    bookingId = Integer.parseInt(orderInfo.substring(orderInfo.lastIndexOf("#") + 1));
                    urlBuilder.queryParam("bookingId", bookingId);
                } catch (NumberFormatException e) {
                    log.error("Không thể parse bookingId từ vnp_OrderInfo: {}", orderInfo);
                    urlBuilder.queryParam("success", "false");
                    urlBuilder.queryParam("message", "invalid_order_info");
                    return new RedirectView(urlBuilder.toUriString());
                }
            } else {
                log.error("Thiếu thông tin bookingId từ vnp_OrderInfo: {}", orderInfo);
                urlBuilder.queryParam("success", "false");
                urlBuilder.queryParam("message", "missing_order_info");
                return new RedirectView(urlBuilder.toUriString());
            }
            
            // 3. Cập nhật trạng thái DB
            bookingService.updatePaymentAndBookingStatusAfterVNPay(bookingId, transactionNo, responseCode);

            // 4. Xây dựng URL chuyển hướng dựa trên kết quả
            if ("00".equals(responseCode)) {
                log.info("VNPay return thành công cho bookingId: {}", bookingId);
                urlBuilder.queryParam("success", "true");
                urlBuilder.queryParam("message", "payment_success");
            } else {
                log.warn("VNPay return thất bại cho bookingId: {} với mã lỗi: {}", bookingId, responseCode);
                urlBuilder.queryParam("success", "false");
                urlBuilder.queryParam("message", "payment_failed");
                urlBuilder.queryParam("code", responseCode);
            }
             
        } catch (Exception e) {
            log.error("Lỗi nghiêm trọng xử lý VNPay return: {}", e.getMessage(), e);
            urlBuilder.queryParam("success", "false");
            urlBuilder.queryParam("message", "internal_server_error");
        }
        
        // Thực hiện chuyển hướng
        return new RedirectView(urlBuilder.toUriString());
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
    
    /**
     * Helper method để lấy IP của client
     */
    private String getClientIp(HttpServletRequest request) {
        String remoteAddr = "";
        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || "".equals(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
            }
        }
        return remoteAddr;
    }
}
