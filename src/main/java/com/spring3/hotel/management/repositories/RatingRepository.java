package com.spring3.hotel.management.repositories;

import com.spring3.hotel.management.models.Rating;
import com.spring3.hotel.management.models.Rating.RatingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Integer> {
    
    Optional<Rating> findByBookingId(String bookingId);
    
    List<Rating> findByRoomNumber(String roomNumber);
    
    List<Rating> findByRoomType(String roomType);
    
    List<Rating> findByRating(Integer rating);
    
    Page<Rating> findByStatus(RatingStatus status, Pageable pageable);
    
    Page<Rating> findByRatingGreaterThanEqual(Integer rating, Pageable pageable);
    
    Page<Rating> findByGuestNameContainingIgnoreCase(String guestName, Pageable pageable);
    
    long countByStatus(RatingStatus status);
    
    @Query("SELECT AVG(r.rating) FROM Rating r")
    Double calculateAverageRating();
} 