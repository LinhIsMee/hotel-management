package com.spring3.hotel.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Integer id;
    private String method;
    private Long amount;
    private LocalDateTime date;
    
    // Thêm các trường cần thiết cho VNPayService
    private String paymentUrl;
    private String transactionNo;
    private String orderInfo;
    private Integer bookingId;
    private String status;
    private String message;
} 