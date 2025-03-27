package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dtos.request.CreateReviewRequest;
import com.spring3.hotel.management.dtos.request.UpdateReviewRequest;
import com.spring3.hotel.management.dtos.response.ReviewResponseDTO;
import com.spring3.hotel.management.services.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    // Lấy thông tin review bằng ID
    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponseDTO> getReviewById(@PathVariable Integer id) {
        ReviewResponseDTO review = reviewService.getReviewById(id);
        return ResponseEntity.ok(review);
    }

    // Lấy tất cả reviews
    @GetMapping("/")
    public ResponseEntity<List<ReviewResponseDTO>> getAllReviews() {
        List<ReviewResponseDTO> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }

    // Tạo mới một review
    @PostMapping("/create")
    public ResponseEntity<ReviewResponseDTO> createReview(@RequestBody CreateReviewRequest request) {
        ReviewResponseDTO newReview = reviewService.createReview(request);
        return new ResponseEntity<>(newReview, HttpStatus.CREATED);
    }

    // Cập nhật thông tin review
    @PutMapping("/update/{id}")
    public ResponseEntity<ReviewResponseDTO> updateReview(
            @PathVariable Integer id,
            @RequestBody UpdateReviewRequest request) {
        ReviewResponseDTO updatedReview = reviewService.updateReview(request, id);
        return ResponseEntity.ok(updatedReview);
    }

    // Xóa một review
    @DeleteMapping("/{id}")
    public ResponseEntity<ReviewResponseDTO> deleteReview(@PathVariable Integer id) {
        ReviewResponseDTO deletedReview = reviewService.deleteReview(id);
        return ResponseEntity.ok(deletedReview);
    }

    // Lấy tất cả reviews của một phòng (room) bằng roomId
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByRoomId(@PathVariable Integer roomId) {
        List<ReviewResponseDTO> reviews = reviewService.getReviewsByRoomId(roomId);
        return ResponseEntity.ok(reviews);
    }
}
