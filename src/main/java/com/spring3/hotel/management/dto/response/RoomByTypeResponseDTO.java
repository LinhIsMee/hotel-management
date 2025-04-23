package com.spring3.hotel.management.dto.response;

import com.spring3.hotel.management.models.Room;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomByTypeResponseDTO {
    private Integer id;
    private String roomNumber;
    private String status;
    private String floor;
    private Boolean isActive;
    private String notes;
    private List<String> images;
    
    public static RoomByTypeResponseDTO fromEntity(Room room) {
        return RoomByTypeResponseDTO.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .status(room.getStatus())
                .floor(room.getFloor())
                .isActive(room.getIsActive())
                .notes(room.getNotes())
                .images(room.getImages())
                .build();
    }
} 