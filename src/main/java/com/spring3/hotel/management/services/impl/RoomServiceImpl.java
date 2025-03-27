package com.spring3.hotel.management.services.impl;

import com.spring3.hotel.management.dtos.request.UpsertRoomRequest;
import com.spring3.hotel.management.dtos.response.RoomResponseDTO;
import com.spring3.hotel.management.exceptions.NotFoundException;
import com.spring3.hotel.management.models.Room;
import com.spring3.hotel.management.models.RoomType;
import com.spring3.hotel.management.repositories.RoomRepository;
import com.spring3.hotel.management.repositories.RoomTypeRepository;
import com.spring3.hotel.management.services.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class RoomServiceImpl implements RoomService {

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    RoomTypeRepository roomTypeRepository;

    @Override
    public RoomResponseDTO getRoomById(Integer id) {
        return roomRepository.findById(id)
                .map(this::convertToRoomResponseDTO)
                .orElseThrow(() -> new RuntimeException("Room not found"));

    }

    @Override
    public RoomResponseDTO getRoomByRoomNumber(String roomNumber) {
        Room room = roomRepository.findByRoomNumber(roomNumber);
        if (room == null) {
            throw new NotFoundException("Room not found with room number: " + roomNumber);
        }
        return convertToRoomResponseDTO(room);
    }

    @Override
    public RoomResponseDTO createRoom(UpsertRoomRequest request) {
        Room room = new Room();
        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new NotFoundException("Room type not found with ID: " + request.getRoomTypeId()));
        room.setRoomNumber(request.getRoomNumber());
        room.setRoomType(roomType);
        room.setStatus(request.getStatus());
        room.setDescription(request.getDescription());
        Room createdRoom = roomRepository.save(room);
        return convertToRoomResponseDTO(createdRoom);
    }

    @Override
    public RoomResponseDTO updateRoom(UpsertRoomRequest request, Integer id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room not found with ID: " + id));
        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new NotFoundException("Room type not found with ID: " + request.getRoomTypeId()));
        room.setRoomNumber(request.getRoomNumber());
        room.setRoomType(roomType);
        room.setStatus(request.getStatus());
        room.setDescription(request.getDescription());
        Room updatedRoom = roomRepository.save(room);
        return convertToRoomResponseDTO(updatedRoom);
    }

    @Override
    public void deleteRoom(Integer id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room not found with ID: " + id));
        roomRepository.delete(room);
    }

    @Override
    public List<RoomResponseDTO> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(this::convertToRoomResponseDTO)
                .toList();
    }

    @Override
    public List<RoomResponseDTO> getRoomsByRoomType(Integer roomTypeId) {
        return roomRepository.findByRoomType_Id(roomTypeId).stream()
                .map(this::convertToRoomResponseDTO)
                .toList();
    }

    @Override
    public List<RoomResponseDTO> getRoomsByStatus(String status) {
        return roomRepository.findByStatus(status).stream()
                .map(this::convertToRoomResponseDTO)
                .toList();
    }

    private RoomResponseDTO convertToRoomResponseDTO(Room room) {
        RoomResponseDTO roomResponseDTO = new RoomResponseDTO();
        roomResponseDTO.setId(room.getId());
        roomResponseDTO.setRoomNumber(room.getRoomNumber());
        roomResponseDTO.setRoomTypeId(room.getRoomType().getId());
        roomResponseDTO.setRoomTypeName(room.getRoomType().getName());
        roomResponseDTO.setStatus(room.getStatus());
        roomResponseDTO.setDescription(room.getDescription());
        return roomResponseDTO;
    }
}
