package com.spring3.hotel.management.dto.response;

import com.spring3.hotel.management.models.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomTypeResponseDTO {
    private Integer id;
    private String name;
    private String code;
    private String description;
    private Double pricePerNight;
    private Integer maxOccupancy;
    private List<String> amenities;
    private String imageUrl;
    private Boolean isActive;
    private String createdAt;
    
    public static RoomTypeResponseDTO fromEntity(RoomType roomType) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        List<String> amenitiesList = null;
        if (roomType.getAmenities() != null && !roomType.getAmenities().isEmpty()) {
            amenitiesList = Arrays.stream(roomType.getAmenities().split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
        }
        
        return RoomTypeResponseDTO.builder()
                .id(roomType.getId())
                .name(roomType.getName())
                .code(roomType.getCode())
                .description(roomType.getDescription())
                .pricePerNight(roomType.getPricePerNight())
                .maxOccupancy(roomType.getMaxOccupancy())
                .amenities(amenitiesList)
                .imageUrl(roomType.getImageUrl())
                .isActive(roomType.getIsActive())
                .createdAt(roomType.getCreatedAt() != null ? roomType.getCreatedAt().format(formatter) : null)
                .build();
    }
} 