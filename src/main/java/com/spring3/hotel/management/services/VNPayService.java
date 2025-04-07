package com.spring3.hotel.management.services;

import com.spring3.hotel.management.dtos.response.PaymentResponse;
import com.spring3.hotel.management.models.Payment;
import com.spring3.hotel.management.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class VNPayService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Value("${vnpay.tmnCode:M7LG94H1}")
    private String vnp_TmnCode;

    @Value("${vnpay.secretKey:VDWO5R8O3RUI3DDDE257QI2SMENDMOWU}")
    private String vnp_HashSecret;

    @Value("${vnpay.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnp_Url;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    // Thời gian thanh toán hết hạn (phút)
    private static final int PAYMENT_EXPIRATION_MINUTES = 15;

    /**
     * Tạo URL thanh toán VNPay
     */
    public PaymentResponse createPayment(String orderInfo, Long amount, String ipAddress, String returnUrl) {
        try {
            // Tạo mã giao dịch ngẫu nhiên
            String vnp_TxnRef = generateTransactionNo();
            
            // Tạo các tham số cho VNPay
            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", "2.1.0");
            vnp_Params.put("vnp_Command", "pay");
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf(amount * 100)); // VNPay yêu cầu số tiền nhân 100
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", orderInfo);
            vnp_Params.put("vnp_OrderType", "other");
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", returnUrl);
            vnp_Params.put("vnp_IpAddr", ipAddress);
            
            // Thêm thời gian
            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
            
            // Thêm thời gian hết hạn (15 phút sau)
            cld.add(Calendar.MINUTE, PAYMENT_EXPIRATION_MINUTES);
            String vnp_ExpireDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);
            
            // Sắp xếp các tham số theo thứ tự alphabet
            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);
            
            // Tạo chuỗi hash
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }
            
            String queryUrl = query.toString();
            String vnp_SecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
            queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
            
            // Lưu thông tin payment
            Payment payment = new Payment();
            payment.setTransactionNo(vnp_TxnRef);
            payment.setAmount(amount);
            payment.setStatus("01"); // Trạng thái chờ thanh toán
            payment.setOrderInfo(orderInfo);
            payment.setCreatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
            
            // Trả về response
            return new PaymentResponse(
                vnp_Url + "?" + queryUrl,
                vnp_TxnRef,
                amount,
                orderInfo
            );
            
        } catch (Exception e) {
            throw new RuntimeException("Không thể tạo thanh toán: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra trạng thái thanh toán
     */
    public Map<String, Object> checkPaymentStatus(String transactionNo) {
        Payment payment = paymentRepository.findByTransactionNo(transactionNo)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch"));
            
        // Kiểm tra nếu trạng thái là "01" (chờ thanh toán) và đã quá thời gian 15 phút
        if ("01".equals(payment.getStatus()) && payment.getCreatedAt() != null) {
            LocalDateTime createdAt = payment.getCreatedAt();
            LocalDateTime expiryTime = createdAt.plusMinutes(PAYMENT_EXPIRATION_MINUTES);
            
            if (LocalDateTime.now().isAfter(expiryTime)) {
                // Cập nhật trạng thái thanh toán thành hết hạn
                payment.setStatus("09");
                paymentRepository.save(payment);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("amount", payment.getAmount());
        result.put("transactionNo", transactionNo);
        
        switch (payment.getStatus()) {
            case "00":
                result.put("success", true);
                result.put("message", "Thanh toán thành công");
                break;
            case "01":
                result.put("success", false);
                result.put("message", "Chờ thanh toán");
                result.put("pending", true);
                break;
            case "09":
                result.put("success", false);
                result.put("message", "Giao dịch đã quá thời gian chờ thanh toán");
                break;
            case "10":
                result.put("success", false);
                result.put("message", "Giao dịch bị từ chối bởi ngân hàng thanh toán");
                break;
            case "11":
                result.put("success", false);
                result.put("message", "Giao dịch bị hủy");
                break;
            case "12":
                result.put("success", false);
                result.put("message", "Giao dịch bị từ chối");
                break;
            case "13":
                result.put("success", false);
                result.put("message", "Khách hàng đã hủy giao dịch");
                break;
            case "24":
                result.put("success", false);
                result.put("message", "Giao dịch không thành công do khách hàng nhập sai thông tin");
                break;
            case "51":
                result.put("success", false);
                result.put("message", "Tài khoản không đủ số dư");
                break;
            case "65":
                result.put("success", false);
                result.put("message", "Tài khoản vượt quá hạn mức giao dịch trong ngày");
                break;
            case "75":
                result.put("success", false);
                result.put("message", "Ngân hàng thanh toán đang bảo trì");
                break;
            case "79":
                result.put("success", false);
                result.put("message", "Giao dịch nghi ngờ gian lận");
                break;
            case "99":
                result.put("success", false);
                result.put("message", "Sai chữ ký");
                break;
            default:
                result.put("success", false);
                result.put("message", "Thanh toán thất bại");
        }
        
        return result;
    }

    /**
     * Xử lý callback từ VNPay
     */
    public Map<String, Object> processPaymentCallback(Map<String, String> queryParams) {
        try {
            // Kiểm tra chữ ký
            String vnp_SecureHash = queryParams.remove("vnp_SecureHash");
            String vnp_SecureHashType = queryParams.remove("vnp_SecureHashType");
            
            // Bỏ qua kiểm tra chữ ký nếu đang ở môi trường dev
            boolean isDevEnvironment = "dev".equals(activeProfile) || activeProfile == null || activeProfile.isEmpty();
            
            if (!isDevEnvironment) {
                // Sắp xếp các tham số theo thứ tự alphabet
                List<String> fieldNames = new ArrayList<>(queryParams.keySet());
                Collections.sort(fieldNames);
                
                // Tạo chuỗi hash
                StringBuilder hashData = new StringBuilder();
                Iterator<String> itr = fieldNames.iterator();
                while (itr.hasNext()) {
                    String fieldName = itr.next();
                    String fieldValue = queryParams.get(fieldName);
                    if ((fieldValue != null) && (fieldValue.length() > 0)) {
                        hashData.append(fieldName);
                        hashData.append('=');
                        hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                        if (itr.hasNext()) {
                            hashData.append('&');
                        }
                    }
                }
                
                String hash = hmacSHA512(vnp_HashSecret, hashData.toString());
                if (!hash.equals(vnp_SecureHash)) {
                    throw new RuntimeException("Sai chữ ký");
                }
            }
            
            // Lấy thông tin giao dịch
            String vnp_TxnRef = queryParams.get("vnp_TxnRef");
            String vnp_ResponseCode = queryParams.get("vnp_ResponseCode");
            String vnp_Amount = queryParams.get("vnp_Amount");
            
            // Cập nhật trạng thái payment
            Payment payment = paymentRepository.findByTransactionNo(vnp_TxnRef)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch"));
                
            payment.setStatus(vnp_ResponseCode);
            paymentRepository.save(payment);
            
            // Trả về kết quả
            Map<String, Object> result = new HashMap<>();
            result.put("success", "00".equals(vnp_ResponseCode));
            result.put("message", "00".equals(vnp_ResponseCode) ? "Thanh toán thành công" : "Thanh toán thất bại");
            result.put("transactionNo", vnp_TxnRef);
            result.put("amount", vnp_Amount != null ? Long.parseLong(vnp_Amount) / 100 : payment.getAmount());
            
            return result;
            
        } catch (Exception e) {
            throw new RuntimeException("Không thể xử lý callback: " + e.getMessage());
        }
    }

    /**
     * Tạo mã giao dịch ngẫu nhiên
     */
    private String generateTransactionNo() {
        return String.format("%08d", new Random().nextInt(99999999));
    }

    /**
     * Tạo chữ ký HMAC-SHA512
     */
    private String hmacSHA512(String key, String data) {
        try {
            byte[] hmacKeyBytes = key.getBytes();
            byte[] dataBytes = data.getBytes();
            
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            mac.init(secretKeySpec);
            byte[] result = mac.doFinal(dataBytes);
            
            return bytesToHex(result);
        } catch (Exception e) {
            throw new RuntimeException("Không thể tạo chữ ký: " + e.getMessage());
        }
    }

    /**
     * Chuyển đổi byte[] thành hex string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
} 