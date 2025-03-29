package com.spring3.hotel.management.dtos.response;

import com.spring3.hotel.management.models.Rating;
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
public class RatingResponseDTO {
    
    private Integer id;
    private String bookingId;
    private String guestName;
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
    private String status;
    private String createdAt;
    private String updatedAt;
    
    public static RatingResponseDTO fromEntity(Rating rating) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        RatingResponseDTO dto = RatingResponseDTO.builder()
                .id(rating.getId())
                .bookingId(rating.getBookingId())
                .guestName(rating.getGuestName())
                .roomNumber(rating.getRoomNumber())
                .roomType(rating.getRoomType())
                .rating(rating.getRating())
                .cleanliness(rating.getCleanliness())
                .service(rating.getService())
                .comfort(rating.getComfort())
                .location(rating.getLocation())
                .facilities(rating.getFacilities())
                .valueForMoney(rating.getValueForMoney())
                .comment(rating.getComment())
                .images(rating.getImages())
                .replyComment(rating.getReplyComment())
                .replyBy(rating.getReplyBy())
                .status(rating.getStatus().name())
                .build();
        
        if (rating.getReplyDate() != null) {
            dto.setReplyDate(rating.getReplyDate().format(formatter));
        }
        
        if (rating.getCreatedAt() != null) {
            dto.setCreatedAt(rating.getCreatedAt().format(formatter));
        }
        
        if (rating.getUpdatedAt() != null) {
            dto.setUpdatedAt(rating.getUpdatedAt().format(formatter));
        }
        
        return dto;
    }
} 