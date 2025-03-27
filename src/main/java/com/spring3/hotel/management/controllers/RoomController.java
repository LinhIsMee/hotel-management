package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dtos.request.UpsertRoomRequest;
import com.spring3.hotel.management.dtos.response.RoomResponseDTO;
import com.spring3.hotel.management.services.RoomService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {
    @Autowired
    private RoomService roomService;

    // Lấy thông tin phòng bằng ID
    @GetMapping("/{id}")
    public ResponseEntity<RoomResponseDTO> getRoomById(@PathVariable Integer id) {
        RoomResponseDTO roomResponseDTO = roomService.getRoomById(id);
        return ResponseEntity.ok(roomResponseDTO);
    }

    // Lấy thông tin phòng bằng số phòng
    @GetMapping("/room-number/{roomNumber}")
    public ResponseEntity<RoomResponseDTO> getRoomByRoomNumber(@PathVariable String roomNumber) {
        RoomResponseDTO roomResponseDTO = roomService.getRoomByRoomNumber(roomNumber);
        return ResponseEntity.ok(roomResponseDTO);
    }

    // Tạo mới phòng
    @PostMapping
    public ResponseEntity<RoomResponseDTO> createRoom(@Valid @RequestBody UpsertRoomRequest request) {
        RoomResponseDTO roomResponseDTO = roomService.createRoom(request);
        return new ResponseEntity<>(roomResponseDTO, HttpStatus.CREATED);
    }

    // Cập nhật thông tin phòng
    @PutMapping("/{id}")
    public ResponseEntity<RoomResponseDTO> updateRoom(
        @PathVariable Integer id,
        @Valid @RequestBody UpsertRoomRequest request) {
        RoomResponseDTO roomResponseDTO = roomService.updateRoom(request, id);
        return ResponseEntity.ok(roomResponseDTO);
    }

    // Xóa phòng
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Integer id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    // Lấy danh sách tất cả phòng
    @GetMapping
    public ResponseEntity<List<RoomResponseDTO>> getAllRooms() {
        List<RoomResponseDTO> rooms = roomService.getAllRooms();
        return ResponseEntity.ok(rooms);
    }

    // Lấy danh sách phòng theo loại phòng
    @GetMapping("/room-type/{roomTypeId}")
    public ResponseEntity<List<RoomResponseDTO>> getRoomsByRoomType(@PathVariable Integer roomTypeId) {
        List<RoomResponseDTO> rooms = roomService.getRoomsByRoomType(roomTypeId);
        return ResponseEntity.ok(rooms);
    }

    // Lấy danh sách phòng theo trạng thái
    @GetMapping("/status/{status}")
    public ResponseEntity<List<RoomResponseDTO>> getRoomsByStatus(@PathVariable String status) {
        List<RoomResponseDTO> rooms = roomService.getRoomsByStatus(status);
        return ResponseEntity.ok(rooms);
    }
}
