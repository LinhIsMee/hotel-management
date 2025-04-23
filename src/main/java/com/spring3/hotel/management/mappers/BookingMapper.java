package com.spring3.hotel.management.mappers;

import com.spring3.hotel.management.dto.request.UpsertBookingRequest;
import com.spring3.hotel.management.dto.response.BookingResponseDTO;
import com.spring3.hotel.management.dto.response.NewBookingResponse;
import com.spring3.hotel.management.dto.response.RoomListResponseDTO;
import com.spring3.hotel.management.dto.response.ServiceResponseDTO;
import com.spring3.hotel.management.models.Booking;
import com.spring3.hotel.management.models.BookingDetail;
import com.spring3.hotel.management.models.Room;
import com.spring3.hotel.management.models.HotelService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    
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
    
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "fullName", source = "user.fullName")
    @Mapping(target = "userAvatar", source = "user.avatar")
    @Mapping(target = "totalAmount", source = "finalPrice")
    @Mapping(target = "bookingDate", source = "createdAt")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "roomCount", expression = "java(booking.getBookingDetails().size())")
    NewBookingResponse toNewBookingResponse(Booking booking);
    
    @Mapping(target = "roomId", source = "room.id")
    @Mapping(target = "roomNumber", source = "room.roomNumber")
    @Mapping(target = "roomType", source = "room.roomType.name")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "images", source = "room.images")
    RoomListResponseDTO toRoomListResponseDTO(BookingDetail bookingDetail);
    
    @Named("formatDateTime")
    default String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    default ServiceResponseDTO toServiceResponseDTO(HotelService service) {
        if (service == null) return null;
        
        return ServiceResponseDTO.builder()
                .id(service.getId())
                .name(service.getName())
                .description(service.getDescription())
                .price(service.getPrice())
                .build();
    }
} 