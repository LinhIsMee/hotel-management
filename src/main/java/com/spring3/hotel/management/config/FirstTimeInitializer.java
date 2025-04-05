package com.spring3.hotel.management.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.spring3.hotel.management.models.Booking;
import com.spring3.hotel.management.models.BookingDetail;
import com.spring3.hotel.management.models.Discount;
import com.spring3.hotel.management.models.Payment;
import com.spring3.hotel.management.models.Room;
import com.spring3.hotel.management.models.User;
import com.spring3.hotel.management.repositories.BookingDetailRepository;
import com.spring3.hotel.management.repositories.BookingRepository;
import com.spring3.hotel.management.repositories.DiscountRepository;
import com.spring3.hotel.management.repositories.PaymentRepository;
import com.spring3.hotel.management.repositories.RoomRepository;
import com.spring3.hotel.management.repositories.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FirstTimeInitializer {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoomRepository roomRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private BookingDetailRepository bookingDetailRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private DiscountRepository discountRepository;

    @Transactional
    public void reinitializeBookingsForDashboard() {
        log.info("Tạo lại dữ liệu booking phân bố đều cho dashboard...");
        
        // Lấy danh sách user và room để tạo booking
        List<User> users = userRepository.findAll();
        List<Room> rooms = roomRepository.findAll();
        List<Discount> discounts = discountRepository.findAll();
        
        if (users.isEmpty() || rooms.isEmpty() || discounts.isEmpty()) {
            log.error("Không có đủ dữ liệu để tạo booking");
            return;
        }
        
        // Tạo dữ liệu booking mẫu
        Random random = new Random();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        // Xóa tất cả booking cũ (nếu cần)
        // bookingDetailRepository.deleteAll();
        // bookingRepository.deleteAll();
        // paymentRepository.deleteAll();
        
        // Thời gian hiện tại giả lập là 5/4/2025
        LocalDate simulatedToday = LocalDate.of(2025, 4, 5);
        
        // Tạo 20 booking mẫu cho tháng 3/2025
        createBookingsForMonth(3, 2025, 20, users, rooms, discounts, random, formatter, simulatedToday);
        
        // Tạo 20 booking mẫu cho tháng 4/2025
        createBookingsForMonth(4, 2025, 20, users, rooms, discounts, random, formatter, simulatedToday);
        
        log.info("Đã tạo thêm dữ liệu booking phân bố đều cho dashboard");
    }
    
    private void createBookingsForMonth(int month, int year, int count, List<User> users, List<Room> rooms,
            List<Discount> discounts, Random random, DateTimeFormatter formatter, LocalDate simulatedToday) {
        
        // Tính số ngày trong tháng
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        int daysInMonth = startOfMonth.lengthOfMonth();
        
        // Phân bố đều các booking trong tháng
        for (int i = 1; i <= count; i++) {
            // Lấy ngày ngẫu nhiên trong tháng
            int bookingDay = (i * daysInMonth / count);
            if (bookingDay < 1) bookingDay = 1;
            if (bookingDay > daysInMonth) bookingDay = daysInMonth;
            
            LocalDate bookingDate = LocalDate.of(year, month, bookingDay);
            
            // Tạo ngày check-in và check-out
            LocalDate checkInDate = bookingDate.plusDays(random.nextInt(5) + 1);
            LocalDate checkOutDate = checkInDate.plusDays(random.nextInt(3) + 1);
            
            // Nếu ngày check-in hoặc check-out vượt qua tháng, đưa về tháng sau
            if (checkInDate.getMonthValue() != month) {
                checkInDate = LocalDate.of(year, month, daysInMonth - 3);
                checkOutDate = checkInDate.plusDays(random.nextInt(3) + 1);
            }
            
            // Ngày tạo booking vào đúng ngày bookingDay trong tháng đó
            LocalDateTime createdAt = LocalDateTime.of(year, month, bookingDay, 
                    random.nextInt(24), random.nextInt(60), random.nextInt(60));
            
            // Quyết định trạng thái dựa trên mối quan hệ với simulatedToday
            String status;
            if (checkInDate.isAfter(simulatedToday)) {
                status = random.nextBoolean() ? "CONFIRMED" : "PENDING";
            } else if (checkOutDate.isBefore(simulatedToday)) {
                status = "CHECKED_OUT";
            } else if (checkInDate.isBefore(simulatedToday) && checkOutDate.isAfter(simulatedToday)) {
                status = "CHECKED_IN";
            } else if (checkInDate.isEqual(simulatedToday)) {
                status = random.nextBoolean() ? "CHECKED_IN" : "CONFIRMED";
            } else {
                status = "CHECKED_OUT";
            }
            
            // Tạo booking ngẫu nhiên
            User user = users.get(random.nextInt(users.size()));
            Discount discount = discounts.get(random.nextInt(discounts.size()));
            
            // Tính số ngày lưu trú
            int stayDuration = (int) (checkOutDate.toEpochDay() - checkInDate.toEpochDay());
            if (stayDuration < 1) stayDuration = 1;
            
            // Tạo booking detail (1-3 phòng mỗi booking)
            int roomCount = random.nextInt(3) + 1;
            double basePrice = 500000.0; // Giá cơ bản là 500,000 VND / đêm
            double totalPrice = 0.0;
            
            List<Room> selectedRooms = new ArrayList<>();
            for (int j = 0; j < roomCount && j < rooms.size(); j++) {
                Room room = rooms.get(random.nextInt(rooms.size()));
                if (!selectedRooms.contains(room)) {
                    selectedRooms.add(room);
                    
                    // Tính tổng giá
                    double roomPrice = room.getRoomType() != null && room.getRoomType().getBasePrice() > 0 
                        ? room.getRoomType().getBasePrice() 
                        : basePrice * (1 + random.nextDouble()); // Từ 500k đến 1M
                    
                    totalPrice += roomPrice * stayDuration;
                }
            }
            
            // Đảm bảo có ít nhất 1 phòng và giá dương
            if (selectedRooms.isEmpty()) {
                Room room = rooms.get(random.nextInt(rooms.size()));
                selectedRooms.add(room);
                totalPrice = basePrice * stayDuration;
            }
            
            // Làm tròn giá tiền
            totalPrice = Math.round(totalPrice / 10000.0) * 10000.0;
            
            // Tạo booking mới
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setCheckInDate(checkInDate);
            booking.setCheckOutDate(checkOutDate);
            booking.setDiscount(discount);
            booking.setCreatedAt(createdAt);
            booking.setStatus(status);
            booking.setTotalPrice(totalPrice);
            
            // Lưu booking để có ID cho booking detail
            bookingRepository.save(booking);
            
            // Tạo booking details
            for (Room room : selectedRooms) {
                BookingDetail bookingDetail = new BookingDetail();
                bookingDetail.setBooking(booking);
                bookingDetail.setRoom(room);
                double pricePerNight = room.getRoomType() != null && room.getRoomType().getBasePrice() > 0 
                    ? room.getRoomType().getBasePrice() 
                    : basePrice * (1 + random.nextDouble());
                
                bookingDetail.setPricePerNight(pricePerNight);
                bookingDetailRepository.save(bookingDetail);
            }
            
            // Tạo payment
            Payment payment = new Payment();
            payment.setBooking(booking);
            payment.setAmount((long) totalPrice);
            
            // Phân bố trạng thái payment phù hợp với booking
            if ("CANCELLED".equals(status)) {
                payment.setStatus("REFUNDED");
            } else if ("PENDING".equals(status)) {
                payment.setStatus("PENDING");
            } else {
                payment.setStatus("PAID");
            }
            
            // Thêm thông tin chi tiết cho payment đã xử lý
            if ("PAID".equals(payment.getStatus()) || "REFUNDED".equals(payment.getStatus())) {
                payment.setPayDate(createdAt.plusHours(1).format(formatter));
                payment.setTransactionNo("TRX" + System.currentTimeMillis() + random.nextInt(1000));
                payment.setOrderInfo("Thanh toán đặt phòng #" + booking.getId());
                payment.setBankCode("NCB");
                payment.setResponseCode("00");
            }
            
            paymentRepository.save(payment);
        }
    }
} 