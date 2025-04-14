package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dtos.request.UpsertRoomTypeRequest;
import com.spring3.hotel.management.dtos.response.RoomTypeResponseDTO;
import com.spring3.hotel.management.services.interfaces.RoomTypeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/room-types")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRoomTypeController {

    @Autowired
    private RoomTypeService roomTypeService;

    /**
     * Lấy thông tin loại phòng theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoomTypeResponseDTO> getRoomTypeById(@PathVariable Integer id) {
        RoomTypeResponseDTO roomType = roomTypeService.getRoomTypeById(id);
        return ResponseEntity.ok(roomType);
    }

    /**
     * Tạo mới loại phòng
     */
    @PostMapping
    public ResponseEntity<RoomTypeResponseDTO> createRoomType(@Valid @RequestBody UpsertRoomTypeRequest request) {
        RoomTypeResponseDTO createdRoomType = roomTypeService.createRoomType(request);
        return new ResponseEntity<>(createdRoomType, HttpStatus.CREATED);
    }

    // Cập nhật thông tin loại phòng
    @PutMapping("/{id}")
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
    public ResponseEntity<RoomTypeResponseDTO> deleteRoomType(@PathVariable Integer id) {
        RoomTypeResponseDTO deletedRoomType = roomTypeService.deleteRoomType(id);
        return ResponseEntity.ok(deletedRoomType);
    }

    /**
     * Lấy danh sách tất cả loại phòng
     */
    @GetMapping
    public ResponseEntity<List<RoomTypeResponseDTO>> getAllRoomTypes() {
        List<RoomTypeResponseDTO> roomTypes = roomTypeService.getAllRoomTypes();
        return ResponseEntity.ok(roomTypes);
    }
} 