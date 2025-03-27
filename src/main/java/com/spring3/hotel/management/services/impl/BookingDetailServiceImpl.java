package com.spring3.hotel.management.services.impl;

import com.spring3.hotel.management.dtos.request.BookingDetailRequest;
import com.spring3.hotel.management.dtos.response.BookingDetailResponse;
import com.spring3.hotel.management.dtos.response.BookingResponseDTO;
import com.spring3.hotel.management.models.Booking;
import com.spring3.hotel.management.models.BookingDetail;
import com.spring3.hotel.management.models.Room;
import com.spring3.hotel.management.repositories.BookingDetailRepository;
import com.spring3.hotel.management.repositories.BookingRepository;
import com.spring3.hotel.management.repositories.RoomRepository;
import com.spring3.hotel.management.services.BookingDetailService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class BookingDetailServiceImpl implements BookingDetailService {

    @Autowired
    private BookingDetailRepository bookingDetailRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private RoomRepository roomRepository;

    @Override
    public List<BookingDetailResponse> getBookingDetailsByBookingId(Integer bookingId) {
        return bookingDetailRepository.findAllByBooking_Id(bookingId)
                .stream()
                .map(this::convertToBookingDetailResponse)
                .toList();
    }

    @Override
    public BookingDetailResponse createBookingDetail(BookingDetailRequest request) {
        BookingDetail bookingDetail = new BookingDetail();

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));
        bookingDetail.setBooking(booking);
        bookingDetail.setRoom(room);
        bookingDetail.setPricePerNight(room.getRoomType().getBasePrice());
        bookingDetailRepository.save(bookingDetail);
        return convertToBookingDetailResponse(bookingDetail);
    }

    @Override
    public BookingDetailResponse deleteBookingDetail(Integer bookingDetailId) {
        return bookingDetailRepository.findById(bookingDetailId)
                .map(bookingDetail -> {
                    bookingDetailRepository.delete(bookingDetail);
                    return convertToBookingDetailResponse(bookingDetail);
                })
                .orElseThrow(() -> new RuntimeException("Booking detail not found"));
    }

    @Override
    public BookingDetailResponse updateBookingDetail(Integer bookingDetailId, BookingDetailRequest request) {
        BookingDetail bookingDetail = bookingDetailRepository.findById(bookingDetailId)
                .orElseThrow(() -> new RuntimeException("Booking detail not found"));

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));
        bookingDetail.setBooking(booking);
        bookingDetail.setRoom(room);
        bookingDetail.setPricePerNight(room.getRoomType().getBasePrice());
        bookingDetailRepository.save(bookingDetail);
        return convertToBookingDetailResponse(bookingDetail);
    }

    private BookingDetailResponse convertToBookingDetailResponse(BookingDetail bookingDetail) {
        BookingDetailResponse response = new BookingDetailResponse();
        response.setId(bookingDetail.getId());
        response.setBookingId(bookingDetail.getBooking().getId());
        response.setRoomId(bookingDetail.getRoom().getId());
        response.setPricePerNight(bookingDetail.getPricePerNight());
        return response;
    }
}
