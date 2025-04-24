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
public class RoomResponseDTO {
    private Integer id;
    private String roomNumber;
    private String roomTypeName;
    private Integer roomTypeId;
    private String status;
    private String floor;
    private Boolean isActive;
    private String notes;
    private List<ServiceResponseDTO> services;
    private Double averageRating;
    private Integer ratingCount;
    private Double pricePerNight;
    private List<String> images;
    private Integer totalReviews;
    private List<ReviewResponseDTO> recentReviews;
    private Boolean isBookedNextFiveDays;
    private List<BookingPeriodDTO> bookingPeriods;
    private Integer maxOccupancy;
    private List<String> amenities;
} 