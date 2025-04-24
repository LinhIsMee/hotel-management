package com.spring3.hotel.management.enums;

/**
 * Trạng thái đặt phòng trong hệ thống
 */
public enum BookingStatus {
    PENDING,      // Đơn đặt phòng đang chờ xác nhận
    CONFIRMED,    // Đã xác nhận đặt phòng
    CHECKED_IN,   // Khách đã nhận phòng
    CHECKED_OUT,  // Khách đã trả phòng
    CANCELLED     // Đơn đặt phòng đã bị hủy
} 