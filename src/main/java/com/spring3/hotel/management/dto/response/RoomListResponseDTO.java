package com.spring3.hotel.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomListResponseDTO {
    private Integer roomId;
    private String roomNumber;
    private String roomType;
    private Double price;
    private List<String> images;
    private Double averageRating;
    private Integer totalReviews;
}
