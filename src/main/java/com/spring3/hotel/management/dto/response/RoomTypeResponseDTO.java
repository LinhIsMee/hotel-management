package com.spring3.hotel.management.dto.response;

import com.spring3.hotel.management.models.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static RoomTypeResponseDTO fromEntity(RoomType roomType) {
        if (roomType == null) {
            return null;
        }
        
        // Chuyển đổi chuỗi amenities thành danh sách
        List<String> amenitiesList = Collections.emptyList();
        if (roomType.getAmenities() != null && !roomType.getAmenities().isEmpty()) {
            amenitiesList = Arrays.asList(roomType.getAmenities().split(","));
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
                .createdAt(roomType.getCreatedAt() != null ? 
                        LocalDateTime.of(roomType.getCreatedAt(), java.time.LocalTime.MIDNIGHT) : null)
                .build();
    }
} 