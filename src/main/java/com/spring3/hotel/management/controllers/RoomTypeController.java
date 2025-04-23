package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dto.request.UpsertRoomTypeRequest;
import com.spring3.hotel.management.dto.response.RoomByTypeResponseDTO;
import com.spring3.hotel.management.dto.response.RoomResponseDTO;
import com.spring3.hotel.management.dto.response.RoomTypeResponseDTO;
import com.spring3.hotel.management.services.interfaces.RoomService;
import com.spring3.hotel.management.services.interfaces.RoomTypeService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/room-types")
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class RoomTypeController {

    @Autowired
    private RoomTypeService roomTypeService;
    
    @Autowired
    private RoomService roomService;

    /**
     * Lấy thông tin loại phòng theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoomTypeResponseDTO> getRoomTypeById(@PathVariable Integer id) {
        RoomTypeResponseDTO roomType = roomTypeService.getRoomTypeById(id);
        return ResponseEntity.ok(roomType);
    }
    
    /**
     * Lấy danh sách phòng theo loại phòng
     */
    @GetMapping("/{id}/rooms")
    public ResponseEntity<List<RoomByTypeResponseDTO>> getRoomsByRoomType(@PathVariable Integer id) {
        List<RoomByTypeResponseDTO> rooms = roomService.getRoomsByRoomType(id);
        return ResponseEntity.ok(rooms);
    }

    /**
     * Lấy danh sách tất cả loại phòng
     */
    @GetMapping
    public ResponseEntity<List<RoomTypeResponseDTO>> getAllRoomTypes() {
        List<RoomTypeResponseDTO> roomTypes = roomTypeService.getAllRoomTypes();
        return ResponseEntity.ok(roomTypes);
    }

    /**
     * Tạo mới loại phòng
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomTypeResponseDTO> createRoomType(@Valid @RequestBody UpsertRoomTypeRequest request) {
        RoomTypeResponseDTO createdRoomType = roomTypeService.createRoomType(request);
        return new ResponseEntity<>(createdRoomType, HttpStatus.CREATED);
    }

    /**
     * Cập nhật thông tin loại phòng
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomTypeResponseDTO> updateRoomType(
        @PathVariable Integer id,
        @Valid @RequestBody UpsertRoomTypeRequest request) {
        RoomTypeResponseDTO updatedRoomType = roomTypeService.updateRoomType(request, id);
        return ResponseEntity.ok(updatedRoomType);
    }

    /**
     * Xóa loại phòng
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomTypeResponseDTO> deleteRoomType(@PathVariable Integer id) {
        RoomTypeResponseDTO deletedRoomType = roomTypeService.deleteRoomType(id);
        return ResponseEntity.ok(deletedRoomType);
    }
}
