package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dto.response.BookingResponseDTO;
import com.spring3.hotel.management.dto.response.PaymentResponse;
import com.spring3.hotel.management.models.Booking;
import com.spring3.hotel.management.models.BookingDetail;
import com.spring3.hotel.management.models.Discount;
import com.spring3.hotel.management.models.Payment;
import com.spring3.hotel.management.models.Room;
import com.spring3.hotel.management.models.User;
import com.spring3.hotel.management.repositories.DiscountRepository;
import com.spring3.hotel.management.repositories.PaymentRepository;
import com.spring3.hotel.management.repositories.RoomRepository;
import com.spring3.hotel.management.repositories.UserRepository;
import com.spring3.hotel.management.repositories.BookingRepository;
import com.spring3.hotel.management.services.BookingService;
import com.spring3.hotel.management.services.VNPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

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
    private final DiscountRepository discountRepository;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final BookingService bookingService;

    /**
     * API tạo thanh toán đơn giản (không kèm đặt phòng)
     */
    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@RequestBody Map<String, Object> paymentRequest) {
        try {
            // Kiểm tra các tham số bắt buộc
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

            return ResponseEntity.ok(paymentResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Không thể tạo thanh toán: " + e.getMessage()
            ));
        }
    }
    
    /**
     * API tạo đặt phòng và thanh toán kết hợp - kiểm tra phòng và tạo đơn hàng cùng với thanh toán
     */
    @PostMapping("/create-booking-payment")
    public ResponseEntity<?> createBookingPayment(@RequestBody Map<String, Object> request) {
        try {
            String ipAddress = request.getOrDefault("ipAddress", "").toString();
            String returnUrl = request.getOrDefault("returnUrl", "").toString();
            
            // Kiểm tra các tham số đầu vào
            if (request.get("userId") == null || 
                request.get("checkInDate") == null || 
                request.get("checkOutDate") == null || 
                request.get("roomIds") == null) {
                
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Thiếu thông tin đặt phòng cần thiết"
                ));
            }
            
            // Lấy thông tin từ request
            Integer userId = Integer.parseInt(request.get("userId").toString());
            LocalDate checkInDate = LocalDate.parse(request.get("checkInDate").toString(), dateFormatter);
            LocalDate checkOutDate = LocalDate.parse(request.get("checkOutDate").toString(), dateFormatter);
            
            @SuppressWarnings("unchecked")
            List<Integer> roomIds = (List<Integer>) request.get("roomIds");
            
            String fullName = request.getOrDefault("fullName", "").toString();
            String nationalId = request.getOrDefault("nationalId", "").toString();
            String email = request.getOrDefault("email", "").toString();
            String phone = request.getOrDefault("phone", "").toString();
            String notes = request.getOrDefault("notes", "").toString();
            
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
            booking.setStatus("PENDING");
            
            // Tính tổng tiền
            long days = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
            if (days <= 0) days = 1;
            
            double totalPrice = 0;
            
            // Tạo booking details
            List<BookingDetail> bookingDetails = new ArrayList<>();
            for (Room room : rooms) {
                BookingDetail detail = new BookingDetail();
                detail.setBooking(booking);
                detail.setRoom(room);
                detail.setPricePerNight(room.getRoomType().getPricePerNight());
                
                double roomPrice = room.getRoomType().getPricePerNight() * days;
                detail.setPrice(roomPrice);
                
                totalPrice += roomPrice;
                bookingDetails.add(detail);
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
            booking.setFinalPrice(totalPrice);
            
            // Lưu booking và các chi tiết booking
            booking = bookingRepository.save(booking);
            
            // Tạo payment
            PaymentResponse paymentResponse = vnPayService.createPayment(
                "Thanh toán đặt phòng #" + booking.getId(),
                (long) (totalPrice * 100), // Đổi sang đơn vị xu
                ipAddress,
                returnUrl,
                booking.getId()
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "booking", booking,
                "payment", paymentResponse
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Lỗi khi tạo đặt phòng và thanh toán: " + e.getMessage()
            ));
        }
    }

    /**
     * Kiểm tra trạng thái thanh toán
     */
    @GetMapping("/check-status/{transactionNo}")
    public ResponseEntity<?> checkPaymentStatus(@PathVariable String transactionNo) {
        try {
            Map<String, Object> result = vnPayService.checkPaymentStatus(transactionNo);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
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
            
            // Kiểm tra nếu không có ResponseCode, thì lấy từ API check status
            if (!queryParams.containsKey("vnp_ResponseCode")) {
                Map<String, Object> statusResult = vnPayService.checkPaymentStatus(transactionNo);
                
                // Tạo URL với tất cả các thông tin có sẵn
                StringBuilder redirectUrl = new StringBuilder(frontendCallbackUrl + "?vnp_TxnRef=" + transactionNo);
                
                // Thêm tất cả thông tin vào URL
                for (Map.Entry<String, Object> entry : statusResult.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (value != null) {
                        String strValue = value.toString();
                        if (value instanceof String) {
                            strValue = URLEncoder.encode(strValue, StandardCharsets.UTF_8);
                        }
                        redirectUrl.append("&").append(key).append("=").append(strValue);
                    }
                }
                
                HttpHeaders headers = new HttpHeaders();
                headers.setLocation(URI.create(redirectUrl.toString()));
                return new ResponseEntity<>(headers, HttpStatus.FOUND);
            }
            
            // Xử lý callback từ VNPay
            Map<String, Object> result = vnPayService.processPaymentCallback(queryParams);
            
            // Tạo URL với tất cả các thông tin từ kết quả
            StringBuilder redirectUrl = new StringBuilder(frontendCallbackUrl + "?vnp_TxnRef=" + transactionNo);
            
            // Thêm tất cả thông tin vào URL
            for (Map.Entry<String, Object> entry : result.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value != null) {
                    String strValue = value.toString();
                    // Bỏ qua các trường vnp_ nếu chúng đã có trong tên
                    if (!key.startsWith("vnp_") || !result.containsKey(key.substring(4))) {
                        if (value instanceof String) {
                            strValue = URLEncoder.encode(strValue, StandardCharsets.UTF_8);
                        }
                        redirectUrl.append("&").append(key).append("=").append(strValue);
                    }
                }
            }
            
            // Đảm bảo có các trường cần thiết
            if (!result.containsKey("success") && result.containsKey("vnp_ResponseCode")) {
                redirectUrl.append("&success=").append("00".equals(result.get("vnp_ResponseCode")));
            }
            
            if (!result.containsKey("pending") && result.containsKey("vnp_ResponseCode")) {
                String responseCode = (String) result.get("vnp_ResponseCode");
                boolean pending = "01".equals(responseCode) || "04".equals(responseCode) || 
                        "05".equals(responseCode) || "06".equals(responseCode);
                redirectUrl.append("&pending=").append(pending);
            }
            
            if (!result.containsKey("message") && result.containsKey("vnp_ResponseCode")) {
                String message = result.get("vnp_ResponseCode").equals("00") ? 
                    "Giao dịch thanh toán thành công" : "Giao dịch chưa hoàn tất";
                redirectUrl.append("&message=").append(URLEncoder.encode(message, StandardCharsets.UTF_8));
            }
            
            // Redirect về frontend
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(redirectUrl.toString()));
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
            String oldStatus = payment.getStatus();
            payment.setStatus(status);
            payment.setResponseCode(status);
            
            payment = paymentRepository.save(payment);
            
            // Cập nhật booking nếu trạng thái thanh toán thay đổi thành công
            if ("00".equals(status)) {
                Booking booking = payment.getBooking();
                if (booking != null && !"CONFIRMED".equals(booking.getStatus())) {
                    booking.setStatus("CONFIRMED");
                    bookingRepository.save(booking);
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã cập nhật trạng thái thanh toán từ [" + oldStatus + "] thành [" + status + "]",
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
     * Đồng bộ trạng thái thanh toán với cổng VNPAY
     */
    @GetMapping("/sync-status/{transactionNo}")
    public ResponseEntity<?> syncPaymentStatus(@PathVariable String transactionNo) {
        try {
            log.info("API sync-status được gọi với transactionNo: {}", transactionNo);
            
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
            
            // Cập nhật trạng thái thanh toán
            payment.setStatus(vnpStatus);
            payment.setResponseCode(vnpStatus);
            payment = paymentRepository.save(payment);
            
            // Cập nhật booking nếu có và thanh toán thành công
            if ("00".equals(vnpStatus) && payment.getBookingId() != null) {
                Booking booking = bookingRepository.findById(payment.getBookingId())
                    .orElse(null);
                
                if (booking != null && !"CONFIRMED".equals(booking.getStatus())) {
                booking.setStatus("CONFIRMED");
                bookingRepository.save(booking);
                    log.info("Đã cập nhật booking ID {} thành CONFIRMED", booking.getId());
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Đã đồng bộ trạng thái thanh toán và booking");
            result.put("transactionNo", transactionNo);
            result.put("payment_status", payment.getStatus());
            
            if (payment.getBookingId() != null) {
                result.put("bookingId", payment.getBookingId());
                result.put("booking_status", "00".equals(vnpStatus) ? "CONFIRMED" : "PENDING");
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Lỗi trong API sync-status: {}", e.getMessage(), e);
            
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "Không thể đồng bộ trạng thái: " + e.getMessage());
            errorResult.put("transactionNo", transactionNo);
            
            return ResponseEntity.badRequest().body(errorResult);
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
                    bookingInfo.put("status", booking.getStatus());
                    bookingInfo.put("totalPrice", booking.getTotalPrice());
                    bookingInfo.put("createdAt", booking.getCreatedAt());
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
            booking.setStatus("CONFIRMED");
            bookingRepository.save(booking);
            
            // Tìm và cập nhật payment nếu có
            List<Payment> payments = paymentRepository.findByBooking_Id(bookingId);
            if (!payments.isEmpty()) {
                // Lấy payment mới nhất để cập nhật
                Payment payment = payments.get(payments.size() - 1); 
                payment.setStatus("00"); // Sử dụng mã "00" cho trạng thái thành công
                payment.setResponseCode("00");
                // Cập nhật thêm các thông tin khác nếu cần
                if (payment.getPayDate() == null) {
                    payment.setPayDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
                }
                paymentRepository.save(payment);
            } else {
                // Nếu không có payment, tạo mới một payment
                Payment payment = new Payment();
                payment.setBooking(booking);
                payment.setAmount(booking.getFinalPrice() != null ? booking.getFinalPrice().longValue() : booking.getTotalPrice().longValue());
                payment.setStatus("00"); // Sử dụng mã "00"
                payment.setResponseCode("00");
                payment.setMethod("CASH"); // Giả sử là thanh toán tiền mặt
                payment.setPayDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))); // Format ngày giờ
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
