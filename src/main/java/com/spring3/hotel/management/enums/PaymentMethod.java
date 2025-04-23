package com.spring3.hotel.management.enums;

/**
 * Enum định nghĩa các phương thức thanh toán
 */
public enum PaymentMethod {
    CASH("Tiền mặt"),
    VNPAY("VNPay"),
    CREDIT_CARD("Thẻ tín dụng"),
    BANK_TRANSFER("Chuyển khoản ngân hàng"),
    MOMO("Ví MoMo"),
    ZALOPAY("ZaloPay");
    
    private final String description;
    
    PaymentMethod(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
} 