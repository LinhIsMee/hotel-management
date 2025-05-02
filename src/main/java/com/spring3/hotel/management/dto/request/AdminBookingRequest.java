package com.spring3.hotel.management.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AdminBookingRequest {
    private Integer userId;
    private List<Integer> roomIds;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Double totalPrice;
    private Double finalPrice;
    private Integer discountId;
    private String status;
    
    // Thông tin thanh toán đơn giản hơn so với UpsertBookingRequest
    private String paymentStatus;
    private String paymentMethod;
    
    // Thông tin khách hàng
    private Integer adults;
    private Integer children;
    private String fullName;
    private String phone;
    private String email;
    private String nationalId;
    private LocalDate paymentDate;
    private String specialRequests;
    private List<String> additionalServices;
    
    // Ghi chú nội bộ dành cho admin
    private String adminNote;
} 