package com.spring3.hotel.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingDTO {
    private Long id;
    private Integer stars;
    private String comment;
    private Integer roomId;
    private Integer userId;
    private String userName;
    private java.time.LocalDateTime createdAt;
} 