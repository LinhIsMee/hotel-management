package com.spring3.hotel.management.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentLinkResponse {
    private String paymentUrl;
    private String orderInfo;
} 