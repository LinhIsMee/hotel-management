package com.spring3.hotel.management.dtos.request;

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
public class UpsertBookingRequest {
    private Integer userId;
    private List<Integer> roomIds;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Double totalPrice;
    private Double finalPrice;
    private Integer discountId;
    private String status;
    
    // Các trường mới
    private String paymentStatus;
    private String paymentMethod;
    private Integer adults;
    private Integer children;
    private String fullName;
    private String phone;
    private String email;
    private String nationalId;
    private LocalDate paymentDate;
    private String specialRequests;
    private List<String> additionalServices;
    
    // Thêm trường cho thanh toán kết hợp
    private String ipAddress;
    private String returnUrl;
}
