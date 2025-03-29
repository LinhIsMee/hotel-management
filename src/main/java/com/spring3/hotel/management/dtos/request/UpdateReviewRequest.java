package com.spring3.hotel.management.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReviewRequest {
    
    private Boolean isFeatured;
    private Boolean isAnonymous;
    private String status; // PENDING, REPLIED, HIDDEN
}
