package com.spring3.hotel.management.services;

import com.spring3.hotel.management.dtos.request.CreateReviewRequest;
import com.spring3.hotel.management.dtos.request.UpdateReviewRequest;
import com.spring3.hotel.management.dtos.response.ReviewResponseDTO;

import java.util.List;

public interface ReviewService {
    ReviewResponseDTO getReviewById(Integer id);
    List<ReviewResponseDTO> getAllReviews();
    ReviewResponseDTO createReview(CreateReviewRequest request);
    ReviewResponseDTO updateReview(UpdateReviewRequest request, Integer id);
    ReviewResponseDTO deleteReview(Integer id);
    List<ReviewResponseDTO> getReviewsByRoomId(Integer roomId);
}
