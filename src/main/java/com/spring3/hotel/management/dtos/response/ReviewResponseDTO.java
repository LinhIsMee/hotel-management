package com.spring3.hotel.management.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDTO {
    private Integer id;
    private Integer userId;
    private String fullName;
    private Integer roomId;
    private String comment;
    private Integer rating;
    private String createdAt;
}
