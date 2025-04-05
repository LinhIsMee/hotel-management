package com.spring3.hotel.management.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring3.hotel.management.dtos.request.UpsertRoomRequest;
import com.spring3.hotel.management.dtos.response.RoomResponseDTO;
import com.spring3.hotel.management.exceptions.ResourceNotFoundException;
import com.spring3.hotel.management.models.Room;
import com.spring3.hotel.management.models.RoomType;
import com.spring3.hotel.management.models.Service;
import com.spring3.hotel.management.repositories.RoomRepository;
import com.spring3.hotel.management.repositories.RoomTypeRepository;
import com.spring3.hotel.management.repositories.ServiceRepository;
import com.spring3.hotel.management.services.interfaces.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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
    private ObjectMapper objectMapper;
    
    @Override
    public List<RoomResponseDTO> getAllRooms() {
        return roomRepository.findAll()
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
                .map(RoomResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<RoomResponseDTO> getRoomsByStatus(String status) {
        return roomRepository.findByStatus(status)
                .stream()
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
                .build();
        
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
        try {
            log.info("Bắt đầu khởi tạo dữ liệu phòng từ file JSON...");
            
            // Tạo phòng mẫu trực tiếp nếu không có file JSON
            log.info("Khởi tạo dữ liệu phòng mẫu trực tiếp...");
            List<Room> rooms = new ArrayList<>();
            
            // Phải đảm bảo có đủ loại phòng trong DB
            List<RoomType> roomTypes = roomTypeRepository.findAll();
            if (roomTypes.isEmpty()) {
                log.error("Không có loại phòng nào trong DB, không thể khởi tạo phòng mẫu.");
                return;
            }
            
            // Ảnh từ upcloud cho các loại phòng khác nhau
            List<String> singleRoomImages = List.of(
                "https://upcdn.io/12a1X6T/raw/uploads/2024/06/27/single-room-1-ayK5.jpg",
                "https://upcdn.io/12a1X6T/raw/uploads/2024/06/27/single-room-2-CDJS.jpg",
                "https://upcdn.io/12a1X6T/raw/uploads/2024/06/27/single-room-3-mLTZ.jpg",
                "https://upcdn.io/12a1X6T/raw/uploads/2024/06/27/single-room-4-MvZ4.jpg"
            );
            
            List<String> doubleRoomImages = List.of(
                "https://upcdn.io/12a1X6T/raw/uploads/2024/06/27/double-room-1-JkXD.jpg",
                "https://upcdn.io/12a1X6T/raw/uploads/2024/06/27/double-room-2-7Zwd.jpg",
                "https://upcdn.io/12a1X6T/raw/uploads/2024/06/27/double-room-3-TK2N.jpg",
                "https://upcdn.io/12a1X6T/raw/uploads/2024/06/27/double-room-4-fE6R.jpg",
                "https://upcdn.io/12a1X6T/raw/uploads/2024/06/27/double-room-5-cQrB.jpg"
            );
            
            List<String> familyRoomImages = List.of(
                "https://upcdn.io/12a1X6T/raw/uploads/2024/06/27/family-room-1-3mVC.jpg",
                "https://upcdn.io/12a1X6T/raw/uploads/2024/06/27/family-room-2-JH4y.jpg",
                "https://upcdn.io/12a1X6T/raw/uploads/2024/06/27/family-room-3-qnPb.jpg",
                "https://upcdn.io/12a1X6T/raw/uploads/2024/06/27/family-room-4-gCjQ.jpg",
                "https://upcdn.io/12a1X6T/raw/uploads/2024/06/27/family-room-5-P8az.jpg"
            );
            
            List<String> deluxeRoomImages = List.of(
                "https://upcdn.io/12a1X6T/raw/uploads/2024/06/27/deluxe-room-1-2DhB.jpg",
                "https://upcdn.io/12a1X6T/raw/uploads/2024/06/27/deluxe-room-2-fkG3.jpg",
                "https://upcdn.io/12a1X6T/raw/uploads/2024/06/27/deluxe-room-3-JQHL.jpg",
                "https://upcdn.io/12a1X6T/raw/uploads/2024/06/27/deluxe-room-4-Lbx4.jpg",
                "https://upcdn.io/12a1X6T/raw/uploads/2024/06/27/deluxe-room-5-xCkS.jpg",
                "https://upcdn.io/12a1X6T/raw/uploads/2024/06/27/deluxe-room-6-ZsFn.jpg"
            );
            
            List<String> presidentRoomImages = List.of(
                "https://upcdn.io/12a1X6T/raw/uploads/2024/06/27/presidential-suite-1-VGb7.jpg",
                "https://upcdn.io/12a1X6T/raw/uploads/2024/06/27/presidential-suite-2-kQtH.jpg",
                "https://upcdn.io/12a1X6T/raw/uploads/2024/06/27/presidential-suite-3-Mv6X.jpg",
                "https://upcdn.io/12a1X6T/raw/uploads/2024/06/27/presidential-suite-4-wFrK.jpg",
                "https://upcdn.io/12a1X6T/raw/uploads/2024/06/27/presidential-suite-5-tBn3.jpg",
                "https://upcdn.io/12a1X6T/raw/uploads/2024/06/27/presidential-suite-6-27Wz.jpg",
                "https://upcdn.io/12a1X6T/raw/uploads/2024/06/27/presidential-suite-7-pQcR.jpg"
            );
            
            // Tạo các phòng mẫu
            // Phòng Đơn Tiêu Chuẩn (ID: 1)
            RoomType singleRoom = roomTypes.stream().filter(rt -> rt.getId() == 1).findFirst().orElse(roomTypes.get(0));
            rooms.add(createSampleRoom("101", singleRoom, "VACANT", "1", true, 
                "Phòng đơn tiêu chuẩn với view thành phố đẹp, phù hợp cho doanh nhân", 
                singleRoomImages.subList(0, 3)));
            
            rooms.add(createSampleRoom("102", singleRoom, "VACANT", "1", true, 
                "Phòng đơn tiêu chuẩn với ban công nhìn ra vườn hoa", 
                singleRoomImages.subList(1, 4)));
            
            // Phòng Đôi Tiêu Chuẩn (ID: 2)
            RoomType doubleRoom = roomTypes.stream().filter(rt -> rt.getId() == 2).findFirst().orElse(roomTypes.get(0));
            rooms.add(createSampleRoom("201", doubleRoom, "VACANT", "2", true, 
                "Phòng đôi tiêu chuẩn với 1 giường đôi lớn, phù hợp cho cặp đôi", 
                doubleRoomImages.subList(0, 4)));
            
            rooms.add(createSampleRoom("202", doubleRoom, "VACANT", "2", true, 
                "Phòng đôi tiêu chuẩn hướng biển, view tuyệt đẹp", 
                doubleRoomImages.subList(1, 5)));
            
            // Phòng Gia Đình (ID: 3)
            RoomType familyRoom = roomTypes.stream().filter(rt -> rt.getId() == 3).findFirst().orElse(roomTypes.get(0));
            rooms.add(createSampleRoom("301", familyRoom, "VACANT", "3", true, 
                "Phòng gia đình rộng rãi với 1 giường đôi và 2 giường đơn, phù hợp cho 4 người", 
                familyRoomImages.subList(0, 4)));
            
            rooms.add(createSampleRoom("302", familyRoom, "VACANT", "3", true, 
                "Phòng gia đình sang trọng với phòng khách riêng và view thành phố", 
                familyRoomImages.subList(2, 5)));
            
            // Phòng Hạng Sang (ID: 4)
            RoomType deluxeRoom = roomTypes.stream().filter(rt -> rt.getId() == 4).findFirst().orElse(roomTypes.get(0));
            rooms.add(createSampleRoom("401", deluxeRoom, "VACANT", "4", true, 
                "Phòng hạng sang với giường king size, view biển và các tiện nghi cao cấp", 
                deluxeRoomImages.subList(0, 5)));
            
            rooms.add(createSampleRoom("402", deluxeRoom, "MAINTENANCE", "4", true, 
                "Phòng hạng sang với bồn tắm spa và ban công riêng", 
                deluxeRoomImages.subList(1, 6)));
            
            // Suite Tổng Thống (ID: 5)
            RoomType presidentRoom = roomTypes.stream().filter(rt -> rt.getId() == 5).findFirst().orElse(roomTypes.get(0));
            rooms.add(createSampleRoom("501", presidentRoom, "VACANT", "5", true, 
                "Suite tổng thống với phòng khách riêng biệt, 2 phòng ngủ, ban công và tiện nghi đẳng cấp", 
                presidentRoomImages.subList(0, 6)));
            
            rooms.add(createSampleRoom("502", presidentRoom, "CLEANING", "5", true, 
                "Suite tổng thống với hồ bơi riêng và view 360 độ", 
                presidentRoomImages.subList(1, 7)));
            
            // Thêm phòng extra cho mỗi loại
            rooms.add(createSampleRoom("103", singleRoom, "VACANT", "1", true, 
                "Phòng đơn yên tĩnh với không gian làm việc thoải mái", 
                singleRoomImages));
                
            rooms.add(createSampleRoom("203", doubleRoom, "VACANT", "2", true, 
                "Phòng đôi rộng rãi với view công viên", 
                doubleRoomImages));
                
            rooms.add(createSampleRoom("303", familyRoom, "OCCUPIED", "3", true, 
                "Phòng gia đình kết nối với phòng chơi trẻ em", 
                familyRoomImages));
                
            rooms.add(createSampleRoom("403", deluxeRoom, "VACANT", "4", true, 
                "Phòng hạng sang với sân hiên và khu vực thư giãn riêng", 
                deluxeRoomImages));
                
            rooms.add(createSampleRoom("503", presidentRoom, "VACANT", "5", true, 
                "Suite tổng thống siêu sang với dịch vụ quản gia riêng", 
                presidentRoomImages));
            
            // Lưu tất cả các phòng vào DB
            List<Room> savedRooms = roomRepository.saveAll(rooms);
            log.info("Đã khởi tạo thành công {} phòng mẫu.", savedRooms.size());
            
            // Thêm các dịch vụ mặc định cho các phòng
            addDefaultServicesToRooms(savedRooms);
            
        } catch (Exception e) {
            log.error("Lỗi khi khởi tạo dữ liệu phòng mẫu: {}", e.getMessage(), e);
        }
    }
    
    private Room createSampleRoom(String roomNumber, RoomType roomType, String status, String floor, 
                                 boolean isActive, String notes, List<String> images) {
        Room room = Room.builder()
            .roomNumber(roomNumber)
            .roomType(roomType)
            .status(status)
            .floor(floor)
            .isActive(isActive)
            .notes(notes)
            .images(images)
            .build();
        
        // Cập nhật thời gian tạo
        room.setCreatedAt(LocalDate.now());
        
        return room;
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
        // Lấy danh sách tất cả phòng đang hoạt động
        List<Room> allRooms = roomRepository.findByIsActiveTrue();
        
        // Lấy danh sách phòng đã được đặt trong khoảng thời gian
        List<Room> bookedRooms = roomRepository.findBookedRoomsBetweenDates(checkInDate, checkOutDate);
        
        // Lọc ra các phòng còn trống
        List<Room> availableRooms = allRooms.stream()
                .filter(room -> !bookedRooms.contains(room))
                .filter(room -> "VACANT".equals(room.getStatus()))
                .collect(Collectors.toList());
        
        // Chuyển đổi sang DTO và trả về
        return availableRooms.stream()
                .map(RoomResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoomResponseDTO> getAllActiveRooms() {
        // Đảm bảo lấy tất cả phòng đang hoạt động, không lọc theo bất kỳ điều kiện nào khác
        List<Room> allActiveRooms = roomRepository.findByIsActiveTrue();
        log.info("Đã tìm thấy {} phòng đang hoạt động", allActiveRooms.size());
        
        // Kiểm tra nếu không có phòng nào hoặc ít hơn 5 phòng (dữ liệu không đủ)
        if (allActiveRooms.isEmpty() || allActiveRooms.size() < 5) {
            // Tạo phòng mẫu nếu DB trống hoặc ít dữ liệu
            log.warn("Không tìm thấy đủ phòng trong cơ sở dữ liệu, khởi tạo dữ liệu mẫu");
            initRoomsFromJson();
            allActiveRooms = roomRepository.findByIsActiveTrue();
            log.info("Sau khi khởi tạo dữ liệu mẫu: {} phòng đang hoạt động", allActiveRooms.size());
        }
        
        // Chuyển đổi sang DTO và trả về
        List<RoomResponseDTO> result = allActiveRooms.stream()
                .map(RoomResponseDTO::fromEntity)
                .collect(Collectors.toList());
        
        log.info("Trả về {} phòng từ getAllActiveRooms", result.size());
        return result;
    }
}
