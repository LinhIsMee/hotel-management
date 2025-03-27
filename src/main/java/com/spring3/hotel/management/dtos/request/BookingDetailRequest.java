package com.spring3.hotel.management.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BookingDetailRequest {
    private Integer bookingId;
    private Integer roomId;
}
