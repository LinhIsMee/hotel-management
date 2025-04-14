package com.spring3.hotel.management.dtos.response;

import com.spring3.hotel.management.models.Room;
import com.spring3.hotel.management.models.Review;
import com.spring3.hotel.management.repositories.ReviewRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.OptionalDouble;
import java.util.ArrayList;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponseDTO {
    private Integer id;
    private String roomNumber;
    private Integer roomTypeId;
    private String roomTypeName;
    private String status;
    private String floor;
    private Boolean isActive;
    private String notes;
    private String createdAt;
    private String updatedAt;
    private Double pricePerNight;
    private List<String> images;
    private List<ServiceResponseDTO> services;
    private Integer maxOccupancy;
    private List<String> amenities;
    private String specialFeatures;
    private Double averageRating;
    private Integer totalReviews;
    private List<ReviewResponseDTO> recentReviews;
    private Boolean isBookedNextFiveDays;
    private List<BookingPeriodDTO> bookingPeriods;
    
    private static ReviewRepository reviewRepository;
    
    @Autowired
    public void setReviewRepository(ReviewRepository reviewRepository) {
        RoomResponseDTO.reviewRepository = reviewRepository;
    }
    
    public static RoomResponseDTO fromEntity(Room room) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        // Chuyển đổi services sang DTO
        List<ServiceResponseDTO> serviceResponseDTOs = room.getServices().stream()
                .map(service -> ServiceResponseDTO.builder()
                        .id(service.getId())
                        .name(service.getName())
                        .description(service.getDescription())
                        .price(service.getPrice())
                        .build())
                .collect(Collectors.toList());
        
        // Xử lý amenities
        List<String> amenitiesList = room.getRoomType().getAmenities() != null ?
                Arrays.asList(room.getRoomType().getAmenities().split(",")) :
                Collections.emptyList();
        
        // Tính rating trung bình và số lượng đánh giá
        double averageRating = 0.0;
        int totalReviews = 0;
        List<ReviewResponseDTO> recentReviews = Collections.emptyList();
        
        if (reviewRepository != null) {
            List<Review> reviews = reviewRepository.findByRoomNumber(room.getRoomNumber());
            if (!reviews.isEmpty()) {
                OptionalDouble avgRating = reviews.stream()
                        .mapToDouble(Review::getRating)
                        .average();
                averageRating = avgRating.orElse(0.0);
                totalReviews = reviews.size();
                
                // Lấy 3 đánh giá gần nhất
                recentReviews = reviews.stream()
                        .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                        .limit(3)
                        .map(ReviewResponseDTO::fromEntity)
                        .collect(Collectors.toList());
            }
        }
        
        return RoomResponseDTO.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .roomTypeId(room.getRoomType().getId())
                .roomTypeName(room.getRoomType().getName())
                .status(room.getStatus())
                .floor(room.getFloor())
                .isActive(room.getIsActive())
                .notes(room.getNotes())
                .createdAt(room.getCreatedAt() != null ? room.getCreatedAt().format(formatter) : null)
                .updatedAt(room.getUpdatedAt() != null ? room.getUpdatedAt().format(formatter) : null)
                .pricePerNight(room.getRoomType().getBasePrice())
                .images(room.getImages())
                .services(serviceResponseDTOs)
                .maxOccupancy(room.getRoomType().getMaxOccupancy())
                .amenities(amenitiesList)
                .specialFeatures(room.getNotes())
                .averageRating(averageRating)
                .totalReviews(totalReviews)
                .recentReviews(recentReviews)
                .isBookedNextFiveDays(false) // Mặc định là false, sẽ được cập nhật sau
                .bookingPeriods(new ArrayList<>()) // Mặc định là rỗng, sẽ được cập nhật sau
                .build();
    }
}
