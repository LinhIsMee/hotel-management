package com.spring3.hotel.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomListResponseDTO {
    private Integer roomId;
    private String roomNumber;
    private String roomType;
    private Double price;
    private List<String> images;
} 