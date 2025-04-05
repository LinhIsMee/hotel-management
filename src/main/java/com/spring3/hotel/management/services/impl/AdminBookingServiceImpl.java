package com.spring3.hotel.management.services.impl;

import com.spring3.hotel.management.dtos.request.AdminBookingRequest;
import com.spring3.hotel.management.dtos.response.BookingResponseDTO;
import com.spring3.hotel.management.dtos.response.NewBookingResponse;
import com.spring3.hotel.management.dtos.response.RoomListResponseDTO;
import com.spring3.hotel.management.models.*;
import com.spring3.hotel.management.repositories.*;
import com.spring3.hotel.management.services.AdminBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminBookingServiceImpl implements AdminBookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private DiscountRepository discountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingDetailRepository bookingDetailRepository;

    @Override
    public List<NewBookingResponse> getRecentBookings() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Booking> recentBookings = bookingRepository.findAll().stream()
                .filter(booking -> booking.getCreatedAt().isAfter(sevenDaysAgo))
                .collect(Collectors.toList());

        return recentBookings.stream()
                .map(this::convertToNewBookingResponse)
                .toList();
    }

    @Override
    public BookingResponseDTO getBookingById(Integer id) {
        return bookingRepository.findById(id)
                .map(this::convertToBookingResponseDTO)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    @Override
    public List<BookingResponseDTO> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(this::convertToBookingResponseDTO)
                .toList();
    }

    @Override
    public List<BookingResponseDTO> getBookingsByStatus(String status) {
        return bookingRepository.findByStatus(status)
                .stream()
                .map(this::convertToBookingResponseDTO)
                .toList();
    }

    @Override
    public List<BookingResponseDTO> getBookingsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Booking> bookings = bookingRepository.findAll().stream()
                .filter(booking -> 
                    (booking.getCheckInDate().isEqual(startDate) || booking.getCheckInDate().isAfter(startDate)) 
                        && (booking.getCheckInDate().isEqual(endDate) || booking.getCheckInDate().isBefore(endDate))
                    || (booking.getCheckOutDate().isEqual(startDate) || booking.getCheckOutDate().isAfter(startDate)) 
                        && (booking.getCheckOutDate().isEqual(endDate) || booking.getCheckOutDate().isBefore(endDate))
                    || (booking.getCheckInDate().isBefore(startDate) && booking.getCheckOutDate().isAfter(endDate))
                )
                .collect(Collectors.toList());
        
        return bookings.stream()
                .map(this::convertToBookingResponseDTO)
                .toList();
    }

    @Override
    public List<RoomListResponseDTO> getBookedRoomsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Booking> bookings = bookingRepository.findAll().stream()
                .filter(booking -> 
                    !"CANCELLED".equals(booking.getStatus())
                    && (
                        (booking.getCheckInDate().isEqual(startDate) || booking.getCheckInDate().isAfter(startDate)) 
                            && (booking.getCheckInDate().isEqual(endDate) || booking.getCheckInDate().isBefore(endDate))
                        || (booking.getCheckOutDate().isEqual(startDate) || booking.getCheckOutDate().isAfter(startDate)) 
                            && (booking.getCheckOutDate().isEqual(endDate) || booking.getCheckOutDate().isBefore(endDate))
                        || (booking.getCheckInDate().isBefore(startDate) && booking.getCheckOutDate().isAfter(endDate))
                    )
                )
                .collect(Collectors.toList());
        
        List<BookingDetail> bookingDetails = new ArrayList<>();
        for (Booking booking : bookings) {
            bookingDetails.addAll(bookingDetailRepository.findAllByBooking_Id(booking.getId()));
        }
        
        return bookingDetails.stream()
                .map(bookingDetail -> {
                    Room room = bookingDetail.getRoom();
                    return convertToRoomListResponseDTO(room);
                })
                .distinct()
                .toList();
    }

    @Override
    public BookingResponseDTO createBookingByAdmin(AdminBookingRequest request) {
        Booking booking = new Booking();
        
        // Xử lý userId - admin có thể tạo đặt phòng cho bất kỳ người dùng nào
        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            booking.setUser(user);
        } else {
            throw new RuntimeException("userId không được để trống");
        }
        
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setTotalPrice(request.getTotalPrice());
        
        if (request.getDiscountId() != null) {
            Discount discount = discountRepository.findDiscountById(request.getDiscountId());
            booking.setDiscount(discount);
        }
        
        // Admin có thể đặt trạng thái ngay từ đầu
        booking.setStatus(request.getStatus() != null ? request.getStatus() : "CONFIRMED");
        
        // Lưu booking để có ID cho booking details
        booking = bookingRepository.save(booking);
        
        // Tạo payment với trạng thái đã được admin xác nhận
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalPrice().longValue());
        payment.setStatus(request.getPaymentStatus() != null ? request.getPaymentStatus() : "PAID");
        payment.setMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "CASH");
        if (request.getPaymentDate() != null) {
            payment.setPayDate(request.getPaymentDate().toString());
        } else {
            payment.setPayDate(LocalDate.now().toString());
        }
        paymentRepository.save(payment);
        
        // Tạo booking details cho các phòng được chọn
        if (request.getRoomIds() != null && !request.getRoomIds().isEmpty()) {
            for (Integer roomId : request.getRoomIds()) {
                Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));
                
                BookingDetail bookingDetail = new BookingDetail();
                bookingDetail.setBooking(booking);
                bookingDetail.setRoom(room);
                bookingDetail.setPricePerNight(room.getRoomType().getBasePrice());
                bookingDetailRepository.save(bookingDetail);
            }
        } else {
            throw new RuntimeException("Danh sách phòng không được để trống");
        }
        
        return convertToBookingResponseDTO(booking);
    }

    @Override
    public BookingResponseDTO updateBookingByAdmin(AdminBookingRequest request, Integer id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        
        // Admin có thể cập nhật thông tin người dùng
        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            booking.setUser(user);
        }
        
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setTotalPrice(request.getTotalPrice());
        
        if (request.getDiscountId() != null) {
            Discount discount = discountRepository.findDiscountById(request.getDiscountId());
            booking.setDiscount(discount);
        } else {
            booking.setDiscount(null);
        }
        
        booking.setStatus(request.getStatus());
        
        // Lưu booking để có ID cho booking details
        booking = bookingRepository.save(booking);
        
        // Cập nhật payment nếu có thông tin thanh toán mới
        paymentRepository.findByBookingId(id).ifPresent(payment -> {
            if (request.getPaymentMethod() != null) {
                payment.setMethod(request.getPaymentMethod());
            }
            if (request.getPaymentStatus() != null) {
                payment.setStatus(request.getPaymentStatus());
            }
            if (request.getPaymentDate() != null) {
                payment.setPayDate(request.getPaymentDate().toString());
            }
            paymentRepository.save(payment);
        });
        
        // Cập nhật booking details nếu có danh sách phòng mới
        if (request.getRoomIds() != null && !request.getRoomIds().isEmpty()) {
            // Xóa booking details cũ
            bookingDetailRepository.deleteAllByBookingId(booking.getId());
            
            // Tạo booking details mới
            for (Integer roomId : request.getRoomIds()) {
                Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));
                
                BookingDetail bookingDetail = new BookingDetail();
                bookingDetail.setBooking(booking);
                bookingDetail.setRoom(room);
                bookingDetail.setPricePerNight(room.getRoomType().getBasePrice());
                bookingDetailRepository.save(bookingDetail);
            }
        }
        
        return convertToBookingResponseDTO(booking);
    }

    @Override
    public BookingResponseDTO cancelBooking(Integer id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        
        // Admin có thể hủy booking ở bất kỳ trạng thái nào
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);
        
        // Cập nhật trạng thái payment nếu có
        paymentRepository.findByBookingId(id).ifPresent(payment -> {
            payment.setStatus("REFUNDED");
            paymentRepository.save(payment);
        });
        
        return convertToBookingResponseDTO(booking);
    }

    @Override
    public BookingResponseDTO confirmBooking(Integer id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        
        // Admin có thể xác nhận booking từ bất kỳ trạng thái nào
        booking.setStatus("CONFIRMED");
        bookingRepository.save(booking);
        
        // Cập nhật trạng thái payment thành PAID
        paymentRepository.findByBookingId(id).ifPresent(payment -> {
            payment.setStatus("PAID");
            paymentRepository.save(payment);
        });
        
        return convertToBookingResponseDTO(booking);
    }

    @Override
    public BookingResponseDTO checkInBooking(Integer id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        
        // Chỉ có thể check-in nếu trạng thái là CONFIRMED
        if (!"CONFIRMED".equals(booking.getStatus())) {
            throw new RuntimeException("Không thể check-in booking có trạng thái: " + booking.getStatus());
        }
        
        // Cập nhật trạng thái booking thành CHECKED_IN
        booking.setStatus("CHECKED_IN");
        bookingRepository.save(booking);
        
        return convertToBookingResponseDTO(booking);
    }

    @Override
    public BookingResponseDTO checkOutBooking(Integer id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        
        // Chỉ có thể check-out nếu trạng thái là CHECKED_IN
        if (!"CHECKED_IN".equals(booking.getStatus())) {
            throw new RuntimeException("Không thể check-out booking có trạng thái: " + booking.getStatus());
        }
        
        // Cập nhật trạng thái booking thành COMPLETED
        booking.setStatus("COMPLETED");
        bookingRepository.save(booking);
        
        return convertToBookingResponseDTO(booking);
    }

    @Override
    @Transactional
    public void deleteBooking(Integer id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        
        // Kiểm tra điều kiện để xóa booking (nếu cần)
        if ("CHECKED_IN".equals(booking.getStatus())) {
            throw new RuntimeException("Không thể xóa booking đang trong trạng thái CHECKED_IN");
        }
        
        // Xóa booking details trước
        bookingDetailRepository.deleteAllByBookingId(id);
        
        // Xóa payment liên quan đến booking
        paymentRepository.deleteByBookingId(id);
        
        // Xóa booking
        bookingRepository.deleteById(id);
    }

    // Phương thức hỗ trợ
    private BookingResponseDTO convertToBookingResponseDTO(Booking booking) {
        BookingResponseDTO dto = new BookingResponseDTO();
        dto.setId(booking.getId());
        dto.setUserId(booking.getUser().getId());
        dto.setFullName(booking.getUser().getFullName());
        dto.setNationalId(booking.getUser().getNationalId());
        dto.setEmail(booking.getUser().getEmail());
        dto.setPhone(booking.getUser().getPhoneNumber());
        
        // Lấy danh sách phòng từ booking_details
        List<BookingDetail> bookingDetails = bookingDetailRepository.findAllByBooking_Id(booking.getId());
        dto.setRooms(bookingDetails.stream()
                .map(bookingDetail -> convertToRoomListResponseDTO(bookingDetail.getRoom()))
                .toList());
        
        dto.setCheckInDate(booking.getCheckInDate());
        dto.setCheckOutDate(booking.getCheckOutDate());
        dto.setTotalPrice(booking.getTotalPrice());
        
        // Calculate final price
        double finalPrice = booking.getTotalPrice();
        if (booking.getDiscount() != null) {
            dto.setDiscountCode(booking.getDiscount().getCode());
            dto.setDiscountValue(booking.getDiscount().getDiscountValue());
            dto.setDiscountType(booking.getDiscount().getDiscountType());
            
            // Tính giá sau khi giảm giá
            if ("PERCENT".equals(booking.getDiscount().getDiscountType())) {
                finalPrice = finalPrice * (1 - booking.getDiscount().getDiscountValue() / 100);
            } else if ("FIXED".equals(booking.getDiscount().getDiscountType())) {
                finalPrice = finalPrice - booking.getDiscount().getDiscountValue();
                if (finalPrice < 0) finalPrice = 0;
            }
        }
        dto.setFinalPrice(finalPrice);
        
        dto.setStatus(booking.getStatus());
        
        // Thông tin thanh toán
        paymentRepository.findByBookingId(booking.getId()).ifPresent(payment -> {
            dto.setPaymentMethod(payment.getMethod());
            dto.setPaymentStatus(payment.getStatus());
            dto.setPaymentDate(payment.getPayDate());
        });
        
        // Format createdAt
        if (booking.getCreatedAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            dto.setCreatedAt(booking.getCreatedAt().format(formatter));
        }
        
        return dto;
    }
    
    private NewBookingResponse convertToNewBookingResponse(Booking booking) {
        NewBookingResponse response = new NewBookingResponse();
        response.setBookingId(booking.getId());
        response.setPrice(booking.getTotalPrice());
        response.setUserId(booking.getUser().getId());
        response.setFullName(booking.getUser().getFullName());
        
        // Lấy số lượng phòng đã đặt
        List<BookingDetail> bookingDetails = bookingDetailRepository.findAllByBooking_Id(booking.getId());
        response.setRoomCount(bookingDetails.size());
        
        return response;
    }
    
    private RoomListResponseDTO convertToRoomListResponseDTO(Room room) {
        RoomListResponseDTO dto = new RoomListResponseDTO();
        dto.setRoomId(room.getId());
        dto.setRoomNumber(room.getRoomNumber());
        dto.setRoomType(room.getRoomType().getName());
        dto.setPrice(room.getRoomType().getBasePrice());
        
        // Lấy ảnh đầu tiên của phòng nếu có
        if (room.getImages() != null && !room.getImages().isEmpty()) {
            dto.setImagePath(room.getImages().get(0));
        }
        
        return dto;
    }
} 