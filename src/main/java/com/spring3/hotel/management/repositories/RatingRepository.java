package com.spring3.hotel.management.repositories;

import com.spring3.hotel.management.models.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByRoomId(Integer roomId);
    List<Rating> findByUserId(Long userId);
} 