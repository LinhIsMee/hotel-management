package com.spring3.hotel.management.repositories;

import com.spring3.hotel.management.dtos.response.RoomResponseDTO;
import com.spring3.hotel.management.models.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {
    Room findByRoomNumber(String roomNumber);

    List<Room> findByRoomType_Id(Integer roomTypeId);

    List<Room> findByStatus(String status);
}
