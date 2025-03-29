package com.spring3.hotel.management.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring3.hotel.management.dtos.request.UpsertRoomRequest;
import com.spring3.hotel.management.dtos.response.RoomResponseDTO;
import com.spring3.hotel.management.exceptions.ResourceNotFoundException;
import com.spring3.hotel.management.models.Room;
import com.spring3.hotel.management.models.RoomType;
import com.spring3.hotel.management.repositories.RoomRepository;
import com.spring3.hotel.management.repositories.RoomTypeRepository;
import com.spring3.hotel.management.services.interfaces.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomRepository roomRepository;
    
    @Autowired
    private RoomTypeRepository roomTypeRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public List<RoomResponseDTO> getAllRooms() {
        return roomRepository.findByIsActive(true)
                .stream()
                .map(RoomResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public RoomResponseDTO getRoomById(Integer id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng với ID: " + id));
        return RoomResponseDTO.fromEntity(room);
    }
    
    @Override
    public RoomResponseDTO getRoomByRoomNumber(String roomNumber) {
        Room room = roomRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng với số phòng: " + roomNumber));
        return RoomResponseDTO.fromEntity(room);
    }
    
    @Override
    public List<RoomResponseDTO> getRoomsByRoomType(Integer roomTypeId) {
        return roomRepository.findByRoomTypeId(roomTypeId)
                .stream()
                .map(RoomResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<RoomResponseDTO> getRoomsByStatus(String status) {
        return roomRepository.findByStatus(status)
                .stream()
                .map(RoomResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public RoomResponseDTO createRoom(UpsertRoomRequest request) {
        // Kiểm tra phòng đã tồn tại chưa
        if (roomRepository.findByRoomNumber(request.getRoomNumber()).isPresent()) {
            throw new IllegalArgumentException("Số phòng đã tồn tại: " + request.getRoomNumber());
        }
        
        // Lấy loại phòng
        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phòng với ID: " + request.getRoomTypeId()));
        
        // Tạo phòng mới
        Room room = Room.builder()
                .roomNumber(request.getRoomNumber())
                .roomType(roomType)
                .status(request.getStatus())
                .floor(request.getFloor())
                .isActive(request.getIsActive())
                .notes(request.getNotes())
                .build();
        
        Room savedRoom = roomRepository.save(room);
        return RoomResponseDTO.fromEntity(savedRoom);
    }
    
    @Override
    public RoomResponseDTO updateRoom(UpsertRoomRequest request, Integer id) {
        Room existingRoom = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng với ID: " + id));
        
        // Kiểm tra nếu số phòng thay đổi và số phòng mới đã tồn tại
        if (!existingRoom.getRoomNumber().equals(request.getRoomNumber()) &&
                roomRepository.findByRoomNumber(request.getRoomNumber()).isPresent()) {
            throw new IllegalArgumentException("Số phòng đã tồn tại: " + request.getRoomNumber());
        }
        
        // Lấy loại phòng
        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phòng với ID: " + request.getRoomTypeId()));
        
        // Cập nhật thông tin
        existingRoom.setRoomNumber(request.getRoomNumber());
        existingRoom.setRoomType(roomType);
        
        if (request.getStatus() != null) {
            existingRoom.setStatus(request.getStatus());
        }
        
        if (request.getFloor() != null) {
            existingRoom.setFloor(request.getFloor());
        }
        
        if (request.getIsActive() != null) {
            existingRoom.setIsActive(request.getIsActive());
        }
        
        if (request.getNotes() != null) {
            existingRoom.setNotes(request.getNotes());
        }
        
        Room updatedRoom = roomRepository.save(existingRoom);
        return RoomResponseDTO.fromEntity(updatedRoom);
    }
    
    @Override
    public void deleteRoom(Integer id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng với ID: " + id));
        
        // Hard delete
        roomRepository.delete(room);
    }
    
    @Override
    public void initRoomsFromJson() {
        try {
            log.info("Bắt đầu khởi tạo dữ liệu phòng từ file JSON...");
            
            // Kiểm tra xem đã có dữ liệu trong DB chưa
            if (roomRepository.count() > 0) {
                log.info("Dữ liệu phòng đã tồn tại trong DB, bỏ qua việc khởi tạo.");
                return;
            }
            
            // Đọc file JSON
            File jsonFile = Paths.get("data", "rooms.json").toFile();
            if (!jsonFile.exists()) {
                log.warn("Không tìm thấy file dữ liệu phòng JSON: {}", jsonFile.getAbsolutePath());
                return;
            }
            
            JsonNode rootNode = objectMapper.readTree(jsonFile);
            JsonNode dataNode = rootNode.get("data");
            
            if (dataNode == null || !dataNode.isArray() || dataNode.isEmpty()) {
                log.warn("Không có dữ liệu hợp lệ trong file JSON.");
                return;
            }
            
            List<Room> rooms = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            for (JsonNode node : dataNode) {
                // Lấy thông tin loại phòng
                Integer roomTypeId = node.get("roomTypeId").asInt();
                RoomType roomType = roomTypeRepository.findById(roomTypeId)
                        .orElse(null);
                
                if (roomType == null) {
                    log.warn("Không tìm thấy loại phòng ID: {} cho phòng: {}, bỏ qua.", 
                            roomTypeId, node.get("roomNumber").asText());
                    continue;
                }
                
                LocalDate createdAt = null;
                if (node.has("createdAt") && !node.get("createdAt").isNull()) {
                    createdAt = LocalDate.parse(node.get("createdAt").asText(), formatter);
                }
                
                // Tạo đối tượng phòng
                Room room = Room.builder()
                        .roomNumber(node.get("roomNumber").asText())
                        .roomType(roomType)
                        .status(node.get("status").asText())
                        .floor(node.has("floor") ? String.valueOf(node.get("floor").asInt()) : null)
                        .isActive(node.has("isActive") ? node.get("isActive").asBoolean() : true)
                        .notes(node.has("specialFeatures") && node.get("specialFeatures").isArray() ? 
                              String.join(", ", objectMapper.convertValue(node.get("specialFeatures"), String[].class)) : null)
                        .createdAt(createdAt)
                        .build();
                
                rooms.add(room);
            }
            
            roomRepository.saveAll(rooms);
            log.info("Đã khởi tạo thành công {} phòng từ file JSON.", rooms.size());
            
        } catch (Exception e) {
            log.error("Lỗi khi khởi tạo dữ liệu phòng từ file JSON: {}", e.getMessage(), e);
        }
    }
}
