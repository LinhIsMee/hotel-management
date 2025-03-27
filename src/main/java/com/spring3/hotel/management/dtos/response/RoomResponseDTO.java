package com.spring3.hotel.management.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RoomResponseDTO {
    private Integer id;
    private String roomNumber;
    private Integer roomTypeId;
    private String roomTypeName;
    private String status;
    private String description;
}
