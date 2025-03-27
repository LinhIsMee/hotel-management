package com.spring3.hotel.management.services;

import com.spring3.hotel.management.dtos.request.UpsertRoomRequest;
import com.spring3.hotel.management.dtos.response.RoomResponseDTO;

import java.util.List;

public interface RoomService {
    RoomResponseDTO getRoomById(Integer id);
    RoomResponseDTO getRoomByRoomNumber(String roomNumber);
    RoomResponseDTO createRoom(UpsertRoomRequest request);
    RoomResponseDTO updateRoom(UpsertRoomRequest request, Integer id);
    void deleteRoom(Integer id);
    List<RoomResponseDTO> getAllRooms();
    List<RoomResponseDTO> getRoomsByRoomType(Integer roomTypeId);
    List<RoomResponseDTO> getRoomsByStatus(String status);
}
