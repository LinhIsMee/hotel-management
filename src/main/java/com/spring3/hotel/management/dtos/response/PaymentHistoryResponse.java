package com.spring3.hotel.management.dtos.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class PaymentHistoryResponse {
    private String transactionNo; // Mã giao dịch
    private Long amount; // Số tiền thanh toán
    private String orderInfo; // Thông tin đơn hàng
    private String payDate; // Ngày thanh toán
    private String responseStatus; // Trạng thái thanh toán
}
