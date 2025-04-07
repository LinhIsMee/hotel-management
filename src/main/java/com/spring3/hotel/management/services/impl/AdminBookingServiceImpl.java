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
        try {
            List<Booking> bookings = bookingRepository.findAll();
            return bookings.stream()
                    .map(this::convertToBookingResponseDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lấy danh sách booking: " + e.getMessage());
        }
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
                .map(detail -> {
                    RoomListResponseDTO roomDto = convertToRoomListResponseDTO(detail.getRoom());
                    // Ghi đè giá phòng từ booking detail
                    if (detail.getPrice() != null) {
                        roomDto.setPrice(detail.getPrice());
                    } else {
                        // Tính giá dựa vào số ngày ở
                        long days = java.time.temporal.ChronoUnit.DAYS.between(
                                booking.getCheckInDate(), booking.getCheckOutDate());
                        if (days < 1) days = 1;
                        roomDto.setPrice(detail.getRoom().getRoomType().getPricePerNight() * days);
                    }
                    return roomDto;
                })
                .toList());
        
        dto.setCheckInDate(booking.getCheckInDate());
        dto.setCheckOutDate(booking.getCheckOutDate());
        dto.setTotalPrice(booking.getTotalPrice());
        
        // Calculate final price
        double finalPrice = booking.getTotalPrice();
        if (booking.getDiscount() != null) {
            Discount discount = discountRepository.findDiscountById(booking.getDiscount().getId());
            if (discount != null) {
                dto.setDiscountCode(discount.getCode());
                dto.setDiscountValue(discount.getDiscountValue());
                dto.setDiscountType(discount.getDiscountType());
                
                // Tính giá sau khi giảm giá
                if ("PERCENT".equals(discount.getDiscountType())) {
                    finalPrice = finalPrice * (1 - discount.getDiscountValue() / 100);
                } else if ("FIXED".equals(discount.getDiscountType())) {
                    finalPrice = finalPrice - discount.getDiscountValue();
                    if (finalPrice < 0) finalPrice = 0;
                }
            }
        }
        dto.setFinalPrice(finalPrice);
        
        // Thông tin thanh toán - lấy payment mới nhất và đầy đủ
        List<Payment> payments = paymentRepository.findAllByBookingId(booking.getId());
        if (!payments.isEmpty()) {
            Payment payment = payments.get(payments.size() - 1); // Lấy payment mới nhất
            
            // Kiểm tra và đồng bộ trạng thái booking với payment
            if ("00".equals(payment.getStatus()) && !"CONFIRMED".equals(booking.getStatus())) {
                // Nếu payment thành công (00) nhưng booking chưa được xác nhận
                // Cập nhật trạng thái booking thành CONFIRMED
                booking.setStatus("CONFIRMED");
                bookingRepository.save(booking);
            }
            
            // Set thông tin payment cơ bản
            dto.setPaymentMethod(payment.getMethod());
            dto.setPaymentStatus(payment.getStatus());
            dto.setPaymentDate(payment.getPayDate());
            
            // Bổ sung thêm thông tin chi tiết payment
            dto.setTransactionNo(payment.getTransactionNo());
            dto.setAmount(payment.getAmount());
            dto.setBankCode(payment.getBankCode());
            
            // Đặt success và pending dựa vào trạng thái payment
            String transactionStatus = payment.getStatus();
            dto.setPaymentSuccess("00".equals(transactionStatus));
            dto.setPaymentPending("01".equals(transactionStatus) || "04".equals(transactionStatus) || 
                "05".equals(transactionStatus) || "06".equals(transactionStatus));
            
            // Định dạng số tiền và thời gian
            if (payment.getAmount() != null) {
                dto.setFormattedAmount(String.format("%,d", payment.getAmount()).replace(",", ".") + " ₫");
            }
            
            // Định dạng thời gian thanh toán
            if (payment.getPayDate() != null && payment.getPayDate().length() >= 14) {
                String payDate = payment.getPayDate();
                try {
                    String year = payDate.substring(0, 4);
                    String month = payDate.substring(4, 6);
                    String day = payDate.substring(6, 8);
                    String hour = payDate.substring(8, 10);
                    String minute = payDate.substring(10, 12);
                    String second = payDate.substring(12, 14);
                    
                    dto.setFormattedPaymentTime(day + "/" + month + "/" + year + " " + hour + ":" + minute + ":" + second);
                } catch (Exception e) {
                    System.out.println("Lỗi khi định dạng ngày thanh toán: " + e.getMessage());
                }
            }
        }
        
        // Cập nhật trạng thái dựa trên giá trị đã đồng bộ
        dto.setStatus(booking.getStatus());
        
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
        dto.setPrice(room.getRoomType().getPricePerNight()); // Giá cơ bản theo đêm
        
        // Lấy danh sách ảnh của phòng
        dto.setImages(room.getImages());
        
        return dto;
    }
} 