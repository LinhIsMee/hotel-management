package com.spring3.hotel.management.services;

import com.spring3.hotel.management.dto.request.AssignRoomRequest;
import com.spring3.hotel.management.dto.request.CreateRoomRequest;
import com.spring3.hotel.management.dto.request.UpdateRoomRequest;
import com.spring3.hotel.management.dto.request.UpsertRoomRequest;
import com.spring3.hotel.management.dto.response.RoomResponseDTO;
import com.spring3.hotel.management.dto.response.RoomByTypeResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface RoomService {
    
    List<RoomResponseDTO> getAllRooms();
    
    Page<RoomResponseDTO> getAllRooms(Pageable pageable);
    
    RoomResponseDTO getRoomById(Integer id);
    
    RoomResponseDTO getRoomByRoomNumber(String roomNumber);
    
    List<RoomByTypeResponseDTO> getRoomsByRoomType(Integer roomTypeId);
    
    List<RoomResponseDTO> getRoomsByStatus(String status);
    
    RoomResponseDTO createRoom(CreateRoomRequest request);
    
    RoomResponseDTO updateRoom(Integer id, UpdateRoomRequest request);
    
    RoomResponseDTO updateRoom(UpsertRoomRequest request, Integer id);
    
    void deleteRoom(Integer id);
    
    List<RoomResponseDTO> getAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate);
    
    List<RoomResponseDTO> getAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate, Integer roomTypeId, Integer guestCount);
    
    List<RoomResponseDTO> getAllActiveRooms();
    
    void updateRoomStatusBatch(Map<String, String> roomStatusMap);
    
    List<RoomResponseDTO> getFeaturedRooms();
    
    List<RoomResponseDTO> getRoomsByType(Integer roomTypeId);
    
    void assignRoom(AssignRoomRequest request);
} 