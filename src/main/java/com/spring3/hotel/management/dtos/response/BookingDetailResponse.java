package com.spring3.hotel.management.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingDetailResponse {
    private Integer id;
    private Integer bookingId;
    private Integer roomId;
    private Double pricePerNight;
}
