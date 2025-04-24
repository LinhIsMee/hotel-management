package com.spring3.hotel.management.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring3.hotel.management.dto.request.AdminBookingRequest;
import com.spring3.hotel.management.dto.request.BookingDetailRequest;
import com.spring3.hotel.management.dto.request.BookingRoomRequest;
import com.spring3.hotel.management.dto.request.UpsertBookingRequest;
import com.spring3.hotel.management.dto.response.BookingResponseDTO;
import com.spring3.hotel.management.dto.response.NewBookingResponse;
import com.spring3.hotel.management.dto.response.RoomListResponseDTO;
import com.spring3.hotel.management.dto.response.ServiceResponseDTO;
import com.spring3.hotel.management.exceptions.ResourceNotFoundException;
import com.spring3.hotel.management.models.*;
import com.spring3.hotel.management.repositories.*;
import com.spring3.hotel.management.services.BookingService;
import com.spring3.hotel.management.services.impl.BookingServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import com.spring3.hotel.management.enums.BookingStatus;
import com.spring3.hotel.management.enums.PaymentMethod;
import com.spring3.hotel.management.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
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
    @Autowired
    private ServiceRepository serviceRepository;

    @Override
    public BookingResponseDTO getBookingById(Integer id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        
        BookingResponseDTO dto = convertToBookingResponseDTO(booking);
        
        // Lấy danh sách chi tiết đặt phòng
        List<BookingDetail> bookingDetails = bookingDetailRepository.findAllByBooking_Id(booking.getId());
        
        if (bookingDetails != null) {
            dto.setRooms(bookingDetails.stream()
                    .map(this::mapToRoomListResponseDTO)
                    .collect(Collectors.toList()));
        } else {
            dto.setRooms(Collections.emptyList());
        }
        
        // Lấy danh sách dịch vụ đã đặt
        List<com.spring3.hotel.management.models.BookingService> bookingServices = 
                bookingServiceRepository.findByBooking(booking);
        if (bookingServices != null && !bookingServices.isEmpty()) {
            List<ServiceResponseDTO> services = bookingServices.stream()
                    .map(bs -> {
                        ServiceResponseDTO serviceDTO = new ServiceResponseDTO();
                        serviceDTO.setId(bs.getOffering().getId());
                        serviceDTO.setName(bs.getOffering().getName());
                        serviceDTO.setDescription(bs.getOffering().getDescription());
                        serviceDTO.setPrice(BigDecimal.valueOf(bs.getOffering().getPrice()));
                        serviceDTO.setQuantity(bs.getQuantity());
                        serviceDTO.setTotalPrice(BigDecimal.valueOf(bs.getTotalPrice()));
                        return serviceDTO;
                    })
                    .collect(Collectors.toList());
            dto.setServices(services);
        } else {
            dto.setServices(Collections.emptyList());
        }
        
        return dto;
    }

    // Phương thức mới để map BookingDetail sang RoomListResponseDTO
    private RoomListResponseDTO mapToRoomListResponseDTO(BookingDetail bookingDetail) {
        RoomListResponseDTO dto = new RoomListResponseDTO();
        Room room = bookingDetail.getRoom();
        dto.setRoomId(room.getId());
        dto.setRoomNumber(room.getRoomNumber());
        dto.setRoomType(room.getRoomType().getName());
        
        // if (bookingDetail.getPrice() != null) {
        //     dto.setPrice(bookingDetail.getPrice());
        // } else {
        //     // Tính giá từ pricePerNight nếu có booking
        //     if (bookingDetail.getBooking() != null) {
        //         long days = java.time.temporal.ChronoUnit.DAYS.between(
        //                 bookingDetail.getBooking().getCheckInDate(), 
        //                 bookingDetail.getBooking().getCheckOutDate());
        //         if (days < 1) days = 1;
        //         dto.setPrice(room.getRoomType().getPricePerNight() * days);
        //     } else {
        //         // Nếu không có booking thì dùng giá cơ bản
        //         dto.setPrice(room.getRoomType().getPricePerNight());
        //     }
        // }
        
        // dto.setImages(room.getImages());
        return dto;
    }

    @Override
    public List<BookingResponseDTO> getBookingsByUserId(Integer userId) {
        List<Booking> bookings = bookingRepository.findByUserId(userId);
        return bookings.stream()
                .map(booking -> getBookingById(booking.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDTO> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAllWithDetailsNoPage();
        return bookings.stream()
                .map(this::convertToBookingResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDTO> getBookingsByStatus(String status) {
        BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
        List<Booking> bookings = bookingRepository.findByStatus(bookingStatus);
        return bookings.stream()
                .map(booking -> getBookingById(booking.getId()))
                .collect(Collectors.toList());
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
                totalPrice += room.getRoomType().getPricePerNight() * stayDuration;
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
        booking.setStatus(BookingStatus.PENDING);
        
        booking = bookingRepository.save(booking);
        
        // Tạo payment với trạng thái từ request
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalPrice().longValue());
        try {
            payment.setStatus(request.getPaymentStatus() != null ? PaymentStatus.valueOf(request.getPaymentStatus().toUpperCase()) : PaymentStatus.PENDING);
            if (request.getPaymentStatus() == null) {
                log.debug("PaymentStatus not provided, defaulting to PENDING.");
            }
        } catch (IllegalArgumentException e) {
            log.warn("Invalid PaymentStatus: {}. Defaulting to PENDING", request.getPaymentStatus());
            payment.setStatus(PaymentStatus.PENDING);
        }
        try {
            payment.setMethod(request.getPaymentMethod() != null ? PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase()) : null);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid PaymentMethod: {}. Setting to null", request.getPaymentMethod());
            payment.setMethod(null);
        }
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
            room.setStatus(RoomStatus.OCCUPIED);
            roomRepository.save(room);
            
            // Tính giá phòng theo số ngày ở
            double roomPrice = room.getRoomType().getPricePerNight() * stayDuration;
            totalPriceCalculated += roomPrice;
            
            BookingDetail bookingDetail = new BookingDetail();
            bookingDetail.setBooking(booking);
            bookingDetail.setRoom(room);
            bookingDetail.setPricePerNight(room.getRoomType().getPricePerNight());
            bookingDetail.setPrice(roomPrice);
            bookingDetailRepository.save(bookingDetail);
        }
        
        // Xử lý các dịch vụ bổ sung nếu có
        if (request.getAdditionalServices() != null && !request.getAdditionalServices().isEmpty()) {
            for (String serviceId : request.getAdditionalServices()) {
                try {
                    Integer offeringId = Integer.parseInt(serviceId);
                    Offering offering = offeringRepository.findById(offeringId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy dịch vụ với id: " + offeringId));
                    
                    // Mặc định số lượng là 1 nếu không được chỉ định
                    int quantity = 1;
                    double serviceTotalPrice = offering.getPrice() * quantity;
                    
                    // Tạo BookingService
                    com.spring3.hotel.management.models.BookingService bookingService 
                        = new com.spring3.hotel.management.models.BookingService();
                    bookingService.setBooking(booking);
                    bookingService.setOffering(offering);
                    bookingService.setQuantity(quantity);
                    bookingService.setTotalPrice(serviceTotalPrice);
                    bookingServiceRepository.save(bookingService);
                    
                    // Cập nhật tổng giá booking
                    totalPriceCalculated += serviceTotalPrice;
                } catch (NumberFormatException e) {
                    log.error("Lỗi chuyển đổi ID dịch vụ: " + serviceId, e);
                } catch (Exception e) {
                    log.error("Lỗi khi thêm dịch vụ vào booking: " + e.getMessage(), e);
                }
            }
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
        if (booking.getStatus() == BookingStatus.CHECKED_IN || booking.getStatus() == BookingStatus.CHECKED_OUT) {
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
        
        booking.setStatus(BookingStatus.PENDING);
        
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
                                    if (bd.getBooking().getStatus() == BookingStatus.CANCELLED) {
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
                    .filter(room -> room.getStatus() != RoomStatus.VACANT && room.getStatus() != RoomStatus.OCCUPIED)
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
                    room.setStatus(RoomStatus.VACANT);
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
                room.setStatus(RoomStatus.OCCUPIED);
                roomRepository.save(room);
                
                // Tính giá phòng theo số ngày ở
                double roomPrice = room.getRoomType().getPricePerNight() * stayDuration;
                totalPriceCalculated += roomPrice;
                
                BookingDetail bookingDetail = new BookingDetail();
                bookingDetail.setBooking(booking);
                bookingDetail.setRoom(room);
                bookingDetail.setPricePerNight(room.getRoomType().getPricePerNight());
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
                    paymentRecord.setAmount(savedBooking.getFinalPrice().longValue());
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
        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Không thể hủy booking có trạng thái: " + booking.getStatus());
        }
        
        // Cập nhật trạng thái booking thành CANCELLED
        booking.setStatus(BookingStatus.CANCELLED);
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
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể xác nhận booking ở trạng thái PENDING. Trạng thái hiện tại: " + booking.getStatus());
        }
        
        // Kiểm tra xem đã thanh toán chưa
        List<Payment> payments = paymentRepository.findByBooking_Id(id);
        Payment payment = payments.isEmpty() ? null : payments.get(0);
        
        // Nếu có payment với trạng thái 00 (thành công), cập nhật
        boolean hasSuccessfulPayment = payment != null && "00".equals(payment.getStatus());
        
        if (hasSuccessfulPayment) {
            // Đã thanh toán thành công, cập nhật trạng thái booking
            booking.setStatus(BookingStatus.CONFIRMED);
            booking = bookingRepository.save(booking);
            System.out.println("Đã xác nhận booking #" + id + " sau khi kiểm tra thanh toán thành công");
        } else {
            // Chưa thanh toán, vẫn xác nhận booking (manual confirm)
            booking.setStatus(BookingStatus.CONFIRMED);
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
                    booking.getStatus() != BookingStatus.CANCELLED
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
                .map(this::mapToRoomListResponseDTO)
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
        newBookingResponse.setFullName(user.getFirstName() + " " + user.getLastName());
        newBookingResponse.setRoomCount(bookingDetails.size());
        newBookingResponse.setEmail(user.getEmail());
        newBookingResponse.setPhone(user.getPhoneNumber());
        return newBookingResponse;
    }

    // Cập nhật trạng thái thành 'CheckedIn' nếu đến ngày checkInDate
    private void updateBookingsToCheckedIn(LocalDate today) {
        List<Booking> bookingsToCheckIn = bookingRepository.findBookingsToCheckIn(today);
        for (Booking booking : bookingsToCheckIn) {
            booking.setStatus(BookingStatus.CHECKED_IN);
            bookingRepository.save(booking);
            System.out.println("Booking ID " + booking.getId() + " has been updated to CheckedIn.");
        }
    }

    // Cập nhật trạng thái thành 'CheckedOut' nếu đến ngày checkOutDate
    private void updateBookingsToCheckedOut(LocalDate today) {
        List<Booking> bookingsToCheckOut = bookingRepository.findBookingsToCheckOut(today);
        for (Booking booking : bookingsToCheckOut) {
            booking.setStatus(BookingStatus.CHECKED_OUT);
            bookingRepository.save(booking);
            System.out.println("Booking ID " + booking.getId() + " has been updated to CheckedOut.");
        }
    }

    private BookingResponseDTO convertToBookingResponseDTO(Booking booking) {
        final Booking finalBooking = booking;
        BookingResponseDTO dto = new BookingResponseDTO();
        dto.setId(finalBooking.getId());
        dto.setUserId(finalBooking.getUser().getId());
        dto.setFullName(finalBooking.getUser().getFirstName() + " " + finalBooking.getUser().getLastName());
        dto.setNationalId(finalBooking.getUser().getNationalId());
        dto.setEmail(finalBooking.getUser().getEmail());
        dto.setPhone(finalBooking.getUser().getPhoneNumber());
        
        List<BookingDetail> bookingDetails = finalBooking.getBookingDetails() != null ? 
                finalBooking.getBookingDetails() : bookingDetailRepository.findAllByBooking_Id(finalBooking.getId());
        
        if (bookingDetails != null) {
            dto.setRooms(bookingDetails.stream()
                    .map(this::mapToRoomListResponseDTO)
                    .collect(Collectors.toList()));
        } else {
            dto.setRooms(Collections.emptyList());
        }
        
        dto.setCheckInDate(finalBooking.getCheckInDate());
        dto.setCheckOutDate(finalBooking.getCheckOutDate());
        dto.setTotalPrice(finalBooking.getTotalPrice());
        
        // Final price calculation
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

        // Set status and payment status from Booking entity
        dto.setStatus(finalBooking.getStatus() != null ? finalBooking.getStatus().name() : null);
        dto.setPaymentStatus(finalBooking.getPaymentStatus() != null ? finalBooking.getPaymentStatus() : "PENDING");

        // Get payment method from the latest payment record if available
        List<Payment> payments = paymentRepository.findByBookingIdOrderByCreatedAtDesc(finalBooking.getId());
        if (!payments.isEmpty()) {
             Payment latestPayment = payments.get(0);
             dto.setPaymentMethod(latestPayment.getMethod() != null ? latestPayment.getMethod().name() : "UNKNOWN");
             dto.setPaymentStatus(latestPayment.getStatus() != null ? latestPayment.getStatus().name() : "UNKNOWN");
        } else {
             dto.setPaymentMethod("NONE");
             dto.setPaymentStatus("PENDING");
        }
        
        // Format createdAt
        if (finalBooking.getCreatedAt() != null) {
            dto.setCreatedAt(finalBooking.getCreatedAt());
        }
        
        // Lấy danh sách dịch vụ đã đặt
        List<com.spring3.hotel.management.models.BookingService> bookingServices = bookingServiceRepository.findByBooking(finalBooking);
        if (bookingServices != null && !bookingServices.isEmpty()) {
            List<ServiceResponseDTO> services = bookingServices.stream().map(bs -> {
                Offering offering = bs.getOffering();
                ServiceResponseDTO serviceDTO = ServiceResponseDTO.fromOffering(offering);
                serviceDTO.setQuantity(bs.getQuantity());
                serviceDTO.setTotalPrice(BigDecimal.valueOf(bs.getTotalPrice()));
                return serviceDTO;
            }).collect(Collectors.toList());
            dto.setServices(services);
        } else {
            dto.setServices(Collections.emptyList());
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
                if (detail.getBooking().getStatus() != BookingStatus.CANCELLED) {
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
            String transactionStatus = payment.getStatus() != null ? payment.getStatus() : "UNKNOWN";
            boolean success = PaymentStatus.SUCCESS.equals(transactionStatus);
            boolean pending = PaymentStatus.PENDING.equals(transactionStatus);
            
            result.put("isSuccess", success);
            result.put("isPending", pending);
            
            // Định dạng số tiền
            String formattedAmount = "N/A";
            if (payment.getAmount() != null) {
                formattedAmount = String.format("%,d", payment.getAmount()).replace(",", ".") + " ₫";
            }
            result.put("formattedAmount", formattedAmount);
            
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
            result.put("isSuccess", false);
            result.put("isPending", false);
            result.put("formattedAmount", String.format("%,d", booking.getTotalPrice().longValue()).replace(",", ".") + " ₫");
            result.put("formattedPaymentTime", "");
        }
        
        return result;
    }
    
    @Override
    public List<BookingResponseDTO> getConfirmedBookingsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Booking> bookings = bookingRepository.findByStatusAndCheckInDateBetween(BookingStatus.CONFIRMED, startDate, endDate);
        return bookings.stream()
                .map(this::convertToBookingResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponseDTO markBookingAsPaid(Integer bookingId, String paymentMethod) {
        // Kiểm tra booking có tồn tại không
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại với ID: " + bookingId));
        
        // Chỉ cho phép thanh toán cho booking PENDING hoặc CONFIRMED
        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Chỉ có thể thanh toán cho booking PENDING hoặc CONFIRMED. Trạng thái hiện tại: " + booking.getStatus());
        }
        
        // Tìm payment hiện có hoặc tạo mới
        List<Payment> payments = paymentRepository.findByBooking_Id(bookingId);
        Payment payment;
        
        if (!payments.isEmpty()) {
            payment = payments.get(0);
            // Cập nhật trạng thái thanh toán
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setResponseCode("00");
            try {
                payment.setMethod(paymentMethod != null ? PaymentMethod.valueOf(paymentMethod.toUpperCase()) : PaymentMethod.CASH);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid payment method: {}. Defaulting to CASH.", paymentMethod);
                payment.setMethod(PaymentMethod.CASH);
            }
            payment.setAmount(booking.getFinalPrice().longValue());
            payment.setPayDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        } else {
            // Tạo mới payment
            payment = new Payment();
            payment.setBooking(booking);
            payment.setAmount(booking.getFinalPrice().longValue());
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setResponseCode("00");
            try {
                payment.setMethod(paymentMethod != null ? PaymentMethod.valueOf(paymentMethod.toUpperCase()) : PaymentMethod.CASH);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid payment method: {}. Defaulting to CASH.", paymentMethod);
                payment.setMethod(PaymentMethod.CASH);
            }
            payment.setPayDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            payment.setOrderInfo("Thanh toán đặt phòng #" + bookingId);
        }
        
        // Lưu payment
        payment = paymentRepository.save(payment);
        
        // Cập nhật trạng thái booking thành CONFIRMED
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
        }
        
        return getBookingById(bookingId);
    }

    @Override
    @Transactional
    public void updatePaymentAndBookingStatusAfterVNPay(Integer bookingId, String transactionNo, String responseCode) {
        log.info("Cập nhật trạng thái sau VNPay callback cho bookingId: {}, transactionNo: {}, responseCode: {}", 
                 bookingId, transactionNo, responseCode);
        
        // 1. Tìm Payment dựa trên transactionNo
        Payment payment = paymentRepository.findByTransactionNo(transactionNo)
            .orElse(null); 
            
        if (payment == null) {
            log.warn("Không tìm thấy payment với transactionNo: {}. Thử tìm payment cuối cùng của bookingId: {}", transactionNo, bookingId);
             List<Payment> payments = paymentRepository.findAllByBooking_Id(bookingId);
             if (!payments.isEmpty()) {
                 payment = payments.stream()
                                   .filter(p -> p.getStatus() == null || p.getStatus().equals("01")) 
                                   .findFirst()
                                   .orElse(payments.get(payments.size() - 1)); 
             } else {
                 log.error("Không tìm thấy payment nào cho bookingId: {}", bookingId);
                 return; 
             }
        }
        
        // 2. Cập nhật thông tin Payment
        payment.setResponseCode(responseCode);
        payment.setTransactionNo(transactionNo); 
        payment.setPayDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))); 

        // 3. Tìm Booking để cập nhật
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
             log.error("Không tìm thấy booking với id: {} để cập nhật trạng thái sau VNPay.", bookingId);
             paymentRepository.save(payment);
             return;
        }

        // 4. Cập nhật trạng thái Payment và Booking dựa trên responseCode
        if ("00".equals(responseCode)) {
            payment.setStatus(PaymentStatus.SUCCESS); 
            booking.setPaymentStatus("PAID");
            booking.setStatus(BookingStatus.CONFIRMED); 
             log.info("Cập nhật bookingId {} thành PAID và CONFIRMED.", bookingId);
        } else {
            payment.setStatus(PaymentStatus.FAILED); 
            booking.setPaymentStatus("FAILED"); 
             log.warn("Thanh toán thất bại cho bookingId {} với mã lỗi: {}. Cập nhật paymentStatus thành FAILED.", bookingId, responseCode);
        }
        
        // 5. Lưu cả hai entity
        paymentRepository.save(payment);
        bookingRepository.save(booking);
         log.info("Đã lưu cập nhật trạng thái cho payment ID {} và booking ID {}", payment.getId(), bookingId);
    }

    private double calculateTotalPrice(UpsertBookingRequest request, List<Room> rooms, List<HotelService> services) {
        double totalPrice = 0;
        long days = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        if (days < 1) days = 1;

        // Tính giá phòng và dịch vụ cho từng phòng
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            BookingRoomRequest roomRequest = request.getRooms().get(i);
            
            // Giá cơ bản của phòng
            double roomPrice = room.getRoomType().getPricePerNight() * days;
            
            // Phụ thu nếu số người vượt quá số người tối đa của phòng
            int totalGuests = roomRequest.getAdults() + roomRequest.getChildren();
            int maxOccupancy = room.getRoomType().getMaxOccupancy();
            
            if (totalGuests > maxOccupancy) {
                // Tính phụ thu cho mỗi người vượt quá
                int extraGuests = totalGuests - maxOccupancy;
                double extraCharge = roomPrice * 0.25 * extraGuests; // Phụ thu 25% giá phòng cho mỗi người vượt quá
                roomPrice += extraCharge;
            }
            
            // Tính giá dịch vụ đi kèm
            if (roomRequest.getServiceIds() != null) {
                double servicePrice = services.stream()
                    .filter(service -> roomRequest.getServiceIds().contains(service.getId()))
                    .mapToDouble(service -> service.getPrice().doubleValue())
                    .sum();
                roomPrice += servicePrice * days; // Giá dịch vụ tính theo ngày
            }
            
            totalPrice += roomPrice;
        }
        
        return totalPrice;
    }

    @Override
    @Transactional
    public BookingResponseDTO createBooking(UpsertBookingRequest request, String username) {
        // Kiểm tra user
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng");
        }
        
        // Kiểm tra và lấy thông tin các phòng
        List<Room> rooms = request.getRooms().stream()
            .map(roomRequest -> roomRepository.findById(roomRequest.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng với ID: " + roomRequest.getRoomId())))
            .collect(Collectors.toList());
            
        // Kiểm tra và lấy thông tin các dịch vụ
        List<HotelService> services = new ArrayList<>();
        for (BookingRoomRequest roomRequest : request.getRooms()) {
            if (roomRequest.getServiceIds() != null && !roomRequest.getServiceIds().isEmpty()) {
                services.addAll(serviceRepository.findAllById(roomRequest.getServiceIds()));
            }
        }
        
        // Tính tổng giá
        double totalPrice = calculateTotalPrice(request, rooms, services);
        
        // Áp dụng mã giảm giá nếu có
        double finalPrice = totalPrice;
        Discount discount = null;
        if (request.getDiscountCode() != null) {
            discount = discountRepository.findByCode(request.getDiscountCode())
                .orElseThrow(() -> new ResourceNotFoundException("Mã giảm giá không hợp lệ"));
                
            if (discount.getDiscountType().equals("PERCENT")) {
                finalPrice = totalPrice * (1 - discount.getDiscountValue() / 100);
            } else {
                finalPrice = totalPrice - discount.getDiscountValue();
            }
        }
        
        // Tạo booking
        Booking booking = Booking.builder()
            .user(user)
            .checkInDate(request.getCheckInDate())
            .checkOutDate(request.getCheckOutDate())
            .totalPrice(totalPrice)
            .finalPrice(finalPrice)
            .status(BookingStatus.PENDING)
            .paymentMethod(request.getPaymentMethod())
            .paymentStatus("UNPAID")
            .discount(discount)
            .notes(request.getNotes())
            .build();
            
        booking = bookingRepository.save(booking);
        
        // Tạo booking details
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            BookingRoomRequest roomRequest = request.getRooms().get(i);
            
            BookingDetail detail = BookingDetail.builder()
                .booking(booking)
                .room(room)
                .adults(roomRequest.getAdults())
                .children(roomRequest.getChildren())
                .build();
                
            // Thêm dịch vụ cho booking detail
            if (roomRequest.getServiceIds() != null) {
                List<HotelService> roomServices = services.stream()
                    .filter(service -> roomRequest.getServiceIds().contains(service.getId()))
                    .collect(Collectors.toList());
                detail.setServices(roomServices);
            }
            
            bookingDetailRepository.save(detail);
        }
        
        return convertToBookingResponseDTO(booking);
    }

    private void processBookingRooms(List<BookingRoomRequest> rooms, Booking booking) {
        for (BookingRoomRequest roomRequest : rooms) {
            BookingDetail detail = BookingDetail.builder()
                    .booking(booking)
                    .room(roomRepository.findById(roomRequest.getRoomId())
                            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng")))
                    .adults(roomRequest.getAdults())
                    .children(roomRequest.getChildren())
                    .build();

            if (roomRequest.getServiceIds() != null && !roomRequest.getServiceIds().isEmpty()) {
                List<HotelService> services = serviceRepository.findAllById(roomRequest.getServiceIds());
                detail.setServices(services);
            }

            bookingDetailRepository.save(detail);
        }
    }

    @Override
    public List<BookingResponseDTO> getAllBookings(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Booking> bookingsPage = bookingRepository.findAll(pageRequest);
        
        return bookingsPage.getContent().stream()
                .map(this::convertToBookingResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponseDTO createBookingByAdmin(AdminBookingRequest request) {
        Booking booking = new Booking();
        
        // Xử lý userId - admin có thể tạo đặt phòng cho bất kỳ người dùng nào
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + request.getUserId()));
        
        booking.setUser(user);
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalPrice(request.getTotalPrice());
        booking.setFinalPrice(request.getFinalPrice());
        
        // Xử lý discount nếu có
        if (request.getDiscountId() != null) {
            Discount discount = discountRepository.findById(request.getDiscountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã giảm giá với ID: " + request.getDiscountId()));
            booking.setDiscount(discount);
        }
        
        Booking savedBooking = bookingRepository.save(booking);
        
        // Xử lý booking details
        if (request.getRoomIds() != null) {
            for (Integer roomId : request.getRoomIds()) {
                Room room = roomRepository.findById(roomId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng với ID: " + roomId));
                
                BookingDetail detail = new BookingDetail();
                detail.setBooking(savedBooking);
                detail.setRoom(room);
                detail.setPricePerNight(room.getRoomType().getPricePerNight());
                
                if (request.getAdults() != null) {
                    detail.setAdults(request.getAdults());
                }
                
                if (request.getChildren() != null) {
                    detail.setChildren(request.getChildren());
                }
                
                // Tính tổng giá cho phòng này
                long days = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
                if (days < 1) days = 1;
                double price = room.getRoomType().getPricePerNight() * days;
                detail.setPrice(price);
                
                bookingDetailRepository.save(detail);
            }
        }
        
        // Xử lý dịch vụ bổ sung nếu có
        if (request.getAdditionalServices() != null && !request.getAdditionalServices().isEmpty()) {
            for (String serviceId : request.getAdditionalServices()) {
                try {
                    Integer offeringId = Integer.parseInt(serviceId);
                    Offering offering = offeringRepository.findById(offeringId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy dịch vụ với id: " + offeringId));
                    
                    // Mặc định số lượng là 1 nếu không được chỉ định
                    int quantity = 1;
                    double serviceTotalPrice = offering.getPrice() * quantity;
                    
                    // Tạo BookingService
                    com.spring3.hotel.management.models.BookingService bookingService 
                        = new com.spring3.hotel.management.models.BookingService();
                    bookingService.setBooking(savedBooking);
                    bookingService.setOffering(offering);
                    bookingService.setQuantity(quantity);
                    bookingService.setTotalPrice(serviceTotalPrice);
                    bookingServiceRepository.save(bookingService);
                } catch (Exception e) {
                    log.error("Lỗi khi thêm dịch vụ vào booking: " + e.getMessage(), e);
                }
            }
        }
        
        return convertToBookingResponseDTO(savedBooking);
    }

    @Override
    public BookingResponseDTO updateBookingByAdmin(AdminBookingRequest request, Integer id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đặt phòng với ID: " + id));
        
        // Cập nhật thông tin cơ bản
        if (request.getCheckInDate() != null) {
            booking.setCheckInDate(request.getCheckInDate());
        }
        
        if (request.getCheckOutDate() != null) {
            booking.setCheckOutDate(request.getCheckOutDate());
        }
        
        if (request.getStatus() != null) {
            booking.setStatus(request.getStatus());
        }
        
        if (request.getTotalPrice() != null) {
            booking.setTotalPrice(request.getTotalPrice());
        }
        
        if (request.getFinalPrice() != null) {
            booking.setFinalPrice(request.getFinalPrice());
        }
        
        // Xử lý discount nếu có
        if (request.getDiscountId() != null) {
            Discount discount = discountRepository.findById(request.getDiscountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã giảm giá với ID: " + request.getDiscountId()));
            booking.setDiscount(discount);
        } else {
            booking.setDiscount(null);
        }
        
        // Cập nhật user nếu cần
        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + request.getUserId()));
            booking.setUser(user);
        }
        
        Booking savedBooking = bookingRepository.save(booking);
        
        // Xử lý booking details
        if (request.getRoomIds() != null) {
            // Xóa tất cả booking details cũ
            List<BookingDetail> oldDetails = bookingDetailRepository.findAllByBooking_Id(booking.getId());
            bookingDetailRepository.deleteAll(oldDetails);
            
            // Tạo booking details mới
            for (Integer roomId : request.getRoomIds()) {
                Room room = roomRepository.findById(roomId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng với ID: " + roomId));
                
                BookingDetail detail = new BookingDetail();
                detail.setBooking(savedBooking);
                detail.setRoom(room);
                detail.setPricePerNight(room.getRoomType().getPricePerNight());
                
                if (request.getAdults() != null) {
                    detail.setAdults(request.getAdults());
                }
                
                if (request.getChildren() != null) {
                    detail.setChildren(request.getChildren());
                }
                
                // Tính tổng giá cho phòng này
                long days = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
                if (days < 1) days = 1;
                double price = room.getRoomType().getPricePerNight() * days;
                detail.setPrice(price);
                
                bookingDetailRepository.save(detail);
            }
        }
        
        // Xử lý dịch vụ bổ sung nếu có
        if (request.getAdditionalServices() != null) {
            // Xóa tất cả booking services cũ
            List<com.spring3.hotel.management.models.BookingService> oldServices = 
                    bookingServiceRepository.findByBooking(savedBooking);
            bookingServiceRepository.deleteAll(oldServices);
            
            // Thêm dịch vụ mới
            if (!request.getAdditionalServices().isEmpty()) {
                for (String serviceId : request.getAdditionalServices()) {
                    try {
                        Integer offeringId = Integer.parseInt(serviceId);
                        Offering offering = offeringRepository.findById(offeringId)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy dịch vụ với id: " + offeringId));
                        
                        // Mặc định số lượng là 1 nếu không được chỉ định
                        int quantity = 1;
                        double serviceTotalPrice = offering.getPrice() * quantity;
                        
                        // Tạo BookingService
                        com.spring3.hotel.management.models.BookingService bookingService 
                            = new com.spring3.hotel.management.models.BookingService();
                        bookingService.setBooking(savedBooking);
                        bookingService.setOffering(offering);
                        bookingService.setQuantity(quantity);
                        bookingService.setTotalPrice(serviceTotalPrice);
                        bookingServiceRepository.save(bookingService);
                    } catch (Exception e) {
                        log.error("Lỗi khi thêm dịch vụ vào booking: " + e.getMessage(), e);
                    }
                }
            }
        }
        
        return convertToBookingResponseDTO(savedBooking);
    }

    @Override
    public BookingResponseDTO checkInBooking(Integer id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đặt phòng với ID: " + id));
        
        // Chỉ cho phép check-in các booking có trạng thái CONFIRMED
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Chỉ có thể check-in các đặt phòng có trạng thái CONFIRMED");
        }
        
        booking.setStatus(BookingStatus.CHECKED_IN);
        bookingRepository.save(booking);
        
        return convertToBookingResponseDTO(booking);
    }

    @Override
    public BookingResponseDTO checkOutBooking(Integer id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đặt phòng với ID: " + id));
        
        // Chỉ cho phép check-out các booking có trạng thái CHECKED_IN
        if (booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new IllegalStateException("Chỉ có thể check-out các đặt phòng có trạng thái CHECKED_IN");
        }
        
        booking.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(booking);
        
        return convertToBookingResponseDTO(booking);
    }

    @Override
    public void deleteBooking(Integer id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đặt phòng với ID: " + id));
        
        // Xóa các booking details liên quan
        List<BookingDetail> details = bookingDetailRepository.findAllByBooking_Id(booking.getId());
        bookingDetailRepository.deleteAll(details);
        
        // Xóa booking
        bookingRepository.delete(booking);
    }
}
