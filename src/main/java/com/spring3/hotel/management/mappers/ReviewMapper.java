package com.spring3.hotel.management.mappers;

import com.spring3.hotel.management.dto.response.ReviewResponseDTO;
import com.spring3.hotel.management.models.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "fullName", source = "user.fullName")
    @Mapping(target = "userAvatar", source = "user.avatar")
    @Mapping(target = "roomId", source = "room.id")
    @Mapping(target = "roomNumber", source = "room.roomNumber")
    @Mapping(target = "isActive", constant = "true")
    ReviewResponseDTO toDTO(Review review);
} 