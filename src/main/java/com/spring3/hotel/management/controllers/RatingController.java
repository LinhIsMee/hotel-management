package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dto.CreateRatingRequest;
import com.spring3.hotel.management.dto.RatingDTO;
import com.spring3.hotel.management.models.Rating;
import com.spring3.hotel.management.models.Room;
import com.spring3.hotel.management.models.User;
import com.spring3.hotel.management.repositories.RatingRepository;
import com.spring3.hotel.management.repositories.RoomRepository;
import com.spring3.hotel.management.repositories.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    // Lấy đánh giá theo phòng
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<RatingDTO>> getRoomRatings(@PathVariable Integer roomId) {
        List<Rating> ratings = ratingRepository.findByRoomId(roomId);
        List<RatingDTO> ratingDTOs = ratings.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ratingDTOs);
    }

    // Tạo đánh giá mới
    @PostMapping
    public ResponseEntity<RatingDTO> createRating(
            @Valid @RequestBody CreateRatingRequest request,
            Authentication authentication) {
        
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
        roomRepository.save(room);

        return ResponseEntity.ok(convertToDTO(rating));
    }

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
} 