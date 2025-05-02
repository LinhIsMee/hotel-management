package com.spring3.hotel.management.services.interfaces;

import com.spring3.hotel.management.dto.request.CreateReviewRequest;
import com.spring3.hotel.management.dto.request.ReplyReviewRequest;
import com.spring3.hotel.management.dto.request.UpdateReviewRequest;
import com.spring3.hotel.management.dto.response.ReviewResponseDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface ReviewService {
    
    List<ReviewResponseDTO> getAllReviews();
    
    Page<ReviewResponseDTO> getAllReviews(Pageable pageable);
    
    Page<ReviewResponseDTO> getPublicReviews(Pageable pageable);
    
    Page<ReviewResponseDTO> getPendingReviews(Pageable pageable);
    
    Page<ReviewResponseDTO> getRepliedReviews(Pageable pageable);
    
    Page<ReviewResponseDTO> getHiddenReviews(Pageable pageable);
    
    ReviewResponseDTO getReviewById(Integer id);
    
    ReviewResponseDTO getReviewByBookingId(String bookingId);
    
    List<ReviewResponseDTO> getReviewsByRoomNumber(String roomNumber);
    
    List<ReviewResponseDTO> getReviewsByRoomType(String roomType);
    
    List<ReviewResponseDTO> getFeaturedReviews();
    
    Page<ReviewResponseDTO> getReviewsByMinRating(Integer minRating, Pageable pageable);
    
    Page<ReviewResponseDTO> searchReviewsByGuestName(String guestName, Pageable pageable);
    
    ReviewResponseDTO createReview(CreateReviewRequest request);
    
    ReviewResponseDTO replyToReview(Integer id, ReplyReviewRequest request);
    
    ReviewResponseDTO updateReview(Integer id, UpdateReviewRequest request);
    
    ReviewResponseDTO updateReview(UpdateReviewRequest request, Integer id);
    
    ReviewResponseDTO deleteReview(Integer id);
    
    Map<String, Object> getReviewStatistics();
    
    void initReviewsFromJson();
} 