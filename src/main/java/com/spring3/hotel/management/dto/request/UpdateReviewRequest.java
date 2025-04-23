package com.spring3.hotel.management.dto.request;

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
public class UpdateReviewRequest {
    
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
    private String replyDate;
    private Boolean isFeatured;
    private Boolean isAnonymous;
    private String status; // PENDING, REPLIED, HIDDEN
    private String createdAt;
    private String updatedAt;
}
