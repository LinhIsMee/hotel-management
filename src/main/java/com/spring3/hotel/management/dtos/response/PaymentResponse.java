package com.spring3.hotel.management.dtos.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class PaymentResponse {
    private boolean success; // Trạng thái thanh toán (thành công hay thất bại)
    private String message;  // Thông điệp phản hồi
    private String transactionNo; // Mã giao dịch
    private Long amount; // Số tiền thanh toán
}
