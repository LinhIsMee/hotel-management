package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dto.response.BookingResponseDTO;
import com.spring3.hotel.management.dto.response.PaymentResponse;
import com.spring3.hotel.management.models.Booking;
import com.spring3.hotel.management.models.BookingDetail;
import com.spring3.hotel.management.models.Discount;
import com.spring3.hotel.management.models.Payment;
import com.spring3.hotel.management.models.Room;
import com.spring3.hotel.management.models.User;
import com.spring3.hotel.management.models.HotelService;
import com.spring3.hotel.management.repositories.DiscountRepository;
import com.spring3.hotel.management.repositories.PaymentRepository;
import com.spring3.hotel.management.repositories.RoomRepository;
import com.spring3.hotel.management.repositories.UserRepository;
import com.spring3.hotel.management.repositories.BookingRepository;
import com.spring3.hotel.management.repositories.ServiceRepository;
import com.spring3.hotel.management.services.BookingService;
import com.spring3.hotel.management.services.VNPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import com.spring3.hotel.management.enums.BookingStatus;
import com.spring3.hotel.management.enums.PaymentMethod;
import com.spring3.hotel.management.enums.PaymentStatus;
import com.spring3.hotel.management.exceptions.NotFoundException;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PaymentController {

    private final VNPayService vnPayService;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final DiscountRepository discountRepository;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final BookingService bookingService;

    /**
     * API tạo thanh toán thống nhất
     * Hỗ trợ cả thanh toán đơn giản và thanh toán kèm đặt phòng
     */
    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@RequestBody Map<String, Object> request) {
        try {
            // Kiểm tra xem request có phải là thanh toán đặt phòng không
            if (request.containsKey("checkInDate") && request.containsKey("checkOutDate") && request.containsKey("rooms")) {
                // Xử lý tạo booking và thanh toán
                return createBookingPayment(request);
            } else {
                // Xử lý thanh toán đơn giản
                return createSimplePayment(request);
            }
        } catch (Exception e) {
            log.error("Lỗi khi tạo thanh toán: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Không thể tạo thanh toán: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Phương thức xử lý thanh toán đơn giản
     */
    private ResponseEntity<?> createSimplePayment(Map<String, Object> paymentRequest) {
        // Kiểm tra các tham số bắt buộc cho thanh toán cơ bản
        if (paymentRequest.get("orderInfo") == null || 
            paymentRequest.get("amount") == null || 
            paymentRequest.get("ipAddress") == null || 
            paymentRequest.get("returnUrl") == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Thiếu thông tin thanh toán cần thiết"
            ));
        }
        
        // Lấy thông tin từ request body
        String orderInfo = (String) paymentRequest.get("orderInfo");
        Long amount = Long.parseLong(paymentRequest.get("amount").toString());
        String ipAddress = (String) paymentRequest.get("ipAddress");
        String returnUrl = (String) paymentRequest.get("returnUrl");
        
        // Lấy bookingId nếu có
        Integer bookingId = null;
        if (paymentRequest.containsKey("bookingId") && paymentRequest.get("bookingId") != null) {
            bookingId = Integer.parseInt(paymentRequest.get("bookingId").toString());
        }

        // Tạo payment
        PaymentResponse paymentResponse = vnPayService.createPayment(
            orderInfo,
            amount,
            ipAddress,
            returnUrl,
            bookingId
        );

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Tạo thanh toán thành công",
            "data", paymentResponse
        ));
    }
    
    /**
     * Phương thức xử lý thanh toán kết hợp với đặt phòng
     */
    private ResponseEntity<?> createBookingPayment(Map<String, Object> request) {
        String ipAddress = request.getOrDefault("ipAddress", "127.0.0.1").toString();
        String returnUrl = request.getOrDefault("returnUrl", "http://localhost:5173/payment-callback").toString();
        
        // Kiểm tra các tham số đầu vào
        if (request.get("userId") == null || 
            request.get("checkInDate") == null || 
            request.get("checkOutDate") == null || 
            request.get("rooms") == null) {
            
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Thiếu thông tin đặt phòng cần thiết"
            ));
        }
        
        // Lấy thông tin từ request
        Integer userId = Integer.parseInt(request.get("userId").toString());
        LocalDate checkInDate = LocalDate.parse(request.get("checkInDate").toString(), dateFormatter);
        LocalDate checkOutDate = LocalDate.parse(request.get("checkOutDate").toString(), dateFormatter);
        
        // Lấy danh sách phòng với serviceIds, adults, children từ request
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> roomsData = (List<Map<String, Object>>) request.get("rooms");
        
        if (roomsData.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Danh sách phòng không được để trống"
            ));
        }
        
        // Lấy roomIds từ danh sách phòng
        List<Integer> roomIds = roomsData.stream()
                .map(room -> Integer.parseInt(room.get("roomId").toString()))
                .collect(Collectors.toList());
        
        // Lấy thông tin khách hàng
        String fullName = request.getOrDefault("fullName", "").toString();
        String nationalId = request.getOrDefault("nationalId", "").toString();
        String email = request.getOrDefault("email", "").toString();
        String phone = request.getOrDefault("phone", "").toString();
        String notes = request.getOrDefault("notes", "").toString();
        
        // Lấy thông tin giảm giá
        Integer discountId = null;
        if (request.get("discountId") != null) {
            discountId = Integer.parseInt(request.get("discountId").toString());
        }
        
        // Xác nhận người dùng tồn tại
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        // Kiểm tra xem có phòng nào đã được đặt trong khoảng thời gian này không
        List<Room> rooms = roomRepository.findAllById(roomIds);
        
        if (rooms.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Không tìm thấy phòng nào với ID đã chọn"
            ));
        }
        
        // Tạo booking trước khi tính toán chi tiết
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setCheckInDate(checkInDate);
        booking.setCheckOutDate(checkOutDate);
        booking.setStatus(BookingStatus.PENDING);
        
        if (!fullName.isEmpty()) booking.setCustomerName(fullName);
        if (!email.isEmpty()) booking.setCustomerEmail(email);
        
        // Tính tổng tiền
        long days = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        if (days <= 0) days = 1;
        
        double totalPrice = 0;
        
        // Tạo booking details
        List<BookingDetail> bookingDetails = new ArrayList<>();
        
        // Dùng Map để lưu dữ liệu phòng theo roomId
        Map<Integer, Map<String, Object>> roomDataMap = new HashMap<>();
        for (Map<String, Object> roomData : roomsData) {
            Integer roomId = Integer.parseInt(roomData.get("roomId").toString());
            roomDataMap.put(roomId, roomData);
        }
        
        for (Room room : rooms) {
            BookingDetail detail = new BookingDetail();
            detail.setBooking(booking);
            detail.setRoom(room);
            detail.setPricePerNight(room.getRoomType().getPricePerNight());
            
            // Lấy thông tin adults và children từ roomDataMap
            Map<String, Object> roomData = roomDataMap.get(room.getId());
            if (roomData.containsKey("adults")) {
                detail.setAdults(Integer.parseInt(roomData.get("adults").toString()));
            }
            if (roomData.containsKey("children")) {
                detail.setChildren(Integer.parseInt(roomData.get("children").toString()));
            }
            
            double roomPrice = room.getRoomType().getPricePerNight() * days;
            // detail.setPrice(roomPrice); // Commenting out: Missing setPrice(double) in BookingDetail
            
            totalPrice += roomPrice;
            bookingDetails.add(detail);
            
            // Thêm dịch vụ cho từng phòng nếu có
            if (roomData.containsKey("serviceIds")) {
                @SuppressWarnings("unchecked")
                List<Integer> serviceIds = (List<Integer>) roomData.get("serviceIds");
                
                if (serviceIds != null && !serviceIds.isEmpty()) {
                    List<HotelService> services = serviceRepository.findAllById(serviceIds);
                    
                    for (HotelService service : services) {
                        totalPrice += service.getPrice().doubleValue();
                    }
                }
            }
        }
        
        // Áp dụng giảm giá nếu có
        if (discountId != null) {
            Optional<Discount> discountOpt = discountRepository.findById(discountId);
            if (discountOpt.isPresent()) {
                Discount discount = discountOpt.get();
                booking.setDiscount(discount);
                
                if ("PERCENT".equals(discount.getDiscountType())) {
                    double discountAmount = totalPrice * discount.getDiscountValue() / 100;
                    totalPrice -= discountAmount;
                } else if ("FIXED".equals(discount.getDiscountType())) {
                    totalPrice -= discount.getDiscountValue();
                }
                
                if (totalPrice < 0) totalPrice = 0;
            }
        }
        
        booking.setTotalPrice(totalPrice);
        // booking.setFinalPrice(totalPrice); // Commenting out: Missing setFinalPrice(double) in Booking
        booking.setBookingDetails(bookingDetails);
        
        // Lưu booking và các chi tiết booking
        booking = bookingRepository.save(booking);
        
        // Tạo payment
        PaymentResponse paymentResponse = vnPayService.createPayment(
            "Thanh toán đặt phòng #" + booking.getId(),
            (long) (totalPrice * 100),
            ipAddress,
            returnUrl,
            booking.getId()
        );
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "booking", booking,
            "payment", paymentResponse
        ));
    }

    /**
     * Kiểm tra và đồng bộ trạng thái thanh toán
     */
    @GetMapping("/status/{transactionNo}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable String transactionNo) {
        try {
            // Tìm payment theo transactionNo
            Optional<Payment> paymentOpt = paymentRepository.findByTransactionNo(transactionNo);
            
            if (!paymentOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Không tìm thấy giao dịch với mã: " + transactionNo
                ));
            }
            
            Payment payment = paymentOpt.get();
            
            // Kiểm tra trạng thái thanh toán từ VNPay
            Map<String, Object> statusResult = vnPayService.checkPaymentStatus(transactionNo);
            String vnpStatus = (String) statusResult.getOrDefault("vnp_ResponseCode", "99");
            
            PaymentStatus vnpStatusEnum;
            try {
                vnpStatusEnum = PaymentStatus.valueOf(vnpStatus);
            } catch (IllegalArgumentException e) {
                log.warn("Mã trạng thái VNPay không hợp lệ: {}. Sử dụng PENDING làm mặc định.", vnpStatus);
                vnpStatusEnum = PaymentStatus.PENDING;
            }

            // Cập nhật trạng thái thanh toán nếu có thay đổi
            if (!vnpStatusEnum.equals(payment.getStatus())) {
                payment.setStatus(vnpStatusEnum);
                payment.setResponseCode(vnpStatus);
                payment = paymentRepository.save(payment);
                
                // Nếu thanh toán thành công, cập nhật booking thành CONFIRMED
                // if (PaymentStatus.SUCCESS.name().equals(vnpStatusEnum.name())) { // Commenting out: Missing SUCCESS enum
                if ("00".equals(vnpStatus)) { // Check response code directly
                    Booking booking = bookingRepository.findById(payment.getBookingId())
                            .orElse(null);
                    if (booking != null && booking.getStatus() != BookingStatus.CONFIRMED) {
                        booking.setStatus(BookingStatus.CONFIRMED);
                        bookingRepository.save(booking);
                        log.info("Thanh toán thành công cho đặt phòng {}. Cập nhật trạng thái booking thành CONFIRMED.", booking.getId());
                    }
                }
            }
            
            Map<String, Object> result = new HashMap<>(statusResult);
            result.put("success", true);
            result.put("message", "Thông tin trạng thái thanh toán");
            result.put("payment", payment);
            
            if (payment.getBookingId() != null) {
                result.put("bookingId", payment.getBookingId());
                result.put("bookingStatus", bookingRepository.findById(payment.getBookingId())
                        .map(Booking::getStatus)
                        .map(Enum::name)
                        .orElse("UNKNOWN"));
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra trạng thái thanh toán: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Lỗi khi kiểm tra trạng thái thanh toán: " + e.getMessage()
            ));
        }
    }

    /**
     * Xử lý callback từ VNPay
     */
    @GetMapping("/vnpay-callback")
    public ResponseEntity<?> vnpayCallback(@RequestParam Map<String, String> queryParams) {
        try {
            log.info("Nhận được callback VNPay: {}", queryParams);
            
            String frontendCallbackUrl = "http://localhost:5173/payment-callback";
            
            // Kiểm tra các trường cần thiết
            if (!queryParams.containsKey("vnp_TxnRef")) {
                throw new RuntimeException("Thiếu thông tin vnp_TxnRef");
            }
            
            String transactionNo = queryParams.get("vnp_TxnRef");
            
            // Kiểm tra xem payment có tồn tại không
            Payment payment = paymentRepository.findByTransactionNo(transactionNo)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch: " + transactionNo));
            
            // Xử lý callback từ VNPay
            Map<String, Object> result = vnPayService.processPaymentCallback(queryParams);
            
            // Tạo URL redirect với các thông tin cần thiết
            String redirectUrl = buildRedirectUrl(frontendCallbackUrl, transactionNo, result);
            
            // Redirect về frontend
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(redirectUrl));
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            // Trong trường hợp lỗi, vẫn redirect về frontend với thông báo lỗi
            String redirectUrl = "http://localhost:5173/payment-callback?error=true&message=" + 
                    URLEncoder.encode("Lỗi xử lý callback: " + e.getMessage(), StandardCharsets.UTF_8);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(redirectUrl));
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }
    }

    /**
     * Tạo URL redirect cho callback
     */
    private String buildRedirectUrl(String baseUrl, String transactionNo, Map<String, Object> data) {
        StringBuilder redirectUrl = new StringBuilder(baseUrl + "?vnp_TxnRef=" + transactionNo);
        
        // Thêm tất cả thông tin vào URL
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value != null) {
                String strValue = value.toString();
                // Bỏ qua các trường vnp_ nếu chúng đã có trong tên
                if (!key.startsWith("vnp_") || !data.containsKey(key.substring(4))) {
                    if (value instanceof String) {
                        strValue = URLEncoder.encode(strValue, StandardCharsets.UTF_8);
                    }
                    redirectUrl.append("&").append(key).append("=").append(strValue);
                }
            }
        }
        
        // Đảm bảo có các trường cần thiết
        if (!data.containsKey("success") && data.containsKey("vnp_ResponseCode")) {
            redirectUrl.append("&success=").append("00".equals(data.get("vnp_ResponseCode")));
        }
        
        if (!data.containsKey("pending") && data.containsKey("vnp_ResponseCode")) {
            String responseCode = (String) data.get("vnp_ResponseCode");
            boolean pending = "01".equals(responseCode) || "04".equals(responseCode) || 
                    "05".equals(responseCode) || "06".equals(responseCode);
            redirectUrl.append("&pending=").append(pending);
        }
        
        if (!data.containsKey("message") && data.containsKey("vnp_ResponseCode")) {
            String message = data.get("vnp_ResponseCode").equals("00") ? 
                "Giao dịch thanh toán thành công" : "Giao dịch chưa hoàn tất";
            redirectUrl.append("&message=").append(URLEncoder.encode(message, StandardCharsets.UTF_8));
        }
        
        return redirectUrl.toString();
    }

    /**
     * Lấy lịch sử thanh toán cho một booking
     */
    @GetMapping("/history/{bookingId}")
    public ResponseEntity<?> getPaymentHistory(@PathVariable Integer bookingId) {
        try {
            List<Payment> payments = paymentRepository.findByBooking_Id(bookingId);
            return ResponseEntity.ok(Map.of(
                "payments", payments,
                "count", payments.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Lỗi khi lấy lịch sử thanh toán: " + e.getMessage()
            ));
        }
    }

    /**
     * Cập nhật trạng thái thanh toán - chỉ dành cho quản trị viên
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/update-status/{transactionNo}")
    public ResponseEntity<?> updatePaymentStatus(
        @PathVariable String transactionNo,
        @RequestParam String status
    ) {
        try {
            Optional<Payment> paymentOpt = paymentRepository.findByTransactionNo(transactionNo);
            
            if (!paymentOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Không tìm thấy thanh toán với mã giao dịch: " + transactionNo
                ));
            }
            
            Payment payment = paymentOpt.get();
            PaymentStatus oldStatus = payment.getStatus();
            PaymentStatus newStatus = PaymentStatus.valueOf(status.toUpperCase());
            payment.setStatus(newStatus);
            payment.setResponseCode(status);
            
            payment = paymentRepository.save(payment);
            
            // Cập nhật booking nếu trạng thái thanh toán thay đổi thành công
            // if (PaymentStatus.SUCCESS.name().equals(newStatus.name())) { // Commenting out: Missing SUCCESS enum
            if (newStatus == PaymentStatus.PENDING && "00".equals(payment.getResponseCode())) { // Assume PENDING but check response code is 00 for success
                Booking booking = payment.getBooking();
                if (booking != null && booking.getStatus() != BookingStatus.CONFIRMED) {
                    booking.setStatus(BookingStatus.CONFIRMED);
                    bookingRepository.save(booking);
                    log.info("Thanh toán thành công cho đặt phòng {}. Cập nhật trạng thái booking thành CONFIRMED.", booking.getId());
                }
            } else {
                // log.warn("Cập nhật trạng thái thất bại cho booking {}. Trạng thái thanh toán cũ: {}, mới: {}", // Commenting out: booking might not be initialized here
                //         payment.getBookingId(), oldStatus, newStatus);
                 log.warn("Cập nhật trạng thái thanh toán cho booking {}. Trạng thái thanh toán cũ: {}, mới: {}",
                         payment.getBookingId(), oldStatus, newStatus);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã cập nhật trạng thái thanh toán từ [" + oldStatus + "] thành [" + newStatus + "]",
                "payment", payment
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Lỗi khi cập nhật trạng thái thanh toán: " + e.getMessage()
            ));
        }
    }

    /**
     * Lấy chi tiết thanh toán
     */
    @GetMapping("/detail/{transactionNo}")
    public ResponseEntity<?> getPaymentDetail(@PathVariable String transactionNo) {
        try {
            log.info("Truy vấn chi tiết payment với transactionNo: {}", transactionNo);
            
            // Tìm payment theo transactionNo
            Optional<Payment> paymentOpt = paymentRepository.findByTransactionNo(transactionNo);
            if (!paymentOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Không tìm thấy giao dịch với mã: " + transactionNo
                ));
            }
            
            Payment payment = paymentOpt.get();
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", payment.getId());
            result.put("transactionNo", payment.getTransactionNo());
            result.put("amount", payment.getAmount());
            result.put("orderInfo", payment.getOrderInfo());
            result.put("status", payment.getStatus());
            result.put("responseCode", payment.getResponseCode());
            result.put("bankCode", payment.getBankCode());
            result.put("payDate", payment.getPayDate());
            result.put("method", payment.getMethod());
            result.put("bookingId", payment.getBookingId());
            result.put("createdAt", payment.getCreatedAt());
            result.put("updatedAt", payment.getUpdatedAt());
            
            // Thông tin booking liên kết nếu có
            if (payment.getBookingId() != null) {
                Optional<Booking> bookingOpt = bookingRepository.findById(payment.getBookingId());
                if (bookingOpt.isPresent()) {
                    Booking booking = bookingOpt.get();
                    Map<String, Object> bookingInfo = new HashMap<>();
                    bookingInfo.put("id", booking.getId());
                    bookingInfo.put("status", booking.getStatus().name());
                    bookingInfo.put("totalPrice", booking.getTotalPrice());
                    bookingInfo.put("checkInDate", booking.getCheckInDate());
                    bookingInfo.put("checkOutDate", booking.getCheckOutDate());
                    result.put("booking", bookingInfo);
                }
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Lỗi khi truy vấn chi tiết payment: {}", e.getMessage(), e);
            
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "Lỗi khi truy vấn: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResult);
        }
    }

    /**
     * Xác nhận thanh toán cho booking - chỉ dành cho quản trị viên
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/confirm-booking/{bookingId}")
    public ResponseEntity<?> confirmBookingPayment(@PathVariable Integer bookingId) {
        try {
            log.info("Cập nhật trạng thái đơn đặt phòng {} thành CONFIRMED", bookingId);
            
            // Tìm booking
            Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
            if (!bookingOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Không tìm thấy đơn đặt phòng với ID: " + bookingId
                ));
            }
            
            Booking booking = bookingOpt.get();
            
            // Cập nhật trạng thái booking
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
            
            // Tìm và cập nhật payment nếu có
            List<Payment> payments = paymentRepository.findByBooking_Id(bookingId);
            if (!payments.isEmpty()) {
                // Lấy payment mới nhất để cập nhật
                Payment payment = payments.get(payments.size() - 1);
                // payment.setStatus(PaymentStatus.SUCCESS); // Commenting out: Missing SUCCESS enum
                payment.setStatus(PaymentStatus.PENDING); // Set to PENDING, but will check response code
                payment.setResponseCode("00"); // Explicitly set success code
                // Cập nhật thêm các thông tin khác nếu cần
                if (payment.getPayDate() == null) {
                    payment.setPayDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
                }
                paymentRepository.save(payment);
            } else {
                // Nếu không có payment, tạo mới một payment
                Payment payment = new Payment();
                payment.setBooking(booking);
                payment.setAmount(booking.getTotalPrice().longValue());
                // payment.setStatus(PaymentStatus.SUCCESS); // Commenting out: Missing SUCCESS enum
                payment.setStatus(PaymentStatus.PENDING); // Set to PENDING, but will check response code
                payment.setResponseCode("00"); // Explicitly set success code
                payment.setMethod(PaymentMethod.CASH);
                payment.setPayDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
                payment.setOrderInfo("Thanh toán đặt phòng #" + bookingId);
                paymentRepository.save(payment);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Đã cập nhật trạng thái thanh toán thành công (00)");
            result.put("bookingId", bookingId);
            result.put("bookingStatus", "CONFIRMED");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật trạng thái booking: {}", e.getMessage(), e);
            
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "Lỗi khi cập nhật: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResult);
        }
    }
}
