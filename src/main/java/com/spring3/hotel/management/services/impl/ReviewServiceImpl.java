package com.spring3.hotel.management.services.impl;

import com.spring3.hotel.management.dtos.request.CreateReviewRequest;
import com.spring3.hotel.management.dtos.request.UpdateReviewRequest;
import com.spring3.hotel.management.dtos.response.ReviewResponseDTO;
import com.spring3.hotel.management.models.Review;
import com.spring3.hotel.management.models.Room;
import com.spring3.hotel.management.models.User;
import com.spring3.hotel.management.repositories.ReviewRepository;
import com.spring3.hotel.management.repositories.RoomRepository;
import com.spring3.hotel.management.repositories.UserRepository;
import com.spring3.hotel.management.services.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoomRepository roomRepository;

    @Override
    public ReviewResponseDTO getReviewById(Integer id) {
        return reviewRepository.findById(id)
                .map(this::convertToReviewResponseDTO)
                .orElseThrow(() -> new RuntimeException("Review not found"));
    }

    @Override
    public List<ReviewResponseDTO> getAllReviews() {
        return reviewRepository.findAll()
                .stream()
                .map(this::convertToReviewResponseDTO)
                .toList();
    }

    @Override
    public ReviewResponseDTO createReview(CreateReviewRequest request) {
        Review review = new Review();
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));
        review.setUser(user);
        review.setRoom(room);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        reviewRepository.save(review);
        return convertToReviewResponseDTO(review);
    }

    @Override
    public ReviewResponseDTO updateReview(UpdateReviewRequest request, Integer id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        reviewRepository.save(review);
        return convertToReviewResponseDTO(review);
    }

    @Override
    public ReviewResponseDTO deleteReview(Integer id) {
        return reviewRepository.findById(id)
                .map(review -> {
                    reviewRepository.delete(review);
                    return convertToReviewResponseDTO(review);
                })
                .orElseThrow(() -> new RuntimeException("Review not found"));
    }

    @Override
    public List<ReviewResponseDTO> getReviewsByRoomId(Integer roomId) {
        return reviewRepository.findByRoomId(roomId)
                .stream()
                .map(this::convertToReviewResponseDTO)
                .toList();
    }

    private ReviewResponseDTO convertToReviewResponseDTO(Review review) {
        ReviewResponseDTO reviewResponseDTO = new ReviewResponseDTO();
        reviewResponseDTO.setId(review.getId());
        reviewResponseDTO.setRating(review.getRating());
        reviewResponseDTO.setComment(review.getComment());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        reviewResponseDTO.setCreatedAt(review.getCreatedAt().format(formatter));
        reviewResponseDTO.setUserId(review.getUser().getId());
        reviewResponseDTO.setFullName(review.getUser().getFullName());
        reviewResponseDTO.setRoomId(review.getRoom().getId());
        return reviewResponseDTO;
    }
}
