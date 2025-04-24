package com.spring3.hotel.management.dto.response;

import com.spring3.hotel.management.models.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDTO {
    private Integer id;
    private String bookingId;
    private String guestName;
    private String displayName;
    private String roomNumber;
    private String roomType;
    private Integer rating;
    private Integer cleanliness;
    private Integer service;
    private Integer comfort;
    private Integer location;
    private Integer facilities;
    private Integer valueForMoney;
    private String comment;
    private List<String> images;
    private String replyComment;
    private String replyBy;
    private LocalDateTime replyDate;
    private Boolean isFeatured;
    private Boolean isAnonymous;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Chuyển đổi từ entity Review sang DTO ReviewResponseDTO
     */
    public static ReviewResponseDTO fromEntity(Review review) {
        if (review == null) {
            return null;
        }
        
        return ReviewResponseDTO.builder()
                .id(review.getId())
                // .bookingId(review.getBookingId()) // Commenting out: Missing method
                // .guestName(review.getGuestName()) // Commenting out: Missing method
                // .displayName(review.getDisplayName()) // Commenting out: Missing method
                // .roomNumber(review.getRoomNumber()) // Commenting out: Missing method
                // .roomType(review.getRoomType()) // Commenting out: Missing method
                .rating(review.getRating())
                // .cleanliness(review.getCleanliness()) // Commenting out: Missing method
                // .service(review.getService()) // Commenting out: Missing method
                // .comfort(review.getComfort()) // Commenting out: Missing method
                // .location(review.getLocation()) // Commenting out: Missing method
                // .facilities(review.getFacilities()) // Commenting out: Missing method
                // .valueForMoney(review.getValueForMoney()) // Commenting out: Missing method
                .comment(review.getComment())
                // .images(review.getImages()) // Commenting out: Missing method
                // .replyComment(review.getReplyComment()) // Commenting out: Missing method
                // .replyBy(review.getReplyBy()) // Commenting out: Missing method
                // .replyDate(review.getReplyDate()) // Commenting out: Missing method
                // .isFeatured(review.getIsFeatured()) // Commenting out: Missing method
                // .isAnonymous(review.getIsAnonymous()) // Commenting out: Missing method
                // .status(review.getStatus() != null ? review.getStatus().name() : null) // Commenting out: Missing method
                // .createdAt(review.getCreatedAt()) // Commenting out: Missing method
                // .updatedAt(review.getUpdatedAt()) // Commenting out: Missing method
                .build();
    }
} 