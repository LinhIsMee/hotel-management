package com.spring3.hotel.management.dtos.response;

import com.spring3.hotel.management.models.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDTO {
    
    private Integer id;
    private Integer userId;
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
    private String replyDate;
    private Boolean isFeatured;
    private Boolean isAnonymous;
    private String status;
    private String createdAt;
    private String updatedAt;
    
    public static ReviewResponseDTO fromEntity(Review review) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        ReviewResponseDTO dto = ReviewResponseDTO.builder()
                .id(review.getId())
                .userId(review.getUserId())
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
                .isFeatured(review.getIsFeatured())
                .isAnonymous(review.getIsAnonymous())
                .status(review.getStatus().name())
                .build();
        
        if (review.getReplyDate() != null) {
            dto.setReplyDate(review.getReplyDate().format(formatter));
        }
        
        if (review.getCreatedAt() != null) {
            dto.setCreatedAt(review.getCreatedAt().format(formatter));
        }
        
        if (review.getUpdatedAt() != null) {
            dto.setUpdatedAt(review.getUpdatedAt().format(formatter));
        }
        
        return dto;
    }
}
