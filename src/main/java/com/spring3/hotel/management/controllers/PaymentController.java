package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dtos.response.PaymentHistoryResponse;
import com.spring3.hotel.management.dtos.response.PaymentLinkResponse;
import com.spring3.hotel.management.dtos.response.PaymentResponse;
import com.spring3.hotel.management.models.Booking;
import com.spring3.hotel.management.models.Payment;
import com.spring3.hotel.management.repositories.BookingRepository;
import com.spring3.hotel.management.repositories.PaymentRepository;
import com.spring3.hotel.management.services.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/payments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentController {

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getPaymentInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("status", "active");
        info.put("paymentMethod", "VNPay");
        info.put("version", "1.0.0");
        return ResponseEntity.ok(info);
    }

    /**
     * Tạo yêu cầu thanh toán cho booking
     */
    @PostMapping("/create/{bookingId}")
    public ResponseEntity<?> createPayment(@PathVariable Integer bookingId, 
                                           @RequestParam(required = false) String orderInfo) {
        try {
            // Kiểm tra booking tồn tại
            Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
            if (bookingOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "Không tìm thấy booking với ID: " + bookingId
                ));
            }
            
            Booking booking = bookingOpt.get();
            String orderInfoText = orderInfo != null ? orderInfo : "Thanh toán đặt phòng " + bookingId;
            String paymentUrl = vnPayService.createPayment(bookingId, booking.getTotalPrice().longValue(), orderInfoText);
            
            // Tìm payment record đã tạo
            Payment payment = paymentRepository.findFirstByBookingIdOrderByIdDesc(bookingId);
            if (payment == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "Có lỗi khi tạo payment cho booking " + bookingId
                ));
            }

            return ResponseEntity.ok(Map.of(
                "payment", new PaymentLinkResponse(paymentUrl, orderInfoText),
                "bookingId", bookingId,
                "transactionNo", payment.getTransactionNo(),
                "amount", booking.getTotalPrice(),
                "message", "Link thanh toán đã được tạo thành công"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "message", "Lỗi khi tạo thanh toán: " + e.getMessage()
            ));
        }
    }

    /**
     * Callback từ VNPay - HTML kết quả thanh toán
     */
    @GetMapping("/callback")
    public String paymentCallback(@RequestParam Map<String, String> queryParams) {
        System.out.println("Query params: " + queryParams);
        String statusClass = "";
        String message = "";
        String transactionNo = "";
        String packageName = "";
        String totalAmount = "";
        String paymentDate = "";

        PaymentResponse response = vnPayService.processPaymentResponse(queryParams);
        statusClass = response.isSuccess() ? "success" : "error";
        message = response.getMessage() != null ? response.getMessage() : "Unknown status";
        transactionNo = response.getTransactionNo() != null ? response.getTransactionNo() : "N/A";
        packageName = "Booking Payment"; // Thay thế bằng thông tin phù hợp nếu có

        // Định dạng số tiền
        totalAmount = queryParams.getOrDefault("vnp_Amount", "N/A");
        if (!totalAmount.equals("N/A")) {
            totalAmount = formatAmount(totalAmount);
        }

        // Định dạng ngày thanh toán
        paymentDate = queryParams.getOrDefault("vnp_PayDate", "N/A");
        if (!paymentDate.equals("N/A")) {
            paymentDate = formatPaymentDate(paymentDate);
        }

        return generatePaymentHtml(statusClass, message, transactionNo, packageName, totalAmount, paymentDate);
    }

    /**
     * Callback từ VNPay - JSON kết quả thanh toán
     */
    @PostMapping("/api-callback")
    public ResponseEntity<?> paymentApiCallback(@RequestParam Map<String, String> queryParams) {
        PaymentResponse response = vnPayService.processPaymentResponse(queryParams);
        Map<String, Object> result = new HashMap<>();
        result.put("success", response.isSuccess());
        result.put("message", response.getMessage());
        result.put("transactionNo", response.getTransactionNo());
        result.put("amount", response.getAmount());
        
        return ResponseEntity.ok(result);
    }

    /**
     * Kiểm tra trạng thái thanh toán
     */
    @GetMapping("/check-status/{transactionNo}")
    public ResponseEntity<Map<String, Object>> checkPaymentStatus(@PathVariable String transactionNo) {
        Optional<Payment> paymentOpt = paymentRepository.findByTransactionNo(transactionNo);
        Map<String, Object> result = new HashMap<>();

        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            result.put("amount", payment.getAmount());
            result.put("transactionNo", transactionNo);
            
            // Kiểm tra mã giao dịch có phải là lỗi sai chữ ký không
            if ("99".equals(payment.getStatus())) {
                result.put("success", false);
                result.put("message", "Sai chữ ký");
                return ResponseEntity.ok(result);
            }
            
            // Kiểm tra mã giao dịch có phải là lỗi quá hạn không
            if ("09".equals(payment.getStatus())) {
                result.put("success", false);
                result.put("message", "Giao dịch đã quá thời gian chờ thanh toán. Quý khách vui lòng thực hiện lại giao dịch");
                return ResponseEntity.ok(result);
            }
            
            // Trạng thái giao dịch thành công là "00"
            boolean isSuccess = "00".equals(payment.getStatus());
            result.put("success", isSuccess);
            result.put("message", isSuccess ? "Payment successful" : "Payment failed");
        } else {
            result.put("amount", 0);
            result.put("success", false);
            result.put("transactionNo", transactionNo);
            result.put("message", "Payment not found with transactionNo: " + transactionNo);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Lấy lịch sử thanh toán của booking
     */
    @GetMapping("/history/{bookingId}")
    public ResponseEntity<List<PaymentHistoryResponse>> getPaymentHistory(@PathVariable Integer bookingId) {
        List<PaymentHistoryResponse> history = vnPayService.getPaymentHistoryByBooking(bookingId);
        return ResponseEntity.ok(history);
    }

    private String formatAmount(String rawAmount) {
        try {
            long amount = Long.parseLong(rawAmount) / 100; // VNPay trả về số tiền nhân 100
            NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
            return formatter.format(amount) + " VND";
        } catch (Exception e) {
            return "Invalid amount";
        }
    }

    private String formatPaymentDate(String rawDate) {
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.parse(rawDate, inputFormatter);
            return dateTime.format(outputFormatter);
        } catch (Exception e) {
            return "Invalid date";
        }
    }

    private String generatePaymentHtml(String statusClass, String message, String transactionNo, String packageName, String totalAmount, String paymentDate) {
        StringBuilder html = new StringBuilder();
        html.append("""
    <!DOCTYPE html>
    <html>
    <head>
        <title>Payment Status</title>
        <style>
            body {
                font-family: Arial, sans-serif;
                background-color: #f4f4f9;
                margin: 0;
                padding: 0;
                display: flex;
                justify-content: center;
                align-items: center;
                height: 100vh;
            }
            .invoice-container {
                width: 100%;
                max-width: 400px;
                background: white;
                box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
                border-radius: 8px;
                padding: 20px;
                text-align: left;
            }
            .invoice-header {
                text-align: center;
                border-bottom: 1px solid #ddd;
                padding-bottom: 10px;
                margin-bottom: 20px;
            }
            .invoice-header h1 {
                font-size: 1.5em;
                margin: 0;
            }
            .invoice-header .status {
                font-size: 1.2em;
                margin-top: 5px;
            }
            .success {
                color: green;
            }
            .error {
                color: red;
            }
            .invoice-details {
                font-size: 1em;
                line-height: 1.5;
            }
            .invoice-details p {
                margin: 5px 0;
            }
            .footer {
                text-align: center;
                margin-top: 20px;
                font-size: 0.9em;
                color: #777;
            }
        </style>
    </head>
    <body>
        <div class="invoice-container">
""");
        html.append("<div class=\"invoice-header\">")
            .append("<h1>Payment Receipt</h1>")
            .append("<div class=\"status ").append(statusClass).append("\">").append(message).append("</div>")
            .append("</div>")
            .append("<div class=\"invoice-details\">")
            .append("<p><strong>Transaction ID:</strong> ").append(transactionNo).append("</p>")
            .append("<p><strong>Package:</strong> ").append(packageName).append("</p>")
            .append("<p><strong>Total Amount:</strong> ").append(totalAmount).append("</p>")
            .append("<p><strong>Payment Date:</strong> ").append(paymentDate).append("</p>")
            .append("</div>")
            .append("<div class=\"footer\">Thank you for your payment!</div>")
            .append("</div>")
            .append("</body></html>");
        return html.toString();
    }
}
