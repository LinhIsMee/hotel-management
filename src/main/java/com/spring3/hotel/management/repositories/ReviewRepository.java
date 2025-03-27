package com.spring3.hotel.management.repositories;

import com.spring3.hotel.management.models.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByRoomId(Integer roomId);

    @Query("SELECT COUNT(DISTINCT r) FROM Review r WHERE r.createdAt BETWEEN :startOfDay AND :endOfDay")
    Integer countDistinctRatesByDateRange(LocalDateTime startOfDay, LocalDateTime endOfDay);

    @Query("SELECT COUNT(r) FROM Review r")
    Integer countAllReviews();

}
