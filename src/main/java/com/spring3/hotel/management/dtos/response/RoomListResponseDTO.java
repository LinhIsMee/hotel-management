package com.spring3.hotel.management.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomListResponseDTO {
    private Integer roomId;
    private String roomNumber;
    private String roomType;
    private String imagePath;
    private Double price;
}
