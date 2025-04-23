package com.spring3.hotel.management.services.impl;

import com.spring3.hotel.management.dto.request.AdminBookingRequest;
import com.spring3.hotel.management.dto.response.BookingResponseDTO;
import com.spring3.hotel.management.dto.response.NewBookingResponse;
import com.spring3.hotel.management.dto.response.RoomListResponseDTO;
import com.spring3.hotel.management.models.*;
import com.spring3.hotel.management.repositories.*;
import com.spring3.hotel.management.services.AdminBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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
    public List<BookingResponseDTO> getAllBookings(int page, int size) {
        try {
            // Step 1: Get basic booking information with user and discount
            Pageable pageable = PageRequest.of(page, size);
            Page<Booking> bookingsPage = bookingRepository.findAllWithDetails(pageable);
            List<Booking> bookings = bookingsPage.getContent();
            
            if (bookings.isEmpty()) {
                return new ArrayList<>();
            }
            
            // Step 2: Extract booking IDs
            List<Integer> bookingIds = bookings.stream()
                    .map(Booking::getId)
                    .collect(Collectors.toList());
            
            // Step 3: Load booking details in a separate query
            List<Booking> bookingsWithDetails = bookingRepository.findBookingsWithDetails(bookingIds);
            Map<Integer, List<BookingDetail>> detailsMap = new java.util.HashMap<>();
            for (Booking b : bookingsWithDetails) {
                detailsMap.put(b.getId(), b.getBookingDetails());
            }
            
            // Step 4: Load payments in a separate query
            List<Booking> bookingsWithPayments = bookingRepository.findBookingsWithPayments(bookingIds);
            Map<Integer, List<Payment>> paymentsMap = new java.util.HashMap<>();
            for (Booking b : bookingsWithPayments) {
                paymentsMap.put(b.getId(), b.getPayments());
            }
            
            // Step 5: Convert to DTO with all the data
            return bookings.stream()
                    .map(booking -> convertToBookingResponseDTOWithCollections(
                            booking, 
                            detailsMap.getOrDefault(booking.getId(), new ArrayList<>()),
                            paymentsMap.getOrDefault(booking.getId(), new ArrayList<>())
                    ))
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
    @Transactional
    public BookingResponseDTO updateBookingByAdmin(AdminBookingRequest request, Integer id) {
        log.info("Admin updating booking ID: {}", id);
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
        
        // Cập nhật trạng thái booking và paymentStatus của Booking
        booking.setStatus(request.getStatus());
        log.debug("Updating booking status to: {}", request.getStatus());
        String requestedPaymentStatus = request.getPaymentStatus();
        log.debug("Requested payment status: {}", requestedPaymentStatus);
        String newBookingPaymentStatus = requestedPaymentStatus;
        String newPaymentRecordStatus = "";
        boolean createNewPaymentRecord = false;

        if ("PAID".equalsIgnoreCase(requestedPaymentStatus)) {
            newBookingPaymentStatus = "PAID";
            newPaymentRecordStatus = "00";
            if (!List.of("CONFIRMED", "CHECKED_IN", "COMPLETED").contains(booking.getStatus())) {
                 booking.setStatus("CONFIRMED");
                 log.debug("Updating booking status to CONFIRMED due to PAID payment status.");
            }
            createNewPaymentRecord = true;
        } else if ("UNPAID".equalsIgnoreCase(requestedPaymentStatus) || "PENDING".equalsIgnoreCase(requestedPaymentStatus)) {
            newBookingPaymentStatus = "PENDING";
            newPaymentRecordStatus = "01";
        } else if ("REFUNDED".equalsIgnoreCase(requestedPaymentStatus)) {
            newBookingPaymentStatus = "REFUNDED";
            newPaymentRecordStatus = "REFUNDED";
             if (!"CANCELLED".equals(booking.getStatus())) {
                 booking.setStatus("CANCELLED");
                 log.debug("Updating booking status to CANCELLED due to REFUNDED payment status.");
            }
            createNewPaymentRecord = true;
        } else {
            log.warn("Unrecognized payment status requested: {}. Keeping it as is for booking paymentStatus.", requestedPaymentStatus);
            newBookingPaymentStatus = requestedPaymentStatus;
            newPaymentRecordStatus = requestedPaymentStatus;
        }

        booking.setPaymentStatus(newBookingPaymentStatus);
        log.debug("Setting booking payment status to: {}", newBookingPaymentStatus);
        
        try {
             final Booking bookingToSave = booking; // Sử dụng biến mới để lưu cuối cùng

            // 1. Xử lý Payment record trước
            if (createNewPaymentRecord) {
                log.debug("Creating new payment record for booking ID: {} with status: {}", id, newPaymentRecordStatus);
                Payment adminPayment = new Payment();
                adminPayment.setBooking(bookingToSave); // Liên kết với booking sẽ được lưu
                long amount = 0;
                if (bookingToSave.getFinalPrice() != null) amount = bookingToSave.getFinalPrice().longValue();
                else if (bookingToSave.getTotalPrice() != null) amount = bookingToSave.getTotalPrice().longValue();
                adminPayment.setAmount(amount);
                adminPayment.setStatus(newPaymentRecordStatus);
                adminPayment.setResponseCode(newPaymentRecordStatus); 
                adminPayment.setMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "ADMIN_UPDATE");
                if ("00".equals(newPaymentRecordStatus) || "REFUNDED".equals(newPaymentRecordStatus)) {
                    adminPayment.setPayDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
                }
                adminPayment.setOrderInfo("Admin update payment status to: " + newBookingPaymentStatus);
                paymentRepository.save(adminPayment);
                log.info("Successfully saved new payment record for booking ID: {}", id);
            } else {
                 log.debug("Not creating a new payment record for booking ID: {} (status: {})", id, newBookingPaymentStatus);
                 List<Payment> payments = paymentRepository.findByBooking_Id(id);
                 if (!payments.isEmpty()) {
                     Payment paymentToUpdate = payments.get(payments.size() - 1);
                     if (!newPaymentRecordStatus.equals(paymentToUpdate.getStatus())) {
                          log.debug("Updating latest payment record (ID: {}) status to: {}", paymentToUpdate.getId(), newPaymentRecordStatus);
                          paymentToUpdate.setStatus(newPaymentRecordStatus);
                          paymentToUpdate.setResponseCode(newPaymentRecordStatus);
                          paymentRepository.save(paymentToUpdate);
                          log.info("Successfully updated latest payment record (ID: {})", paymentToUpdate.getId());
                     }
                 }
            }
            
            // 2. Cập nhật booking details nếu có danh sách phòng mới
        if (request.getRoomIds() != null && !request.getRoomIds().isEmpty()) {
                 // Kiểm tra xem danh sách phòng có thực sự thay đổi không để tránh xóa/tạo lại không cần thiết
                 List<Integer> currentRoomIds = bookingDetailRepository.findAllByBooking_Id(id).stream()
                                                     .map(bd -> bd.getRoom().getId())
                                                     .sorted()
                                                     .toList();
                 List<Integer> requestedRoomIds = request.getRoomIds().stream().sorted().toList();

                 if (!currentRoomIds.equals(requestedRoomIds)) {
                     log.debug("Updating booking details for booking ID: {}", id);
            // Xóa booking details cũ
                     bookingDetailRepository.deleteAllByBookingId(bookingToSave.getId());
            
            // Tạo booking details mới
            for (Integer roomId : request.getRoomIds()) {
                Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));
                
                BookingDetail bookingDetail = new BookingDetail();
                         bookingDetail.setBooking(bookingToSave); // Liên kết với booking sẽ được lưu
                bookingDetail.setRoom(room);
                         bookingDetail.setPricePerNight(room.getRoomType().getBasePrice()); // Hoặc giá từ request nếu có
                bookingDetailRepository.save(bookingDetail);
                     }
                     log.info("Successfully updated booking details for booking ID: {}", id);
                 } else {
                      log.debug("Room IDs haven't changed for booking ID: {}. Skipping booking detail update.", id);
                 }
            }
            
            // 3. Lưu booking cuối cùng sau khi mọi thứ thành công
            log.info("Saving final booking state for ID: {} with paymentStatus: {}", id, bookingToSave.getPaymentStatus());
            Booking savedBooking = bookingRepository.save(bookingToSave);
            log.info("Admin update completed successfully for booking ID: {}", id);
            return convertToBookingResponseDTO(savedBooking);

        } catch (Exception e) {
            log.error("Error during admin update for booking ID: {}. Rolling back transaction.", id, e);
            throw new RuntimeException("Error updating booking by admin: " + e.getMessage(), e);
        }
    }

    @Override
    public BookingResponseDTO cancelBooking(Integer id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        
        booking.setStatus("CANCELLED");
        booking.setPaymentStatus("REFUNDED"); // Cập nhật trạng thái payment của booking
        bookingRepository.save(booking);
        
        // Tìm hoặc tạo payment record cho việc refund
        List<Payment> payments = paymentRepository.findByBooking_Id(id);
        // Luôn tạo mới record cho hành động cancel/refund từ admin
        Payment refundPayment = new Payment();
        refundPayment.setBooking(booking);
        refundPayment.setAmount(booking.getFinalPrice() != null ? booking.getFinalPrice().longValue() : booking.getTotalPrice().longValue());
        refundPayment.setStatus("REFUNDED"); // Trạng thái refund
        refundPayment.setResponseCode("REFUNDED"); // Có thể dùng mã khác nếu cần
        refundPayment.setMethod("CANCELLED_BY_ADMIN");
        refundPayment.setPayDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        refundPayment.setOrderInfo("Booking cancelled and refunded by admin");
        paymentRepository.save(refundPayment);
        
        return convertToBookingResponseDTO(booking);
    }

    @Override
    public BookingResponseDTO confirmBooking(Integer id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        
        booking.setStatus("CONFIRMED");
        booking.setPaymentStatus("PAID"); // Cập nhật trạng thái payment của booking
        bookingRepository.save(booking);
        
        // Tìm hoặc tạo payment record
        List<Payment> payments = paymentRepository.findByBooking_Id(id);
        Payment payment;
        if (!payments.isEmpty()) {
             // Ưu tiên cập nhật payment chưa có transaction_no (có thể là payment do admin tạo)
             payment = payments.stream()
                        .filter(p -> p.getTransactionNo() == null || p.getTransactionNo().isEmpty())
                        .findFirst()
                        .orElse(payments.get(payments.size() - 1)); // Nếu không có thì cập nhật cái mới nhất
            payment.setStatus("00"); // Trạng thái thành công
            payment.setResponseCode("00");
            payment.setMethod(payment.getMethod() != null ? payment.getMethod() : "CONFIRMED_BY_ADMIN");
             if (payment.getPayDate() == null) { // Chỉ đặt ngày nếu chưa có
                 payment.setPayDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
             }
        } else {
            payment = new Payment();
            payment.setBooking(booking);
            payment.setAmount(booking.getFinalPrice() != null ? booking.getFinalPrice().longValue() : booking.getTotalPrice().longValue());
            payment.setStatus("00");
            payment.setResponseCode("00");
            payment.setMethod("CONFIRMED_BY_ADMIN");
            payment.setPayDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            payment.setOrderInfo("Booking confirmed by admin");
        }
        paymentRepository.save(payment);
        
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

    @Override
    public List<BookingResponseDTO> getAllBookingsNoPage() {
        try {
            // Step 1: Get all bookings with user and discount
            List<Booking> bookings = bookingRepository.findAllWithDetailsNoPage();
            
            if (bookings.isEmpty()) {
                return new ArrayList<>();
            }
            
            // Step 2: Extract booking IDs
            List<Integer> bookingIds = bookings.stream()
                    .map(Booking::getId)
                    .collect(Collectors.toList());
            
            // Step 3: Load booking details in a separate query
            List<Booking> bookingsWithDetails = bookingRepository.findBookingsWithDetails(bookingIds);
            Map<Integer, List<BookingDetail>> detailsMap = new java.util.HashMap<>();
            for (Booking b : bookingsWithDetails) {
                detailsMap.put(b.getId(), b.getBookingDetails());
            }
            
            // Step 4: Load payments in a separate query
            List<Booking> bookingsWithPayments = bookingRepository.findBookingsWithPayments(bookingIds);
            Map<Integer, List<Payment>> paymentsMap = new java.util.HashMap<>();
            for (Booking b : bookingsWithPayments) {
                paymentsMap.put(b.getId(), b.getPayments());
            }
            
            // Step 5: Convert to DTO with all the data
            return bookings.stream()
                    .map(booking -> convertToBookingResponseDTOWithCollections(
                            booking, 
                            detailsMap.getOrDefault(booking.getId(), new ArrayList<>()), 
                            paymentsMap.getOrDefault(booking.getId(), new ArrayList<>())
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lấy danh sách booking: " + e.getMessage());
        }
    }

    // Phương thức hỗ trợ
    private BookingResponseDTO convertToBookingResponseDTO(Booking booking) {
        final Booking finalBooking = booking; // Sử dụng biến final này xuyên suốt
        BookingResponseDTO dto = new BookingResponseDTO();
        dto.setId(finalBooking.getId());
        dto.setUserId(finalBooking.getUser().getId());
        dto.setFullName(finalBooking.getUser().getFullName());
        dto.setNationalId(finalBooking.getUser().getNationalId());
        dto.setEmail(finalBooking.getUser().getEmail());
        dto.setPhone(finalBooking.getUser().getPhoneNumber());
        
        List<BookingDetail> bookingDetails = finalBooking.getBookingDetails() != null ? 
                finalBooking.getBookingDetails() : bookingDetailRepository.findAllByBooking_Id(finalBooking.getId());
        
        dto.setRooms(bookingDetails.stream()
                .map(detail -> {
                    RoomListResponseDTO roomDto = new RoomListResponseDTO();
                    Room room = detail.getRoom();
                    roomDto.setRoomId(room.getId());
                    roomDto.setRoomNumber(room.getRoomNumber());
                    roomDto.setRoomType(room.getRoomType().getName());
                    if (detail.getPrice() != null) {
                        roomDto.setPrice(detail.getPrice());
                    } else {
                        long days = java.time.temporal.ChronoUnit.DAYS.between(
                                 finalBooking.getCheckInDate(), finalBooking.getCheckOutDate());
                        if (days < 1) days = 1;
                         roomDto.setPrice(room.getRoomType().getPricePerNight() * days);
                    }
                    roomDto.setImages(room.getImages());
                    return roomDto;
                })
                .toList());
        
        dto.setCheckInDate(finalBooking.getCheckInDate());
        dto.setCheckOutDate(finalBooking.getCheckOutDate());
        dto.setTotalPrice(finalBooking.getTotalPrice());

        // Calculate final price - Chỉ tính toán, không lưu lại DB ở đây
        double finalPrice = finalBooking.getTotalPrice();
        if (finalBooking.getDiscount() != null) {
            Discount discount = discountRepository.findDiscountById(finalBooking.getDiscount().getId()); 
            if (discount != null) {
                if ("PERCENT".equals(discount.getDiscountType())) {
                    finalPrice = finalPrice * (1 - discount.getDiscountValue() / 100);
                } else if ("FIXED".equals(discount.getDiscountType())) {
                    finalPrice = finalPrice - discount.getDiscountValue();
                    if (finalPrice < 0) finalPrice = 0;
                }
                 dto.setDiscountCode(discount.getCode());
                 dto.setDiscountValue(discount.getDiscountValue());
                 dto.setDiscountType(discount.getDiscountType());
            } 
         } else if (finalBooking.getFinalPrice() != null) {
             // Nếu có finalPrice đã lưu trong booking thì dùng nó
             finalPrice = finalBooking.getFinalPrice();
         }
         // Chỉ gán giá trị finalPrice đã tính/lấy được vào DTO
        dto.setFinalPrice(finalPrice);
        
        // Lấy trạng thái booking và payment từ Booking entity
        dto.setStatus(finalBooking.getStatus());
        dto.setPaymentStatus(finalBooking.getPaymentStatus() != null ? finalBooking.getPaymentStatus() : "PENDING");

        // Lấy phương thức thanh toán từ payment mới nhất (nếu có) để tham khảo
        List<Payment> payments = finalBooking.getPayments() != null ? 
                finalBooking.getPayments() : paymentRepository.findAllByBooking_Id(finalBooking.getId());
        if (!payments.isEmpty()) {
             Payment latestPayment = payments.get(payments.size() - 1);
             dto.setPaymentMethod(latestPayment.getMethod() != null ? latestPayment.getMethod() : "UNKNOWN");
        } else {
             dto.setPaymentMethod("NONE");
        }

        // Sửa lỗi setCreatedAt - chuyển String sang LocalDateTime
        if (finalBooking.getCreatedAt() != null) {
            dto.setCreatedAt(finalBooking.getCreatedAt());
        }
        
        return dto;
    }
    
    private NewBookingResponse convertToNewBookingResponse(Booking booking) {
        NewBookingResponse response = new NewBookingResponse();
        response.setBookingId(booking.getId());
        response.setUserId(booking.getUser().getId());
        response.setFullName(booking.getUser().getFullName());
        // Sửa lỗi - User model không có avatar field
        response.setUserAvatar(""); // Để trống vì User không có avatar field
        response.setTotalAmount(booking.getTotalPrice());
        response.setBookingDate(booking.getCreatedAt());
        response.setStatus(booking.getStatus());
        
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
        dto.setPrice(room.getRoomType().getPricePerNight());
        dto.setImages(room.getImages());

        // Sửa lỗi - RoomListResponseDTO không có averageRating và totalReviews
        // dto.setAverageRating(room.getAverageRating());
        // dto.setTotalReviews(room.getRatingCount());
        
        return dto;
    }
    
    // Chỉ giữ lại một phương thức convertToBookingResponseDTOWithCollections để tối ưu code
    private BookingResponseDTO convertToBookingResponseDTOWithCollections(
            Booking booking, List<BookingDetail> bookingDetails, List<Payment> payments) {
        final Booking finalBooking = booking;
        BookingResponseDTO dto = new BookingResponseDTO();
        
        dto.setId(finalBooking.getId());
        dto.setUserId(finalBooking.getUser().getId());
        dto.setFullName(finalBooking.getUser().getFullName());
        dto.setNationalId(finalBooking.getUser().getNationalId());
        dto.setEmail(finalBooking.getUser().getEmail());
        dto.setPhone(finalBooking.getUser().getPhoneNumber());
        
        dto.setRooms(bookingDetails.stream()
                .map(detail -> {
                    RoomListResponseDTO roomDto = new RoomListResponseDTO();
                    Room room = detail.getRoom();
                    roomDto.setRoomId(room.getId());
                    roomDto.setRoomNumber(room.getRoomNumber());
                    roomDto.setRoomType(room.getRoomType().getName());
                    if (detail.getPrice() != null) {
                        roomDto.setPrice(detail.getPrice());
                    } else {
                        long days = java.time.temporal.ChronoUnit.DAYS.between(
                                 finalBooking.getCheckInDate(), finalBooking.getCheckOutDate());
                        if (days < 1) days = 1;
                        roomDto.setPrice(room.getRoomType().getPricePerNight() * days);
                    }
                    roomDto.setImages(room.getImages());
                    return roomDto;
                })
                .toList());
        
        dto.setCheckInDate(finalBooking.getCheckInDate());
        dto.setCheckOutDate(finalBooking.getCheckOutDate());
        dto.setTotalPrice(finalBooking.getTotalPrice());

        // Tính finalPrice
        double finalPrice = finalBooking.getTotalPrice();
         if (finalBooking.getDiscount() != null) {
             if ("PERCENT".equals(finalBooking.getDiscount().getDiscountType())) {
                 finalPrice = finalPrice * (1 - finalBooking.getDiscount().getDiscountValue() / 100);
             } else if ("FIXED".equals(finalBooking.getDiscount().getDiscountType())) {
                 finalPrice = finalPrice - finalBooking.getDiscount().getDiscountValue();
                if (finalPrice < 0) finalPrice = 0;
            }
             dto.setDiscountCode(finalBooking.getDiscount().getCode());
             dto.setDiscountValue(finalBooking.getDiscount().getDiscountValue());
             dto.setDiscountType(finalBooking.getDiscount().getDiscountType());
         } else if (finalBooking.getFinalPrice() != null) {
             finalPrice = finalBooking.getFinalPrice();
         }
        dto.setFinalPrice(finalPrice);
        
        // Trạng thái booking và payment
        dto.setStatus(finalBooking.getStatus());
        dto.setPaymentStatus(finalBooking.getPaymentStatus() != null ? finalBooking.getPaymentStatus() : "PENDING");

        // Lấy payment method
        if (!payments.isEmpty()) {
             Payment latestPayment = payments.get(payments.size() - 1);
             dto.setPaymentMethod(latestPayment.getMethod() != null ? latestPayment.getMethod() : "UNKNOWN");
        } else {
             dto.setPaymentMethod("NONE");
        }
        
        // Sửa lỗi - Sử dụng trực tiếp LocalDateTime thay vì string
        if (finalBooking.getCreatedAt() != null) {
            dto.setCreatedAt(finalBooking.getCreatedAt());
        }
        
        return dto;
    }
}
