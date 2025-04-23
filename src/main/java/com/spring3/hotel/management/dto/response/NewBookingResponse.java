package com.spring3.hotel.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewBookingResponse {
    private Integer bookingId;
    private Integer userId;
    private String fullName;
    private String userAvatar;
    private Double totalAmount;
    private LocalDateTime bookingDate;
    private String status;
    private Integer roomCount;
} 