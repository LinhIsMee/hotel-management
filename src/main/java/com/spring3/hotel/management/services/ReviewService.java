package com.spring3.hotel.management.services;

// import com.spring3.hotel.management.dto.request.CreateRatingRequest; // Commenting out unused import
import com.spring3.hotel.management.dto.request.CreateReviewRequest;
import com.spring3.hotel.management.dto.request.ReplyReviewRequest;
import com.spring3.hotel.management.dto.request.UpdateReviewRequest;
import com.spring3.hotel.management.dto.response.ReviewResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * Service xử lý đánh giá và bình luận
 */
public interface ReviewService {
    // Lấy thông tin đánh giá
    ReviewResponseDTO getReviewById(Integer id);
    ReviewResponseDTO getReviewByBookingId(String bookingId);
    List<ReviewResponseDTO> getAllReviews();
    
    // Quản lý phân trang
    Page<ReviewResponseDTO> getAllReviews(Pageable pageable);
    Page<ReviewResponseDTO> getPublicReviews(Pageable pageable);
    Page<ReviewResponseDTO> getPendingReviews(Pageable pageable);
    Page<ReviewResponseDTO> getRepliedReviews(Pageable pageable);
    Page<ReviewResponseDTO> getHiddenReviews(Pageable pageable);
    
    // Lọc đánh giá
    List<ReviewResponseDTO> getReviewsByRoomId(Integer roomId);
    List<ReviewResponseDTO> getReviewsByRoomNumber(String roomNumber);
    List<ReviewResponseDTO> getReviewsByRoomType(String roomType);
    List<ReviewResponseDTO> getFeaturedReviews();
    Page<ReviewResponseDTO> getReviewsByMinRating(Integer minRating, Pageable pageable);
    Page<ReviewResponseDTO> searchReviewsByGuestName(String guestName, Pageable pageable);
    
    // Thêm, sửa, xóa đánh giá
    ReviewResponseDTO createReview(CreateReviewRequest request);
    ReviewResponseDTO updateReview(UpdateReviewRequest request, Integer id);
    ReviewResponseDTO updateReview(Integer id, UpdateReviewRequest request);
    ReviewResponseDTO deleteReview(Integer id);
    ReviewResponseDTO replyToReview(Integer id, ReplyReviewRequest request);
    
    // Đánh giá về review
    // void rateReview(CreateRatingRequest request, Integer reviewId); // Commenting out unused method
    
    // Thống kê
    Map<String, Object> getReviewStatistics();
    
    // Khởi tạo dữ liệu mẫu
    void initReviewsFromJson();
}
