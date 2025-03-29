package com.spring3.hotel.management.repositories;

import com.spring3.hotel.management.models.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, Integer> {
    Optional<RoomType> findByNameIgnoreCase(String name);
}
