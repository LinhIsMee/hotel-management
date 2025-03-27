package com.spring3.hotel.management.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UpsertRoomRequest {
    private String roomNumber;
    private String status;
    private String description;
    private Integer roomTypeId;
}
