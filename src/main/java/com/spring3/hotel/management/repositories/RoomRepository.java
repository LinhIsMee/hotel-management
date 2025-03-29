package com.spring3.hotel.management.repositories;

import com.spring3.hotel.management.models.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {
    
    Optional<Room> findByRoomNumber(String roomNumber);
    
    List<Room> findByRoomTypeId(Integer roomTypeId);
    
    List<Room> findByStatus(String status);
    
    List<Room> findByIsActive(Boolean isActive);
}
