package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dtos.request.UpsertRoomTypeRequest;
import com.spring3.hotel.management.dtos.response.RoomTypeResponseDTO;
import com.spring3.hotel.management.services.interfaces.RoomTypeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/room-types")
public class RoomTypeController {

    @Autowired
    private RoomTypeService roomTypeService;

    @GetMapping("/{id}")
    public ResponseEntity<RoomTypeResponseDTO> getRoomTypeById(@PathVariable Integer id) {
        RoomTypeResponseDTO roomType = roomTypeService.getRoomTypeById(id);
        return ResponseEntity.ok(roomType);
    }

    @PostMapping
    public ResponseEntity<RoomTypeResponseDTO> createRoomType(@Valid @RequestBody UpsertRoomTypeRequest request) {
        RoomTypeResponseDTO createdRoomType = roomTypeService.createRoomType(request);
        return new ResponseEntity<>(createdRoomType, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomTypeResponseDTO> updateRoomType(
        @PathVariable Integer id,
        @Valid @RequestBody UpsertRoomTypeRequest request) {
        RoomTypeResponseDTO updatedRoomType = roomTypeService.updateRoomType(request, id);
        return ResponseEntity.ok(updatedRoomType);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RoomTypeResponseDTO> deleteRoomType(@PathVariable Integer id) {
        RoomTypeResponseDTO deletedRoomType = roomTypeService.deleteRoomType(id);
        return ResponseEntity.ok(deletedRoomType);
    }

    @GetMapping
    public ResponseEntity<List<RoomTypeResponseDTO>> getAllRoomTypes() {
        List<RoomTypeResponseDTO> roomTypes = roomTypeService.getAllRoomTypes();
        return ResponseEntity.ok(roomTypes);
    }
}
