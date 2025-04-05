package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dtos.response.ReviewResponseDTO;
import com.spring3.hotel.management.dtos.response.RoomResponseDTO;
import com.spring3.hotel.management.models.Review;
import com.spring3.hotel.management.repositories.ReviewRepository;
import com.spring3.hotel.management.services.interfaces.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/rooms")
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class RoomController {

    @Autowired
    private RoomService roomService;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    // Lấy thông tin một phòng cụ thể
    @GetMapping("/{id}")
    public ResponseEntity<RoomResponseDTO> getRoomById(@PathVariable Integer id) {
        log.info("Nhận yêu cầu lấy thông tin phòng với ID: {}", id);
        RoomResponseDTO room = roomService.getRoomById(id);
        enrichWithReviewData(List.of(room));
        return ResponseEntity.ok(room);
    }
    
    // Lấy danh sách phòng trống trong khoảng thời gian
    @GetMapping("/available")
    public ResponseEntity<List<RoomResponseDTO>> getAvailableRooms(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate) {
        log.info("Nhận yêu cầu lấy danh sách phòng trống từ {} đến {}", checkInDate, checkOutDate);
        List<RoomResponseDTO> availableRooms = roomService.getAvailableRooms(checkInDate, checkOutDate);
        enrichWithReviewData(availableRooms);
        log.info("Trả về {} phòng trống", availableRooms.size());
        return ResponseEntity.ok(availableRooms);
    }
    
    // Lấy danh sách tất cả phòng
    @GetMapping
    public ResponseEntity<List<RoomResponseDTO>> getAllActiveRooms() {
        log.info("Nhận yêu cầu lấy danh sách tất cả phòng đang hoạt động");
        try {
            List<RoomResponseDTO> rooms = roomService.getAllActiveRooms();
            log.info("Tìm thấy {} phòng từ DB", rooms.size());
            
            if (rooms.isEmpty()) {
                log.info("Gọi API reload-data để khởi tạo dữ liệu ban đầu do danh sách phòng trống");
                roomService.initRoomsFromJson();
                rooms = roomService.getAllActiveRooms();
                log.info("Sau khi khởi tạo lại: {} phòng", rooms.size());
            }
            
            enrichWithReviewData(rooms);
            log.info("Trả về {} phòng sau khi làm phong phú dữ liệu", rooms.size());
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách phòng: {}", e.getMessage(), e);
            return ResponseEntity.ok(List.of()); // Trả về danh sách rỗng thay vì lỗi
        }
    }
    
    // Lấy danh sách phòng theo loại phòng
    @GetMapping("/room-type/{roomTypeId}")
    public ResponseEntity<List<RoomResponseDTO>> getRoomsByRoomType(@PathVariable Integer roomTypeId) {
        log.info("Nhận yêu cầu lấy danh sách phòng theo loại phòng ID: {}", roomTypeId);
        List<RoomResponseDTO> rooms = roomService.getRoomsByRoomType(roomTypeId);
        enrichWithReviewData(rooms);
        log.info("Trả về {} phòng thuộc loại phòng ID: {}", rooms.size(), roomTypeId);
        return ResponseEntity.ok(rooms);
    }
    
    // Lấy danh sách phòng theo số người lớn
    @GetMapping("/occupancy/{maxOccupancy}")
    public ResponseEntity<List<RoomResponseDTO>> getRoomsByMaxOccupancy(@PathVariable Integer maxOccupancy) {
        log.info("Nhận yêu cầu lấy danh sách phòng theo số người lớn: {}", maxOccupancy);
        List<RoomResponseDTO> rooms = roomService.getAllRooms().stream()
                .filter(room -> room.getMaxOccupancy() >= maxOccupancy)
                .collect(Collectors.toList());
        enrichWithReviewData(rooms);
        log.info("Trả về {} phòng cho số người: {}", rooms.size(), maxOccupancy);
        return ResponseEntity.ok(rooms);
    }
    
    // Thêm thông tin đánh giá vào dữ liệu phòng
    private void enrichWithReviewData(List<RoomResponseDTO> rooms) {
        if (rooms == null || rooms.isEmpty()) {
            return;
        }
        
        for (RoomResponseDTO room : rooms) {
            try {
                // Lấy tất cả đánh giá của phòng
                List<Review> reviews = reviewRepository.findByRoomId(room.getId());
                
                // Kiểm tra đánh giá có tồn tại không
                if (reviews != null && !reviews.isEmpty()) {
                    // Tính xếp hạng trung bình
                    OptionalDouble avgRating = reviews.stream()
                            .mapToDouble(Review::getRating)
                            .average();
                    room.setAverageRating(avgRating.isPresent() ? avgRating.getAsDouble() : null);
                    room.setTotalReviews(reviews.size());
                    
                    // Lấy 3 đánh giá gần nhất 
                    List<ReviewResponseDTO> recentReviews = reviews.stream()
                            .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                            .limit(3)
                            .map(ReviewResponseDTO::fromEntity)
                            .collect(Collectors.toList());
                    room.setRecentReviews(recentReviews);
                } else {
                    // Đặt giá trị mặc định nếu không có đánh giá
                    room.setAverageRating(null);
                    room.setTotalReviews(0);
                    room.setRecentReviews(List.of());
                }
            } catch (Exception e) {
                // Xử lý ngoại lệ một cách an toàn
                log.error("Lỗi khi làm phong phú dữ liệu cho phòng ID {}: {}", room.getId(), e.getMessage());
                room.setAverageRating(null);
                room.setTotalReviews(0);
                room.setRecentReviews(List.of());
            }
        }
    }

    // Thêm API mới để tái khởi tạo dữ liệu phòng mẫu
    @GetMapping("/reload-data")
    public ResponseEntity<String> reloadRoomSampleData() {
        log.info("Nhận yêu cầu tái khởi tạo dữ liệu phòng mẫu");
        try {
            roomService.initRoomsFromJson();
            return ResponseEntity.ok("Đã tái khởi tạo dữ liệu phòng mẫu thành công");
        } catch (Exception e) {
            log.error("Lỗi khi tái khởi tạo dữ liệu phòng mẫu: {}", e.getMessage());
            return ResponseEntity.status(500).body("Lỗi khi tái khởi tạo dữ liệu: " + e.getMessage());
        }
    }
} 