package com.spring3.hotel.management.services.interfaces;

import com.spring3.hotel.management.dtos.request.CreateRatingRequest;
import com.spring3.hotel.management.dtos.request.ReplyRatingRequest;
import com.spring3.hotel.management.dtos.response.RatingResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface RatingService {
    
    Page<RatingResponseDTO> getAllRatings(Pageable pageable);
    
    Page<RatingResponseDTO> getPendingRatings(Pageable pageable);
    
    Page<RatingResponseDTO> getRepliedRatings(Pageable pageable);
    
    RatingResponseDTO getRatingById(Integer id);
    
    RatingResponseDTO getRatingByBookingId(String bookingId);
    
    List<RatingResponseDTO> getRatingsByRoomNumber(String roomNumber);
    
    List<RatingResponseDTO> getRatingsByRoomType(String roomType);
    
    Page<RatingResponseDTO> getRatingsByMinRating(Integer minRating, Pageable pageable);
    
    Page<RatingResponseDTO> searchRatingsByGuestName(String guestName, Pageable pageable);
    
    RatingResponseDTO createRating(CreateRatingRequest request);
    
    RatingResponseDTO replyToRating(Integer id, ReplyRatingRequest request);
    
    void deleteRating(Integer id);
    
    Map<String, Object> getRatingStatistics();
    
    void initRatingsFromJson();
} 