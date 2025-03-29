package com.spring3.hotel.management.services.impl;

import com.spring3.hotel.management.exceptions.ResourceNotFoundException;
import com.spring3.hotel.management.models.RoomType;
import com.spring3.hotel.management.repositories.RoomTypeRepository;
import com.spring3.hotel.management.services.interfaces.RoomTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class RoomTypeServiceImpl implements RoomTypeService {

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Override
    public RoomType getRoomTypeById(Integer id) {
        return roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phòng với ID: " + id));
    }

    @Override
    public RoomType createRoomType(RoomType roomType) {
        // Kiểm tra xem tên loại phòng đã tồn tại chưa
        if (roomTypeRepository.findByNameIgnoreCase(roomType.getName()).isPresent()) {
            throw new IllegalArgumentException("Tên loại phòng đã tồn tại: " + roomType.getName());
        }
        
        return roomTypeRepository.save(roomType);
    }

    @Override
    public RoomType updateRoomType(RoomType roomType, Integer id) {
        RoomType existingRoomType = getRoomTypeById(id);
        
        // Kiểm tra nếu tên thay đổi và tên mới đã tồn tại
        if (!existingRoomType.getName().equalsIgnoreCase(roomType.getName()) &&
                roomTypeRepository.findByNameIgnoreCase(roomType.getName()).isPresent()) {
            throw new IllegalArgumentException("Tên loại phòng đã tồn tại: " + roomType.getName());
        }
        
        existingRoomType.setName(roomType.getName());
        existingRoomType.setDescription(roomType.getDescription());
        existingRoomType.setBasePrice(roomType.getBasePrice());
        existingRoomType.setCapacity(roomType.getCapacity());
        
        return roomTypeRepository.save(existingRoomType);
    }

    @Override
    public void deleteRoomType(Integer id) {
        RoomType roomType = getRoomTypeById(id);
        roomTypeRepository.delete(roomType);
    }

    @Override
    public List<RoomType> getAllRoomTypes() {
        return roomTypeRepository.findAll();
    }
}
