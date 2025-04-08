package com.spring3.hotel.management.services.impl;

import com.spring3.hotel.management.dtos.request.UpsertBookingRequest;
import com.spring3.hotel.management.dtos.response.BookingResponseDTO;
import com.spring3.hotel.management.dtos.response.NewBookingResponse;
import com.spring3.hotel.management.dtos.response.RoomListResponseDTO;
import com.spring3.hotel.management.models.*;
import com.spring3.hotel.management.repositories.*;
import com.spring3.hotel.management.services.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    DiscountRepository discountRepository;

    @Autowired
    UserRepository userRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private BookingDetailRepository bookingDetailRepository;

    @Override
    public BookingResponseDTO getBookingById(Integer id) {
        return bookingRepository.findById(id)
                .map(this::convertToBookingResponseDTO)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    @Override
    public List<BookingResponseDTO> getBookingsByUserId(Integer userId) {
        return bookingRepository.findByUserId(userId)
                .stream()
                .map(this::convertToBookingResponseDTO)
                .toList();
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
    public BookingResponseDTO createBooking(UpsertBookingRequest request) {
        // Kiểm tra xem có user không
        if (request.getUserId() == null) {
            throw new RuntimeException("User ID is required");
        }
        
        // Kiểm tra xem có danh sách phòng được chọn không
        if (request.getRoomIds() == null || request.getRoomIds().isEmpty()) {
            throw new RuntimeException("At least one room must be selected");
        }
        
        // Kiểm tra ngày check-in và check-out
        if (request.getCheckInDate() == null || request.getCheckOutDate() == null) {
            throw new RuntimeException("Check-in and check-out dates are required");
        }
        
        if (!request.getCheckOutDate().isAfter(request.getCheckInDate())) {
            throw new RuntimeException("Check-out date must be after check-in date");
        }
        
        // Tìm user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));
        
        // Kiểm tra xem phòng có sẵn không
        checkRoomAvailability(request.getRoomIds(), request.getCheckInDate(), request.getCheckOutDate());
        
        // Tạo mới booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        
        // Tính tổng giá phòng nếu không được chỉ định
        if (request.getTotalPrice() == null) {
            double totalPrice = 0;
            long stayDuration = java.time.temporal.ChronoUnit.DAYS.between(
                    request.getCheckInDate(), request.getCheckOutDate());
            if (stayDuration < 1) stayDuration = 1;
            
            for (Integer roomId : request.getRoomIds()) {
                Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));
                totalPrice += room.getRoomType().getBasePrice() * stayDuration;
            }
            booking.setTotalPrice(totalPrice);
        } else {
            booking.setTotalPrice(request.getTotalPrice());
        }
        
        // Tính finalPrice dựa trên discount nếu có
        if (request.getDiscountId() != null) {
            Discount discount = discountRepository.findById(request.getDiscountId())
                .orElse(null);
            
            if (discount != null) {
                booking.setDiscount(discount);
                
                // Tính giá sau khi giảm giá
                double finalPrice = booking.getTotalPrice();
                if ("PERCENT".equals(discount.getDiscountType())) {
                    finalPrice = finalPrice * (1 - discount.getDiscountValue() / 100);
                } else if ("FIXED".equals(discount.getDiscountType())) {
                    finalPrice = finalPrice - discount.getDiscountValue();
                    if (finalPrice < 0) finalPrice = 0;
                }
                
                booking.setFinalPrice(finalPrice);
            }
        } else {
            // Nếu không có discount thì finalPrice = totalPrice
            booking.setFinalPrice(booking.getTotalPrice());
        }
        
        // Đặt trạng thái mặc định là PENDING nếu không được chỉ định
        booking.setStatus(request.getStatus() != null ? request.getStatus() : "PENDING");
        
        booking = bookingRepository.save(booking);
        
        // Tạo payment với trạng thái từ request
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getFinalPrice().longValue());
        payment.setStatus(request.getPaymentStatus() != null ? request.getPaymentStatus() : "UNPAID");
        payment.setMethod(request.getPaymentMethod());
        if (request.getPaymentDate() != null) {
            payment.setPayDate(request.getPaymentDate().toString());
        }
        paymentRepository.save(payment);
        
        // Tạo booking details cho các phòng được chọn
        double totalPriceCalculated = 0;
        long stayDuration = java.time.temporal.ChronoUnit.DAYS.between(
                request.getCheckInDate(), request.getCheckOutDate());
        
        for (Integer roomId : request.getRoomIds()) {
            Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));
            
            // Cập nhật trạng thái phòng thành BOOKED
            room.setStatus("BOOKED");
            roomRepository.save(room);
            
            // Tính giá phòng theo số ngày ở
            double roomPrice = room.getRoomType().getBasePrice() * stayDuration;
            totalPriceCalculated += roomPrice;
            
            BookingDetail bookingDetail = new BookingDetail();
            bookingDetail.setBooking(booking);
            bookingDetail.setRoom(room);
            bookingDetail.setPricePerNight(room.getRoomType().getBasePrice());
            bookingDetail.setPrice(roomPrice);
            bookingDetailRepository.save(bookingDetail);
        }
        
        // Kiểm tra và cập nhật tổng giá nếu cần
        if (Math.abs(totalPriceCalculated - booking.getTotalPrice()) > 0.01) {
            booking.setTotalPrice(totalPriceCalculated);
            // Cập nhật lại finalPrice nếu totalPrice thay đổi
            if (booking.getDiscount() != null) {
                Discount discount = booking.getDiscount();
                double finalPrice = totalPriceCalculated;
                if ("PERCENT".equals(discount.getDiscountType())) {
                    finalPrice = finalPrice * (1 - discount.getDiscountValue() / 100);
                } else if ("FIXED".equals(discount.getDiscountType())) {
                    finalPrice = finalPrice - discount.getDiscountValue();
                    if (finalPrice < 0) finalPrice = 0;
                }
                booking.setFinalPrice(finalPrice);
            } else {
                booking.setFinalPrice(totalPriceCalculated);
            }
            
            Booking savedBooking = bookingRepository.save(booking);
            
            // Cập nhật lại payment nếu có
            final Integer bookingId = savedBooking.getId();
            List<Payment> payments = paymentRepository.findByBooking_Id(bookingId);
            if (!payments.isEmpty()) {
                Payment paymentRecord = payments.get(0);
                paymentRecord.setAmount(savedBooking.getFinalPrice().longValue());
                paymentRepository.save(paymentRecord);
            }
            
            return convertToBookingResponseDTO(savedBooking);
        }
        
        return convertToBookingResponseDTO(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDTO updateBooking(UpsertBookingRequest request, Integer id) {
        // Kiểm tra tính hợp lệ của ngày
        if (request.getCheckInDate() == null || request.getCheckOutDate() == null) {
            throw new IllegalArgumentException("Ngày nhận phòng và trả phòng không được để trống");
        }
        
        if (request.getCheckInDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày nhận phòng không thể là ngày trong quá khứ");
        }
        
        if (request.getCheckOutDate().isBefore(request.getCheckInDate())) {
            throw new IllegalArgumentException("Ngày trả phòng phải sau ngày nhận phòng");
        }
        
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        
        // Không cho phép cập nhật booking đã CHECKED_IN hoặc CHECKED_OUT
        if ("CHECKED_IN".equals(booking.getStatus()) || "CHECKED_OUT".equals(booking.getStatus())) {
            throw new IllegalStateException("Không thể cập nhật booking có trạng thái: " + booking.getStatus());
        }
        
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        booking.setUser(user);
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setTotalPrice(request.getTotalPrice());
        
        // Xử lý trường hợp discountId là null
        if (request.getDiscountId() != null) {
            Discount discount = discountRepository.findDiscountById(request.getDiscountId());
            booking.setDiscount(discount);
        } else {
            booking.setDiscount(null); // Đặt giá trị discount là null nếu request không có discountId
        }
        
        booking.setStatus(request.getStatus());
        
        // Cập nhật booking details nếu có danh sách phòng mới
        if (request.getRoomIds() != null && !request.getRoomIds().isEmpty()) {
            // Kiểm tra xem các phòng có khả dụng không
            // Lấy danh sách phòng đã đặt trong khoảng thời gian, ngoại trừ booking hiện tại
            List<Room> bookedRooms = roomRepository.findAll().stream()
                    .filter(room -> {
                        // Lấy các booking của phòng này
                        List<BookingDetail> bookingDetails = bookingDetailRepository.findAll().stream()
                                .filter(bd -> bd.getRoom() != null && bd.getRoom().getId().equals(room.getId()))
                                .collect(Collectors.toList());
                                
                        // Kiểm tra xem có booking nào khác (không phải booking hiện tại) mà có overlap thời gian
                        return bookingDetails.stream()
                                .anyMatch(bd -> {
                                    // Bỏ qua nếu là booking hiện tại
                                    if (bd.getBooking().getId().equals(id)) {
                                        return false;
                                    }
                                    
                                    // Bỏ qua booking đã bị hủy
                                    if ("CANCELLED".equals(bd.getBooking().getStatus())) {
                                        return false;
                                    }
                                    
                                    // Kiểm tra overlap thời gian
                                    return (bd.getBooking().getCheckInDate().isEqual(request.getCheckInDate()) 
                                            || bd.getBooking().getCheckInDate().isAfter(request.getCheckInDate()))
                                            && (bd.getBooking().getCheckInDate().isEqual(request.getCheckOutDate()) 
                                            || bd.getBooking().getCheckInDate().isBefore(request.getCheckOutDate()))
                                            || (bd.getBooking().getCheckOutDate().isEqual(request.getCheckInDate()) 
                                            || bd.getBooking().getCheckOutDate().isAfter(request.getCheckInDate()))
                                            && (bd.getBooking().getCheckOutDate().isEqual(request.getCheckOutDate()) 
                                            || bd.getBooking().getCheckOutDate().isBefore(request.getCheckOutDate()))
                                            || (bd.getBooking().getCheckInDate().isBefore(request.getCheckInDate()) 
                                            && bd.getBooking().getCheckOutDate().isAfter(request.getCheckOutDate()));
                                });
                    })
                    .collect(Collectors.toList());
                    
            // Lấy danh sách ID của các phòng đã đặt
            List<Integer> bookedRoomIds = bookedRooms.stream()
                    .map(Room::getId)
                    .collect(Collectors.toList());
            
            // Kiểm tra xem có phòng nào trong request đã được đặt hay không
            List<Integer> unavailableRoomIds = request.getRoomIds().stream()
                    .filter(bookedRoomIds::contains)
                    .collect(Collectors.toList());
            
            if (!unavailableRoomIds.isEmpty()) {
                // Lấy thông tin các phòng không khả dụng để hiển thị thông báo chi tiết
                List<Room> unavailableRooms = roomRepository.findAllById(unavailableRoomIds);
                String roomNumbers = unavailableRooms.stream()
                        .map(Room::getRoomNumber)
                        .collect(Collectors.joining(", "));
                
                throw new RuntimeException("Các phòng sau không khả dụng trong khoảng thời gian đã chọn: " + roomNumbers);
            }
            
            // Kiểm tra trạng thái phòng (trừ các phòng đã thuộc booking hiện tại)
            List<Room> currentBookingRooms = booking.getBookingDetails().stream()
                    .map(BookingDetail::getRoom)
                    .collect(Collectors.toList());
                    
            List<Integer> currentBookingRoomIds = currentBookingRooms.stream()
                    .map(Room::getId)
                    .collect(Collectors.toList());
                    
            List<Room> newRoomsToCheck = roomRepository.findAllById(
                    request.getRoomIds().stream()
                            .filter(rid -> !currentBookingRoomIds.contains(rid))
                            .collect(Collectors.toList()));
                            
            List<Room> invalidStatusRooms = newRoomsToCheck.stream()
                    .filter(room -> !"VACANT".equals(room.getStatus()) && !"BOOKED".equals(room.getStatus()))
                    .collect(Collectors.toList());
            
            if (!invalidStatusRooms.isEmpty()) {
                String roomNumbers = invalidStatusRooms.stream()
                        .map(room -> room.getRoomNumber() + " (" + room.getStatus() + ")")
                        .collect(Collectors.joining(", "));
                
                throw new RuntimeException("Các phòng sau có trạng thái không hợp lệ để đặt: " + roomNumbers);
            }
            
            // Xóa booking details cũ
            List<BookingDetail> oldDetails = bookingDetailRepository.findAllByBooking_Id(booking.getId());
            for (BookingDetail detail : oldDetails) {
                // Khôi phục trạng thái phòng về VACANT nếu phòng không còn trong danh sách mới
                if (detail.getRoom() != null && !request.getRoomIds().contains(detail.getRoom().getId())) {
                    Room room = detail.getRoom();
                    room.setStatus("VACANT");
                    roomRepository.save(room);
                }
            }
            bookingDetailRepository.deleteAllByBookingId(booking.getId());
            
            // Tạo booking details mới
            double totalPriceCalculated = 0;
            long stayDuration = java.time.temporal.ChronoUnit.DAYS.between(
                    request.getCheckInDate(), request.getCheckOutDate());
                    
            for (Integer roomId : request.getRoomIds()) {
                Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));
                
                // Cập nhật trạng thái phòng thành BOOKED
                room.setStatus("BOOKED");
                roomRepository.save(room);
                
                // Tính giá phòng theo số ngày ở
                double roomPrice = room.getRoomType().getBasePrice() * stayDuration;
                totalPriceCalculated += roomPrice;
                
                BookingDetail bookingDetail = new BookingDetail();
                bookingDetail.setBooking(booking);
                bookingDetail.setRoom(room);
                bookingDetail.setPricePerNight(room.getRoomType().getBasePrice());
                bookingDetail.setPrice(roomPrice);
                bookingDetailRepository.save(bookingDetail);
            }
            
            // Kiểm tra và cập nhật tổng giá nếu cần
            if (Math.abs(totalPriceCalculated - booking.getTotalPrice()) > 0.01) {
                booking.setTotalPrice(totalPriceCalculated);
                Booking savedBooking = bookingRepository.save(booking);
                
                // Cập nhật lại payment nếu có
                final Integer bookingId = savedBooking.getId();
                List<Payment> payments = paymentRepository.findByBooking_Id(bookingId);
                if (!payments.isEmpty()) {
                    Payment paymentRecord = payments.get(0);
                    paymentRecord.setAmount(savedBooking.getTotalPrice().longValue());
                    paymentRepository.save(paymentRecord);
                }
                
                return convertToBookingResponseDTO(savedBooking);
            }
            
            return convertToBookingResponseDTO(bookingRepository.save(booking));
        }
        
        return convertToBookingResponseDTO(bookingRepository.save(booking));
    }

    // Phương thức mới: hủy booking
    @Override
    public BookingResponseDTO cancelBooking(Integer id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        
        // Chỉ có thể hủy booking nếu trạng thái là PENDING hoặc CONFIRMED
        if (!"PENDING".equals(booking.getStatus()) && !"CONFIRMED".equals(booking.getStatus())) {
            throw new RuntimeException("Không thể hủy booking có trạng thái: " + booking.getStatus());
        }
        
        // Cập nhật trạng thái booking thành CANCELLED
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);
        
        // Cập nhật trạng thái payment nếu có
        List<Payment> payments = paymentRepository.findByBooking_Id(id);
        if (!payments.isEmpty()) {
            Payment payment = payments.get(0);
            payment.setStatus("REFUNDED");
            paymentRepository.save(payment);
        }
        
        return convertToBookingResponseDTO(booking);
    }
    
    // Phương thức mới: lấy danh sách booking trong khoảng thời gian
    @Override
    public List<BookingResponseDTO> getBookingsByDateRange(LocalDate startDate, LocalDate endDate) {
        // Tìm tất cả các booking có checkInDate nằm trong khoảng từ startDate đến endDate
        // hoặc checkOutDate nằm trong khoảng từ startDate đến endDate
        // hoặc bookings bao phủ khoảng thời gian từ startDate đến endDate
        List<Booking> bookings = bookingRepository.findAll().stream()
                .filter(booking -> 
                    // checkInDate nằm trong khoảng
                    (booking.getCheckInDate().isEqual(startDate) || booking.getCheckInDate().isAfter(startDate)) 
                        && (booking.getCheckInDate().isEqual(endDate) || booking.getCheckInDate().isBefore(endDate))
                    // hoặc checkOutDate nằm trong khoảng
                    || (booking.getCheckOutDate().isEqual(startDate) || booking.getCheckOutDate().isAfter(startDate)) 
                        && (booking.getCheckOutDate().isEqual(endDate) || booking.getCheckOutDate().isBefore(endDate))
                    // hoặc booking bao phủ khoảng thời gian
                    || (booking.getCheckInDate().isBefore(startDate) && booking.getCheckOutDate().isAfter(endDate))
                )
                .collect(Collectors.toList());
        
        return bookings.stream()
                .map(this::convertToBookingResponseDTO)
                .toList();
    }
    
    // Phương thức mới: xác nhận booking sau khi thanh toán
    @Override
    public BookingResponseDTO confirmBooking(Integer id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        
        // Chỉ cho phép xác nhận booking đang ở trạng thái PENDING
        if (!"PENDING".equals(booking.getStatus())) {
            throw new IllegalStateException("Chỉ có thể xác nhận booking ở trạng thái PENDING. Trạng thái hiện tại: " + booking.getStatus());
        }
        
        // Kiểm tra xem đã thanh toán chưa
        List<Payment> payments = paymentRepository.findByBooking_Id(id);
        Payment payment = payments.isEmpty() ? null : payments.get(0);
        
        // Nếu có payment với trạng thái 00 (thành công), cập nhật
        boolean hasSuccessfulPayment = payment != null && "00".equals(payment.getStatus());
        
        if (hasSuccessfulPayment) {
            // Đã thanh toán thành công, cập nhật trạng thái booking
            booking.setStatus("CONFIRMED");
            booking = bookingRepository.save(booking);
            System.out.println("Đã xác nhận booking #" + id + " sau khi kiểm tra thanh toán thành công");
        } else {
            // Chưa thanh toán, vẫn xác nhận booking (manual confirm)
            booking.setStatus("CONFIRMED");
            booking = bookingRepository.save(booking);
            System.out.println("Đã xác nhận booking #" + id + " thủ công mặc dù chưa thanh toán");
        }
        
        return convertToBookingResponseDTO(booking);
    }
    
    // Phương thức mới: lấy danh sách phòng đã được đặt trong khoảng thời gian
    @Override
    public List<RoomListResponseDTO> getBookedRoomsByDateRange(LocalDate startDate, LocalDate endDate) {
        // Lấy tất cả các booking trong khoảng thời gian
        List<Booking> bookings = bookingRepository.findAll().stream()
                .filter(booking -> 
                    // Bỏ qua các booking bị hủy
                    !"CANCELLED".equals(booking.getStatus())
                    // Lọc các booking có overlap với khoảng thời gian
                    && (
                        // checkInDate nằm trong khoảng
                        (booking.getCheckInDate().isEqual(startDate) || booking.getCheckInDate().isAfter(startDate)) 
                            && (booking.getCheckInDate().isEqual(endDate) || booking.getCheckInDate().isBefore(endDate))
                        // hoặc checkOutDate nằm trong khoảng
                        || (booking.getCheckOutDate().isEqual(startDate) || booking.getCheckOutDate().isAfter(startDate)) 
                            && (booking.getCheckOutDate().isEqual(endDate) || booking.getCheckOutDate().isBefore(endDate))
                        // hoặc booking bao phủ khoảng thời gian
                        || (booking.getCheckInDate().isBefore(startDate) && booking.getCheckOutDate().isAfter(endDate))
                    )
                )
                .collect(Collectors.toList());
        
        // Lấy tất cả các booking detail của các booking này
        List<BookingDetail> bookingDetails = new ArrayList<>();
        for (Booking booking : bookings) {
            bookingDetails.addAll(bookingDetailRepository.findAllByBooking_Id(booking.getId()));
        }
        
        // Lấy danh sách phòng từ booking detail
        return bookingDetails.stream()
                .map(bookingDetail -> {
                    Room room = bookingDetail.getRoom();
                    RoomListResponseDTO dto = new RoomListResponseDTO();
                    dto.setRoomId(room.getId());
                    dto.setRoomNumber(room.getRoomNumber());
                    dto.setRoomType(room.getRoomType().getName());
                    dto.setPrice(room.getRoomType().getBasePrice());
                    return dto;
                })
                .distinct() // Loại bỏ các phòng trùng lặp
                .toList();
    }

    // Scheduled task chạy mỗi ngày lúc 00:00:00
    @Scheduled(cron = "0 0 0 * * ?") // Cron expression: Chạy vào lúc 00:00 mỗi ngày
    public void updateBookingStatusAutomatically() {
        LocalDate today = LocalDate.now();
        updateBookingsToCheckedIn(today);
        updateBookingsToCheckedOut(today);
    }

    // Lấy ra danh sách booking mới nhất trong 7 ngày
    @Override
    public List<NewBookingResponse> getRecentBookings() {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(7);
        return bookingRepository.findRecentBookings(startDate, endDate)
                .stream()
                .map(this::convertToNewBookingResponse)
                .toList();
    }

    private NewBookingResponse convertToNewBookingResponse(Booking booking) {
        User user = userRepository.findById(booking.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<BookingDetail> bookingDetails = bookingDetailRepository.findAllByBooking_Id(booking.getId());
        NewBookingResponse newBookingResponse = new NewBookingResponse();
        newBookingResponse.setBookingId(booking.getId());
        newBookingResponse.setUserId(user.getId());
        newBookingResponse.setFullName(user.getFullName());
        newBookingResponse.setRoomCount(bookingDetails.size());
        return newBookingResponse;
    }

    // Cập nhật trạng thái thành 'CheckedIn' nếu đến ngày checkInDate
    private void updateBookingsToCheckedIn(LocalDate today) {
        List<Booking> bookingsToCheckIn = bookingRepository.findBookingsToCheckIn(today);
        for (Booking booking : bookingsToCheckIn) {
            booking.setStatus("CHECKED IN");
            bookingRepository.save(booking);
            System.out.println("Booking ID " + booking.getId() + " has been updated to CheckedIn.");
        }
    }

    // Cập nhật trạng thái thành 'CheckedOut' nếu đến ngày checkOutDate
    private void updateBookingsToCheckedOut(LocalDate today) {
        List<Booking> bookingsToCheckOut = bookingRepository.findBookingsToCheckOut(today);
        for (Booking booking : bookingsToCheckOut) {
            booking.setStatus("CHECKED OUT");
            bookingRepository.save(booking);
            System.out.println("Booking ID " + booking.getId() + " has been updated to CheckedOut.");
        }
    }

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
        List<RoomListResponseDTO> roomList = new ArrayList<>();
        
        for (BookingDetail detail : bookingDetails) {
            Room room = detail.getRoom();
            RoomListResponseDTO roomDto = new RoomListResponseDTO();
            roomDto.setRoomId(room.getId());
            roomDto.setRoomNumber(room.getRoomNumber() != null ? room.getRoomNumber() : "");
            roomDto.setRoomType(room.getRoomType() != null ? room.getRoomType().getName() : "");
            roomDto.setPrice(detail.getPrice());
            
            // Thêm danh sách hình ảnh
            roomDto.setImages(room.getImages());
            
            roomList.add(roomDto);
        }
        
        dto.setRooms(roomList);
        dto.setCheckInDate(booking.getCheckInDate());
        dto.setCheckOutDate(booking.getCheckOutDate());
        dto.setTotalPrice(booking.getTotalPrice());
        
        // Tính finalPrice nếu chưa được tính
        if (booking.getFinalPrice() != null) {
            dto.setFinalPrice(booking.getFinalPrice());
        } else if (booking.getDiscount() != null) {
            Discount discount = booking.getDiscount();
            double finalPrice = booking.getTotalPrice();
            if ("PERCENT".equals(discount.getDiscountType())) {
                finalPrice = finalPrice * (1 - discount.getDiscountValue() / 100);
            } else if ("FIXED".equals(discount.getDiscountType())) {
                finalPrice = finalPrice - discount.getDiscountValue();
                if (finalPrice < 0) finalPrice = 0;
            }
            dto.setFinalPrice(finalPrice);
        } else {
            dto.setFinalPrice(booking.getTotalPrice());
        }
        
        // Lấy thông tin payment
        List<Payment> payments = paymentRepository.findByBooking_Id(booking.getId());
        Payment payment = null;
        if (!payments.isEmpty()) {
            // Tìm payment với status là "00" (thành công) trước
            payment = payments.stream()
                    .filter(p -> "00".equals(p.getStatus()))
                    .findFirst()
                    .orElse(payments.get(0)); // Nếu không có payment thành công, lấy payment đầu tiên
            
            // Cập nhật thông tin payment vào DTO
            dto.setPaymentMethod(payment.getMethod() != null ? payment.getMethod() : "VNPAY");
            dto.setPaymentStatus(payment.getStatus() != null ? payment.getStatus() : "UNPAID");
            dto.setPaymentDate(payment.getPayDate() != null ? payment.getPayDate() : "");
            dto.setTransactionNo(payment.getTransactionNo() != null ? payment.getTransactionNo() : "");
            dto.setAmount(payment.getAmount() != null ? payment.getAmount() : (booking.getFinalPrice() != null ? booking.getFinalPrice().longValue() : 0L));
            dto.setBankCode(payment.getBankCode() != null ? payment.getBankCode() : "");
            
            // Cập nhật trạng thái success và pending
            dto.setPaymentSuccess("00".equals(payment.getStatus()));
            dto.setPaymentPending("01".equals(payment.getStatus()) || "04".equals(payment.getStatus()) 
                    || "05".equals(payment.getStatus()) || "06".equals(payment.getStatus()));
            
            // Format lại số tiền và thời gian thanh toán
            dto.setFormattedAmount(String.format("%,d", payment.getAmount() != null ? payment.getAmount() : 0).replace(",", ".") + " ₫");
            
            // Định dạng ngày giờ thanh toán nếu có
            if (payment.getPayDate() != null && payment.getPayDate().length() >= 14) {
                try {
                    String payDate = payment.getPayDate();
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
        
        // Đặt trạng thái booking sau khi đã đồng bộ
        dto.setStatus(booking.getStatus());
        
        if (booking.getCreatedAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            dto.setCreatedAt(booking.getCreatedAt().format(formatter));
        }
        
        return dto;
    }

    private void checkRoomAvailability(List<Integer> roomIds, LocalDate checkInDate, LocalDate checkOutDate) {
        for (Integer roomId : roomIds) {
            // Kiểm tra xem phòng có tồn tại không
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng với ID: " + roomId));

            // Bỏ kiểm tra phòng có đang trống hay không
            // Chỉ kiểm tra phòng đã bị đặt trong khoảng thời gian này chưa
            
            // Kiểm tra xem phòng có bị đặt trong khoảng thời gian này không
            List<BookingDetail> existingBookings = bookingDetailRepository.findByRoomIdAndDateRange(
                    roomId, checkInDate, checkOutDate);
            
            for (BookingDetail detail : existingBookings) {
                // Bỏ qua các booking đã hủy
                if (!"CANCELLED".equals(detail.getBooking().getStatus())) {
                    throw new RuntimeException("Phòng " + roomId + " đã được đặt trong khoảng thời gian này");
                }
            }
        }
    }

    @Override
    public Map<String, Object> getBookingPaymentInfo(Integer bookingId) {
        // Lấy thông tin booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));
        
        // Lấy thông tin payment - lấy payment thành công hoặc mới nhất
        List<Payment> payments = paymentRepository.findByBooking_Id(bookingId);
        
        // Tìm payment thành công hoặc lấy payment mới nhất
        Payment payment = null;
        if (!payments.isEmpty()) {
            // Tìm payment với status là "00" (thành công) trước
            payment = payments.stream()
                    .filter(p -> "00".equals(p.getStatus()))
                    .findFirst()
                    .orElse(payments.get(0)); // Nếu không có payment thành công, lấy payment đầu tiên
        }
        
        Map<String, Object> result = new HashMap<>();
        
        // Thông tin cơ bản
        result.put("bookingId", booking.getId());
        result.put("totalPrice", booking.getTotalPrice());
        result.put("status", booking.getStatus());
        
        // Thông tin thanh toán
        if (payment != null) {
            result.put("paymentId", payment.getId());
            result.put("paymentStatus", payment.getStatus() != null ? payment.getStatus() : "UNPAID");
            result.put("paymentMethod", payment.getMethod() != null ? payment.getMethod() : "VNPAY");
            result.put("paymentDate", payment.getPayDate() != null ? payment.getPayDate() : "");
            result.put("transactionNo", payment.getTransactionNo() != null ? payment.getTransactionNo() : "");
            result.put("amount", payment.getAmount() != null ? payment.getAmount() : 0);
            result.put("bankCode", payment.getBankCode() != null ? payment.getBankCode() : "");
            
            // Các trường thông tin bổ sung
            String transactionStatus = payment.getStatus() != null ? payment.getStatus() : "UNPAID";
            boolean success = "00".equals(transactionStatus);
            boolean pending = "01".equals(transactionStatus) || "04".equals(transactionStatus) || 
                    "05".equals(transactionStatus) || "06".equals(transactionStatus);
            
            result.put("success", success);
            result.put("pending", pending);
            
            // Định dạng số tiền
            if (payment.getAmount() != null) {
                result.put("formattedAmount", String.format("%,d", payment.getAmount()).replace(",", ".") + " ₫");
            } else {
                result.put("formattedAmount", "0 ₫");
            }
            
            // Định dạng ngày giờ thanh toán
            if (payment.getPayDate() != null && payment.getPayDate().length() >= 14) {
                String payDate = payment.getPayDate();
                String year = payDate.substring(0, 4);
                String month = payDate.substring(4, 6);
                String day = payDate.substring(6, 8);
                String hour = payDate.substring(8, 10);
                String minute = payDate.substring(10, 12);
                String second = payDate.substring(12, 14);
                
                result.put("formattedPaymentTime", day + "/" + month + "/" + year + " " + hour + ":" + minute + ":" + second);
            } else {
                result.put("formattedPaymentTime", "");
            }
        } else {
            // Cung cấp giá trị mặc định cho tất cả các trường
            result.put("paymentId", 0);
            result.put("paymentStatus", "UNPAID");
            result.put("paymentMethod", "VNPAY");
            result.put("paymentDate", "");
            result.put("transactionNo", "");
            result.put("amount", booking.getTotalPrice().longValue());
            result.put("bankCode", "");
            result.put("success", false);
            result.put("pending", false);
            result.put("formattedAmount", String.format("%,d", booking.getTotalPrice().longValue()).replace(",", ".") + " ₫");
            result.put("formattedPaymentTime", "");
        }
        
        return result;
    }
    
    @Override
    public List<BookingResponseDTO> getConfirmedBookingsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Booking> bookings = bookingRepository.findByStatusAndCheckInDateBetween("CONFIRMED", startDate, endDate);
        return bookings.stream()
                .map(this::convertToBookingResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponseDTO createBookingTest(UpsertBookingRequest request) {
        try {
            // Tạo booking
            BookingResponseDTO bookingResponseDTO = createBooking(request);
            Integer bookingId = bookingResponseDTO.getId();
            
            // Tạo thanh toán (nếu yêu cầu)
            if ("PAID".equals(request.getPaymentStatus()) || "00".equals(request.getPaymentStatus())) {
                // Tìm payment đã tạo
                List<Payment> payments = paymentRepository.findByBooking_Id(bookingId);
                if (!payments.isEmpty()) {
                    Payment payment = payments.get(0);
                    // Cập nhật trạng thái thành công
                    payment.setStatus("00");
                    payment.setResponseCode("00");
                    payment.setMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "CASH");
                    if (request.getPaymentDate() != null) {
                        payment.setPayDate(request.getPaymentDate().toString());
                    } else {
                        payment.setPayDate(LocalDateTime.now().toString());
                    }
                    paymentRepository.save(payment);
                    
                    // Xác nhận booking
                    confirmBooking(bookingId);
                }
            }
            
            return getBookingById(bookingId);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo booking test: " + e.getMessage());
        }
    }

    @Override
    public BookingResponseDTO markBookingAsPaid(Integer bookingId, String paymentMethod) {
        // Kiểm tra booking có tồn tại không
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại với ID: " + bookingId));
        
        // Chỉ cho phép thanh toán cho booking PENDING hoặc CONFIRMED
        if (!"PENDING".equals(booking.getStatus()) && !"CONFIRMED".equals(booking.getStatus())) {
            throw new IllegalStateException("Chỉ có thể thanh toán cho booking PENDING hoặc CONFIRMED. Trạng thái hiện tại: " + booking.getStatus());
        }
        
        // Tìm payment hiện có hoặc tạo mới
        List<Payment> payments = paymentRepository.findByBooking_Id(bookingId);
        Payment payment;
        
        if (!payments.isEmpty()) {
            payment = payments.get(0);
            // Cập nhật trạng thái thanh toán
            payment.setStatus("00"); // 00: Thành công
            payment.setResponseCode("00");
            payment.setMethod(paymentMethod != null ? paymentMethod : "CASH");
            payment.setAmount(booking.getFinalPrice().longValue());
            payment.setPayDate(LocalDateTime.now().toString());
        } else {
            // Tạo mới payment
            payment = new Payment();
            payment.setBooking(booking);
            payment.setAmount(booking.getFinalPrice().longValue());
            payment.setStatus("00");
            payment.setResponseCode("00");
            payment.setMethod(paymentMethod != null ? paymentMethod : "CASH");
            payment.setPayDate(LocalDateTime.now().toString());
            payment.setOrderInfo("Thanh toán đặt phòng #" + bookingId);
        }
        
        // Lưu payment
        payment = paymentRepository.save(payment);
        
        // Cập nhật trạng thái booking thành CONFIRMED
        if (!"CONFIRMED".equals(booking.getStatus())) {
            booking.setStatus("CONFIRMED");
            bookingRepository.save(booking);
        }
        
        return getBookingById(bookingId);
    }
}
