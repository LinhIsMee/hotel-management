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
                .bookingId(review.getBookingId())
                .guestName(review.getGuestName())
                .displayName(review.getDisplayName())
                .roomNumber(review.getRoomNumber())
                .roomType(review.getRoomType())
                .rating(review.getRating())
                .cleanliness(review.getCleanliness())
                .service(review.getService())
                .comfort(review.getComfort())
                .location(review.getLocation())
                .facilities(review.getFacilities())
                .valueForMoney(review.getValueForMoney())
                .comment(review.getComment())
                .images(review.getImages())
                .replyComment(review.getReplyComment())
                .replyBy(review.getReplyBy())
                .replyDate(review.getReplyDate())
                .isFeatured(review.getIsFeatured())
                .isAnonymous(review.getIsAnonymous())
                .status(review.getStatus() != null ? review.getStatus().name() : null)
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
} 