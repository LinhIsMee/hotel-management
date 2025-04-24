package com.spring3.hotel.management.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring3.hotel.management.dto.CreateRatingRequest;
import com.spring3.hotel.management.dto.request.CreateReviewRequest;
import com.spring3.hotel.management.dto.request.ReplyReviewRequest;
import com.spring3.hotel.management.dto.request.UpdateReviewRequest;
import com.spring3.hotel.management.dto.response.ReviewResponseDTO;
import com.spring3.hotel.management.exceptions.NotFoundException;
import com.spring3.hotel.management.exceptions.ResourceNotFoundException;
import com.spring3.hotel.management.models.Review;
import com.spring3.hotel.management.models.Review.ReviewStatus;
import com.spring3.hotel.management.repositories.BookingRepository;
import com.spring3.hotel.management.repositories.ReviewRepository;
import com.spring3.hotel.management.repositories.RoomRepository;
import com.spring3.hotel.management.repositories.UserRepository;
import com.spring3.hotel.management.services.ReviewService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    @Override
    public ReviewResponseDTO getReviewById(Integer id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đánh giá với ID: " + id));
        return mapToResponseDTO(review);
    }

    @Override
    public List<ReviewResponseDTO> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ReviewResponseDTO> getAllReviews(Pageable pageable) {
        Page<Review> reviews = reviewRepository.findAll(pageable);
        return reviews.map(this::mapToResponseDTO);
    }

    @Override
    public Page<ReviewResponseDTO> getPublicReviews(Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByStatus(ReviewStatus.REPLIED, pageable);
        return reviews.map(this::mapToResponseDTO);
    }

    @Override
    public Page<ReviewResponseDTO> getPendingReviews(Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByStatus(ReviewStatus.PENDING, pageable);
        return reviews.map(this::mapToResponseDTO);
    }

    @Override
    public Page<ReviewResponseDTO> getRepliedReviews(Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByStatus(ReviewStatus.REPLIED, pageable);
        return reviews.map(this::mapToResponseDTO);
    }

    @Override
    public Page<ReviewResponseDTO> getHiddenReviews(Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByStatus(ReviewStatus.HIDDEN, pageable);
        return reviews.map(this::mapToResponseDTO);
    }

    @Override
    public ReviewResponseDTO getReviewByBookingId(String bookingId) {
        Review review = reviewRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đánh giá cho đặt phòng: " + bookingId));
        return mapToResponseDTO(review);
    }

    @Override
    public List<ReviewResponseDTO> getReviewsByRoomNumber(String roomNumber) {
        List<Review> reviews = reviewRepository.findByRoomNumber(roomNumber);
        return reviews.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Override
    public List<ReviewResponseDTO> getReviewsByRoomType(String roomType) {
        List<Review> reviews = reviewRepository.findByRoomType(roomType);
        return reviews.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Override
    public List<ReviewResponseDTO> getReviewsByRoomId(Integer roomId) {
        List<Review> reviews = reviewRepository.findByRoomId(roomId);
        return reviews.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewResponseDTO> getFeaturedReviews() {
        List<Review> featuredReviews = reviewRepository.findByIsFeaturedTrue();
        return featuredReviews.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Override
    public Page<ReviewResponseDTO> getReviewsByMinRating(Integer minRating, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByRatingGreaterThanEqual(minRating, pageable);
        return reviews.map(this::mapToResponseDTO);
    }

    @Override
    public Page<ReviewResponseDTO> searchReviewsByGuestName(String guestName, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByGuestNameContainingIgnoreCase(guestName, pageable);
        return reviews.map(this::mapToResponseDTO);
    }

    @Override
    @Transactional
    public ReviewResponseDTO createReview(CreateReviewRequest request) {
        log.info("Bắt đầu tạo đánh giá mới");
        
        // Kiểm tra xem đã có đánh giá cho booking này chưa
        if (reviewRepository.findByBookingId(request.getBookingId()).isPresent()) {
            throw new IllegalArgumentException("Đơn đặt phòng với mã " + request.getBookingId() + " đã được đánh giá trước đó. Mỗi đơn đặt phòng chỉ được đánh giá một lần.");
        }
        
        // Tạo review mới dựa trên thông tin từ request
        Review review = new Review();
        review.setBookingId(request.getBookingId());
        review.setGuestName(request.getGuestName());
        review.setRoomNumber(request.getRoomNumber());
        review.setRoomType(request.getRoomType());
        review.setRating(request.getRating());
        review.setCleanliness(request.getCleanliness());
        review.setService(request.getService());
        review.setComfort(request.getComfort());
        review.setLocation(request.getLocation());
        review.setFacilities(request.getFacilities());
        review.setValueForMoney(request.getValueForMoney());
        review.setComment(request.getComment());
        review.setIsAnonymous(request.getIsAnonymous());
        review.setCreatedAt(LocalDateTime.now());
        review.setStatus(ReviewStatus.PENDING);
        
        if (request.getImages() != null) {
            review.setImages(request.getImages());
        }
        
        review = reviewRepository.save(review);
        
        log.info("Đã tạo đánh giá mới với ID: {}", review.getId());
        return mapToResponseDTO(review);
    }

    @Override
    @Transactional
    public ReviewResponseDTO replyToReview(Integer id, ReplyReviewRequest request) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đánh giá với ID: " + id));
        
        review.setReplyComment(request.getReplyComment());
        review.setReplyBy(request.getReplyBy());
        review.setReplyDate(LocalDateTime.now());
        review.setStatus(ReviewStatus.REPLIED);
        
        review = reviewRepository.save(review);
        return mapToResponseDTO(review);
    }

    @Override
    @Transactional
    public ReviewResponseDTO updateReview(Integer id, UpdateReviewRequest request) {
        return updateReview(request, id);
    }

    @Override
    @Transactional
    public ReviewResponseDTO updateReview(UpdateReviewRequest request, Integer id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đánh giá với ID: " + id));
        
        // Cập nhật thông tin
        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        
        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }
        
        if (request.getStatus() != null) {
            review.setStatus(ReviewStatus.valueOf(request.getStatus()));
        }
        
        if (request.getReplyComment() != null) {
            review.setReplyComment(request.getReplyComment());
            review.setReplyDate(LocalDateTime.now());
            review.setStatus(ReviewStatus.REPLIED);
        }
        
        review.setUpdatedAt(LocalDateTime.now());
        review = reviewRepository.save(review);
        
        return mapToResponseDTO(review);
    }

    @Override
    @Transactional
    public ReviewResponseDTO deleteReview(Integer id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đánh giá với ID: " + id));
        
        ReviewResponseDTO reviewDTO = mapToResponseDTO(review);
        
        // Xóa đánh giá
        reviewRepository.delete(review);
        
        return reviewDTO;
    }

    @Override
    public Map<String, Object> getReviewStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // Tổng số đánh giá
        statistics.put("totalReviews", reviewRepository.count());
        
        // Số lượng đánh giá theo trạng thái
        statistics.put("pendingReviews", reviewRepository.countByStatus(ReviewStatus.PENDING));
        statistics.put("repliedReviews", reviewRepository.countByStatus(ReviewStatus.REPLIED));
        statistics.put("hiddenReviews", reviewRepository.countByStatus(ReviewStatus.HIDDEN));
        
        // Phân phối điểm đánh giá
        statistics.put("rating5Count", reviewRepository.countByRating(5));
        statistics.put("rating4Count", reviewRepository.countByRating(4));
        statistics.put("rating3Count", reviewRepository.countByRating(3));
        statistics.put("rating2Count", reviewRepository.countByRating(2));
        statistics.put("rating1Count", reviewRepository.countByRating(1));
        
        // Điểm đánh giá trung bình
        Double averageRating = reviewRepository.calculateAverageRating();
        statistics.put("averageRating", averageRating != null ? averageRating : 0.0);
        
        return statistics;
    }

    @Override
    public void rateReview(CreateRatingRequest request, Integer reviewId) {
        // Phương thức này không được sử dụng trong controller hiện tại
        throw new UnsupportedOperationException("Phương thức này chưa được triển khai");
    }
    
    // Phương thức hỗ trợ chuyển đổi từ Review sang ReviewResponseDTO
    private ReviewResponseDTO mapToResponseDTO(Review review) {
        return ReviewResponseDTO.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .guestName(review.getGuestName())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .status(review.getStatus().name())
                .replyComment(review.getReplyComment())
                .replyDate(review.getReplyDate())
                .roomNumber(review.getRoomNumber())
                .roomType(review.getRoomType())
                .bookingId(review.getBookingId())
                .build();
    }
}