package com.spring3.hotel.management.services.implementations;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring3.hotel.management.dtos.request.CreateRatingRequest;
import com.spring3.hotel.management.dtos.request.ReplyRatingRequest;
import com.spring3.hotel.management.dtos.response.RatingResponseDTO;
import com.spring3.hotel.management.models.Rating;
import com.spring3.hotel.management.models.Rating.RatingStatus;
import com.spring3.hotel.management.repositories.RatingRepository;
import com.spring3.hotel.management.services.interfaces.RatingService;
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
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final ObjectMapper objectMapper;

    @Override
    public Page<RatingResponseDTO> getAllRatings(Pageable pageable) {
        Page<Rating> ratingsPage = ratingRepository.findAll(pageable);
        List<RatingResponseDTO> ratingDTOs = ratingsPage.getContent().stream()
                .map(RatingResponseDTO::fromEntity)
                .collect(Collectors.toList());
        
        return new PageImpl<>(ratingDTOs, pageable, ratingsPage.getTotalElements());
    }

    @Override
    public Page<RatingResponseDTO> getPendingRatings(Pageable pageable) {
        Page<Rating> ratingsPage = ratingRepository.findByStatus(RatingStatus.PENDING, pageable);
        List<RatingResponseDTO> ratingDTOs = ratingsPage.getContent().stream()
                .map(RatingResponseDTO::fromEntity)
                .collect(Collectors.toList());
        
        return new PageImpl<>(ratingDTOs, pageable, ratingsPage.getTotalElements());
    }

    @Override
    public Page<RatingResponseDTO> getRepliedRatings(Pageable pageable) {
        Page<Rating> ratingsPage = ratingRepository.findByStatus(RatingStatus.REPLIED, pageable);
        List<RatingResponseDTO> ratingDTOs = ratingsPage.getContent().stream()
                .map(RatingResponseDTO::fromEntity)
                .collect(Collectors.toList());
        
        return new PageImpl<>(ratingDTOs, pageable, ratingsPage.getTotalElements());
    }

    @Override
    public RatingResponseDTO getRatingById(Integer id) {
        Rating rating = ratingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá không tìm thấy với ID: " + id));
        
        return RatingResponseDTO.fromEntity(rating);
    }

    @Override
    public RatingResponseDTO getRatingByBookingId(String bookingId) {
        Rating rating = ratingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá không tìm thấy với Booking ID: " + bookingId));
        
        return RatingResponseDTO.fromEntity(rating);
    }

    @Override
    public List<RatingResponseDTO> getRatingsByRoomNumber(String roomNumber) {
        List<Rating> ratings = ratingRepository.findByRoomNumber(roomNumber);
        
        return ratings.stream()
                .map(RatingResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<RatingResponseDTO> getRatingsByRoomType(String roomType) {
        List<Rating> ratings = ratingRepository.findByRoomType(roomType);
        
        return ratings.stream()
                .map(RatingResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Page<RatingResponseDTO> getRatingsByMinRating(Integer minRating, Pageable pageable) {
        Page<Rating> ratingsPage = ratingRepository.findByRatingGreaterThanEqual(minRating, pageable);
        List<RatingResponseDTO> ratingDTOs = ratingsPage.getContent().stream()
                .map(RatingResponseDTO::fromEntity)
                .collect(Collectors.toList());
        
        return new PageImpl<>(ratingDTOs, pageable, ratingsPage.getTotalElements());
    }

    @Override
    public Page<RatingResponseDTO> searchRatingsByGuestName(String guestName, Pageable pageable) {
        Page<Rating> ratingsPage = ratingRepository.findByGuestNameContainingIgnoreCase(guestName, pageable);
        List<RatingResponseDTO> ratingDTOs = ratingsPage.getContent().stream()
                .map(RatingResponseDTO::fromEntity)
                .collect(Collectors.toList());
        
        return new PageImpl<>(ratingDTOs, pageable, ratingsPage.getTotalElements());
    }

    @Override
    public RatingResponseDTO createRating(CreateRatingRequest request) {
        Rating rating = Rating.builder()
                .bookingId(request.getBookingId())
                .guestName(request.getGuestName())
                .roomNumber(request.getRoomNumber())
                .roomType(request.getRoomType())
                .rating(request.getRating())
                .comment(request.getComment())
                .images(request.getImages())
                .status(RatingStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        Rating savedRating = ratingRepository.save(rating);
        return RatingResponseDTO.fromEntity(savedRating);
    }

    @Override
    public RatingResponseDTO replyToRating(Integer id, ReplyRatingRequest request) {
        Rating rating = ratingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá không tìm thấy với ID: " + id));
        
        rating.setReplyComment(request.getReplyComment());
        rating.setReplyBy(request.getReplyBy());
        rating.setStatus(RatingStatus.REPLIED);
        rating.setRepliedAt(LocalDateTime.now());
        rating.setUpdatedAt(LocalDateTime.now());
        
        Rating updatedRating = ratingRepository.save(rating);
        return RatingResponseDTO.fromEntity(updatedRating);
    }

    @Override
    public void deleteRating(Integer id) {
        if (!ratingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Đánh giá không tìm thấy với ID: " + id);
        }
        
        ratingRepository.deleteById(id);
    }

    @Override
    public Map<String, Object> getRatingStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        long totalRatings = ratingRepository.count();
        long pendingRatings = ratingRepository.countByStatus(RatingStatus.PENDING);
        long repliedRatings = ratingRepository.countByStatus(RatingStatus.REPLIED);
        
        Double averageRating = ratingRepository.calculateAverageRating();
        
        List<Rating> fiveStarRatings = ratingRepository.findByRating(5);
        List<Rating> fourStarRatings = ratingRepository.findByRating(4);
        List<Rating> threeStarRatings = ratingRepository.findByRating(3);
        List<Rating> twoStarRatings = ratingRepository.findByRating(2);
        List<Rating> oneStarRatings = ratingRepository.findByRating(1);
        
        statistics.put("totalRatings", totalRatings);
        statistics.put("pendingRatings", pendingRatings);
        statistics.put("repliedRatings", repliedRatings);
        statistics.put("averageRating", averageRating);
        statistics.put("fiveStarCount", fiveStarRatings.size());
        statistics.put("fourStarCount", fourStarRatings.size());
        statistics.put("threeStarCount", threeStarRatings.size());
        statistics.put("twoStarCount", twoStarRatings.size());
        statistics.put("oneStarCount", oneStarRatings.size());
        
        return statistics;
    }

    @Override
    public void initRatingsFromJson() {
        try {
            ClassPathResource resource = new ClassPathResource("data/ratings.json");
            InputStream inputStream = resource.getInputStream();
            
            List<Rating> ratings = objectMapper.readValue(inputStream, new TypeReference<List<Rating>>() {});
            
            // Kiểm tra dữ liệu và thiết lập thời gian
            ratings.forEach(rating -> {
                if (rating.getCreatedAt() == null) {
                    rating.setCreatedAt(LocalDateTime.now());
                }
                
                if (rating.getUpdatedAt() == null) {
                    rating.setUpdatedAt(LocalDateTime.now());
                }
                
                // Thiết lập trạng thái mặc định nếu null
                if (rating.getStatus() == null) {
                    rating.setStatus(RatingStatus.PENDING);
                }
            });
            
            ratingRepository.saveAll(ratings);
            log.info("Đã khởi tạo {} đánh giá từ ratings.json", ratings.size());
        } catch (IOException e) {
            log.error("Lỗi khi khởi tạo đánh giá từ JSON", e);
            throw new RuntimeException("Không thể khởi tạo dữ liệu đánh giá từ file JSON", e);
        }
    }
} 