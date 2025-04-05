package com.spring3.hotel.management.services.interfaces;

import com.spring3.hotel.management.dtos.request.UpsertRoomRequest;
import com.spring3.hotel.management.dtos.response.RoomResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface RoomService {
    
    List<RoomResponseDTO> getAllRooms();
    
    RoomResponseDTO getRoomById(Integer id);
    
    RoomResponseDTO getRoomByRoomNumber(String roomNumber);
    
    List<RoomResponseDTO> getRoomsByRoomType(Integer roomTypeId);
    
    List<RoomResponseDTO> getRoomsByStatus(String status);
    
    RoomResponseDTO createRoom(UpsertRoomRequest request);
    
    RoomResponseDTO updateRoom(UpsertRoomRequest request, Integer id);
    
    void deleteRoom(Integer id);
    
    void initRoomsFromJson();
    
    List<RoomResponseDTO> getAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate);
    
    List<RoomResponseDTO> getAllActiveRooms();
} 