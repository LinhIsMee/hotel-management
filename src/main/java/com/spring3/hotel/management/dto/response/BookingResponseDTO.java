package com.spring3.hotel.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDTO {
    private Integer id;
    private Integer userId;
    private String fullName;
    private String email;
    private String phone;
    private String nationalId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String status;
    private Double totalPrice;
    private Double finalPrice;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<RoomListResponseDTO> rooms;
    private List<ServiceResponseDTO> services;
    private String paymentMethod;
    private String paymentStatus;
    private Double discountAmount;
    private String discountCode;
    private Double discountValue;
    private String discountType;
} 