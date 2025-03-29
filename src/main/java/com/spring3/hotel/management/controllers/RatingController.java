package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dtos.request.CreateRatingRequest;
import com.spring3.hotel.management.dtos.request.ReplyRatingRequest;
import com.spring3.hotel.management.dtos.response.RatingResponseDTO;
import com.spring3.hotel.management.services.interfaces.RatingService;
import com.spring3.hotel.management.utils.responses.ApiResponse;
import com.spring3.hotel.management.utils.responses.SuccessResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<RatingResponseDTO>>> getAllRatings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<RatingResponseDTO> ratings = ratingService.getAllRatings(pageable);
        
        return ResponseEntity.ok(new SuccessResponse<>(ratings, "Lấy danh sách đánh giá thành công"));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<Page<RatingResponseDTO>>> getPendingRatings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<RatingResponseDTO> ratings = ratingService.getPendingRatings(pageable);
        
        return ResponseEntity.ok(new SuccessResponse<>(ratings, "Lấy danh sách đánh giá đang chờ xử lý thành công"));
    }

    @GetMapping("/replied")
    public ResponseEntity<ApiResponse<Page<RatingResponseDTO>>> getRepliedRatings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "repliedAt"));
        
        Page<RatingResponseDTO> ratings = ratingService.getRepliedRatings(pageable);
        
        return ResponseEntity.ok(new SuccessResponse<>(ratings, "Lấy danh sách đánh giá đã được phản hồi thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RatingResponseDTO>> getRatingById(@PathVariable Integer id) {
        RatingResponseDTO rating = ratingService.getRatingById(id);
        
        return ResponseEntity.ok(new SuccessResponse<>(rating, "Lấy đánh giá thành công"));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<RatingResponseDTO>> getRatingByBookingId(@PathVariable String bookingId) {
        RatingResponseDTO rating = ratingService.getRatingByBookingId(bookingId);
        
        return ResponseEntity.ok(new SuccessResponse<>(rating, "Lấy đánh giá theo mã đặt phòng thành công"));
    }

    @GetMapping("/room/{roomNumber}")
    public ResponseEntity<ApiResponse<List<RatingResponseDTO>>> getRatingsByRoomNumber(@PathVariable String roomNumber) {
        List<RatingResponseDTO> ratings = ratingService.getRatingsByRoomNumber(roomNumber);
        
        return ResponseEntity.ok(new SuccessResponse<>(ratings, "Lấy đánh giá theo số phòng thành công"));
    }

    @GetMapping("/room-type/{roomType}")
    public ResponseEntity<ApiResponse<List<RatingResponseDTO>>> getRatingsByRoomType(@PathVariable String roomType) {
        List<RatingResponseDTO> ratings = ratingService.getRatingsByRoomType(roomType);
        
        return ResponseEntity.ok(new SuccessResponse<>(ratings, "Lấy đánh giá theo loại phòng thành công"));
    }

    @GetMapping("/min-rating/{minRating}")
    public ResponseEntity<ApiResponse<Page<RatingResponseDTO>>> getRatingsByMinRating(
            @PathVariable Integer minRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "rating"));
        
        Page<RatingResponseDTO> ratings = ratingService.getRatingsByMinRating(minRating, pageable);
        
        return ResponseEntity.ok(new SuccessResponse<>(ratings, "Lấy đánh giá theo điểm tối thiểu thành công"));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<RatingResponseDTO>>> searchRatingsByGuestName(
            @RequestParam String guestName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<RatingResponseDTO> ratings = ratingService.searchRatingsByGuestName(guestName, pageable);
        
        return ResponseEntity.ok(new SuccessResponse<>(ratings, "Tìm kiếm đánh giá theo tên khách hàng thành công"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RatingResponseDTO>> createRating(@Valid @RequestBody CreateRatingRequest request) {
        RatingResponseDTO createdRating = ratingService.createRating(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse<>(createdRating, "Tạo đánh giá thành công"));
    }

    @PostMapping("/{id}/reply")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<RatingResponseDTO>> replyToRating(
            @PathVariable Integer id,
            @Valid @RequestBody ReplyRatingRequest request) {
        
        RatingResponseDTO updatedRating = ratingService.replyToRating(id, request);
        
        return ResponseEntity.ok(new SuccessResponse<>(updatedRating, "Phản hồi đánh giá thành công"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRating(@PathVariable Integer id) {
        ratingService.deleteRating(id);
        
        return ResponseEntity.ok(new SuccessResponse<>(null, "Xóa đánh giá thành công"));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRatingStatistics() {
        Map<String, Object> statistics = ratingService.getRatingStatistics();
        
        return ResponseEntity.ok(new SuccessResponse<>(statistics, "Lấy thống kê đánh giá thành công"));
    }

    @PostMapping("/init")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> initRatingsFromJson() {
        ratingService.initRatingsFromJson();
        
        return ResponseEntity.ok(new SuccessResponse<>(null, "Khởi tạo dữ liệu đánh giá từ file JSON thành công"));
    }
} 