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

    /**
     * Tạo URL thanh toán VNPay
     * @param bookingId ID của booking cần thanh toán
     * @param amount Số tiền cần thanh toán
     * @param orderInfo Thông tin đơn hàng
     * @return URL thanh toán VNPay
     */
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
        payment.setResponseCode("04");

        // Save payment record in the database with status "04"
        paymentRepository.save(payment);

        // Return the payment URL to redirect to VNPay
        return payUrl + "?" + queryUrl;
    }

    /**
     * Xử lý callback từ VNPay
     * @param queryParams Tham số từ VNPay
     * @return Kết quả xử lý
     */
    @Transactional
    public PaymentResponse processPaymentResponse(Map<String, String> queryParams) {
        PaymentResponse response = new PaymentResponse();
        log.info("Received payment callback with params: {}", queryParams);

        try {
            // 1. Lấy các thông tin từ queryParams
            String responseCode = queryParams.get("vnp_ResponseCode");
            String transactionNo = queryParams.get("vnp_TxnRef");
            String orderInfo = queryParams.get("vnp_OrderInfo");
            
            // Đảm bảo amount không lỗi
            String amountStr = queryParams.get("vnp_Amount");
            long amount = 0;
            if (amountStr != null && !amountStr.isEmpty()) {
                amount = Long.parseLong(amountStr) / 100;  // VNPay sends amount in cents
            }

            // 2. Tìm payment record trong database
            Optional<Payment> paymentOpt = paymentRepository.findByTransactionNo(transactionNo);
            if (paymentOpt.isEmpty()) {
                log.error("Payment not found with transactionNo: {}", transactionNo);
                response.setSuccess(false);
                response.setMessage("Payment not found with transactionNo: " + transactionNo);
                response.setTransactionNo(transactionNo);
                response.setAmount(amount);
                return response;
            }
            
            Payment payment = paymentOpt.get();

            // 3. Cập nhật thông tin payment từ VNPay
            payment.setResponseCode(responseCode);
            payment.setPayDate(LocalDateTime.now().toString());
            payment.setBankCode(queryParams.getOrDefault("vnp_BankCode", ""));
            
            // Xác định trạng thái thanh toán
            boolean isSuccess = "00".equals(responseCode); 
            payment.setStatus(responseCode); // Lưu mã trạng thái thực tế
            
            // 4. Nếu thanh toán thành công, cập nhật booking
            if (isSuccess && payment.getBooking() != null) {
                Booking booking = payment.getBooking();
                booking.setStatus("CONFIRMED");
                bookingRepository.save(booking);
            }

            // 5. Lưu thông tin payment vào database
            paymentRepository.save(payment);

            // 6. Trả về thông tin phản hồi
            response.setSuccess(isSuccess);
            String message = getPaymentStatusMessage(responseCode);
            response.setMessage(message);
            response.setTransactionNo(transactionNo);
            response.setAmount(amount);

        } catch (Exception e) {
            log.error("Error processing payment callback: ", e);
            response.setSuccess(false);
            response.setMessage("Error processing payment: " + e.getMessage());
        }

        return response;
    }

    /**
     * Lấy thông báo dựa trên mã trạng thái VNPay
     * @param responseCode Mã trạng thái từ VNPay
     * @return Thông báo cho người dùng
     */
    private String getPaymentStatusMessage(String responseCode) {
        return switch (responseCode) {
            case "00" -> "Payment successful";
            case "01" -> "Giao dịch đã tồn tại";
            case "02" -> "Merchant không hợp lệ";
            case "03" -> "Dữ liệu gửi sang không đúng định dạng";
            case "04" -> "Khởi tạo giao dịch thành công";
            case "05" -> "Giao dịch không thành công";
            case "06" -> "Giao dịch đã gửi sang Ngân hàng";
            case "07" -> "Trừ tiền thành công, giao dịch bị nghi ngờ gian lận";
            case "09" -> "Giao dịch đã quá thời gian chờ thanh toán. Quý khách vui lòng thực hiện lại giao dịch";
            case "10" -> "Giao dịch đang xử lý";
            case "11" -> "Giao dịch bị hủy";
            case "12" -> "Giao dịch bị từ chối";
            case "13" -> "Giao dịch bị từ chối do OTP";
            case "24" -> "Giao dịch không thành công do khách hàng hủy giao dịch";
            case "51" -> "Tài khoản không đủ số dư";
            case "65" -> "Tài khoản quá giới hạn thanh toán";
            case "75" -> "Ngân hàng thanh toán đang bảo trì";
            case "79" -> "Khách hàng đã hủy giao dịch trên cổng thanh toán VNPay";
            case "99" -> "Sai chữ ký";
            default -> "Payment failed with code: " + responseCode;
        };
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

    /**
     * Lấy lịch sử thanh toán của booking
     * @param bookingId ID của booking
     * @return Danh sách lịch sử thanh toán
     */
    public List<PaymentHistoryResponse> getPaymentHistoryByBooking(Integer bookingId) {
        List<Payment> payments = paymentRepository.findAllByBookingId(bookingId);
        if (payments.isEmpty()) {
            // Trả về danh sách rỗng thay vì ném ngoại lệ
            return new ArrayList<>();
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
        
        // Lấy thông báo dựa vào mã trạng thái
        response.setResponseStatus(getPaymentStatusMessage(payment.getStatus()));
        return response;
    }

    /**
     * Tìm thông tin thanh toán theo mã giao dịch
     * @param transactionNo Mã giao dịch
     * @return Thông tin thanh toán
     */
    public PaymentResponse findPaymentByTransactionNo(String transactionNo) {
        Optional<Payment> paymentOpt = paymentRepository.findByTransactionNo(transactionNo);
        
        if (paymentOpt.isEmpty()) {
            PaymentResponse response = new PaymentResponse();
            response.setSuccess(false);
            response.setMessage("Payment not found with transactionNo: " + transactionNo);
            response.setTransactionNo(transactionNo);
            response.setAmount(0L);
            return response;
        }
        
        Payment payment = paymentOpt.get();
        PaymentResponse response = new PaymentResponse();
        response.setSuccess("00".equals(payment.getStatus()));
        response.setMessage(getPaymentStatusMessage(payment.getStatus()));
        response.setTransactionNo(payment.getTransactionNo());
        response.setAmount(payment.getAmount());
        return response;
    }
}