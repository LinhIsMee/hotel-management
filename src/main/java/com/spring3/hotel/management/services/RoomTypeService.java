package com.spring3.hotel.management.services;

import com.spring3.hotel.management.models.RoomType;

import java.util.List;

public interface RoomTypeService {
    RoomType getRoomTypeById(Integer id);
    RoomType createRoomType(RoomType roomType);
    RoomType updateRoomType(RoomType roomType, Integer id);
    void deleteRoomType(Integer id);
    List<RoomType> getAllRoomTypes();
}
