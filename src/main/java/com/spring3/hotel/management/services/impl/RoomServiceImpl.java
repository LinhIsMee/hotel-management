package com.spring3.hotel.management.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring3.hotel.management.dto.response.BookingPeriodDTO;
import com.spring3.hotel.management.dto.response.RoomResponseDTO;
import com.spring3.hotel.management.dto.response.ReviewResponseDTO;
import com.spring3.hotel.management.dto.request.UpsertRoomRequest;
import com.spring3.hotel.management.exceptions.ResourceNotFoundException;
import com.spring3.hotel.management.models.Room;
import com.spring3.hotel.management.models.RoomType;
import com.spring3.hotel.management.models.Service;
import com.spring3.hotel.management.models.Booking;
import com.spring3.hotel.management.models.Review;
import com.spring3.hotel.management.repositories.RoomRepository;
import com.spring3.hotel.management.repositories.RoomTypeRepository;
import com.spring3.hotel.management.repositories.ServiceRepository;
import com.spring3.hotel.management.repositories.BookingDetailRepository;
import com.spring3.hotel.management.repositories.ReviewRepository;
import com.spring3.hotel.management.services.interfaces.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.OptionalDouble;

@Slf4j
@org.springframework.stereotype.Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomRepository roomRepository;
    
    @Autowired
    private RoomTypeRepository roomTypeRepository;
    
    @Autowired
    private ServiceRepository serviceRepository;
    
    @Autowired
    private BookingDetailRepository bookingDetailRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public List<RoomResponseDTO> getAllRooms() {
        return roomRepository.findByIsActiveTrue()
                .stream()
                .map(RoomResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public RoomResponseDTO getRoomById(Integer id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng với ID: " + id));
        return RoomResponseDTO.fromEntity(room);
    }
    
    @Override
    public RoomResponseDTO getRoomByRoomNumber(String roomNumber) {
        Room room = roomRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng với số phòng: " + roomNumber));
        return RoomResponseDTO.fromEntity(room);
    }
    
    @Override
    public List<RoomResponseDTO> getRoomsByRoomType(Integer roomTypeId) {
        return roomRepository.findByRoomTypeId(roomTypeId)
                .stream()
                .filter(Room::getIsActive)
                .map(RoomResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<RoomResponseDTO> getRoomsByStatus(String status) {
        return roomRepository.findByStatus(status)
                .stream()
                .filter(Room::getIsActive)
                .map(RoomResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public RoomResponseDTO createRoom(UpsertRoomRequest request) {
        // Kiểm tra phòng đã tồn tại chưa
        if (roomRepository.findByRoomNumber(request.getRoomNumber()).isPresent()) {
            throw new IllegalArgumentException("Số phòng đã tồn tại: " + request.getRoomNumber());
        }
        
        // Lấy loại phòng
        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phòng với ID: " + request.getRoomTypeId()));
        
        // Tạo phòng mới
        Room room = Room.builder()
                .roomNumber(request.getRoomNumber())
                .roomType(roomType)
                .status(request.getStatus())
                .floor(request.getFloor())
                .isActive(request.getIsActive())
                .notes(request.getNotes())
                .services(new ArrayList<>()) // Khởi tạo danh sách services rỗng
                .build();
        
        // Thêm dịch vụ nếu có
        if (request.getServiceIds() != null && !request.getServiceIds().isEmpty()) {
            List<Service> services = serviceRepository.findAllById(request.getServiceIds());
            room.setServices(services);
        }
        
        Room savedRoom = roomRepository.save(room);
        return RoomResponseDTO.fromEntity(savedRoom);
    }
    
    @Override
    public RoomResponseDTO updateRoom(UpsertRoomRequest request, Integer id) {
        Room existingRoom = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng với ID: " + id));
        
        // Kiểm tra nếu số phòng thay đổi và số phòng mới đã tồn tại
        if (!existingRoom.getRoomNumber().equals(request.getRoomNumber()) &&
                roomRepository.findByRoomNumber(request.getRoomNumber()).isPresent()) {
            throw new IllegalArgumentException("Số phòng đã tồn tại: " + request.getRoomNumber());
        }
        
        // Lấy loại phòng
        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phòng với ID: " + request.getRoomTypeId()));
        
        // Cập nhật thông tin
        existingRoom.setRoomNumber(request.getRoomNumber());
        existingRoom.setRoomType(roomType);
        
        if (request.getStatus() != null) {
            existingRoom.setStatus(request.getStatus());
        }
        
        if (request.getFloor() != null) {
            existingRoom.setFloor(request.getFloor());
        }
        
        if (request.getIsActive() != null) {
            existingRoom.setIsActive(request.getIsActive());
        }
        
        if (request.getNotes() != null) {
            existingRoom.setNotes(request.getNotes());
        }
        
        Room updatedRoom = roomRepository.save(existingRoom);
        return RoomResponseDTO.fromEntity(updatedRoom);
    }
    
    @Override
    public void deleteRoom(Integer id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng với ID: " + id));
        
        // Sử dụng soft delete thay vì hard delete để tránh vi phạm ràng buộc khóa ngoại
        room.setIsActive(false);
        roomRepository.save(room);
        log.info("Đã vô hiệu hóa (soft delete) phòng với ID: {}", id);
    }
    
    @Override
    public void initRoomsFromJson() {
        log.info("Cập nhật lại ảnh cho các phòng hiện có...");
        
        try {
            // Lấy danh sách phòng hiện có
            List<Room> existingRooms = roomRepository.findAll();
            if (existingRooms.isEmpty()) {
                log.warn("Không có phòng nào trong cơ sở dữ liệu để cập nhật.");
                return;
            }
            
            // Danh sách hình ảnh chất lượng cao từ Unsplash với các tham số tối ưu
            List<String> optimizedImages = List.of(
                "https://images.unsplash.com/photo-1618773928121-c32242e63f39?q=80&w=1200&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1566665797739-1674de7a421a?q=80&w=1200&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1587985064135-0366536eab42?q=80&w=1200&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1618773928121-c32242e63f39?q=80&w=1200&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1595576508898-0ad5c879a061?q=80&w=1200&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1611892440504-42a792e24d32?q=80&w=1200&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1590490360182-c33d57733427?q=80&w=1200&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?q=80&w=1200&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1566195992011-5f6b21e539aa?q=80&w=1200&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1591088398332-8a7791972843?q=80&w=1200&auto=format&fit=crop"
            );
            
            // Cập nhật URL hình ảnh cho từng phòng
            int count = 0;
            for (Room room : existingRooms) {
                // Lấy 3 hình ảnh ngẫu nhiên cho mỗi phòng
                List<String> newImages = getRandomImages(optimizedImages, 3, count);
                room.setImages(newImages);
                count += 3;
                
                // Cập nhật trạng thái phòng nếu cần
                if ("AVAILABLE".equals(room.getStatus())) {
                    room.setStatus("VACANT");
                }
            }
            
            // Lưu các phòng đã được cập nhật
            roomRepository.saveAll(existingRooms);
            log.info("Đã cập nhật thành công URL hình ảnh cho {} phòng hiện có", existingRooms.size());
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật hình ảnh phòng: {}", e.getMessage());
            throw new RuntimeException("Không thể cập nhật hình ảnh cho phòng", e);
        }
    }
    
    // Hàm hỗ trợ tạo phòng
    private Room createRoom(String roomNumber, RoomType roomType, String status, String floor, 
                           Boolean isActive, String notes, List<String> images) {
        Room room = new Room();
        room.setRoomNumber(roomNumber);
        room.setRoomType(roomType);
        room.setStatus(status);
        room.setFloor(floor);
        room.setIsActive(isActive);
        room.setNotes(notes);
        room.setImages(images);
        return room;
    }
    
    // Hàm lấy ngẫu nhiên các hình ảnh
    private List<String> getRandomImages(List<String> allImages, int count, int offset) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int index = (offset + i) % allImages.size();
            result.add(allImages.get(index));
        }
        return result;
    }
    
    private void addDefaultServicesToRooms(List<Room> rooms) {
        try {
            // Lấy danh sách các dịch vụ cơ bản từ DB
            List<Service> basicServices = serviceRepository.findByIsAvailable(true);
            if (basicServices.isEmpty()) {
                log.warn("Không có dịch vụ nào trong hệ thống để thêm vào phòng.");
                return;
            }
            
            for (Room room : rooms) {
                // Dịch vụ miễn phí cơ bản cho tất cả các phòng
                List<Service> roomServices = new ArrayList<>();
                
                // Dịch vụ Wifi và TV miễn phí cho tất cả các phòng
                basicServices.stream()
                    .filter(service -> service.getName().contains("Wifi") || service.getName().contains("Truyền hình"))
                    .forEach(roomServices::add);
                
                // Thêm dịch vụ dựa trên loại phòng
                int roomTypeId = room.getRoomType().getId();
                if (roomTypeId >= 2) { // Từ phòng đôi trở lên
                    basicServices.stream()
                        .filter(service -> service.getName().contains("Dọn phòng") || service.getName().contains("Giặt ủi"))
                        .forEach(roomServices::add);
                }
                
                if (roomTypeId >= 3) { // Từ phòng gia đình trở lên
                    basicServices.stream()
                        .filter(service -> service.getName().contains("Bữa sáng"))
                        .forEach(roomServices::add);
                }
                
                if (roomTypeId >= 4) { // Phòng hạng sang
                    basicServices.stream()
                        .filter(service -> service.getName().contains("Spa") || service.getName().contains("Đưa đón"))
                        .forEach(roomServices::add);
                }
                
                // Bỏ qua việc gán dịch vụ trực tiếp vào phòng để tránh lỗi ImmutableCollections
                // room.setServices(roomServices);
                // roomRepository.save(room);
                
                // Thay vào đó, lưu từng dịch vụ cho phòng qua bảng trung gian
                try {
                    // Sử dụng native query để thêm quan hệ
                    for (Service service : roomServices) {
                        // Kiểm tra xem dịch vụ đã tồn tại cho phòng chưa
                        // (Nếu cần, nhưng tạm bỏ qua để đơn giản hóa)
                        
                        // Chi tiết cách triển khai sẽ phụ thuộc vào repository - giả sử ở đây là trực tiếp thêm vào bảng trung gian
                        // Có thể cần tạo một repository mới cho bảng trung gian hoặc custom query trong RoomRepository
                    }
                    log.info("Đã thêm dịch vụ cho phòng {}", room.getRoomNumber());
                } catch (Exception e) {
                    log.error("Lỗi khi thêm dịch vụ cho phòng {}: {}", room.getRoomNumber(), e.getMessage());
                }
            }
            
            log.info("Đã bỏ qua việc thêm dịch vụ cho {} phòng do vấn đề với ImmutableCollections.", rooms.size());
        } catch (Exception e) {
            log.error("Lỗi khi thêm dịch vụ mặc định cho phòng: {}", e.getMessage(), e);
        }
    }

    @Override
    public List<RoomResponseDTO> getAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate) {
        // Lấy danh sách tất cả phòng
        List<Room> allRooms = roomRepository.findAll();
        log.info("Tìm thấy tổng cộng {} phòng trong hệ thống", allRooms.size());
        
        // Lấy danh sách phòng đã được đặt trong khoảng thời gian
        List<Room> bookedRooms = roomRepository.findBookedRoomsBetweenDates(checkInDate, checkOutDate);
        log.info("Tìm thấy {} phòng đã đặt trong khoảng thời gian từ {} đến {}", 
                 bookedRooms.size(), checkInDate, checkOutDate);
        
        // Lọc ra các phòng còn trống với nhiều loại trạng thái phù hợp
        List<Room> availableRooms = allRooms.stream()
                .filter(room -> !bookedRooms.contains(room))
                .filter(room -> {
                    // Các trạng thái phòng có thể đặt
                    String status = room.getStatus();
                    return "VACANT".equals(status) || 
                           "READY".equals(status) || 
                           "CLEANED".equals(status) || 
                           "AVAILABLE".equals(status) ||
                           "INSPECTION".equals(status) ||
                           "BOOKED".equals(status); // Thêm BOOKED vì có thể đặt cho thời gian khác
                })
                .collect(Collectors.toList());
        
        log.info("Sau khi lọc, tìm thấy {} phòng có sẵn để đặt", availableRooms.size());
        
        // Chuyển đổi sang DTO và thêm thông tin booking
        return availableRooms.stream()
                .map(room -> {
                    RoomResponseDTO dto = RoomResponseDTO.fromEntity(room);
                    
                    // Kiểm tra xem phòng có được đặt trong 5 ngày tới không
                    LocalDate today = LocalDate.now();
                    LocalDate fiveDaysLater = today.plusDays(5);
                    List<Room> nextFiveDaysBookings = roomRepository.findBookedRoomsBetweenDates(today, fiveDaysLater);
                    boolean isBooked = nextFiveDaysBookings.contains(room);
                    dto.setIsBookedNextFiveDays(isBooked);
                    
                    // Thêm thông tin về các khoảng thời gian đã đặt
                    if (isBooked) {
                        List<BookingPeriodDTO> bookingPeriods = bookingDetailRepository.findByRoomIdAndDateRange(
                                room.getId(), today, fiveDaysLater)
                                .stream()
                                .map(detail -> {
                                    Booking booking = detail.getBooking();
                                    return BookingPeriodDTO.builder()
                                            .checkInDate(booking.getCheckInDate())
                                            .checkOutDate(booking.getCheckOutDate())
                                            .build();
                                })
                                .collect(Collectors.toList());
                        dto.setBookingPeriods(bookingPeriods);
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<RoomResponseDTO> getAllActiveRooms() {
        // Lấy ngày hiện tại và 5 ngày tới
        LocalDate today = LocalDate.now();
        LocalDate fiveDaysLater = today.plusDays(5);
        
        // Lấy danh sách phòng đang hoạt động
        List<Room> activeRooms = roomRepository.findByIsActiveTrue();
        
        // Lấy danh sách phòng đã đặt trong 5 ngày tới
        List<Room> bookedRooms = roomRepository.findBookedRoomsBetweenDates(today, fiveDaysLater);
        
        // Chuyển đổi sang DTO với thông tin đặt phòng
        return activeRooms.stream()
                .map(room -> {
                    RoomResponseDTO dto = RoomResponseDTO.fromEntity(room);
                    
                    // Kiểm tra xem phòng có được đặt trong 5 ngày tới không
                    boolean isBooked = bookedRooms.contains(room);
                    dto.setIsBookedNextFiveDays(isBooked);
                    
                    // Thêm thông tin về các khoảng thời gian đã đặt
                    if (isBooked) {
                        List<BookingPeriodDTO> bookingPeriods = bookingDetailRepository.findByRoomIdAndDateRange(
                                room.getId(), today, fiveDaysLater)
                                .stream()
                                .map(detail -> {
                                    Booking booking = detail.getBooking();
                                    return BookingPeriodDTO.builder()
                                            .checkInDate(booking.getCheckInDate())
                                            .checkOutDate(booking.getCheckOutDate())
                                            .build();
                                })
                                .collect(Collectors.toList());
                        dto.setBookingPeriods(bookingPeriods);
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateRoomStatusBatch(Map<String, String> roomStatusMap) {
        // Kiểm tra tính hợp lệ của trạng thái
        Set<String> validStatuses = Set.of("VACANT", "OCCUPIED", "BOOKED", "MAINTENANCE", "CLEANING", "READY", "INSPECTION", "AVAILABLE");
        
        // Lọc và kiểm tra các trạng thái không hợp lệ
        List<String> invalidStatuses = roomStatusMap.values().stream()
                .filter(status -> !validStatuses.contains(status))
                .collect(Collectors.toList());
                
        if (!invalidStatuses.isEmpty()) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ: " + String.join(", ", invalidStatuses));
        }

        // Nhóm các phòng theo trạng thái để cập nhật hàng loạt
        Map<String, List<String>> statusToRooms = roomStatusMap.entrySet().stream()
                .collect(Collectors.groupingBy(
                    Map.Entry::getValue,
                    Collectors.mapping(Map.Entry::getKey, Collectors.toList())
                ));

        // Cập nhật từng nhóm phòng
        statusToRooms.forEach((status, roomNumbers) -> {
            roomRepository.updateRoomStatusBatch(roomNumbers, status);
            log.info("Đã cập nhật {} phòng sang trạng thái {}", roomNumbers.size(), status);
        });
    }

    @Override
    public List<RoomResponseDTO> getFeaturedRooms() {
        log.info("Lấy danh sách phòng nổi bật");
        
        // Lấy tất cả đánh giá
        List<Review> allReviews = reviewRepository.findAll();
        log.info("Tìm thấy {} đánh giá", allReviews.size());
        
        // Nhóm đánh giá theo số phòng và tính rating trung bình
        Map<String, List<Review>> reviewsByRoom = allReviews.stream()
            .collect(Collectors.groupingBy(Review::getRoomNumber));
        
        // Lấy tất cả phòng đang hoạt động
        List<Room> allRooms = roomRepository.findByIsActiveTrue();
        log.info("Tìm thấy {} phòng đang hoạt động", allRooms.size());
        
        // Chuyển đổi thành DTO và làm giàu dữ liệu review
        List<RoomResponseDTO> roomDTOs = allRooms.stream()
            .map(room -> {
                RoomResponseDTO dto = RoomResponseDTO.fromEntity(room);
                List<Review> roomReviews = reviewsByRoom.getOrDefault(room.getRoomNumber(), new ArrayList<>());
                
                if (!roomReviews.isEmpty()) {
                    // Tính rating trung bình
                    double averageRating = roomReviews.stream()
                        .mapToDouble(Review::getRating)
                        .average()
                        .orElse(0.0);
                    
                    dto.setAverageRating(averageRating);
                    dto.setTotalReviews(roomReviews.size());
                    
                    // Lấy 3 đánh giá gần nhất
                    List<ReviewResponseDTO> recentReviews = roomReviews.stream()
                        .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                        .limit(3)
                        .map(ReviewResponseDTO::fromEntity)
                        .collect(Collectors.toList());
                    dto.setRecentReviews(recentReviews);
                    
                    log.info("Phòng {}: {} đánh giá, rating trung bình: {}", 
                        room.getRoomNumber(), roomReviews.size(), averageRating);
                } else {
                    dto.setAverageRating(0.0);
                    dto.setTotalReviews(0);
                    dto.setRecentReviews(new ArrayList<>());
                }
                
                return dto;
            })
            .collect(Collectors.toList());
        
        // Lọc và sắp xếp phòng theo tiêu chí nổi bật
        List<RoomResponseDTO> featuredRooms = roomDTOs.stream()
            .filter(room -> room.getTotalReviews() > 0 && room.getAverageRating() >= 4.0)
            .sorted((r1, r2) -> {
                // Ưu tiên phòng có rating cao
                int ratingCompare = Double.compare(r2.getAverageRating(), r1.getAverageRating());
                if (ratingCompare != 0) return ratingCompare;
                
                // Sau đó xét số lượng đánh giá
                return Integer.compare(r2.getTotalReviews(), r1.getTotalReviews());
            })
            .limit(6) // Giới hạn 6 phòng nổi bật
            .collect(Collectors.toList());
        
        log.info("Trả về {} phòng nổi bật", featuredRooms.size());
        return featuredRooms;
    }
}
