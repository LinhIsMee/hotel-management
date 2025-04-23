package com.spring3.hotel.management.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring3.hotel.management.dto.request.UpsertRoomTypeRequest;
import com.spring3.hotel.management.dto.response.RoomTypeResponseDTO;
import com.spring3.hotel.management.exceptions.ResourceNotFoundException;
import com.spring3.hotel.management.models.RoomType;
import com.spring3.hotel.management.repositories.RoomTypeRepository;
import com.spring3.hotel.management.services.interfaces.RoomTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoomTypeServiceImpl implements RoomTypeService {

    @Autowired
    private RoomTypeRepository roomTypeRepository;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public RoomTypeResponseDTO getRoomTypeById(Integer id) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phòng với ID: " + id));
        return mapToDTO(roomType);
    }

    @Override
    public RoomTypeResponseDTO createRoomType(UpsertRoomTypeRequest request) {
        // Kiểm tra xem tên loại phòng đã tồn tại chưa
        if (roomTypeRepository.findByNameIgnoreCase(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Tên loại phòng đã tồn tại: " + request.getName());
        }
        
        // Kiểm tra xem mã loại phòng đã tồn tại chưa
        if (roomTypeRepository.findByCodeIgnoreCase(request.getCode()).isPresent()) {
            throw new IllegalArgumentException("Mã loại phòng đã tồn tại: " + request.getCode());
        }
        
        RoomType roomType = mapToEntity(request);
        
        // Set các giá trị mặc định nếu cần
        if (roomType.getIsActive() == null) {
            roomType.setIsActive(true);
        }
        
        RoomType savedRoomType = roomTypeRepository.save(roomType);
        return mapToDTO(savedRoomType);
    }

    @Override
    public RoomTypeResponseDTO updateRoomType(UpsertRoomTypeRequest request, Integer id) {
        RoomType existingRoomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phòng với ID: " + id));
        
        // Kiểm tra nếu tên thay đổi và tên mới đã tồn tại
        if (!existingRoomType.getName().equalsIgnoreCase(request.getName()) &&
                roomTypeRepository.findByNameIgnoreCase(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Tên loại phòng đã tồn tại: " + request.getName());
        }
        
        // Kiểm tra nếu mã thay đổi và mã mới đã tồn tại
        if (!existingRoomType.getCode().equalsIgnoreCase(request.getCode()) &&
                roomTypeRepository.findByCodeIgnoreCase(request.getCode()).isPresent()) {
            throw new IllegalArgumentException("Mã loại phòng đã tồn tại: " + request.getCode());
        }
        
        // Cập nhật thông tin
        existingRoomType.setName(request.getName());
        existingRoomType.setCode(request.getCode());
        existingRoomType.setDescription(request.getDescription());
        existingRoomType.setPricePerNight(request.getPricePerNight());
        existingRoomType.setBasePrice(request.getPricePerNight()); // Cập nhật cả basePrice
        existingRoomType.setMaxOccupancy(request.getMaxOccupancy());
        existingRoomType.setCapacity(request.getMaxOccupancy()); // Cập nhật cả capacity
        
        // Cập nhật amenities
        if (request.getAmenities() != null && !request.getAmenities().isEmpty()) {
            existingRoomType.setAmenities(String.join(",", request.getAmenities()));
        }
        
        if (request.getImageUrl() != null) {
            existingRoomType.setImageUrl(request.getImageUrl());
        }
        
        if (request.getIsActive() != null) {
            existingRoomType.setIsActive(request.getIsActive());
        }
        
        RoomType updatedRoomType = roomTypeRepository.save(existingRoomType);
        return mapToDTO(updatedRoomType);
    }

    @Override
    public RoomTypeResponseDTO deleteRoomType(Integer id) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phòng với ID: " + id));
        
        // Thực hiện soft delete bằng cách đặt isActive = false
        roomType.setIsActive(false);
        RoomType deactivatedRoomType = roomTypeRepository.save(roomType);
        
        return mapToDTO(deactivatedRoomType);
    }

    @Override
    public List<RoomTypeResponseDTO> getAllRoomTypes() {
        return roomTypeRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void initRoomTypesFromJson() {
        try {
            log.info("Bắt đầu khởi tạo dữ liệu loại phòng từ file JSON...");
            
            // Kiểm tra xem đã có dữ liệu trong DB chưa
            if (roomTypeRepository.count() > 0) {
                log.info("Dữ liệu loại phòng đã tồn tại trong DB, bỏ qua việc khởi tạo.");
                return;
            }
            
            // Đọc file JSON
            File jsonFile = Paths.get("data", "room-types.json").toFile();
            if (!jsonFile.exists()) {
                log.warn("Không tìm thấy file dữ liệu loại phòng JSON: {}", jsonFile.getAbsolutePath());
                return;
            }
            
            JsonNode rootNode = objectMapper.readTree(jsonFile);
            JsonNode dataNode = rootNode.get("data");
            
            if (dataNode == null || !dataNode.isArray() || dataNode.isEmpty()) {
                log.warn("Không có dữ liệu hợp lệ trong file JSON.");
                return;
            }
            
            List<RoomType> roomTypes = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            for (JsonNode node : dataNode) {
                String amenitiesStr = null;
                if (node.has("amenities") && node.get("amenities").isArray()) {
                    List<String> amenitiesList = objectMapper.convertValue(
                            node.get("amenities"),
                            new TypeReference<List<String>>() {}
                    );
                    amenitiesStr = String.join(",", amenitiesList);
                }
                
                LocalDate createdAt = null;
                if (node.has("createdAt") && !node.get("createdAt").isNull()) {
                    createdAt = LocalDate.parse(node.get("createdAt").asText(), formatter);
                }
                
                RoomType roomType = RoomType.builder()
                        .name(node.get("name").asText())
                        .code(node.get("code").asText())
                        .description(node.has("description") ? node.get("description").asText() : null)
                        .pricePerNight(node.get("pricePerNight").asDouble())
                        .basePrice(node.get("pricePerNight").asDouble())
                        .maxOccupancy(node.get("maxOccupancy").asInt())
                        .capacity(node.get("maxOccupancy").asInt())
                        .amenities(amenitiesStr)
                        .imageUrl(node.has("imageUrl") ? node.get("imageUrl").asText() : null)
                        .isActive(node.has("isActive") ? node.get("isActive").asBoolean() : true)
                        .createdAt(createdAt)
                        .build();
                
                roomTypes.add(roomType);
            }
            
            roomTypeRepository.saveAll(roomTypes);
            log.info("Đã khởi tạo thành công {} loại phòng từ file JSON.", roomTypes.size());
            
        } catch (IOException e) {
            log.error("Lỗi khi đọc file JSON loại phòng: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Lỗi không xác định khi khởi tạo dữ liệu loại phòng: {}", e.getMessage(), e);
        }
    }

    // Phương thức mới để map RoomType sang RoomTypeResponseDTO
    private RoomTypeResponseDTO mapToDTO(RoomType roomType) {
        if (roomType == null) return null;
        
        RoomTypeResponseDTO dto = new RoomTypeResponseDTO();
        dto.setId(roomType.getId());
        dto.setName(roomType.getName());
        dto.setCode(roomType.getCode());
        dto.setDescription(roomType.getDescription());
        dto.setPricePerNight(roomType.getPricePerNight());
        dto.setMaxOccupancy(roomType.getMaxOccupancy());
        dto.setImageUrl(roomType.getImageUrl());
        dto.setIsActive(roomType.getIsActive());
        
        // Chuyển đổi chuỗi amenities thành danh sách
        if (roomType.getAmenities() != null && !roomType.getAmenities().isEmpty()) {
            dto.setAmenities(List.of(roomType.getAmenities().split(",")));
        }
        
        // Chuyển đổi LocalDate sang LocalDateTime nếu cần
        if (roomType.getCreatedAt() != null) {
            dto.setCreatedAt(roomType.getCreatedAt().atStartOfDay());
        }
        
        return dto;
    }
    
    // Phương thức mới để map UpsertRoomTypeRequest sang RoomType
    private RoomType mapToEntity(UpsertRoomTypeRequest request) {
        if (request == null) return null;
        
        RoomType roomType = new RoomType();
        roomType.setName(request.getName());
        roomType.setCode(request.getCode());
        roomType.setDescription(request.getDescription());
        roomType.setPricePerNight(request.getPricePerNight());
        roomType.setBasePrice(request.getPricePerNight()); // Set basePrice = pricePerNight
        roomType.setMaxOccupancy(request.getMaxOccupancy());
        roomType.setCapacity(request.getMaxOccupancy()); // Set capacity = maxOccupancy
        roomType.setImageUrl(request.getImageUrl());
        roomType.setIsActive(request.getIsActive());
        
        // Chuyển danh sách amenities thành chuỗi ngăn cách bởi dấu phẩy
        if (request.getAmenities() != null && !request.getAmenities().isEmpty()) {
            roomType.setAmenities(String.join(",", request.getAmenities()));
        }
        
        // Set createdAt về ngày hiện tại
        roomType.setCreatedAt(LocalDate.now());
        
        return roomType;
    }
}
