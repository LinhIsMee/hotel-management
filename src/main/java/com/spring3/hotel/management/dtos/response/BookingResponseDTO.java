package com.spring3.hotel.management.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDTO {
    private Integer id;
    private Integer userId;
    private String fullName;
    private String nationalId;
    private String email;
    private String phone;
    private List<RoomListResponseDTO> rooms;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Double totalPrice;
    private Double finalPrice;
    private String discountCode;
    private Double discountValue;
    private String discountType;
    private String status;
    private String paymentMethod;
    private String paymentStatus;
    private String createdAt;
}
