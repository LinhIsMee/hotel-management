package com.spring3.hotel.management.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusRequestDTO {
    //trang thai thanh toan
    private String orderId;
    private String orderInfo;
    private String transactionNo;
    private String transDate;
    private Long amount;
}
