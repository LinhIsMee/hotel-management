package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dto.response.BookingResponseDTO;
import com.spring3.hotel.management.dto.response.PaymentLinkResponse;
import com.spring3.hotel.management.dto.response.PaymentResponse;
import com.spring3.hotel.management.dto.response.RoomListResponseDTO;
import com.spring3.hotel.management.dto.request.UpsertBookingRequest;
import com.spring3.hotel.management.models.Booking;
import com.spring3.hotel.management.models.User;
import com.spring3.hotel.management.repositories.UserRepository;
import com.spring3.hotel.management.services.BookingService;
import com.spring3.hotel.management.services.VNPayService;
import com.spring3.hotel.management.repositories.BookingRepository;
import com.spring3.hotel.management.repositories.PaymentRepository;
import com.spring3.hotel.management.models.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user/bookings")
@Slf4j
public class BookingUserController {

    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    // Lấy thông tin booking theo ID (cho người dùng đã đăng nhập)
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingById(@PathVariable Integer id) {
        try {
             // Lấy thông tin user đang đăng nhập
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentPrincipalName = authentication.getName();
            User currentUser = userRepository.findByEmail(currentPrincipalName)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng"));

             // Sử dụng phương thức getBookingById từ service
             BookingResponseDTO bookingResponse = bookingService.getBookingById(id);
             
             // Kiểm tra booking có tồn tại và thuộc về user đang đăng nhập không
            if (bookingResponse != null && bookingResponse.getUserId().equals(currentUser.getId())) {
                return ResponseEntity.ok(bookingResponse);
            } else if (bookingResponse == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", "Không tìm thấy booking."));
            } else {
                 return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", "Bạn không có quyền truy cập booking này."));
            }
        } catch (Exception e) {
             log.error("Lỗi lấy booking {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "Lỗi máy chủ nội bộ."));
        }
    }

    // Lấy danh sách booking của người dùng hiện tại
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @GetMapping("/my-bookings")
    public ResponseEntity<?> getCurrentUserBookings() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // Kiểm tra xem người dùng đã xác thực chưa
            if (username.equals("anonymousUser") || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Vui lòng đăng nhập để xem danh sách đặt phòng"));
            }
            
            // Tìm người dùng theo email thay vì username
            User user = userRepository.findByEmail(username)
                .orElseGet(() -> userRepository.findByUsername(username));
                
            if (user == null) {
                log.error("Không tìm thấy thông tin người dùng với username/email: {}", username);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Không tìm thấy thông tin người dùng"));
            }
            
            List<BookingResponseDTO> bookings = bookingService.getBookingsByUserId(user.getId());
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách đặt phòng: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi máy chủ: " + e.getMessage()));
        }
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
        
