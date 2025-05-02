package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dto.response.PaymentLinkResponse;
import com.spring3.hotel.management.models.Booking;
import com.spring3.hotel.management.models.Payment;
import com.spring3.hotel.management.repositories.BookingRepository;
import com.spring3.hotel.management.repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payment-mock")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentMockController {

    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private BookingRepository bookingRepository;

    // API tạo URL thanh toán mẫu - không yêu cầu booking
    @GetMapping("/create")
    public ResponseEntity<?> createMockPayment() {
        String transactionNo = String.valueOf(System.currentTimeMillis() % 100000000);
        String mockedPaymentUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=100000000&vnp_Command=pay&vnp_CreateDate=20250405175217&vnp_CurrCode=VND&vnp_ExpireDate=20250405180717&vnp_IpAddr=127.0.0.1&vnp_Locale=vn&vnp_OrderInfo=Thanh+toan+test&vnp_OrderType=other&vnp_ReturnUrl=http%3A%2F%2Flocalhost%3A9000%2Fapi%2Fv1%2Fpayment%2Fcallback-html&vnp_TmnCode=M7LG94H1&vnp_TxnRef=" + transactionNo + "&vnp_Version=2.1.0&vnp_SecureHash=6c27b679fd4d9d486c21ddfde020ba6a4404cf80f58f2bf40f16d0a9e18c11d50b0b8b3ef30af07d4e7e2c014819e76dca59843bd4524e6d8ce1f9376f620bee";
        
        // Tạo payment record mẫu
        Payment payment = new Payment();
        payment.setTransactionNo(transactionNo);
        payment.setAmount(1000000L);
        payment.setOrderInfo("Thanh toán test mock");
        payment.setStatus("01"); // Chưa thanh toán
        payment.setResponseCode("01");
        payment = paymentRepository.save(payment);
            
        Map<String, Object> response = new HashMap<>();
        response.put("payment", Map.of(
            "paymentUrl", mockedPaymentUrl,
            "orderInfo", "Thanh toán test mock",
            "transactionNo", transactionNo,
            "amount", 1000000
        ));
        response.put("message", "Link thanh toán test đã được tạo");
        
        return ResponseEntity.ok(response);
    }
    
    // API tạo URL thanh toán mẫu cho booking cụ thể
    @GetMapping("/create-for-booking/{bookingId}")
    public ResponseEntity<?> createMockPaymentForBooking(@PathVariable Integer bookingId) {
        // Tìm booking
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (!bookingOpt.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of(
                "message", "Không tìm thấy booking với ID: " + bookingId
            ));
        }
        
        Booking booking = bookingOpt.get();
        String transactionNo = String.valueOf(System.currentTimeMillis() % 100000000);
        String mockedPaymentUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=" + 
            booking.getTotalPrice().longValue() * 100 + 
            "&vnp_Command=pay&vnp_CreateDate=20250405175217&vnp_CurrCode=VND&vnp_ExpireDate=20250405180717&vnp_IpAddr=127.0.0.1&vnp_Locale=vn&vnp_OrderInfo=Thanh+toan+dat+phong+" + 
            bookingId + 
            "&vnp_OrderType=other&vnp_ReturnUrl=http%3A%2F%2Flocalhost%3A9000%2Fapi%2Fv1%2Fpayment%2Fcallback-html&vnp_TmnCode=M7LG94H1&vnp_TxnRef=" + 
            transactionNo + 
            "&vnp_Version=2.1.0&vnp_SecureHash=6c27b679fd4d9d486c21ddfde020ba6a4404cf80f58f2bf40f16d0a9e18c11d50b0b8b3ef30af07d4e7e2c014819e76dca59843bd4524e6d8ce1f9376f620bee";
        
        // Tạo payment record
        Payment payment = new Payment();
        payment.setTransactionNo(transactionNo);
        payment.setAmount(booking.getTotalPrice().longValue());
        payment.setOrderInfo("Thanh toán đặt phòng " + bookingId);
        payment.setStatus("01"); // Chưa thanh toán
        payment.setResponseCode("01");
        payment.setBooking(booking);
        payment = paymentRepository.save(payment);
            
        Map<String, Object> response = new HashMap<>();
        response.put("payment", new PaymentLinkResponse(
            mockedPaymentUrl, 
            "Thanh toán đặt phòng " + bookingId
        ));
        response.put("bookingId", bookingId);
        response.put("transactionNo", transactionNo);
        response.put("amount", booking.getTotalPrice());
        response.put("message", "Link thanh toán cho booking " + bookingId + " đã được tạo");
        
        return ResponseEntity.ok(response);
    }
    
    // API mô phỏng payment success callback
    @GetMapping("/success/{transactionNo}")
    public ResponseEntity<?> simulateSuccessPayment(@PathVariable String transactionNo) {
        Optional<Payment> paymentOpt = paymentRepository.findByTransactionNo(transactionNo);
        if (!paymentOpt.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of(
                "message", "Không tìm thấy payment với mã giao dịch: " + transactionNo
            ));
        }
        
        Payment payment = paymentOpt.get();
        
        // Cập nhật payment thành công
        payment.setStatus("00"); // 00: Thành công
        payment.setResponseCode("00");
        payment.setPayDate(LocalDateTime.now().toString());
        payment.setBankCode("NCB");
        payment = paymentRepository.save(payment);
        
        // Cập nhật booking nếu có
        Booking booking = payment.getBooking();
        if (booking != null) {
            booking.setStatus("CONFIRMED");
            bookingRepository.save(booking);
        }
        
        return ResponseEntity.ok(Map.of(
            "transactionNo", transactionNo,
            "status", "SUCCESS",
            "message", "Đã cập nhật thanh toán thành công cho mã giao dịch: " + transactionNo,
            "bookingId", booking != null ? booking.getId() : "N/A"
        ));
    }
    
    // API mô phỏng payment failure callback
    @GetMapping("/failure/{transactionNo}")
    public ResponseEntity<?> simulateFailurePayment(@PathVariable String transactionNo) {
        Optional<Payment> paymentOpt = paymentRepository.findByTransactionNo(transactionNo);
        if (!paymentOpt.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of(
                "message", "Không tìm thấy payment với mã giao dịch: " + transactionNo
            ));
        }
        
        Payment payment = paymentOpt.get();
        
        // Cập nhật payment thất bại
        payment.setStatus("24"); // 24: Khách hàng hủy giao dịch
        payment.setResponseCode("24");
        payment.setPayDate(LocalDateTime.now().toString());
        payment.setBankCode("NCB");
        payment = paymentRepository.save(payment);
        
        // Không cập nhật booking
        Booking booking = payment.getBooking();
        
        return ResponseEntity.ok(Map.of(
            "transactionNo", transactionNo,
            "status", "FAILED",
            "message", "Đã cập nhật thanh toán thất bại cho mã giao dịch: " + transactionNo,
            "bookingId", booking != null ? booking.getId() : "N/A"
        ));
    }

    // API kiểm tra và tạo giao dịch cho mã tra cứu cụ thể
    @GetMapping("/check-specific/{transactionNo}")
    public ResponseEntity<?> checkSpecificTransaction(@PathVariable String transactionNo) {
        Optional<Payment> paymentOpt = paymentRepository.findByTransactionNo(transactionNo);
        
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            return ResponseEntity.ok(Map.of(
                "transactionNo", payment.getTransactionNo(),
                "status", payment.getStatus(),
                "message", "Giao dịch đã tồn tại",
                "amount", payment.getAmount(),
                "orderInfo", payment.getOrderInfo(),
                "payDate", payment.getPayDate()
            ));
        } else {
            // Tạo payment record mẫu nếu là mã 310Y3tcMdb
            if ("310Y3tcMdb".equals(transactionNo)) {
                Payment payment = new Payment();
                payment.setTransactionNo(transactionNo);
                payment.setAmount(1000000L);
                payment.setOrderInfo("Giao dịch có mã tra cứu 310Y3tcMdb");
                payment.setStatus("99"); // Sai chữ ký
                payment.setResponseCode("99");
                payment.setPayDate("05/04/2025 6:04:39 CH");
                payment.setBankCode("NCB");
                payment = paymentRepository.save(payment);
                
                return ResponseEntity.ok(Map.of(
                    "transactionNo", payment.getTransactionNo(),
                    "status", payment.getStatus(),
                    "message", "Đã tạo giao dịch lỗi - Sai chữ ký",
                    "amount", payment.getAmount(),
                    "orderInfo", payment.getOrderInfo(),
                    "payDate", payment.getPayDate()
                ));
            }
            // Tạo payment record mẫu nếu là mã VRR8FbWacN - giao dịch quá hạn
            else if ("VRR8FbWacN".equals(transactionNo)) {
                Payment payment = new Payment();
                payment.setTransactionNo(transactionNo);
                payment.setAmount(1200000L);
                payment.setOrderInfo("Giao dịch có mã tra cứu VRR8FbWacN");
                payment.setStatus("09"); // Giao dịch quá hạn
                payment.setResponseCode("09");
                payment.setPayDate("05/04/2025 6:08:13 CH");
                payment.setBankCode("NCB");
                payment = paymentRepository.save(payment);
                
                return ResponseEntity.ok(Map.of(
                    "transactionNo", payment.getTransactionNo(),
                    "status", payment.getStatus(),
                    "message", "Đã tạo giao dịch lỗi - Giao dịch đã quá thời gian chờ thanh toán",
                    "amount", payment.getAmount(),
                    "orderInfo", payment.getOrderInfo(),
                    "payDate", payment.getPayDate()
                ));
            }
            
            return ResponseEntity.badRequest().body(Map.of(
                "message", "Không tìm thấy giao dịch với mã: " + transactionNo
            ));
        }
    }
} 