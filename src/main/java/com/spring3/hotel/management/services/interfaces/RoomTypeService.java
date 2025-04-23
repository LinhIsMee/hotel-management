package com.spring3.hotel.management.services.interfaces;

import com.spring3.hotel.management.dto.request.UpsertRoomTypeRequest;
import com.spring3.hotel.management.dto.response.RoomTypeResponseDTO;

import java.util.List;

public interface RoomTypeService {
    
    List<RoomTypeResponseDTO> getAllRoomTypes();
    
    RoomTypeResponseDTO getRoomTypeById(Integer id);
    
    RoomTypeResponseDTO getRoomTypeByName(String name);
    
    RoomTypeResponseDTO createRoomType(UpsertRoomTypeRequest request);
    
    RoomTypeResponseDTO updateRoomType(UpsertRoomTypeRequest request, Integer id);
    
    void deleteRoomType(Integer id);
} 