        // Tìm người dùng theo username
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("Không tìm thấy người dùng với username: " + username);
        }
        
        request.setUserId(user.getId());
        
        // Đặt trạng thái mặc định là PENDING (chờ thanh toán)
        request.setStatus("PENDING");
        
        // Tạo booking
        BookingResponseDTO bookingResponseDTO = bookingService.createBooking(request);
        
        // Tạo liên kết thanh toán VNPay
        String orderInfo = "Thanh toan dat phong khach san - Ma dat phong: " + bookingResponseDTO.getId();
        Long amount = bookingResponseDTO.getFinalPrice().longValue();
        // Tạo returnUrl của ứng dụng
        String returnUrl = "http://localhost:9000/api/v1/payments/callback";
        // IP Address của khách hàng
        String ipAddress = "127.0.0.1";
        
        PaymentResponse paymentResponse = vnPayService.createPayment(orderInfo, amount, ipAddress, returnUrl);
        
        // Trả về thông tin booking và liên kết thanh toán
        Map<String, Object> response = new HashMap<>();
        response.put("booking", bookingResponseDTO);
        response.put("payment", paymentResponse);
        
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
        
        // Tìm người dùng theo username
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
            // Tạo returnUrl của ứng dụng
            String returnUrl = "http://localhost:9000/api/v1/payments/callback";
            // IP Address của khách hàng
            String ipAddress = "127.0.0.1";
            
            PaymentResponse paymentResponse = vnPayService.createPayment(orderInfo, amount, ipAddress, returnUrl);
            
            Map<String, Object> response = new HashMap<>();
            response.put("booking", bookingResponseDTO);
            response.put("payment", paymentResponse);
            
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
        
        // Tìm người dùng theo username
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
    
    // Kiểm tra trạng thái thanh toán của booking
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/check-status/{bookingId}")
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
    
    // API test thanh toán không cần xác thực
    @GetMapping("/test-payment")
    public ResponseEntity<?> testPayment() {
        try {
            UpsertBookingRequest request = new UpsertBookingRequest();
            request.setUserId(40); // Sử dụng ID của người dùng thực tế
            request.setRoomIds(List.of(62)); // Sử dụng ID phòng 62 - Phòng đơn tiêu chuẩn số 103
            request.setCheckInDate(LocalDate.now().plusDays(10)); // Đặt xa hơn để tránh xung đột
            request.setCheckOutDate(LocalDate.now().plusDays(12));
            request.setAdults(1);
            request.setChildren(0);
            request.setStatus("PENDING");
            request.setTotalPrice(1000000.0); // Giá 500,000 x 2 đêm = 1,000,000
            request.setFinalPrice(1000000.0); // Giá 500,000 x 2 đêm = 1,000,000
            request.setSpecialRequests("Booking test để thử nghiệm thanh toán");
            request.setFullName("Phạm Thùy Linh");
            request.setEmail("linh8686@gmail.com");
            request.setPhone("0773352286");

            BookingResponseDTO bookingResponseDTO = bookingService.createBooking(request);
            if (bookingResponseDTO == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Lỗi khi tạo booking test"));
            }

            String orderInfo = "Thanh toán đặt phòng " + bookingResponseDTO.getId();
            Long amount = bookingResponseDTO.getFinalPrice().longValue();
            // Tạo returnUrl của ứng dụng
            String returnUrl = "http://localhost:9000/api/v1/payments/callback";
            // IP Address của khách hàng
            String ipAddress = "127.0.0.1";
            
            PaymentResponse paymentResponse = vnPayService.createPayment(orderInfo, amount, ipAddress, returnUrl);

            return ResponseEntity.ok(Map.of(
                    "booking", bookingResponseDTO,
                    "payment", paymentResponse
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Lỗi khi tạo booking test: " + e.getMessage()));
        }
    }

    // Lấy chi tiết booking kèm thông tin thanh toán
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getBookingDetail(@PathVariable Integer id) {
        try {
            // Lấy thông tin booking DTO
            BookingResponseDTO booking = bookingService.getBookingById(id);
            
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
    
    // API lấy danh sách tất cả các đơn đặt phòng đã xác nhận, không cần xác thực
    @GetMapping("/list-confirmed")
    public ResponseEntity<?> getConfirmedBookings(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            // Nếu không có startDate, lấy ngày hiện tại
            if (startDate == null) {
                startDate = LocalDate.now();
            }
            
            // Nếu không có endDate, lấy 30 ngày tính từ startDate
            if (endDate == null) {
                endDate = startDate.plusDays(30);
            }
            
            // Lấy danh sách booking đã xác nhận
            List<BookingResponseDTO> bookings = bookingService.getConfirmedBookingsByDateRange(startDate, endDate);
            
            return ResponseEntity.ok(Map.of(
                "bookings", bookings,
                "startDate", startDate,
                "endDate", endDate,
                "count", bookings.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi lấy danh sách đơn đặt phòng: " + e.getMessage()));
        }
    }

    @PostMapping("/create-payment")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> createPaymentUrl(@RequestParam Integer bookingId, 
                                               @RequestParam(required = false) String bankCode, 
                                               HttpServletRequest request) { // Thêm HttpServletRequest để lấy IP
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
             // long amountToPay = amount * 100; // VNPay yêu cầu đơn vị xu - Amount trong service đã xử lý nhân 100
             String orderInfo = "Thanh toan don hang #" + bookingId; 
             String ipAddress = getClientIp(request); // Lấy IP client
             String returnUrl = "http://localhost:5173/payment/return"; // URL frontend để redirect sau thanh toán

            // Gọi VNPayService
            PaymentResponse paymentResponse = vnPayService.createPayment(
                orderInfo, 
                amount, // Truyền số tiền gốc
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

            return ResponseEntity.ok(paymentResponse); // Trả về PaymentResponse từ VNPayService
        } catch (Exception e) {
            log.error("Lỗi tạo URL thanh toán cho booking {}: {}", bookingId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Lỗi tạo URL thanh toán: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> getBookingHistory() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentPrincipalName = authentication.getName();
            User currentUser = userRepository.findByEmail(currentPrincipalName)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng"));
            
            // Sử dụng getBookingsByUserId
            List<BookingResponseDTO> bookingHistory = bookingService.getBookingsByUserId(currentUser.getId());
            
            return ResponseEntity.ok(bookingHistory);
        } catch (Exception e) {
             log.error("Lỗi lấy lịch sử booking: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "Lỗi máy chủ nội bộ."));
        }
    }

    @GetMapping("/payment/vnpay_return")
    public RedirectView vnpayReturn(@RequestParam Map<String, String> queryParams) {
         // URL của frontend để chuyển hướng đến
         String frontendReturnUrlBase = "http://localhost:5173/payment-result"; // Thay bằng URL thực tế của bạn
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
    
    // Helper method to get client IP address
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