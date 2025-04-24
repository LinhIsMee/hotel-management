package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dto.response.ReviewResponseDTO;
import com.spring3.hotel.management.dto.response.RoomResponseDTO;
import com.spring3.hotel.management.dto.response.RoomByTypeResponseDTO;
import com.spring3.hotel.management.dto.request.UpsertRoomRequest;
import com.spring3.hotel.management.dto.request.CreateRoomRequest;
import com.spring3.hotel.management.models.Review;
import com.spring3.hotel.management.repositories.ReviewRepository;
import com.spring3.hotel.management.services.RoomService;
import com.spring3.hotel.management.services.impl.RoomServiceImpl;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
            
            enrichWithReviewData(rooms);
            log.info("Trả về {} phòng sau khi làm phong phú dữ liệu", rooms.size());
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách phòng: {}", e.getMessage(), e);
            return ResponseEntity.ok(List.of()); // Trả về danh sách rỗng thay vì lỗi
        }
    }
    
    /**
     * API tổng hợp lấy danh sách phòng với nhiều tiêu chí lọc
     * Hỗ trợ lọc theo: loại phòng, số người, phòng nổi bật
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchRooms(
            @RequestParam(required = false) Integer roomTypeId,
            @RequestParam(required = false) Integer maxOccupancy,
            @RequestParam(required = false, defaultValue = "false") Boolean featured) {
        try {
            List<RoomResponseDTO> rooms;
            String message = "Tìm kiếm phòng thành công";
            
            // Lọc theo loại phòng
            if (roomTypeId != null) {
                List<RoomByTypeResponseDTO> roomsByType = roomService.getRoomsByRoomType(roomTypeId);
                // Chuyển đổi từ RoomByTypeResponseDTO sang RoomResponseDTO
                rooms = roomsByType.stream()
                        .map(room -> roomService.getRoomById(room.getId()))
                        .collect(Collectors.toList());
                message = "Lấy danh sách phòng theo loại thành công";
            }
            // Lọc theo số người
            else if (maxOccupancy != null) {
                log.info("Nhận yêu cầu lấy danh sách phòng theo số người lớn: {}", maxOccupancy);
                rooms = roomService.getAllRooms().stream()
                        .filter(room -> room.getMaxOccupancy() >= maxOccupancy)
                        .collect(Collectors.toList());
                message = "Lấy danh sách phòng theo số người thành công";
            }
            // Lấy phòng nổi bật
            else if (featured) {
                log.info("Nhận yêu cầu lấy danh sách phòng nổi bật");
                rooms = roomService.getFeaturedRooms();
                message = "Lấy danh sách phòng nổi bật thành công";
            }
            // Lấy tất cả phòng nếu không có tiêu chí lọc
            else {
                rooms = roomService.getAllActiveRooms();
                message = "Lấy danh sách tất cả phòng thành công";
            }
            
            enrichWithReviewData(rooms);
            log.info("Trả về {} phòng sau khi tìm kiếm", rooms.size());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", message,
                "rooms", rooms
            ));
        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm phòng: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }
    
    // Lấy danh sách phòng theo loại phòng
    @GetMapping("/room-type/{roomTypeId}")
    @Deprecated
    public ResponseEntity<List<RoomByTypeResponseDTO>> getRoomsByRoomType(@PathVariable Integer roomTypeId) {
        List<RoomByTypeResponseDTO> rooms = roomService.getRoomsByRoomType(roomTypeId);
        return ResponseEntity.ok(rooms);
    }
    
    // Lấy danh sách phòng theo số người lớn
    @GetMapping("/occupancy/{maxOccupancy}")
    @Deprecated
    public ResponseEntity<List<RoomResponseDTO>> getRoomsByMaxOccupancy(@PathVariable Integer maxOccupancy) {
        log.info("Nhận yêu cầu lấy danh sách phòng theo số người lớn: {}", maxOccupancy);
        List<RoomResponseDTO> rooms = roomService.getAllRooms().stream()
                .filter(room -> room.getMaxOccupancy() >= maxOccupancy)
                .collect(Collectors.toList());
        enrichWithReviewData(rooms);
        log.info("Trả về {} phòng cho số người: {}", rooms.size(), maxOccupancy);
        return ResponseEntity.ok(rooms);
    }
    
    // Lấy danh sách phòng nổi bật
    @GetMapping("/featured")
    @Deprecated
    public ResponseEntity<List<RoomResponseDTO>> getFeaturedRooms() {
        log.info("Nhận yêu cầu lấy danh sách phòng nổi bật");
        List<RoomResponseDTO> featuredRooms = roomService.getFeaturedRooms();
        enrichWithReviewData(featuredRooms);
        log.info("Trả về {} phòng nổi bật", featuredRooms.size());
        return ResponseEntity.ok(featuredRooms);
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
                            // .sorted((r1, r2) -> { // Tạm comment vì Review thiếu getCreatedAt()
                            //     if (r1.getCreatedAt() == null && r2.getCreatedAt() == null) return 0;
                            //     if (r1.getCreatedAt() == null) return 1;
                            //     if (r2.getCreatedAt() == null) return -1;
                            //     return r2.getCreatedAt().compareTo(r1.getCreatedAt());
                            // })
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

    @PutMapping("/batch-update-status")
    public ResponseEntity<?> updateRoomStatusBatch(@RequestBody Map<String, String> roomStatusMap) {
        try {
            ((RoomServiceImpl) roomService).updateRoomStatusBatch(roomStatusMap);
            return ResponseEntity.ok()
                .body(Map.of("message", "Đã cập nhật trạng thái thành công cho " + roomStatusMap.size() + " phòng"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật trạng thái phòng hàng loạt", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Có lỗi xảy ra khi cập nhật trạng thái phòng: " + e.getMessage()));
        }
    }
    
    /* === CÁC API DÀNH CHO ADMIN === */
    
    /**
     * Lấy danh sách tất cả phòng (bao gồm cả phòng không hoạt động)
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RoomResponseDTO>> getAllRooms() {
        List<RoomResponseDTO> rooms = roomService.getAllRooms();
        return ResponseEntity.ok(rooms);
    }

    /**
     * Lấy thông tin phòng bằng số phòng
     */
    @GetMapping("/room-number/{roomNumber}")
    public ResponseEntity<RoomResponseDTO> getRoomByRoomNumber(@PathVariable String roomNumber) {
        RoomResponseDTO room = roomService.getRoomByRoomNumber(roomNumber);
        if (room == null) {
            return ResponseEntity.notFound().build();
        }
        enrichWithReviewData(List.of(room));
        return ResponseEntity.ok(room);
    }

    /**
     * Tạo phòng mới (chỉ cho Admin)
     */
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomResponseDTO> createRoom(@Valid @RequestBody UpsertRoomRequest request) {
        // Chuyển đổi UpsertRoomRequest sang CreateRoomRequest
        CreateRoomRequest createRequest = new CreateRoomRequest();
        createRequest.setRoomNumber(request.getRoomNumber());
        createRequest.setRoomTypeId(request.getRoomTypeId());
        createRequest.setStatus(request.getStatus()); // Cần kiểm tra kiểu dữ liệu Enum/String
        createRequest.setFloor(request.getFloor());
        createRequest.setIsActive(request.getIsActive());
        createRequest.setNotes(request.getNotes());
        // createRequest.setServiceIds(...); // Cần lấy serviceIds từ UpsertRoomRequest nếu có
        // createRequest.setImages(...); // Cần lấy images từ UpsertRoomRequest nếu có
        
        RoomResponseDTO createdRoom = roomService.createRoom(createRequest); 
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRoom);
    }

    /**
     * Cập nhật thông tin phòng (chỉ cho Admin)
     */
    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomResponseDTO> updateRoom(
            @PathVariable Integer id,
            @Valid @RequestBody UpsertRoomRequest request) {
        RoomResponseDTO updatedRoom = roomService.updateRoom(request, id);
        return ResponseEntity.ok(updatedRoom);
    }

    /**
     * Xóa phòng (chỉ cho Admin)
     */
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRoom(@PathVariable Integer id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lấy danh sách phòng theo trạng thái
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<RoomResponseDTO>> getRoomsByStatus(@PathVariable String status) {
        List<RoomResponseDTO> rooms = roomService.getRoomsByStatus(status);
        return ResponseEntity.ok(rooms);
    }
}