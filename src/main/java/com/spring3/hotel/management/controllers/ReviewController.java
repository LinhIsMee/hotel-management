package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dto.request.CreateRatingRequest;
import com.spring3.hotel.management.dto.response.RatingDTO;
import com.spring3.hotel.management.dto.request.CreateReviewRequest;
import com.spring3.hotel.management.dto.request.ReplyReviewRequest;
import com.spring3.hotel.management.dto.request.UpdateReviewRequest;
import com.spring3.hotel.management.dto.response.ApiResponse;
import com.spring3.hotel.management.dto.response.MessageResponse;
import com.spring3.hotel.management.dto.response.ReviewResponseDTO;
import com.spring3.hotel.management.models.Rating;
import com.spring3.hotel.management.models.Room;
import com.spring3.hotel.management.models.User;
import com.spring3.hotel.management.repositories.RatingRepository;
import com.spring3.hotel.management.repositories.RoomRepository;
import com.spring3.hotel.management.repositories.UserRepository;
import com.spring3.hotel.management.services.ReviewService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller quản lý tất cả các đánh giá và xếp hạng
 * Gộp chức năng của ReviewController và RatingController
 */
@RestController
@RequestMapping("/api/v1/reviews")
@Slf4j
public class ReviewController {

    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    // === REVIEW APIs ===

    /**
     * Lấy thông tin đánh giá theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponseDTO> getReviewById(@PathVariable Integer id) {
        ReviewResponseDTO review = reviewService.getReviewById(id);
        return ResponseEntity.ok(review);
    }

    /**
     * Lấy tất cả đánh giá với hỗ trợ phân trang
     */
    @GetMapping
    public ResponseEntity<?> getAllReviews(Pageable pageable) {
        if (pageable != null) {
            Page<ReviewResponseDTO> reviewsPage = reviewService.getAllReviews(pageable);
            return ResponseEntity.ok(reviewsPage);
        } else {
            List<ReviewResponseDTO> reviews = reviewService.getAllReviews();
            return ResponseEntity.ok(reviews);
        }
    }

    /**
     * Tạo mới đánh giá
     */
    @PostMapping
    public ResponseEntity<ReviewResponseDTO> createReview(@Valid @RequestBody CreateReviewRequest request) {
        ReviewResponseDTO newReview = reviewService.createReview(request);
        return new ResponseEntity<>(newReview, HttpStatus.CREATED);
    }

    /**
     * Cập nhật thông tin đánh giá
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponseDTO> updateReview(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateReviewRequest request) {
        ReviewResponseDTO updatedReview = reviewService.updateReview(request, id);
        return ResponseEntity.ok(updatedReview);
    }

    /**
     * Xóa đánh giá
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Integer id) {
        try {
            reviewService.deleteReview(id);
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

    /**
     * Lấy tất cả đánh giá của một phòng theo roomId
     */
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByRoomId(@PathVariable Integer roomId) {
        List<ReviewResponseDTO> reviews = reviewService.getReviewsByRoomId(roomId);
        return ResponseEntity.ok(reviews);
    }
    
    /**
     * Phản hồi đánh giá
     */
    @PostMapping("/{id}/reply")
    public ResponseEntity<ReviewResponseDTO> replyToReview(
            @PathVariable Integer id,
            @Valid @RequestBody ReplyReviewRequest request) {
        ReviewResponseDTO updatedReview = reviewService.replyToReview(id, request);
        return ResponseEntity.ok(updatedReview);
    }
    
    /**
     * Lấy thống kê đánh giá
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getReviewStatistics() {
        Map<String, Object> statistics = reviewService.getReviewStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    // === RATING APIs (đã tối ưu) ===
    
    /**
     * Lấy xếp hạng theo phòng
     * Đã gộp vào API /room/{roomId} để tránh trùng lặp
     * @deprecated Sử dụng API GET /room/{roomId} thay thế
     */
    @GetMapping("/ratings/room/{roomId}")
    @Deprecated
    public ResponseEntity<List<RatingDTO>> getRoomRatings(@PathVariable Integer roomId) {
        List<Rating> ratings = ratingRepository.findByRoomId(roomId);
        List<RatingDTO> ratingDTOs = ratings.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ratingDTOs);
    }

    /**
     * Tạo xếp hạng mới
     * @deprecated Sử dụng API POST /reviews thay thế với CreateReviewRequest
     */
    @PostMapping("/ratings")
    @Transactional
    @Deprecated
    public ResponseEntity<RatingDTO> createRating(
            @Valid @RequestBody CreateRatingRequest request,
            Authentication authentication) {
        
        log.info("Bắt đầu tạo xếp hạng mới cho phòng {}", request.getRoomId());
        
        User user = userRepository.findByUsername(authentication.getName());
        if (user == null) {
            throw new RuntimeException("Không tìm thấy người dùng");
        }
        
        Room room = roomRepository.findById(request.getRoomId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng"));

        Rating rating = new Rating();
        rating.setStars(request.getStars());
        rating.setComment(request.getComment());
        rating.setRoom(room);
        rating.setUser(user);

        rating = ratingRepository.save(rating);
        
        // Cập nhật điểm trung bình của phòng
        room.updateAverageRating(rating.getStars());
        room = roomRepository.save(room);
        
        log.info("Hoàn thành tạo xếp hạng cho phòng {}", request.getRoomId());

        return ResponseEntity.ok(convertToDTO(rating));
    }

    /**
     * Chuyển đổi Rating thành RatingDTO
     */
    private RatingDTO convertToDTO(Rating rating) {
        return RatingDTO.builder()
            .id(rating.getId())
            .stars(rating.getStars())
            .comment(rating.getComment())
            .roomId(rating.getRoom().getId())
            .userId(rating.getUser().getId())
            .userName(rating.getUser().getFullName())
            .createdAt(rating.getCreatedAt())
            .build();
    }
    
    // === API bổ sung ===
    
    /**
     * Lấy đánh giá theo mã đặt phòng
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ReviewResponseDTO> getReviewByBookingId(@PathVariable String bookingId) {
        ReviewResponseDTO review = reviewService.getReviewByBookingId(bookingId);
        return ResponseEntity.ok(review);
    }
    
    /**
     * Lấy đánh giá được đề xuất
     */
    @GetMapping("/featured")
    public ResponseEntity<List<ReviewResponseDTO>> getFeaturedReviews() {
        List<ReviewResponseDTO> reviews = reviewService.getFeaturedReviews();
        return ResponseEntity.ok(reviews);
    }
}
