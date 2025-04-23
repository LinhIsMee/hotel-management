package com.spring3.hotel.management.mappers;

import com.spring3.hotel.management.dto.request.UpsertRoomTypeRequest;
import com.spring3.hotel.management.dto.response.RoomTypeResponseDTO;
import com.spring3.hotel.management.models.RoomType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface RoomTypeMapper {
    
    @Mapping(target = "amenities", source = "amenities", qualifiedByName = "stringToList")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "localDateToDateTime")
    RoomTypeResponseDTO toDTO(RoomType roomType);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "basePrice", source = "pricePerNight")
    @Mapping(target = "capacity", source = "maxOccupancy")
    @Mapping(target = "amenities", source = "amenities", qualifiedByName = "listToString")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDate.now())")
    @Mapping(target = "updatedAt", ignore = true)
    RoomType toEntity(UpsertRoomTypeRequest request);
    
    @Named("stringToList")
    default List<String> stringToList(String value) {
        if (value == null || value.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(value.split(","));
    }
    
    @Named("listToString")
    default String listToString(List<String> value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return String.join(",", value);
    }
    
    @Named("localDateToDateTime")
    default LocalDateTime localDateToDateTime(LocalDate date) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.of(date, LocalTime.MIDNIGHT);
    }
} 