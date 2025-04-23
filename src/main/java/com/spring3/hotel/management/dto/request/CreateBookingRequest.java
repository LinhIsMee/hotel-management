package com.spring3.hotel.management.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {
    @NotNull(message = "Ngày check-in không được để trống")
    private LocalDate checkInDate;
    
    @NotNull(message = "Ngày check-out không được để trống")
    private LocalDate checkOutDate;
    
    @NotEmpty(message = "Danh sách phòng không được để trống")
    private List<BookingRoomRequest> rooms;
    
    private String discountCode;
    
    @NotNull(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod;
} 
