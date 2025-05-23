package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dto.request.CreateReviewRequest;
import com.spring3.hotel.management.dto.request.ReplyReviewRequest;
import com.spring3.hotel.management.dto.request.UpdateReviewRequest;
import com.spring3.hotel.management.dto.response.ReviewResponseDTO;
import com.spring3.hotel.management.services.interfaces.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reviews")
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
    @PostMapping("/")
    public ResponseEntity<ReviewResponseDTO> createReview(@RequestBody CreateReviewRequest request) {
        ReviewResponseDTO newReview = reviewService.createReview(request);
        return new ResponseEntity<>(newReview, HttpStatus.CREATED);
    }

    // Cập nhật thông tin review
    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponseDTO> updateReview(
            @PathVariable Integer id,
            @RequestBody UpdateReviewRequest request) {
        ReviewResponseDTO updatedReview = reviewService.updateReview(request, id);
        return ResponseEntity.ok(updatedReview);
    }

    // Xóa một review
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Integer id) {
        try {
            ReviewResponseDTO deletedReview = reviewService.deleteReview(id);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Xóa đánh giá thành công",
                "reviewId", id
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "error",
                    "message", "Không thể xóa đánh giá: " + e.getMessage()
                ));
        }
    }

    // Lấy tất cả reviews của một phòng (room) bằng roomNumber
    @GetMapping("/room/number/{roomNumber}")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByRoomNumber(@PathVariable String roomNumber) {
        List<ReviewResponseDTO> reviews = reviewService.getReviewsByRoomNumber(roomNumber);
        return ResponseEntity.ok(reviews);
    }
    
    // Phản hồi review
    @PostMapping("/{id}/reply")
    public ResponseEntity<ReviewResponseDTO> replyToReview(
            @PathVariable Integer id,
            @RequestBody ReplyReviewRequest request) {
        ReviewResponseDTO updatedReview = reviewService.replyToReview(id, request);
        return ResponseEntity.ok(updatedReview);
    }
    
    // Lấy thống kê đánh giá
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getReviewStatistics() {
        Map<String, Object> statistics = reviewService.getReviewStatistics();
        return ResponseEntity.ok(statistics);
    }
}
