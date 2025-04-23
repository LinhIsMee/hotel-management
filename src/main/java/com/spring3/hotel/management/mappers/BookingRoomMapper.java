package com.spring3.hotel.management.mappers;

import com.spring3.hotel.management.dto.request.BookingRoomRequest;
import com.spring3.hotel.management.dto.request.UpsertBookingRequest;
import com.spring3.hotel.management.dto.response.BookingResponseDTO;
import com.spring3.hotel.management.models.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingRoomMapper {
    
    @Mapping(target = "id", source = "id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "fullName", source = "user.fullName")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "phone", source = "user.phoneNumber")
    @Mapping(target = "nationalId", source = "user.nationalId")
    @Mapping(target = "discountCode", source = "discount.code")
    @Mapping(target = "discountAmount", source = "discountAmount")
    @Mapping(target = "discountValue", source = "discount.discountValue")
    @Mapping(target = "discountType", source = "discount.discountType")
    @Mapping(target = "rooms", ignore = true)
    @Mapping(target = "services", ignore = true)
    BookingResponseDTO toDTO(Booking booking);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "bookingDetails", ignore = true)
    @Mapping(target = "discount", ignore = true)
    @Mapping(target = "services", ignore = true)
    Booking toEntity(UpsertBookingRequest request);
} 