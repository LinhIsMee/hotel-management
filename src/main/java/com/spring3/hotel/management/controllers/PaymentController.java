package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dtos.response.PaymentHistoryResponse;
import com.spring3.hotel.management.dtos.response.PaymentResponse;
import com.spring3.hotel.management.models.Payment;
import com.spring3.hotel.management.repositories.BookingRepository;
import com.spring3.hotel.management.repositories.PaymentRepository;
import com.spring3.hotel.management.services.VNPayService;
import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @PostMapping("/create/{bookingId}")
    public ResponseEntity<Map<String, String>> createPayment(@PathVariable Integer bookingId, @RequestParam Long amount, @RequestParam String orderInfo) {
        String paymentUrl = vnPayService.createPayment(bookingId, amount, orderInfo);
        Payment payment = paymentRepository.findFirstByBookingIdOrderByIdDesc(bookingId);

        Map<String, String> response = new HashMap<>();
        response.put("paymentUrl", paymentUrl);
        response.put("transactionNo", payment.getTransactionNo());

        return ResponseEntity.ok(response);
    }

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

    @GetMapping("/check-status/{transactionNo}")
    public ResponseEntity<Map<String, Object>> checkPaymentStatus(@PathVariable String transactionNo) {
        PaymentResponse response = vnPayService.findPaymentByTransactionNo(transactionNo);

        Map<String, Object> result = new HashMap<>();
        result.put("success", response.isSuccess());
        result.put("message", response.getMessage());
        result.put("transactionNo", response.getTransactionNo());
        result.put("amount", response.getAmount());

        return ResponseEntity.ok(result);
    }

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
