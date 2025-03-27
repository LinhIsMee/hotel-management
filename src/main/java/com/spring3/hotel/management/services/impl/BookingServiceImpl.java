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
import java.util.List;

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
        Booking booking = new Booking();
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        booking.setUser(user);
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setTotalPrice(request.getTotalPrice());
        Discount discount = discountRepository.findDiscountById(request.getDiscountId());
        booking.setDiscount(discount);
        booking.setStatus("PENDING"); // Mặc định trạng thái là 'PENDING' sau khi thanh toán sẽ chuyển sang 'CONFIRMED'
        bookingRepository.save(booking);
        return convertToBookingResponseDTO(booking);
    }

    @Override
    public BookingResponseDTO updateBooking(UpsertBookingRequest request, Integer id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        booking.setUser(user);
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setTotalPrice(request.getTotalPrice());
        Discount discount = discountRepository.findDiscountById(request.getDiscountId());
        booking.setDiscount(discount);
        booking.setStatus(request.getStatus());
        bookingRepository.save(booking);
        return convertToBookingResponseDTO(booking);
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
        User user = userRepository.findById(booking.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        BookingResponseDTO bookingResponseDTO = new BookingResponseDTO();
        Payment payment = paymentRepository.findByBookingId(booking.getId())
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        List<BookingDetail> bookingDetails = bookingDetailRepository.findAllByBooking_Id(booking.getId());
        List<Room> rooms = bookingDetails.stream().map(BookingDetail::getRoom).toList();
        List<RoomListResponseDTO> bookingRoomList = rooms.stream().map(room -> {
            RoomListResponseDTO roomListResponseDTO = new RoomListResponseDTO();
            roomListResponseDTO.setRoomId(room.getId());
            roomListResponseDTO.setRoomNumber(room.getRoomNumber());
            roomListResponseDTO.setRoomType(room.getRoomType().getName());
            roomListResponseDTO.setPrice(room.getRoomType().getBasePrice());
            return roomListResponseDTO;
        }).toList();
        bookingResponseDTO.setId(booking.getId());
        bookingResponseDTO.setUserId(user.getId());
        bookingResponseDTO.setFullName(user.getFullName());
        bookingResponseDTO.setNationalId(user.getNationalId());
        bookingResponseDTO.setEmail(user.getEmail());
        bookingResponseDTO.setRooms(bookingRoomList);
        bookingResponseDTO.setPhone(user.getPhoneNumber());
        bookingResponseDTO.setCheckInDate(booking.getCheckInDate());
        bookingResponseDTO.setCheckOutDate(booking.getCheckOutDate());
        bookingResponseDTO.setTotalPrice(booking.getTotalPrice());
        Discount discount = discountRepository.findDiscountById(booking.getDiscount().getId());
        if (discount == null) {
            bookingResponseDTO.setFinalPrice(booking.getTotalPrice());
            bookingResponseDTO.setDiscountCode(null);
            bookingResponseDTO.setDiscountValue(0.0);
            bookingResponseDTO.setDiscountType(null);
        }else {
            bookingResponseDTO.setFinalPrice(booking.getTotalPrice() - booking.getTotalPrice() * discount.getDiscountValue());
            bookingResponseDTO.setDiscountCode(discount.getCode());
            bookingResponseDTO.setDiscountValue(discount.getDiscountValue());
            bookingResponseDTO.setDiscountType(discount.getDiscountType());
        }
        bookingResponseDTO.setStatus(booking.getStatus());
        bookingResponseDTO.setPaymentMethod("VnPay");
        bookingResponseDTO.setPaymentStatus(payment.getStatus());
        bookingResponseDTO.setPaymentDate(payment.getPayDate());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        bookingResponseDTO.setCreatedAt(booking.getCreatedAt().format(formatter));
        return bookingResponseDTO;
    }
}
