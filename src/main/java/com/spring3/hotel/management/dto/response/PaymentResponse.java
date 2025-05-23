package com.spring3.hotel.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String paymentUrl;
    private String transactionNo;
    private Long amount;
    private String orderInfo;
    private Integer bookingId;
}
