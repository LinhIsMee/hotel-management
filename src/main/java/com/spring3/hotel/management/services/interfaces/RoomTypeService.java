package com.spring3.hotel.management.services.interfaces;

import com.spring3.hotel.management.dtos.request.UpsertRoomTypeRequest;
import com.spring3.hotel.management.dtos.response.RoomTypeResponseDTO;
import com.spring3.hotel.management.models.RoomType;

import java.util.List;

public interface RoomTypeService {
    
    RoomTypeResponseDTO getRoomTypeById(Integer id);
    
    RoomTypeResponseDTO createRoomType(UpsertRoomTypeRequest request);
    
    RoomTypeResponseDTO updateRoomType(UpsertRoomTypeRequest request, Integer id);
    
    RoomTypeResponseDTO deleteRoomType(Integer id);
    
    List<RoomTypeResponseDTO> getAllRoomTypes();
    
    void initRoomTypesFromJson();
} 