package com.spring3.hotel.management.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class UpsertBookingRequest {
    private Integer userId;
    
    @NotNull(message = "Ngày nhận phòng không được để trống")
    @Future(message = "Ngày nhận phòng phải là ngày trong tương lai")
    private LocalDate checkInDate;
    
    @NotNull(message = "Ngày trả phòng không được để trống")
    @Future(message = "Ngày trả phòng phải là ngày trong tương lai")
    private LocalDate checkOutDate;
    
    @NotEmpty(message = "Danh sách phòng không được để trống")
    @Valid
    private List<BookingRoomRequest> rooms;
    
    private String notes;
    
    private String paymentMethod;
    
    private Integer discountId;
    
    private String status;
    
    private String paymentStatus;
    
    private LocalDateTime paymentDate;
    
    private Double totalPrice;
    
    private String discountCode;
    
    private List<String> additionalServices;
    
    // Phương thức tiện ích để lấy danh sách roomIds từ rooms
    public List<Integer> getRoomIds() {
        if (rooms == null) {
            return List.of();
        }
        return rooms.stream().map(BookingRoomRequest::getRoomId).toList();
    }
} 