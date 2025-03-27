package com.spring3.hotel.management.services;

import com.spring3.hotel.management.dtos.request.BookingDetailRequest;
import com.spring3.hotel.management.dtos.response.BookingDetailResponse;
import com.spring3.hotel.management.dtos.response.BookingResponseDTO;

import java.util.List;

public interface BookingDetailService {

    List<BookingDetailResponse> getBookingDetailsByBookingId(Integer bookingId);
    BookingDetailResponse createBookingDetail(BookingDetailRequest request);
    BookingDetailResponse deleteBookingDetail(Integer bookingDetailId);
    BookingDetailResponse updateBookingDetail(Integer bookingDetailId, BookingDetailRequest request);

}
