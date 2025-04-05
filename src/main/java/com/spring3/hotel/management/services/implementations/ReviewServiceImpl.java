package com.spring3.hotel.management.services.implementations;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring3.hotel.management.dtos.request.CreateReviewRequest;
import com.spring3.hotel.management.dtos.request.ReplyReviewRequest;
import com.spring3.hotel.management.dtos.request.UpdateReviewRequest;
import com.spring3.hotel.management.dtos.response.ReviewResponseDTO;
import com.spring3.hotel.management.models.Booking;
import com.spring3.hotel.management.models.BookingDetail;
import com.spring3.hotel.management.models.Review;
import com.spring3.hotel.management.models.Review.ReviewStatus;
import com.spring3.hotel.management.repositories.BookingRepository;
import com.spring3.hotel.management.repositories.ReviewRepository;
import com.spring3.hotel.management.services.interfaces.ReviewService;
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
    private final BookingRepository bookingRepository;
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
        
        // Kiểm tra booking có tồn tại không
        Booking booking = bookingRepository.findById(Integer.valueOf(request.getBookingId().replace("B", "")))
                .orElseThrow(() -> new ResourceNotFoundException("Booking không tồn tại với ID: " + request.getBookingId()));
        
        // Kiểm tra trạng thái booking - chỉ cho phép đánh giá khi đã CHECKED_OUT
        if (!"CHECKED_OUT".equals(booking.getStatus())) {
            throw new IllegalStateException("Chỉ có thể đánh giá khi đã trả phòng. Trạng thái hiện tại: " + booking.getStatus());
        }
        
        // Kiểm tra phòng có tồn tại không
        boolean roomExists = false;
        String validRoomType = "";
        
        // Duyệt qua chi tiết đặt phòng để xác nhận thông tin phòng
        for (BookingDetail detail : booking.getBookingDetail()) {
            if (request.getRoomNumber().equals(detail.getRoomNumber())) {
                roomExists = true;
                validRoomType = detail.getRoomType();
                break;
            }
        }
        
        if (!roomExists) {
            throw new ResourceNotFoundException("Phòng với số " + request.getRoomNumber() + " không tồn tại trong đặt phòng này");
        }
        
        // Đảm bảo loại phòng nhất quán
        if (!validRoomType.equals(request.getRoomType())) {
            log.warn("Loại phòng trong yêu cầu ({}) không khớp với loại phòng thực tế ({}). Sử dụng loại phòng thực tế.", 
                    request.getRoomType(), validRoomType);
            request.setRoomType(validRoomType);
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
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));

        // Cập nhật phản hồi
        review.setReplyComment(request.getReplyComment());
        review.setReplyBy(request.getReplyBy());
        review.setReplyDate(LocalDateTime.now());
        review.setStatus(ReviewStatus.REPLIED);

        Review savedReview = reviewRepository.save(review);
        return ReviewResponseDTO.fromEntity(savedReview);
    }
    
    @Override
    public ReviewResponseDTO updateReview(Integer id, UpdateReviewRequest request) {
        log.info("Bắt đầu cập nhật đánh giá với ID: {} và dữ liệu: {}", id, request);
        
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá không tìm thấy với ID: " + id));
        
        log.info("Tìm thấy đánh giá: {}", review);
        
        boolean hasChanges = false;
        
        // Khi cập nhật booking ID, kiểm tra booking mới có tồn tại không
        if (request.getBookingId() != null && !request.getBookingId().equals(review.getBookingId())) {
            if (reviewRepository.findByBookingId(request.getBookingId()).isPresent()) {
                throw new RuntimeException("Đã tồn tại đánh giá cho booking ID: " + request.getBookingId());
            }
            
            try {
                // Kiểm tra booking có tồn tại không
                Booking booking = bookingRepository.findById(Integer.valueOf(request.getBookingId().replace("B", "")))
                        .orElseThrow(() -> new ResourceNotFoundException("Booking không tồn tại với ID: " + request.getBookingId()));
                
                review.setBookingId(request.getBookingId());
                hasChanges = true;
                
                // Nếu cập nhật booking, đồng bộ lại thông tin phòng và loại phòng
                if (booking.getBookingDetail() != null && !booking.getBookingDetail().isEmpty()) {
                    BookingDetail detail = booking.getBookingDetail().get(0);
                    review.setRoomNumber(detail.getRoomNumber());
                    review.setRoomType(detail.getRoomType());
                    log.info("Cập nhật tự động thông tin phòng theo booking mới: Phòng {}, Loại {}", 
                            detail.getRoomNumber(), detail.getRoomType());
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Mã booking không hợp lệ: " + request.getBookingId());
            }
        } else if (request.getBookingId() != null) {
            review.setBookingId(request.getBookingId());
            hasChanges = true;
        }
        
        if (request.getGuestName() != null) {
            review.setGuestName(request.getGuestName());
            hasChanges = true;
        }
        
        // Nếu cập nhật thông tin phòng, kiểm tra tính hợp lệ
        if (request.getRoomNumber() != null || request.getRoomType() != null) {
            try {
                // Lấy booking hiện tại để kiểm tra
                Booking booking = bookingRepository.findById(Integer.valueOf(review.getBookingId().replace("B", "")))
                        .orElseThrow(() -> new ResourceNotFoundException("Booking không tồn tại với ID: " + review.getBookingId()));
                
                String newRoomNumber = request.getRoomNumber() != null ? request.getRoomNumber() : review.getRoomNumber();
                String newRoomType = request.getRoomType() != null ? request.getRoomType() : review.getRoomType();
                
                // Kiểm tra tính hợp lệ của thông tin phòng
                boolean isValid = false;
                String validRoomType = "";
                
                for (BookingDetail detail : booking.getBookingDetail()) {
                    if (newRoomNumber.equals(detail.getRoomNumber())) {
                        isValid = true;
                        validRoomType = detail.getRoomType();
                        break;
                    }
                }
                
                if (!isValid) {
                    throw new IllegalArgumentException("Phòng " + newRoomNumber + " không thuộc về booking này");
                }
                
                // Đảm bảo loại phòng nhất quán
                if (!validRoomType.equals(newRoomType)) {
                    log.warn("Loại phòng yêu cầu ({}) không khớp với loại phòng thực tế ({}). Sử dụng loại phòng thực tế.",
                            newRoomType, validRoomType);
                    newRoomType = validRoomType;
                }
                
                if (request.getRoomNumber() != null) {
                    review.setRoomNumber(newRoomNumber);
                    hasChanges = true;
                }
                
                if (request.getRoomType() != null) {
                    review.setRoomType(newRoomType);
                    hasChanges = true;
                }
                
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Mã booking không hợp lệ: " + review.getBookingId());
            }
        }
        
        // Cập nhật các trường đánh giá
        if (request.getRating() != null) {
            review.setRating(request.getRating());
            hasChanges = true;
        }
        
        if (request.getCleanliness() != null) {
            review.setCleanliness(request.getCleanliness());
            hasChanges = true;
        }
        
        if (request.getService() != null) {
            review.setService(request.getService());
            hasChanges = true;
        }
        
        if (request.getComfort() != null) {
            review.setComfort(request.getComfort());
            hasChanges = true;
        }
        
        if (request.getLocation() != null) {
            review.setLocation(request.getLocation());
            hasChanges = true;
        }
        
        if (request.getFacilities() != null) {
            review.setFacilities(request.getFacilities());
            hasChanges = true;
        }
        
        if (request.getValueForMoney() != null) {
            review.setValueForMoney(request.getValueForMoney());
            hasChanges = true;
        }
        
        if (request.getComment() != null) {
            review.setComment(request.getComment());
            hasChanges = true;
        }
        
        if (request.getImages() != null) {
            review.setImages(request.getImages());
            hasChanges = true;
        }
        
        // Cập nhật thông tin phản hồi
        if (request.getReplyComment() != null) {
            review.setReplyComment(request.getReplyComment());
            hasChanges = true;
        }
        
        if (request.getReplyBy() != null) {
            review.setReplyBy(request.getReplyBy());
            hasChanges = true;
        }
        
        // Cập nhật trạng thái hiển thị
        if (request.getIsFeatured() != null) {
            log.info("Cập nhật trạng thái nổi bật từ {} thành {}", review.getIsFeatured(), request.getIsFeatured());
            review.setIsFeatured(request.getIsFeatured());
            hasChanges = true;
        }
        
        if (request.getIsAnonymous() != null) {
            log.info("Cập nhật trạng thái ẩn danh từ {} thành {}", review.getIsAnonymous(), request.getIsAnonymous());
            review.setIsAnonymous(request.getIsAnonymous());
            hasChanges = true;
        }
        
        if (request.getStatus() != null) {
            try {
                ReviewStatus status = ReviewStatus.valueOf(request.getStatus());
                log.info("Cập nhật trạng thái từ {} thành {}", review.getStatus(), status);
                review.setStatus(status);
                hasChanges = true;
            } catch (IllegalArgumentException e) {
                log.error("Trạng thái không hợp lệ: {}", request.getStatus());
                throw new IllegalArgumentException("Trạng thái không hợp lệ. Các giá trị hợp lệ: PENDING, REPLIED, HIDDEN");
            }
        }
        
        if (!hasChanges) {
            log.info("Không có thay đổi nào được thực hiện");
            return ReviewResponseDTO.fromEntity(review);
        }
        
        review.setUpdatedAt(LocalDateTime.now());
        
        log.info("Lưu đánh giá đã cập nhật: {}", review);
        Review updatedReview = reviewRepository.save(review);
        log.info("Đã lưu đánh giá thành công: {}", updatedReview);
        
        return ReviewResponseDTO.fromEntity(updatedReview);
    }
    
    @Override
    public ReviewResponseDTO updateReview(UpdateReviewRequest request, Integer id) {
        return updateReview(id, request);
    }
    
    @Override
    public ReviewResponseDTO deleteReview(Integer id) {
        log.info("Bắt đầu xóa đánh giá có ID: {}", id);
        
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy đánh giá với ID: {}", id);
                    return new ResourceNotFoundException("Đánh giá không tìm thấy với ID: " + id);
                });
        
        log.info("Đã tìm thấy đánh giá cần xóa: {}", review);
        
        try {
            ReviewResponseDTO response = ReviewResponseDTO.fromEntity(review);
            reviewRepository.delete(review);
            log.info("Đã xóa thành công đánh giá có ID: {}", id);
            return response;
        } catch (Exception e) {
            log.error("Lỗi khi xóa đánh giá có ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Lỗi khi xóa đánh giá: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<ReviewResponseDTO> getReviewsByRoomId(Integer roomId) {
        return reviewRepository.findByRoomId(roomId).stream()
                .map(ReviewResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getReviewStatistics() {
        long totalReviews = reviewRepository.count();
        long pendingReviews = reviewRepository.countByStatus(ReviewStatus.PENDING);
        long repliedReviews = reviewRepository.countByStatus(ReviewStatus.REPLIED);
        long hiddenReviews = reviewRepository.countByStatus(ReviewStatus.HIDDEN);
        
        double averageRating = reviewRepository.findAll().stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);
        
        long fiveStarCount = reviewRepository.countByRating(5);
        long fourStarCount = reviewRepository.countByRating(4);
        long threeStarCount = reviewRepository.countByRating(3);
        long twoStarCount = reviewRepository.countByRating(2);
        long oneStarCount = reviewRepository.countByRating(1);
        
        double fiveStarPercent = totalReviews > 0 ? (double) fiveStarCount / totalReviews * 100 : 0;
        double fourStarPercent = totalReviews > 0 ? (double) fourStarCount / totalReviews * 100 : 0;
        double threeStarPercent = totalReviews > 0 ? (double) threeStarCount / totalReviews * 100 : 0;
        double twoStarPercent = totalReviews > 0 ? (double) twoStarCount / totalReviews * 100 : 0;
        double oneStarPercent = totalReviews > 0 ? (double) oneStarCount / totalReviews * 100 : 0;
        
        Map<String, Object> statistics = new HashMap<>();
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
        statistics.put("fiveStarPercent", fiveStarPercent);
        statistics.put("fourStarPercent", fourStarPercent);
        statistics.put("threeStarPercent", threeStarPercent);
        statistics.put("twoStarPercent", twoStarPercent);
        statistics.put("oneStarPercent", oneStarPercent);
        
        return statistics;
    }

    @Override
    public void initReviewsFromJson() {
        try {
            ClassPathResource resource = new ClassPathResource("data/reviews.json");
            InputStream inputStream = resource.getInputStream();
            
            // Đọc dữ liệu từ JSON với cấu trúc { "data": [ ... ] }
            Map<String, List<Review>> reviewsMap = objectMapper.readValue(
                inputStream, 
                new TypeReference<Map<String, List<Review>>>() {}
            );
            
            List<Review> reviews = reviewsMap.get("data");
            
            if (reviews == null || reviews.isEmpty()) {
                log.warn("Không tìm thấy dữ liệu đánh giá trong file JSON hoặc danh sách rỗng");
                return;
            }
            
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