package com.spring3.hotel.management.services.implementations;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring3.hotel.management.dtos.request.CreateReviewRequest;
import com.spring3.hotel.management.dtos.request.ReplyReviewRequest;
import com.spring3.hotel.management.dtos.request.UpdateReviewRequest;
import com.spring3.hotel.management.dtos.response.ReviewResponseDTO;
import com.spring3.hotel.management.models.Review;
import com.spring3.hotel.management.models.Review.ReviewStatus;
import com.spring3.hotel.management.repositories.ReviewRepository;
import com.spring3.hotel.management.services.ReviewService;
import com.spring3.hotel.management.utils.exceptions.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
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
    private final ObjectMapper objectMapper;

    @Override
    public ReviewResponseDTO getReviewById(Integer id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá không tìm thấy với ID: " + id));
        
        return ReviewResponseDTO.fromEntity(review);
    }
    
    @Override
    public List<ReviewResponseDTO> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(ReviewResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ReviewResponseDTO> getAllReviews(Pageable pageable) {
        Page<Review> reviewsPage = reviewRepository.findAll(pageable);
        List<ReviewResponseDTO> reviewDTOs = reviewsPage.getContent().stream()
                .map(ReviewResponseDTO::fromEntity)
                .collect(Collectors.toList());
        
        return new PageImpl<>(reviewDTOs, pageable, reviewsPage.getTotalElements());
    }
    
    @Override
    public Page<ReviewResponseDTO> getPublicReviews(Pageable pageable) {
        Page<Review> reviewsPage = reviewRepository.findByStatusNotOrderByCreatedAtDesc(ReviewStatus.HIDDEN, pageable);
        List<ReviewResponseDTO> reviewDTOs = reviewsPage.getContent().stream()
                .map(ReviewResponseDTO::fromEntity)
                .collect(Collectors.toList());
        
        return new PageImpl<>(reviewDTOs, pageable, reviewsPage.getTotalElements());
    }

    @Override
    public Page<ReviewResponseDTO> getPendingReviews(Pageable pageable) {
        Page<Review> reviewsPage = reviewRepository.findByStatus(ReviewStatus.PENDING, pageable);
        List<ReviewResponseDTO> reviewDTOs = reviewsPage.getContent().stream()
                .map(ReviewResponseDTO::fromEntity)
                .collect(Collectors.toList());
        
        return new PageImpl<>(reviewDTOs, pageable, reviewsPage.getTotalElements());
    }

    @Override
    public Page<ReviewResponseDTO> getRepliedReviews(Pageable pageable) {
        Page<Review> reviewsPage = reviewRepository.findByStatus(ReviewStatus.REPLIED, pageable);
        List<ReviewResponseDTO> reviewDTOs = reviewsPage.getContent().stream()
                .map(ReviewResponseDTO::fromEntity)
                .collect(Collectors.toList());
        
        return new PageImpl<>(reviewDTOs, pageable, reviewsPage.getTotalElements());
    }
    
    @Override
    public Page<ReviewResponseDTO> getHiddenReviews(Pageable pageable) {
        Page<Review> reviewsPage = reviewRepository.findByStatus(ReviewStatus.HIDDEN, pageable);
        List<ReviewResponseDTO> reviewDTOs = reviewsPage.getContent().stream()
                .map(ReviewResponseDTO::fromEntity)
                .collect(Collectors.toList());
        
        return new PageImpl<>(reviewDTOs, pageable, reviewsPage.getTotalElements());
    }

    @Override
    public ReviewResponseDTO getReviewByBookingId(String bookingId) {
        Review review = reviewRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá không tìm thấy với Booking ID: " + bookingId));
        
        return ReviewResponseDTO.fromEntity(review);
    }

    @Override
    public List<ReviewResponseDTO> getReviewsByRoomNumber(String roomNumber) {
        List<Review> reviews = reviewRepository.findByRoomNumber(roomNumber);
        
        return reviews.stream()
                .map(ReviewResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewResponseDTO> getReviewsByRoomType(String roomType) {
        List<Review> reviews = reviewRepository.findByRoomType(roomType);
        
        return reviews.stream()
                .map(ReviewResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ReviewResponseDTO> getFeaturedReviews() {
        List<Review> reviews = reviewRepository.findByIsFeaturedTrue();
        
        return reviews.stream()
                .map(ReviewResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ReviewResponseDTO> getReviewsByMinRating(Integer minRating, Pageable pageable) {
        Page<Review> reviewsPage = reviewRepository.findByRatingGreaterThanEqual(minRating, pageable);
        List<ReviewResponseDTO> reviewDTOs = reviewsPage.getContent().stream()
                .map(ReviewResponseDTO::fromEntity)
                .collect(Collectors.toList());
        
        return new PageImpl<>(reviewDTOs, pageable, reviewsPage.getTotalElements());
    }

    @Override
    public Page<ReviewResponseDTO> searchReviewsByGuestName(String guestName, Pageable pageable) {
        Page<Review> reviewsPage = reviewRepository.findByGuestNameContainingIgnoreCase(guestName, pageable);
        List<ReviewResponseDTO> reviewDTOs = reviewsPage.getContent().stream()
                .map(ReviewResponseDTO::fromEntity)
                .collect(Collectors.toList());
        
        return new PageImpl<>(reviewDTOs, pageable, reviewsPage.getTotalElements());
    }

    @Override
    public ReviewResponseDTO createReview(CreateReviewRequest request) {
        // Kiểm tra xem đã có đánh giá cho booking này chưa
        if (reviewRepository.findByBookingId(request.getBookingId()).isPresent()) {
            throw new RuntimeException("Đã tồn tại đánh giá cho booking ID: " + request.getBookingId());
        }
        
        Review review = Review.builder()
                .bookingId(request.getBookingId())
                .guestName(request.getGuestName())
                .roomNumber(request.getRoomNumber())
                .roomType(request.getRoomType())
                .rating(request.getRating())
                .cleanliness(request.getCleanliness())
                .service(request.getService())
                .comfort(request.getComfort())
                .location(request.getLocation())
                .facilities(request.getFacilities())
                .valueForMoney(request.getValueForMoney())
                .comment(request.getComment())
                .images(request.getImages())
                .isAnonymous(request.getIsAnonymous())
                .isFeatured(false)
                .status(ReviewStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        Review savedReview = reviewRepository.save(review);
        return ReviewResponseDTO.fromEntity(savedReview);
    }

    @Override
    public ReviewResponseDTO replyToReview(Integer id, ReplyReviewRequest request) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá không tìm thấy với ID: " + id));
        
        review.setReplyComment(request.getReplyComment());
        review.setReplyBy(request.getReplyBy());
        review.setStatus(ReviewStatus.REPLIED);
        review.setReplyDate(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        
        Review updatedReview = reviewRepository.save(review);
        return ReviewResponseDTO.fromEntity(updatedReview);
    }
    
    @Override
    public ReviewResponseDTO updateReview(UpdateReviewRequest request, Integer id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá không tìm thấy với ID: " + id));
        
        if (request.getIsFeatured() != null) {
            review.setIsFeatured(request.getIsFeatured());
        }
        
        if (request.getIsAnonymous() != null) {
            review.setIsAnonymous(request.getIsAnonymous());
        }
        
        if (request.getStatus() != null) {
            try {
                ReviewStatus status = ReviewStatus.valueOf(request.getStatus());
                review.setStatus(status);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Trạng thái không hợp lệ. Các giá trị hợp lệ: PENDING, REPLIED, HIDDEN");
            }
        }
        
        review.setUpdatedAt(LocalDateTime.now());
        
        Review updatedReview = reviewRepository.save(review);
        return ReviewResponseDTO.fromEntity(updatedReview);
    }
    
    @Override
    public ReviewResponseDTO deleteReview(Integer id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá không tìm thấy với ID: " + id));
        
        ReviewResponseDTO reviewResponseDTO = ReviewResponseDTO.fromEntity(review);
        reviewRepository.deleteById(id);
        return reviewResponseDTO;
    }
    
    @Override
    public List<ReviewResponseDTO> getReviewsByRoomId(Integer roomId) {
        return reviewRepository.findByRoomId(roomId).stream()
                .map(ReviewResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getReviewStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        long totalReviews = reviewRepository.count();
        long pendingReviews = reviewRepository.countByStatus(ReviewStatus.PENDING);
        long repliedReviews = reviewRepository.countByStatus(ReviewStatus.REPLIED);
        long hiddenReviews = reviewRepository.countByStatus(ReviewStatus.HIDDEN);
        
        Double averageRating = reviewRepository.calculateAverageRating();
        
        long fiveStarCount = reviewRepository.countByRatingStar(5);
        long fourStarCount = reviewRepository.countByRatingStar(4);
        long threeStarCount = reviewRepository.countByRatingStar(3);
        long twoStarCount = reviewRepository.countByRatingStar(2);
        long oneStarCount = reviewRepository.countByRatingStar(1);
        
        statistics.put("totalReviews", totalReviews);
        statistics.put("pendingReviews", pendingReviews);
        statistics.put("repliedReviews", repliedReviews);
        statistics.put("hiddenReviews", hiddenReviews);
        statistics.put("averageRating", averageRating);
        statistics.put("fiveStarCount", fiveStarCount);
        statistics.put("fourStarCount", fourStarCount);
        statistics.put("threeStarCount", threeStarCount);
        statistics.put("twoStarCount", twoStarCount);
        statistics.put("oneStarCount", oneStarCount);
        
        // Thêm phân phối đánh giá theo phần trăm
        if (totalReviews > 0) {
            statistics.put("fiveStarPercent", (fiveStarCount * 100.0) / totalReviews);
            statistics.put("fourStarPercent", (fourStarCount * 100.0) / totalReviews);
            statistics.put("threeStarPercent", (threeStarCount * 100.0) / totalReviews);
            statistics.put("twoStarPercent", (twoStarCount * 100.0) / totalReviews);
            statistics.put("oneStarPercent", (oneStarCount * 100.0) / totalReviews);
        }
        
        return statistics;
    }

    @Override
    public void initReviewsFromJson() {
        try {
            ClassPathResource resource = new ClassPathResource("data/reviews.json");
            InputStream inputStream = resource.getInputStream();
            
            List<Review> reviews = objectMapper.readValue(inputStream, new TypeReference<List<Review>>() {});
            
            // Kiểm tra dữ liệu và thiết lập thời gian
            reviews.forEach(review -> {
                if (review.getCreatedAt() == null) {
                    review.setCreatedAt(LocalDateTime.now());
                }
                
                if (review.getUpdatedAt() == null) {
                    review.setUpdatedAt(LocalDateTime.now());
                }
                
                // Thiết lập trạng thái mặc định nếu null
                if (review.getStatus() == null) {
                    review.setStatus(ReviewStatus.PENDING);
                }
                
                // Thiết lập các giá trị boolean mặc định nếu null
                if (review.getIsFeatured() == null) {
                    review.setIsFeatured(false);
                }
                
                if (review.getIsAnonymous() == null) {
                    review.setIsAnonymous(false);
                }
            });
            
            reviewRepository.saveAll(reviews);
            log.info("Đã khởi tạo {} đánh giá từ reviews.json", reviews.size());
        } catch (IOException e) {
            log.error("Lỗi khi khởi tạo đánh giá từ JSON", e);
            throw new RuntimeException("Không thể khởi tạo dữ liệu đánh giá từ file JSON", e);
        }
    }
} 