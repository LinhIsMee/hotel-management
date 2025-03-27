package com.spring3.hotel.management.services;

import com.spring3.hotel.management.dtos.response.PaymentHistoryResponse;
import com.spring3.hotel.management.dtos.response.PaymentResponse;
import com.spring3.hotel.management.models.Booking;
import com.spring3.hotel.management.models.Payment;
import com.spring3.hotel.management.repositories.BookingRepository;
import com.spring3.hotel.management.repositories.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Service
@ConfigurationProperties(prefix = "vnpay")
@Getter
@Setter
@AllArgsConstructor
@Slf4j
@NoArgsConstructor
public class VNPayService {

    private String tmnCode;
    private String hashSecret;
    private String payUrl;
    private String returnUrl;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public String createPayment(Integer bookingId, Long amount, String orderInfo) {
        // Validate booking
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Tạo thông tin PaymentParams cho VNPay
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", tmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));  // VNPay expects amount in "VND" cents
        vnp_Params.put("vnp_CurrCode", "VND");

        String orderType = "other";
        vnp_Params.put("vnp_TxnRef", getRandomNumber(8));  // Random transaction reference
        vnp_Params.put("vnp_OrderInfo", orderInfo);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", returnUrl);
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");

        // Get current time
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);  // Set expiration time (15 minutes later)
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Sort the params and create the query string
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder query = new StringBuilder();
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                    .append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        // Create Secure Hash using HMAC-SHA512
        String queryUrl = query.toString();
        String vnp_SecureHash = hmacSHA512(hashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        // Now create the Payment record with status "04" (processing)
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(amount);
        payment.setTransactionNo(vnp_Params.get("vnp_TxnRef"));
        payment.setOrderInfo(orderInfo);
        payment.setBankCode("");  // No bank code at creation
        payment.setPayDate("");  // No payment date yet
        payment.setStatus("04");  // Payment status: "04" (Processing)

        // Save payment record in the database with status "04"
        paymentRepository.save(payment);

        // Return the payment URL to redirect to VNPay
        return payUrl + "?" + queryUrl;
    }

    @Transactional
    public PaymentResponse processPaymentResponse(Map<String, String> queryParams) {
        PaymentResponse response = new PaymentResponse();
        log.info("Received payment callback with params: {}", queryParams);

        try {
            // 1. Lấy các thông tin từ queryParams
            String responseCode = queryParams.get("vnp_ResponseCode");
            String transactionNo = queryParams.get("vnp_TxnRef");
            String orderInfo = queryParams.get("vnp_OrderInfo");
            long amount = Long.parseLong(queryParams.get("vnp_Amount")) / 100;  // VNPay sends amount in cents

            // 2. Tìm payment record trong database
            Payment payment = paymentRepository.findByTransactionNo(transactionNo)
                .orElseThrow(() -> new RuntimeException("Payment not found with transactionNo: " + transactionNo));

            // 3. Cập nhật thông tin payment từ VNPay
            payment.setResponseCode(responseCode);
            payment.setPayDate(LocalDateTime.now().toString());
            payment.setBankCode(queryParams.get("vnp_BankCode"));
            payment.setStatus(responseCode.equals("00") ? "00" : "99");  // "00" là thành công, "99" là thất bại

            // 4. Lưu thông tin payment vào database
            paymentRepository.save(payment);

            // 5. Trả về thông tin phản hồi
            response.setSuccess(responseCode.equals("00"));
            response.setMessage(responseCode.equals("00") ? "Payment successful" : "Payment failed");
            response.setTransactionNo(transactionNo);
            response.setAmount(amount);

        } catch (Exception e) {
            log.error("Error processing payment callback: ", e);
            response.setSuccess(false);
            response.setMessage("Error processing payment: " + e.getMessage());
        }

        return response;
    }

    private String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac sha512_HMAC = Mac.getInstance("HmacSHA512");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            sha512_HMAC.init(secret_key);
            byte[] hash = sha512_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC", e);
        }
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public List<PaymentHistoryResponse> getPaymentHistoryByBooking(Integer bookingId) {
        List<Payment> payments = paymentRepository.findAllByBookingId(bookingId);
        if (payments.isEmpty()) {
            throw new RuntimeException("No payment history found for booking ID: " + bookingId);
        }
        return payments.stream()
            .map(this::convertToHistoryResponse)
            .toList();
    }

    private PaymentHistoryResponse convertToHistoryResponse(Payment payment) {
        PaymentHistoryResponse response = new PaymentHistoryResponse();
        response.setTransactionNo(payment.getTransactionNo());
        response.setAmount(payment.getAmount());
        response.setOrderInfo(payment.getOrderInfo());
        response.setPayDate(payment.getPayDate());
        response.setResponseStatus(payment.getStatus().equals("00") ? "Payment successful" : "Payment failed");
        return response;
    }

    public PaymentResponse findPaymentByTransactionNo(String transactionNo) {
        Payment payment = paymentRepository.findByTransactionNo(transactionNo)
            .orElseThrow(() -> new RuntimeException("Payment not found with transactionNo: " + transactionNo));

        PaymentResponse response = new PaymentResponse();
        response.setSuccess(payment.getStatus().equals("00"));
        response.setMessage(payment.getStatus().equals("00") ? "Payment successful" : "Payment failed");
        response.setTransactionNo(payment.getTransactionNo());
        response.setAmount(payment.getAmount());
        return response;
    }
}