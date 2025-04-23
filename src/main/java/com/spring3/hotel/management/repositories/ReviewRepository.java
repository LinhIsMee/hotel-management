package com.spring3.hotel.management.repositories;

import com.spring3.hotel.management.models.Review;
import com.spring3.hotel.management.models.Review.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    @Query("SELECT r FROM Review r WHERE CAST(r.roomNumber AS integer) = :roomId")
    List<Review> findByRoomId(Integer roomId);

    @Query("SELECT COUNT(DISTINCT r) FROM Review r WHERE r.createdAt BETWEEN :startOfDay AND :endOfDay")
    Integer countDistinctRatesByDateRange(LocalDateTime startOfDay, LocalDateTime endOfDay);

    @Query("SELECT COUNT(r) FROM Review r")
    Integer countAllReviews();

    Optional<Review> findByBookingId(String bookingId);
    
    List<Review> findByRoomNumber(String roomNumber);
    
    List<Review> findByRoomType(String roomType);
    
    List<Review> findByRating(Integer rating);
    
    Page<Review> findByStatus(ReviewStatus status, Pageable pageable);
    
    Page<Review> findByRatingGreaterThanEqual(Integer rating, Pageable pageable);
    
    Page<Review> findByGuestNameContainingIgnoreCase(String guestName, Pageable pageable);
    
    long countByStatus(ReviewStatus status);
    
    @Query("SELECT AVG(r.rating) FROM Review r")
    Double calculateAverageRating();
    
    List<Review> findByIsFeaturedTrue();
    
    Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    Page<Review> findByStatusNotOrderByCreatedAtDesc(ReviewStatus status, Pageable pageable);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.rating = :star")
    long countByRatingStar(int star);
    
    long countByRating(int rating);
}
