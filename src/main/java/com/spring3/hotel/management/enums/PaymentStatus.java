package com.spring3.hotel.management.enums;

/**
 * Enum định nghĩa các trạng thái thanh toán
 */
public enum PaymentStatus {
    PENDING("Đang chờ thanh toán"),
    COMPLETED("Đã thanh toán"),
    FAILED("Thanh toán thất bại"),
    CANCELLED("Đã hủy"),
    REFUNDED("Đã hoàn tiền"),
    EXPIRED("Đã hết hạn");
    
    private final String description;
    
    PaymentStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
} 