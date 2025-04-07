package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dtos.response.PaymentResponse;
import com.spring3.hotel.management.models.Payment;
import com.spring3.hotel.management.repositories.PaymentRepository;
import com.spring3.hotel.management.services.VNPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {

    private final VNPayService vnPayService;
    private final PaymentRepository paymentRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@RequestBody Map<String, Object> paymentRequest) {
        try {
            // Lấy thông tin từ request body
            String orderInfo = (String) paymentRequest.get("orderInfo");
            Long amount = Long.parseLong(paymentRequest.get("amount").toString());
            String ipAddress = (String) paymentRequest.get("ipAddress");
            String returnUrl = (String) paymentRequest.get("returnUrl");

            // Tạo payment
            PaymentResponse paymentResponse = vnPayService.createPayment(
                orderInfo,
                amount,
                ipAddress,
                returnUrl
            );

            return ResponseEntity.ok(paymentResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Không thể tạo thanh toán: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/check-status/{transactionNo}")
    public ResponseEntity<?> checkPaymentStatus(@PathVariable String transactionNo) {
        try {
            return ResponseEntity.ok(vnPayService.checkPaymentStatus(transactionNo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Không thể kiểm tra trạng thái thanh toán: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/callback")
    public ResponseEntity<?> paymentCallback(@RequestParam Map<String, String> queryParams) {
        try {
            return ResponseEntity.ok(vnPayService.processPaymentCallback(queryParams));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Không thể xử lý callback: " + e.getMessage()
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
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã cập nhật trạng thái thanh toán",
                "transactionNo", transactionNo,
                "newStatus", status
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Không thể cập nhật trạng thái thanh toán: " + e.getMessage()
            ));
        }
    }
} 