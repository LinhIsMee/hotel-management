package com.spring3.hotel.management.services.interfaces;

import com.spring3.hotel.management.dto.request.UpsertRoomRequest;
import com.spring3.hotel.management.dto.response.RoomResponseDTO;
import com.spring3.hotel.management.dto.response.RoomByTypeResponseDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface RoomService {
    
    List<RoomResponseDTO> getAllRooms();
    
    RoomResponseDTO getRoomById(Integer id);
    
    RoomResponseDTO getRoomByRoomNumber(String roomNumber);
    
    List<RoomByTypeResponseDTO> getRoomsByRoomType(Integer roomTypeId);
    
    List<RoomResponseDTO> getRoomsByStatus(String status);
    
    RoomResponseDTO createRoom(UpsertRoomRequest request);
    
    RoomResponseDTO updateRoom(UpsertRoomRequest request, Integer id);
    
    void deleteRoom(Integer id);
    
    List<RoomResponseDTO> getAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate);
    
    List<RoomResponseDTO> getAllActiveRooms();
    
    void updateRoomStatusBatch(Map<String, String> roomStatusMap);
    
    List<RoomResponseDTO> getFeaturedRooms();
} 