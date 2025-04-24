package com.spring3.hotel.management.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring3.hotel.management.dto.request.UpsertRoomTypeRequest;
import com.spring3.hotel.management.dto.response.RoomTypeResponseDTO;
import com.spring3.hotel.management.exceptions.ResourceNotFoundException;
import com.spring3.hotel.management.models.RoomType;
import com.spring3.hotel.management.repositories.RoomTypeRepository;
import com.spring3.hotel.management.services.RoomTypeService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    public RoomTypeResponseDTO getRoomTypeByName(String name) {
        RoomType roomType = roomTypeRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phòng với tên: " + name));
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
    public void deleteRoomType(Integer id) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phòng với ID: " + id));
        
        // Thực hiện soft delete bằng cách đặt isActive = false
        roomType.setIsActive(false);
        roomTypeRepository.save(roomType);
    }

    @Override
    public List<RoomTypeResponseDTO> getAllRoomTypes() {
        return roomTypeRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
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
