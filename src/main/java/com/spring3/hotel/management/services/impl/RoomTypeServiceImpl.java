package com.spring3.hotel.management.services.impl;

import com.spring3.hotel.management.models.RoomType;
import com.spring3.hotel.management.repositories.RoomTypeRepository;
import com.spring3.hotel.management.services.RoomTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class RoomTypeServiceImpl implements RoomTypeService {

    @Autowired
    RoomTypeRepository roomTypeRepository;

    @Override
    public RoomType getRoomTypeById(Integer id) {
        return roomTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room type not found"));
    }

    @Override
    public RoomType createRoomType(RoomType roomType) {
        log.info("Creating room type: {}", roomType);
        return roomTypeRepository.save(roomType);
    }

    @Override
    public RoomType updateRoomType(RoomType roomType, Integer id) {
        log.info("Updating room type with ID: {}", id);
        RoomType existingRoomType = roomTypeRepository.findById(id)
            .orElseThrow(() -> {
                log.error("Room type not found with ID: {}", id);
                return new RuntimeException("Room type not found");
            });
        existingRoomType.setName(roomType.getName());
        existingRoomType.setDescription(roomType.getDescription());
        existingRoomType.setBasePrice(roomType.getBasePrice());
        existingRoomType.setCapacity(roomType.getCapacity());
        log.info("Room type updated: {}", existingRoomType);
        return roomTypeRepository.save(existingRoomType);
    }

    @Override
    public void deleteRoomType(Integer id) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room type not found"));
        roomTypeRepository.delete(roomType);
    }

    @Override
    public List<RoomType> getAllRoomTypes() {
        return roomTypeRepository.findAll();
    }
}
