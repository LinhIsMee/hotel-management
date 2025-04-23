package com.spring3.hotel.management.services;

import com.spring3.hotel.management.dto.request.BookingDetailRequest;
import com.spring3.hotel.management.dto.response.BookingDetailResponse;
import com.spring3.hotel.management.dto.response.BookingResponseDTO;

import java.util.List;

public interface BookingDetailService {

    List<BookingDetailResponse> getBookingDetailsByBookingId(Integer bookingId);
    BookingDetailResponse createBookingDetail(BookingDetailRequest request);
    BookingDetailResponse deleteBookingDetail(Integer bookingDetailId);
    BookingDetailResponse updateBookingDetail(Integer bookingDetailId, BookingDetailRequest request);

}
