package com.spring3.hotel.management.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring3.hotel.management.dto.request.UpsertRoomRequest;
import com.spring3.hotel.management.dto.response.RoomResponseDTO;
import com.spring3.hotel.management.dto.response.BookingPeriodDTO;
import com.spring3.hotel.management.dto.response.RoomByTypeResponseDTO;
import com.spring3.hotel.management.dto.response.ServiceResponseDTO;
import com.spring3.hotel.management.exceptions.ResourceNotFoundException;
import com.spring3.hotel.management.models.Room;
import com.spring3.hotel.management.models.RoomType;
import com.spring3.hotel.management.models.HotelService;
import com.spring3.hotel.management.models.Booking;
import com.spring3.hotel.management.repositories.RoomRepository;
import com.spring3.hotel.management.repositories.RoomTypeRepository;
import com.spring3.hotel.management.repositories.ServiceRepository;
import com.spring3.hotel.management.repositories.BookingDetailRepository;
import com.spring3.hotel.management.services.interfaces.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    private ObjectMapper objectMapper;
    
    @Override
    public List<RoomResponseDTO> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(this::mapRoomToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public RoomResponseDTO getRoomById(Integer id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng với ID: " + id));
        return mapRoomToDTO(room);
    }
    
    @Override
    public RoomResponseDTO getRoomByRoomNumber(String roomNumber) {
        Room room = roomRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng với số phòng: " + roomNumber));
        return mapRoomToDTO(room);
    }
    
    @Override
    public List<RoomByTypeResponseDTO> getRoomsByRoomType(Integer roomTypeId) {
        return roomRepository.findByRoomTypeId(roomTypeId)
                .stream()
                .map(RoomByTypeResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<RoomResponseDTO> getRoomsByStatus(String status) {
        return roomRepository.findByStatus(status).stream()
                .map(this::mapRoomToDTO)
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
            List<HotelService> services = serviceRepository.findAllById(request.getServiceIds());
            room.setServices(services);
        }
        
        Room savedRoom = roomRepository.save(room);
        return mapRoomToDTO(savedRoom);
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
        return mapRoomToDTO(updatedRoom);
    }
    
    @Override
    public void deleteRoom(Integer id) {
        log.info("Xóa phòng có ID: {}", id);
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng với ID: " + id));
        
        // Sử dụng soft delete thay vì hard delete để tránh vi phạm ràng buộc khóa ngoại
        room.setIsActive(false);
        roomRepository.save(room);
        log.info("Đã vô hiệu hóa (soft delete) phòng với ID: {}", id);
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
                    RoomResponseDTO dto = mapRoomToDTO(room);
                    
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
                    RoomResponseDTO dto = mapRoomToDTO(room);
                    
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
        
        // Lấy tất cả phòng đang hoạt động
        List<Room> allRooms = roomRepository.findByIsActiveTrue();
        
        // Lọc và sắp xếp phòng theo tiêu chí nổi bật:
        // 1. Phòng có rating cao
        // 2. Phòng có nhiều đánh giá
        // 3. Phòng có giá trị cao
        List<RoomResponseDTO> featuredRooms = allRooms.stream()
            .map(this::mapRoomToDTO)
            .sorted((r1, r2) -> {
                // Ưu tiên phòng có rating cao
                if (r1.getAverageRating() != null && r2.getAverageRating() != null) {
                    int ratingCompare = Double.compare(r2.getAverageRating(), r1.getAverageRating());
                    if (ratingCompare != 0) return ratingCompare;
                }
                
                // Sau đó xét số lượng đánh giá
                int reviewCompare = Integer.compare(r2.getTotalReviews(), r1.getTotalReviews());
                if (reviewCompare != 0) return reviewCompare;
                
                // Cuối cùng xét giá phòng
                return Double.compare(r2.getPricePerNight(), r1.getPricePerNight());
            })
            .limit(6) // Giới hạn 6 phòng nổi bật
            .collect(Collectors.toList());
        
        return featuredRooms;
    }
    
    // Phương thức mới để map Room sang RoomResponseDTO
    private RoomResponseDTO mapRoomToDTO(Room room) {
        if (room == null) return null;
        
        RoomResponseDTO dto = new RoomResponseDTO();
        dto.setId(room.getId());
        dto.setRoomNumber(room.getRoomNumber());
        dto.setFloor(room.getFloor());
        dto.setStatus(room.getStatus());
        dto.setIsActive(room.getIsActive());
        dto.setNotes(room.getNotes());
        dto.setImages(room.getImages());
        
        // Thông tin loại phòng
        if (room.getRoomType() != null) {
            dto.setRoomTypeId(room.getRoomType().getId());
            dto.setRoomTypeName(room.getRoomType().getName());
            dto.setMaxOccupancy(room.getRoomType().getMaxOccupancy());
            dto.setPricePerNight(room.getRoomType().getPricePerNight());
        }
        
        // Thông tin dịch vụ
        if (room.getServices() != null) {
            dto.setServices(room.getServices().stream()
                .map(service -> {
                    ServiceResponseDTO serviceDTO = new ServiceResponseDTO();
                    serviceDTO.setId(service.getId());
                    serviceDTO.setName(service.getName());
                    serviceDTO.setDescription(service.getDescription());
                    serviceDTO.setPrice(service.getPrice());
                    return serviceDTO;
                })
                .collect(Collectors.toList()));
        }
        
        // Thông tin đánh giá
        dto.setAverageRating(room.getAverageRating());
        
        // Tính tổng số đánh giá
        if (room.getRatings() != null) {
            dto.setTotalReviews(room.getRatings().size());
            dto.setRatingCount(room.getRatingCount());
        } else {
            dto.setTotalReviews(0);
            dto.setRatingCount(0);
        }
        
        return dto;
    }
}
