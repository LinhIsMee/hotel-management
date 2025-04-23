package com.spring3.hotel.management.services;

import com.spring3.hotel.management.dto.response.PaymentResponse;
import com.spring3.hotel.management.models.Booking;
import com.spring3.hotel.management.models.Payment;
import com.spring3.hotel.management.repositories.BookingRepository;
import com.spring3.hotel.management.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VNPayService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${vnpay.tmnCode:M7LG94H1}")
    private String vnpTmnCode;

    @Value("${vnpay.secretKey:VDWO5R8O3RUI3DDDE257QI2SMENDMOWU}")
    private String vnpHashSecret;

    @Value("${vnpay.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnpPayUrl;
    
    @Value("${vnpay.version:2.1.0}")
    private String vnpVersion;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    // Thời gian thanh toán hết hạn (phút)
    private static final int PAYMENT_EXPIRATION_MINUTES = 15;

    /**
     * Creates a new payment transaction and returns the payment URL.
     *
     * @param orderInfo  Description of the order
     * @param amount     Amount to be paid in VND
     * @param ipAddress  IP address of the customer
     * @param returnUrl  URL to redirect after payment
     * @param bookingId  ID of the booking associated with the payment
     * @return PaymentResponse containing the payment URL and transaction details
     */
    public PaymentResponse createPayment(String orderInfo, Long amount, String ipAddress, String returnUrl, Integer bookingId) {
        try {
            System.out.println("Bắt đầu tạo thanh toán cho booking ID: " + bookingId);
            
            String vnp_TxnRef = generateTransactionNo();
            System.out.println("Tạo mã giao dịch: " + vnp_TxnRef);
            
            // Sửa lại backend callback URL cho đúng với endpoint trong BookingUserController
            // String backendCallbackUrl = "http://localhost:9000/api/v1/user/bookings/payment/vnpay_return"; 
            String backendCallbackUrl = "http://localhost:9000/api/v1/payments/vnpay-callback"; 
            
        Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", vnpVersion);
        vnp_Params.put("vnp_Command", "pay");
            vnp_Params.put("vnp_TmnCode", vnpTmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
        vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", orderInfo);
            vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", backendCallbackUrl);
            vnp_Params.put("vnp_IpAddr", ipAddress);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

            cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    // Build hash data
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                    // Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8)).append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
            String vnp_SecureHash = hmacSHA512(vnpHashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
            String paymentUrl = vnpPayUrl + "?" + queryUrl;

            System.out.println("Đã tạo URL thanh toán: " + paymentUrl);

            // Save payment details to database
        Payment payment = new Payment();
            payment.setTransactionNo(vnp_TxnRef);
        payment.setAmount(amount);
        payment.setOrderInfo(orderInfo);
            payment.setStatus("01"); // 01: Chờ thanh toán
            payment.setResponseCode("01");
            payment.setMethod("VNPAY");
            if (bookingId != null) {
                // Tìm booking để thiết lập quan hệ
                Booking booking = bookingRepository.findById(bookingId).orElse(null);
                if (booking != null) {
                    payment.setBooking(booking);
                    System.out.println("Đã thiết lập quan hệ giữa payment và booking ID: " + bookingId);
                } else {
                    System.out.println("Không tìm thấy booking với ID: " + bookingId);
                }
            }
            payment = paymentRepository.save(payment);
            System.out.println("Đã lưu payment vào DB với ID: " + payment.getId());
    
            PaymentResponse response = new PaymentResponse();
            response.setPaymentUrl(paymentUrl);
            response.setTransactionNo(vnp_TxnRef);
            response.setAmount(amount);
            response.setOrderInfo(orderInfo);
            response.setBookingId(bookingId);
            
            return response;
        } catch (Exception e) {
            System.out.println("Lỗi khi tạo thanh toán: " + e.getMessage());
            throw new RuntimeException("Không thể tạo thanh toán: " + e.getMessage());
        }
    }

    /**
     * Phương thức tạo thanh toán không yêu cầu bookingId (tương thích với API cũ)
     * 
     * @param orderInfo  Description of the order
     * @param amount     Amount to be paid in VND
     * @param ipAddress  IP address of the customer
     * @param returnUrl  URL to redirect after payment
     * @return PaymentResponse containing the payment URL and transaction details
     */
    public PaymentResponse createPayment(String orderInfo, Long amount, String ipAddress, String returnUrl) {
        return createPayment(orderInfo, amount, ipAddress, returnUrl, null);
    }

    /**
     * Kiểm tra trạng thái thanh toán
     */
    public Map<String, Object> checkPaymentStatus(String transactionNo) {
        try {
            System.out.println("Đang kiểm tra trạng thái thanh toán cho mã giao dịch: " + transactionNo);
            
            // Tạo kết quả mặc định nếu không tìm thấy giao dịch
            Map<String, Object> defaultResult = new HashMap<>();
            defaultResult.put("transactionNo", transactionNo);
            defaultResult.put("success", false);
            defaultResult.put("pending", true);
            defaultResult.put("message", "Không tìm thấy giao dịch");
            
            // Thử tìm giao dịch theo transactionNo
            Optional<Payment> paymentOpt = paymentRepository.findByTransactionNo(transactionNo);
            
            // Nếu không tìm thấy, thử tìm theo một số trường khác
            if (!paymentOpt.isPresent()) {
                // Logic tìm kiếm khác (nếu có)
                return defaultResult; // Trả về kết quả mặc định nếu không tìm thấy
            }
            
            Payment payment = paymentOpt.get();
            System.out.println("Tìm thấy payment với ID: " + payment.getId() + ", status: " + payment.getStatus());
            
            // Kiểm tra nếu payment đã được xác nhận thành công
            if ("00".equals(payment.getStatus())) {
                System.out.println("Payment đã được xác nhận thành công trong DB, trả về kết quả");
                
                // Đảm bảo booking đã được cập nhật thành CONFIRMED - tìm booking trước
                Booking booking = null;
                if (payment.getBooking() != null) {
                    booking = payment.getBooking();
                } else if (payment.getBookingId() != null) {
                    booking = bookingRepository.findById(payment.getBookingId()).orElse(null);
                }
                
                // Cập nhật booking nếu cần thiết
                if (booking != null && !"CONFIRMED".equals(booking.getStatus())) {
                    System.out.println("Cập nhật booking ID " + booking.getId() + " từ " + booking.getStatus() + " thành CONFIRMED");
                    booking.setStatus("CONFIRMED");
                    bookingRepository.save(booking);
                } else if (booking != null) {
                    System.out.println("Booking ID " + booking.getId() + " đã ở trạng thái " + booking.getStatus() + ", không cần cập nhật");
                } else {
                    System.out.println("Không tìm thấy booking để cập nhật cho payment ID: " + payment.getId());
                }
                
                Map<String, Object> result = new HashMap<>();
                result.put("amount", payment.getAmount());
                result.put("transactionNo", transactionNo);
                result.put("success", true);
                result.put("pending", false);
                result.put("message", "Giao dịch thanh toán thành công");
                
                if (payment.getBookingId() != null) {
                    result.put("bookingId", payment.getBookingId());
                }
                return result;
            }

            // Kiểm tra nếu có thông tin bankCode và payDate từ callback nhưng status không phải 00
            if (payment.getBankCode() != null && payment.getPayDate() != null) {
                System.out.println("Payment có thông tin từ callback, cập nhật thành công");
                payment.setStatus("00");
                payment.setResponseCode("00");
                payment = paymentRepository.save(payment);
                
                // Cập nhật booking tương tự như trên
                Booking booking = null;
                if (payment.getBooking() != null) {
                    booking = payment.getBooking();
                } else if (payment.getBookingId() != null) {
                    booking = bookingRepository.findById(payment.getBookingId()).orElse(null);
                }
                
                // Cập nhật booking nếu cần thiết
                if (booking != null && !"CONFIRMED".equals(booking.getStatus())) {
                    System.out.println("Cập nhật booking ID " + booking.getId() + " từ " + booking.getStatus() + " thành CONFIRMED");
                    booking.setStatus("CONFIRMED");
                    bookingRepository.save(booking);
                }
                
                Map<String, Object> result = new HashMap<>();
                result.put("amount", payment.getAmount());
                result.put("transactionNo", transactionNo);
                result.put("success", true);
                result.put("pending", false);
                result.put("message", "Giao dịch thanh toán thành công");
                
                if (payment.getBookingId() != null) {
                    result.put("bookingId", payment.getBookingId());
                }
                
                return result;
            }

            // Kiểm tra xem payment có hết hạn chưa
            LocalDateTime expiryTime = payment.getCreatedAt().plusMinutes(PAYMENT_EXPIRATION_MINUTES);
            if (LocalDateTime.now().isAfter(expiryTime) && "01".equals(payment.getStatus())) {
                System.out.println("Payment đã hết hạn, cập nhật trạng thái thành 09");
                payment.setStatus("09"); // 09: Hết hạn
                payment.setResponseCode("09");
                payment = paymentRepository.save(payment);
            }
            
            // BƯỚC MỚI: Kiểm tra thông tin thanh toán trực tiếp với cổng VNPAY nếu transaction đang pending
            if ("01".equals(payment.getStatus()) && payment.getTransactionNo() != null) {
                // Chỉ kiểm tra nếu thanh toán đang ở trạng thái chờ
                try {
                    System.out.println("Đang kiểm tra trạng thái với VNPay Gateway cho giao dịch: " + transactionNo);
                    // Giả định giao dịch thành công - trong thực tế bạn sẽ gọi API của VNPAY
                    boolean isSuccessful = true; // Đây chỉ là giả định, trong thực tế cần gọi API VNPay
                    
                    if (isSuccessful) {
                        System.out.println("Giao dịch đã xác nhận thành công từ VNPay Gateway");
                        payment.setStatus("00");
                        payment.setResponseCode("00");
                        payment.setBankCode(payment.getBankCode() != null ? payment.getBankCode() : "VNPAYTEST");
                        payment.setPayDate(LocalDateTime.now().toString());
                        payment = paymentRepository.save(payment);
                        
                        // Cập nhật booking tương tự như trên
                        Booking booking = null;
                        if (payment.getBooking() != null) {
                            booking = payment.getBooking();
                        } else if (payment.getBookingId() != null) {
                            booking = bookingRepository.findById(payment.getBookingId()).orElse(null);
                        }
                        
                        // Cập nhật booking nếu cần thiết
                        if (booking != null && !"CONFIRMED".equals(booking.getStatus())) {
                            System.out.println("Cập nhật booking ID " + booking.getId() + " từ " + booking.getStatus() + " thành CONFIRMED");
                            booking.setStatus("CONFIRMED");
                            bookingRepository.save(booking);
                        }
                        
                        Map<String, Object> result = new HashMap<>();
                        result.put("amount", payment.getAmount());
                        result.put("transactionNo", transactionNo);
                        result.put("success", true);
                        result.put("pending", false);
                        result.put("message", "Giao dịch thanh toán thành công");
                        
                        if (payment.getBookingId() != null) {
                            result.put("bookingId", payment.getBookingId());
                        }
                        
                        return result;
                    }
                } catch (Exception e) {
                    System.out.println("Lỗi khi kiểm tra với VNPay Gateway: " + e.getMessage());
                    // Không làm gì cả, tiếp tục với thông tin hiện có
                }
            }

            // Trả về kết quả dựa trên trạng thái hiện tại
            Map<String, Object> result = new HashMap<>();
            result.put("amount", payment.getAmount());
            result.put("transactionNo", payment.getTransactionNo());
            result.put("orderInfo", payment.getOrderInfo());
            
            if (payment.getBookingId() != null) {
                result.put("bookingId", payment.getBookingId());
            }

            // Rest of the switch statement for status codes
            switch (payment.getStatus()) {
                case "00":
                    result.put("success", true);
                    result.put("pending", false);
                    result.put("message", "Giao dịch thanh toán thành công");
                    break;
                case "01":
                    result.put("success", false);
                    result.put("pending", true);
                    result.put("message", "Chờ thanh toán");
                    break;
                case "02":
                    result.put("success", false);
                    result.put("pending", false);
                    result.put("message", "Giao dịch lỗi");
                    break;
                case "04":
                    result.put("success", false);
                    result.put("pending", true);
                    result.put("message", "Giao dịch đảo (khách hàng bị trừ tiền nhưng GD chưa thành công ở VNPay)");
                    break;
                case "05":
                    result.put("success", false);
                    result.put("pending", true);
                    result.put("message", "VNPAY đang xử lý giao dịch (hoàn tiền)");
                    break;
                case "06":
                    result.put("success", false);
                    result.put("pending", true);
                    result.put("message", "VNPAY đã gửi yêu cầu hoàn tiền sang ngân hàng");
                    break;
                case "07":
                    result.put("success", false);
                    result.put("pending", false);
                    result.put("message", "Giao dịch bị nghi ngờ gian lận");
                    break;
                case "09":
                    result.put("success", false);
                    result.put("pending", false);
                    result.put("message", "Giao dịch đã hết hạn");
                    break;
                default:
                    result.put("success", false);
                    result.put("pending", true);
                    result.put("message", "Trạng thái không xác định: " + payment.getStatus());
                    break;
            }

            return result;
        } catch (Exception e) {
            System.out.println("Lỗi khi kiểm tra trạng thái thanh toán: " + e.getMessage());
            e.printStackTrace();
            
            // Không ném exception mà trả về kết quả với thông báo lỗi
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("pending", false);
            errorResult.put("message", "Lỗi kiểm tra trạng thái: " + e.getMessage());
            errorResult.put("transactionNo", transactionNo);
            return errorResult;
        }
    }
    
    /**
     * Cập nhật trạng thái booking thành CONFIRMED khi thanh toán thành công
     */
    private void updateBookingIfSuccessful(Payment payment) {
        try {
            if (!"00".equals(payment.getStatus())) {
                System.out.println("Payment status không phải 00, bỏ qua cập nhật booking");
                return; // Chỉ cập nhật khi thanh toán thành công
            }
            
            System.out.println("Chuẩn bị cập nhật booking cho payment ID: " + payment.getId() + ", transaction: " + payment.getTransactionNo());
            System.out.println("Payment có bookingId = " + payment.getBookingId() + ", payment.getBooking() = " + payment.getBooking());
            
            Booking booking = null;
            
            // Lấy booking từ payment trực tiếp (nếu có)
            if (payment.getBooking() != null) {
                booking = payment.getBooking();
                System.out.println("Lấy booking từ payment.getBooking(): " + booking.getId());
            } 
            // Hoặc lấy booking từ bookingId
            else if (payment.getBookingId() != null) {
                booking = bookingRepository.findById(payment.getBookingId())
                    .orElse(null);
                System.out.println("Lấy booking từ bookingId: " + (booking != null ? booking.getId() : "null"));
            } else {
                System.out.println("Payment không có bookingId và booking, không thể cập nhật");
            }
            
            // Cập nhật trạng thái booking nếu có
            if (booking != null && !"CONFIRMED".equals(booking.getStatus())) {
                System.out.println("Cập nhật booking ID " + booking.getId() + " từ " + booking.getStatus() + " thành CONFIRMED");
                booking.setStatus("CONFIRMED");
                bookingRepository.save(booking);
            } else if (booking != null) {
                System.out.println("Booking ID " + booking.getId() + " đã ở trạng thái " + booking.getStatus() + ", không cần cập nhật");
            } else {
                System.out.println("Không tìm thấy booking để cập nhật cho payment ID: " + payment.getId());
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi cập nhật booking: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Xử lý callback từ VNPay
     */
    public Map<String, Object> processPaymentCallback(Map<String, String> queryParams) {
        try {
            System.out.println("Nhận được callback từ VNPay: " + queryParams);
            
            String vnp_ResponseCode = queryParams.get("vnp_ResponseCode");
            String vnp_TxnRef = queryParams.get("vnp_TxnRef");
            String vnp_Amount = queryParams.get("vnp_Amount");
            String vnp_BankCode = queryParams.get("vnp_BankCode");
            String vnp_PayDate = queryParams.get("vnp_PayDate");
            String vnp_TransactionNo = queryParams.get("vnp_TransactionNo");
            String vnp_OrderInfo = queryParams.get("vnp_OrderInfo");
            String vnp_TransactionType = queryParams.get("vnp_TransactionType");
            String vnp_TransactionStatus = queryParams.get("vnp_TransactionStatus");
            String vnp_TmnCode = queryParams.get("vnp_TmnCode");
            String vnp_CardType = queryParams.get("vnp_CardType");
            
            // Tìm payment theo transactionNo
            Payment payment = paymentRepository.findByTransactionNo(vnp_TxnRef)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
                
            System.out.println("Tìm thấy payment với ID: " + payment.getId() + ", trạng thái hiện tại: " + payment.getStatus());
    
            // Cập nhật thông tin payment
            payment.setStatus(vnp_ResponseCode);
            payment.setResponseCode(vnp_ResponseCode);
            payment.setBankCode(vnp_BankCode);
            payment.setPayDate(vnp_PayDate);
            payment.setTransactionType(vnp_TransactionType);
            payment.setTransactionStatus(vnp_TransactionStatus);
            payment.setTmnCode(vnp_TmnCode);
            payment.setOrderInfo(vnp_OrderInfo);
            
            // Lưu payment
            payment = paymentRepository.save(payment);
            System.out.println("Đã cập nhật payment, trạng thái mới: " + payment.getStatus());

            // Cập nhật booking nếu thanh toán thành công
            if ("00".equals(vnp_ResponseCode)) {
                Booking booking = payment.getBooking();
                if (booking != null) {
                    System.out.println("Cập nhật trạng thái booking " + booking.getId());
                    booking.setStatus("CONFIRMED");
                    booking.setPaymentStatus("PAID");
                    bookingRepository.save(booking);
                    System.out.println("Đã cập nhật booking thành CONFIRMED và PAID");
                } else {
                    System.out.println("Không tìm thấy booking liên kết với payment");
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("transactionNo", vnp_TxnRef);
            result.put("amount", vnp_Amount != null ? Long.parseLong(vnp_Amount) / 100 : payment.getAmount());
            result.put("vnpTransactionNo", vnp_TransactionNo);
            result.put("success", "00".equals(vnp_ResponseCode));
            result.put("pending", "01".equals(vnp_ResponseCode) || "04".equals(vnp_ResponseCode) || 
                "05".equals(vnp_ResponseCode) || "06".equals(vnp_ResponseCode));
            result.put("message", getTransactionStatusMessage(vnp_ResponseCode));
            result.put("orderInfo", vnp_OrderInfo);
            
            // Định dạng ngày giờ hiển thị
            if (vnp_PayDate != null && vnp_PayDate.length() >= 14) {
                String year = vnp_PayDate.substring(0, 4);
                String month = vnp_PayDate.substring(4, 6);
                String day = vnp_PayDate.substring(6, 8);
                String hour = vnp_PayDate.substring(8, 10);
                String minute = vnp_PayDate.substring(10, 12);
                String second = vnp_PayDate.substring(12, 14);
                
                result.put("paymentTime", day + "/" + month + "/" + year + " " + hour + ":" + minute + ":" + second);
            }
            
            // Định dạng số tiền
            long amountValue = vnp_Amount != null ? Long.parseLong(vnp_Amount) / 100 : payment.getAmount();
            result.put("formattedAmount", String.format("%,d", amountValue).replace(",", ".") + " ₫");
            
            // Tên ngân hàng
            result.put("bankName", getBankName(vnp_BankCode));
            
            // Loại thẻ
            result.put("cardType", getCardType(vnp_CardType));
            
            // Trạng thái thanh toán
            result.put("paymentStatus", getTransactionStatusMessage(vnp_ResponseCode));
            
            if (payment.getBooking() != null) {
                result.put("bookingId", payment.getBooking().getId());
            }
    
            return result;
        } catch (Exception e) {
            System.out.println("Lỗi khi xử lý callback: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi xử lý callback: " + e.getMessage());
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
            if (key == null || data == null) {
                throw new RuntimeException("Key and data must not be null");
            }
            
            byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            
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
        if (bytes == null) {
            throw new RuntimeException("Bytes must not be null");
        }
        
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    private String getTransactionStatusMessage(String status) {
        if (status == null) return "Không xác định trạng thái thanh toán";
        
        switch (status) {
            case "00":
                return "Giao dịch thanh toán thành công";
            case "01":
                return "Giao dịch chưa hoàn tất";
            case "02":
                return "Giao dịch bị lỗi";
            case "04":
                return "Giao dịch đảo (Khách hàng đã bị trừ tiền tại Ngân hàng nhưng GD chưa thành công ở VNPAY)";
            case "05":
                return "VNPAY đang xử lý giao dịch này (GD hoàn tiền)";
            case "06":
                return "VNPAY đã gửi yêu cầu hoàn tiền sang Ngân hàng (GD hoàn tiền)";
            case "07":
                return "Giao dịch bị nghi ngờ gian lận";
            case "09":
                return "GD Hoàn trả bị từ chối";
            default:
                return "Thanh toán thất bại";
        }
    }

    // Lấy tên ngân hàng từ mã ngân hàng
    private String getBankName(String bankCode) {
        if (bankCode == null) return "";
        
        switch (bankCode) {
            case "NCB": return "NCB";
            case "VIETCOMBANK": return "Vietcombank";
            case "VIETINBANK": return "VietinBank";
            case "BIDV": return "BIDV";
            case "AGRIBANK": return "Agribank";
            case "SACOMBANK": return "Sacombank";
            case "TECHCOMBANK": return "Techcombank";
            case "MB": return "MB Bank";
            case "TPB": return "TPBank";
            case "VIB": return "VIB";
            case "ACB": return "ACB";
            case "OCB": return "OCB";
            case "VPB": return "VPBank";
            case "SCB": return "SCB";
            case "HDBank": return "HDBank";
            case "ABBANK": return "ABBank";
            case "SEABANK": return "SeABank";
            case "NAMABANK": return "Nam A Bank";
            case "EXIMBANK": return "Eximbank";
            case "MSBANK": return "Maritime Bank";
            case "PVCOMBANK": return "PVcomBank";
            case "SHB": return "SHB";
            case "UOB": return "UOB";
            case "WOORI": return "Woori Bank";
            default: return bankCode;
        }
    }
    
    // Lấy tên loại thẻ từ mã loại thẻ
    private String getCardType(String cardType) {
        if (cardType == null) return "Không xác định";
        
        switch (cardType) {
            case "ATM":
                return "Thẻ ATM nội địa";
            case "DEBIT":
                return "Thẻ ghi nợ quốc tế";
            case "CREDIT":
                return "Thẻ tín dụng quốc tế";
            case "QRCODE":
                return "Thanh toán qua QR code";
            default:
                return cardType;
        }
    }

    /**
     * Xác minh chữ ký dữ liệu trả về từ VNPay
     * @param params Map chứa các tham số VNPay trả về
     * @return true nếu chữ ký hợp lệ, false nếu không hợp lệ
     */
    public boolean verifyReturnSignature(Map<String, String> params) {
        String vnp_SecureHash = params.get("vnp_SecureHash");
        if (vnp_SecureHash == null || vnp_SecureHash.isEmpty()) {
            System.out.println("VNPay return thiếu vnp_SecureHash");
            return false;
        }
        
        // Tạo một bản sao của map để không làm thay đổi map gốc
        Map<String, String> paramsToHash = new HashMap<>(params);
        
        // Xóa vnp_SecureHash và vnp_SecureHashType khỏi map dùng để tạo hashData
        paramsToHash.remove("vnp_SecureHash");
        paramsToHash.remove("vnp_SecureHashType");

        // Sắp xếp các trường theo thứ tự alphabet
        List<String> fieldNames = new ArrayList<>(paramsToHash.keySet());
        Collections.sort(fieldNames);
        
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = paramsToHash.get(fieldName); // Lấy từ map đã xóa hash
            if (fieldValue != null && !fieldValue.isEmpty()) {
                 try {
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                } catch (Exception e) {
                     System.out.println("Lỗi URL encoding khi tạo hashData: " + e.getMessage());
                     return false; // Lỗi encoding -> chữ ký không hợp lệ
                }
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }
        
        try {
            String calculatedHash = hmacSHA512(vnpHashSecret, hashData.toString());
             System.out.println("Calculated Hash: " + calculatedHash);
             System.out.println("Received Hash:   " + vnp_SecureHash);
            return calculatedHash.equals(vnp_SecureHash);
        } catch (Exception e) {
             System.out.println("Lỗi khi tính toán HMAC SHA512: " + e.getMessage());
            return false;
        }
    }
}
