package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dtos.request.UpsertBookingRequest;
import com.spring3.hotel.management.dtos.response.BookingResponseDTO;
import com.spring3.hotel.management.dtos.response.PaymentResponse;
import com.spring3.hotel.management.dtos.response.RoomListResponseDTO;
import com.spring3.hotel.management.models.Booking;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
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
            if (paymentRequest.get("userId") == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Thiếu thông tin userId"
                ));
            }
            if (paymentRequest.get("orderInfo") == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Thiếu thông tin orderInfo"
                ));
            }
            if (paymentRequest.get("amount") == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Thiếu thông tin amount"
                ));
            }
            if (paymentRequest.get("ipAddress") == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Thiếu thông tin ipAddress"
                ));
            }
            if (paymentRequest.get("returnUrl") == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Thiếu thông tin returnUrl"
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
            // Kiểm tra các tham số bắt buộc
            if (!request.containsKey("userId")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Thiếu thông tin userId"));
            }
            if (!request.containsKey("roomIds")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Thiếu thông tin roomIds"));
            }
            if (!request.containsKey("checkInDate")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Thiếu thông tin checkInDate"));
            }
            if (!request.containsKey("checkOutDate")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Thiếu thông tin checkOutDate"));
            }
            if (!request.containsKey("ipAddress")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Thiếu thông tin ipAddress"));
            }
            if (!request.containsKey("returnUrl")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Thiếu thông tin returnUrl"));
            }

            // Lấy thông tin user
            Integer userId = (Integer) request.get("userId");
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user với id: " + userId));

            // Lấy danh sách phòng
            @SuppressWarnings("unchecked")
            List<Integer> roomIds = (List<Integer>) request.get("roomIds");
            
            // Kiểm tra phòng có sẵn không
            LocalDate checkInDate = LocalDate.parse((String) request.get("checkInDate"));
            LocalDate checkOutDate = LocalDate.parse((String) request.get("checkOutDate"));
            
            List<BookingResponseDTO> existingBookings = bookingService.getBookingsByDateRange(checkInDate, checkOutDate);
            for (BookingResponseDTO booking : existingBookings) {
                for (RoomListResponseDTO room : booking.getRooms()) {
                    if (roomIds.contains(room.getRoomId())) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(Map.of(
                                    "message", "Phòng đã được đặt trong khoảng thời gian này",
                                    "conflictRoomId", room.getRoomId(),
                                    "conflictBookingId", booking.getId()
                                ));
                    }
                }
            }

            // Tính tổng số đêm
            long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
            
            // Tính tổng giá phòng
            double totalPrice = 0;
            for (Integer roomId : roomIds) {
                Room room = roomRepository.findById(roomId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng với id: " + roomId));
                totalPrice += room.getRoomType().getPricePerNight() * nights;
            }

            // Kiểm tra và áp dụng mã giảm giá nếu có
            String discountCode = (String) request.get("discountCode");
            double finalPrice = totalPrice;
            
            if (discountCode != null && !discountCode.isEmpty()) {
                try {
                    Discount discount = discountRepository.findByCode(discountCode)
                            .orElseThrow(() -> new RuntimeException("Mã giảm giá không hợp lệ"));
                    
                    // Kiểm tra thời hạn sử dụng
                    if (discount.getValidFrom().isAfter(LocalDate.now()) || 
                        discount.getValidTo().isBefore(LocalDate.now())) {
                        return ResponseEntity.badRequest().body(Map.of("message", "Mã giảm giá đã hết hạn"));
                    }
                    
                    // Kiểm tra số lần sử dụng
                    if (discount.getUsedCount() >= discount.getMaxUses()) {
                        return ResponseEntity.badRequest().body(Map.of("message", "Mã giảm giá đã hết số lần sử dụng"));
                    }
                    
                    // Áp dụng giảm giá
                    if ("PERCENT".equals(discount.getDiscountType())) {
                        finalPrice = totalPrice * (1 - discount.getDiscountValue());
                    } else if ("FIXED".equals(discount.getDiscountType())) {
                        finalPrice = Math.max(0, totalPrice - discount.getDiscountValue());
                    }
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Mã giảm giá không hợp lệ"));
                }
            }

            // Tạo booking request
            UpsertBookingRequest bookingRequest = new UpsertBookingRequest();
            bookingRequest.setUserId(userId);
            bookingRequest.setFullName(user.getFullName());
            bookingRequest.setNationalId(user.getNationalId());
            bookingRequest.setEmail(user.getEmail());
            bookingRequest.setPhone(user.getPhoneNumber());
            bookingRequest.setRoomIds(roomIds);
            bookingRequest.setCheckInDate(checkInDate);
            bookingRequest.setCheckOutDate(checkOutDate);
            bookingRequest.setTotalPrice(totalPrice);
            bookingRequest.setFinalPrice(finalPrice);
            if (discountCode != null && !discountCode.isEmpty()) {
                Discount discount = discountRepository.findByCode(discountCode)
                        .orElseThrow(() -> new RuntimeException("Mã giảm giá không hợp lệ"));
                bookingRequest.setDiscountId(discount.getId());
            }

            // Tạo booking
            BookingResponseDTO booking = bookingService.createBooking(bookingRequest);

            // Tạo payment
            String orderInfo = "Thanh toán đặt phòng #" + booking.getId();
            String ipAddress = (String) request.get("ipAddress");
            String returnUrl = (String) request.get("returnUrl");
            
            PaymentResponse paymentResponse = vnPayService.createPayment(
                orderInfo,
                Math.round(finalPrice),
                ipAddress,
                returnUrl,
                booking.getId()
            );

            return ResponseEntity.ok(paymentResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/check-status/{transactionNo}")
    public ResponseEntity<?> checkPaymentStatus(@PathVariable String transactionNo) {
        try {
            System.out.println("API check-status được gọi với transactionNo: " + transactionNo);
            
            Map<String, Object> result = vnPayService.checkPaymentStatus(transactionNo);
            
            // Trả về kết quả với HTTP 200 ngay cả khi không tìm thấy payment
        return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.out.println("Lỗi trong API check-status: " + e.getMessage());
            e.printStackTrace();
            
            // Tạo kết quả mặc định trong trường hợp lỗi
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "Không thể kiểm tra trạng thái thanh toán: " + e.getMessage());
            errorResult.put("transactionNo", transactionNo);
            errorResult.put("pending", true);
            
            // Vẫn trả về HTTP 200 với thông tin lỗi
            return ResponseEntity.ok(errorResult);
        }
    }

    @GetMapping("/callback")
    public ResponseEntity<?> paymentCallback(@RequestParam Map<String, String> queryParams) {
        try {
            System.out.println("Callback URL được gọi với tham số: " + queryParams);
            
            // Ghi log tất cả các tham số để debug
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
            
            Map<String, Object> result = vnPayService.processPaymentCallback(queryParams);
            
            System.out.println("Kết quả xử lý callback: " + result);
            
            // Không cần cập nhật booking ở đây, đã được xử lý trong VNPayService
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.out.println("Lỗi khi xử lý callback: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi xử lý callback: " + e.getMessage());
            
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    /**
     * API lấy lịch sử thanh toán của đặt phòng
     */
    @GetMapping("/history/{bookingId}")
    public ResponseEntity<?> getPaymentHistory(@PathVariable Integer bookingId) {
        try {
            Optional<Payment> paymentOptional = paymentRepository.findByBookingId(bookingId);
            if (paymentOptional.isPresent()) {
                return ResponseEntity.ok(paymentOptional.get());
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Không tìm thấy thanh toán cho đặt phòng này"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Không thể lấy lịch sử thanh toán: " + e.getMessage()
            ));
        }
    }
    
    /**
     * API để cập nhật trạng thái thanh toán khi người dùng hủy thanh toán
     * Chức năng này chỉ dùng cho mục đích phát triển và kiểm thử
     */
    @PostMapping("/update-status/{transactionNo}")
    public ResponseEntity<?> updatePaymentStatus(
        @PathVariable String transactionNo,
        @RequestParam String status
    ) {
        try {
            Payment payment = paymentRepository.findByTransactionNo(transactionNo)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch"));
            
            payment.setStatus(status);
            paymentRepository.save(payment);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã cập nhật trạng thái thanh toán");
            response.put("transactionNo", transactionNo);
            response.put("newStatus", status);
            
            // Thêm bookingId vào phản hồi nếu có
            if (payment.getBookingId() != null) {
                response.put("bookingId", payment.getBookingId());
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Không thể cập nhật trạng thái thanh toán: " + e.getMessage()
            ));
        }
    }

    /**
     * Endpoint xử lý callback từ VNPay và redirect về frontend
     */
    @GetMapping("/vnpay-callback")
    public ResponseEntity<?> vnpayCallback(@RequestParam Map<String, String> queryParams) {
        try {
            System.out.println("Nhận được callback VNPay: " + queryParams);
            
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
     * Endpoint HTML để hiển thị kết quả thanh toán cho trường hợp không có frontend
     */
    @GetMapping("/callback-html")
    public String callbackHtml(@RequestParam Map<String, String> queryParams, Model model) {
        try {
            // Xử lý callback từ VNPay
            Map<String, Object> result = vnPayService.processPaymentCallback(queryParams);
            
            // Thêm kết quả vào model để hiển thị trong HTML
            model.addAttribute("success", result.get("success"));
            model.addAttribute("pending", result.get("pending"));
            model.addAttribute("message", result.get("message"));
            model.addAttribute("amount", result.get("amount"));
            model.addAttribute("transactionNo", result.get("transactionNo"));
            if (result.containsKey("bookingId")) {
                model.addAttribute("bookingId", result.get("bookingId"));
            }
            
            return "payment-result";
        } catch (Exception e) {
            // Trong trường hợp lỗi, hiển thị thông báo lỗi
            model.addAttribute("success", false);
            model.addAttribute("message", "Lỗi xử lý thanh toán: " + e.getMessage());
            return "payment-result";
        }
    }
} 