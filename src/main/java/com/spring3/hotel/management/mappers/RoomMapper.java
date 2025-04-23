package com.spring3.hotel.management.mappers;

import com.spring3.hotel.management.dto.response.RoomResponseDTO;
import com.spring3.hotel.management.models.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoomMapper {
    
    @Mapping(target = "roomTypeName", source = "roomType.name")
    @Mapping(target = "roomTypeId", source = "roomType.id")
    @Mapping(target = "services", source = "services")
    @Mapping(target = "pricePerNight", source = "roomType.pricePerNight")
    @Mapping(target = "images", source = "images")
    @Mapping(target = "totalReviews", expression = "java(room.getRatings() != null ? room.getRatings().size() : 0)")
    @Mapping(target = "maxOccupancy", source = "roomType.maxOccupancy")
    @Mapping(target = "recentReviews", ignore = true)
    @Mapping(target = "isBookedNextFiveDays", ignore = true)
    @Mapping(target = "bookingPeriods", ignore = true)
    RoomResponseDTO toDTO(Room room);
} 