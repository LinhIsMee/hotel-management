package com.spring3.hotel.management.enums;

/**
 * Enum định nghĩa các trạng thái phòng
 */
public enum RoomStatus {
    VACANT("Phòng trống"),
    OCCUPIED("Đang sử dụng"),
    MAINTENANCE("Đang bảo trì"),
    CLEANING("Đang dọn dẹp"),
    RESERVED("Đã đặt trước");
    
    private final String description;
    
    RoomStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
} 