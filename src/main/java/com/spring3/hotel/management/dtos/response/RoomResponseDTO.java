package com.spring3.hotel.management.dtos.response;

import com.spring3.hotel.management.models.Room;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponseDTO {
    private Integer id;
    private String roomNumber;
    private Integer roomTypeId;
    private String roomTypeName;
    private String status;
    private String floor;
    private Boolean isActive;
    private String notes;
    private String createdAt;
    private String updatedAt;
    private Double pricePerNight;
    private List<String> images;
    private List<ServiceResponseDTO> services;
    
    public static RoomResponseDTO fromEntity(Room room) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        List<ServiceResponseDTO> serviceResponseDTOs = null;
        if (room.getServices() != null) {
            serviceResponseDTOs = room.getServices().stream()
                    .map(ServiceResponseDTO::fromEntity)
                    .collect(Collectors.toList());
        }
        
        return RoomResponseDTO.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .roomTypeId(room.getRoomType().getId())
                .roomTypeName(room.getRoomType().getName())
                .status(room.getStatus())
                .floor(room.getFloor())
                .isActive(room.getIsActive())
                .notes(room.getNotes())
                .createdAt(room.getCreatedAt() != null ? room.getCreatedAt().format(formatter) : null)
                .updatedAt(room.getUpdatedAt() != null ? room.getUpdatedAt().format(formatter) : null)
                .pricePerNight(room.getRoomType().getBasePrice())
                .images(room.getImages())
                .services(serviceResponseDTOs)
                .build();
    }
}
