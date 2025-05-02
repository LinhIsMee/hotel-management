package com.spring3.hotel.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewBookingResponse {
    private Integer bookingId;
    private Double price;
    private Integer userId;
    private String fullName;
    private int roomCount;
}
