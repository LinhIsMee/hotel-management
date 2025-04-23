package com.spring3.hotel.management.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class RoomListResponseDTO {
    private Integer roomId;
    private String roomNumber;
    private String roomType;
    private Double price;
    private List<String> images;
    private Double averageRating; // Rating trung bình của phòng
    private Integer totalReviews; // Tổng số đánh giá
} 