package com.spring3.hotel.management.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingPeriodDTO {
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
} 