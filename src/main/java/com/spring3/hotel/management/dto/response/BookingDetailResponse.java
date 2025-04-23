package com.spring3.hotel.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDetailResponse {
    private Integer id;
    private Integer bookingId;
    private Integer roomId;
    private String roomNumber;
    private String roomType;
    private Double pricePerNight;
    private Integer adults;
    private Integer children;
    private List<ServiceResponseDTO> services;
    private Double totalPrice; // Tổng giá phòng + dịch vụ
}
