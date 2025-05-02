package com.spring3.hotel.management.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.spring3.hotel.management.models.Booking;
import com.spring3.hotel.management.models.BookingDetail;
import com.spring3.hotel.management.models.Discount;
import com.spring3.hotel.management.models.Payment;
import com.spring3.hotel.management.models.Role;
import com.spring3.hotel.management.models.Room;
import com.spring3.hotel.management.models.User;
import com.spring3.hotel.management.repositories.BookingDetailRepository;
import com.spring3.hotel.management.repositories.BookingRepository;
import com.spring3.hotel.management.repositories.DiscountRepository;
import com.spring3.hotel.management.repositories.PaymentRepository;
import com.spring3.hotel.management.repositories.RoleRepository;
import com.spring3.hotel.management.repositories.RoomRepository;
import com.spring3.hotel.management.repositories.RoomTypeRepository;
import com.spring3.hotel.management.repositories.ServiceRepository;
import com.spring3.hotel.management.repositories.UserRepository;
import com.spring3.hotel.management.repositories.ReviewRepository;
import com.spring3.hotel.management.services.interfaces.RoomService;
import com.spring3.hotel.management.services.interfaces.RoomTypeService;
import com.spring3.hotel.management.services.interfaces.ServiceService;
import com.spring3.hotel.management.services.interfaces.ReviewService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoomTypeRepository roomTypeRepository;
    
    @Autowired
    private RoomRepository roomRepository;
    
    @Autowired
    private RoomTypeService roomTypeService;
    
    @Autowired
    private RoomService roomService;
    
    @Autowired
    private ServiceRepository serviceRepository;
    
    @Autowired
    private ServiceService serviceService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private BookingDetailRepository bookingDetailRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private DiscountRepository discountRepository;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing roles and default admin account...");
        
        // Khởi tạo vai trò nếu chưa tồn tại
        if (roleRepository.count() == 0) {
            log.info("Creating roles...");
            
            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            adminRole.setDescription("Administrator role");
            roleRepository.save(adminRole);
            
            Role staffRole = new Role();
            staffRole.setName("ROLE_STAFF");
            staffRole.setDescription("Hotel staff role");
            roleRepository.save(staffRole);
            
            Role userRole = new Role();
            userRole.setName("ROLE_USER");
            userRole.setDescription("Regular user role");
            roleRepository.save(userRole);
            
            log.info("Roles created successfully");
            
            // Tạo tài khoản admin mặc định
            if (userRepository.findByUsername("admin") == null) {
                log.info("Creating default admin account...");
                
                User adminUser = new User();
                adminUser.setUsername("admin");
                adminUser.setPassword(passwordEncoder.encode("Admin123!"));
                adminUser.setEmail("admin@hotel.com");
                adminUser.setFullName("Hotel Admin");
                adminUser.setPhoneNumber("0123456789");
                adminUser.setRole(adminRole);
                adminUser.setCreatedAt(LocalDateTime.now());
                
                userRepository.save(adminUser);
                
                log.info("Default admin account created successfully");
            }
        }
        
        // Khởi tạo dữ liệu loại phòng nếu chưa có
        if (roomTypeRepository.count() == 0) {
            log.info("Initializing room types from JSON...");
            try {
                roomTypeService.initRoomTypesFromJson();
                log.info("Room types initialized successfully");
            } catch (Exception e) {
                log.error("Failed to initialize room types from JSON: {}", e.getMessage());
            }
        }
        
        // Khởi tạo dữ liệu phòng nếu chưa có
        if (roomRepository.count() < 10) {
            log.info("Khởi tạo lại dữ liệu phòng với ảnh mới...");
                roomService.initRoomsFromJson();
            log.info("Đã hoàn thành khởi tạo dữ liệu phòng.");
        } else {
            log.info("Đã có {} phòng trong cơ sở dữ liệu, bỏ qua khởi tạo.", roomRepository.count());
        }
        
        // Khởi tạo dữ liệu dịch vụ nếu chưa có
        if (serviceRepository.count() == 0) {
            log.info("Initializing services from JSON...");
            try {
                serviceService.initServicesFromJson();
                log.info("Services initialized successfully");
            } catch (Exception e) {
                log.error("Failed to initialize services from JSON: {}", e.getMessage());
            }
        }
        
        // Khởi tạo dữ liệu đánh giá nếu chưa có
        if (reviewRepository.count() == 0) {
            log.info("Initializing reviews from JSON...");
            try {
                reviewService.initReviewsFromJson();
                log.info("Reviews initialized successfully");
            } catch (Exception e) {
                log.error("Failed to initialize reviews from JSON: {}", e.getMessage());
            }
        }
        
        // Khởi tạo dữ liệu discount nếu chưa có
        if (discountRepository.count() == 0) {
            log.info("Creating sample discounts...");
            
            List<Discount> discounts = new ArrayList<>();
            
            Discount summer = new Discount();
            summer.setCode("SUMMER2023");
            summer.setDiscountType("PERCENT");
            summer.setDiscountValue(0.1); // 10%
            summer.setValidFrom(LocalDate.of(2023, 6, 1));
            summer.setValidTo(LocalDate.of(2023, 8, 31));
            summer.setMaxUses(100);
            summer.setUsedCount(15);
            discounts.add(summer);
            
            Discount weekend = new Discount();
            weekend.setCode("WEEKEND");
            weekend.setDiscountType("PERCENT");
            weekend.setDiscountValue(0.15); // 15%
            weekend.setValidFrom(LocalDate.of(2023, 1, 1));
            weekend.setValidTo(LocalDate.of(2023, 12, 31));
            weekend.setMaxUses(200);
            weekend.setUsedCount(45);
            discounts.add(weekend);
            
            Discount welcome = new Discount();
            welcome.setCode("WELCOME");
            welcome.setDiscountType("FIXED");
            welcome.setDiscountValue(200000.0); // 200,000 VND
            welcome.setValidFrom(LocalDate.of(2023, 1, 1));
            welcome.setValidTo(LocalDate.of(2023, 12, 31));
            welcome.setMaxUses(500);
            welcome.setUsedCount(120);
            discounts.add(welcome);
            
            Discount special = new Discount();
            special.setCode("SPECIAL");
            special.setDiscountType("PERCENT");
            special.setDiscountValue(0.2); // 20%
            special.setValidFrom(LocalDate.of(2023, 10, 1));
            special.setValidTo(LocalDate.of(2023, 12, 31));
            special.setMaxUses(100);
            special.setUsedCount(0);
            discounts.add(special);
            
            Discount loyal = new Discount();
            loyal.setCode("LOYAL2023");
            loyal.setDiscountType("PERCENT");
            loyal.setDiscountValue(0.25); // 25%
            loyal.setValidFrom(LocalDate.of(2023, 1, 1));
            loyal.setValidTo(LocalDate.of(2023, 12, 31));
            loyal.setMaxUses(50);
            loyal.setUsedCount(10);
            discounts.add(loyal);
            
            discountRepository.saveAll(discounts);
            
            log.info("Sample discounts created successfully");
        }
        
        // Khởi tạo dữ liệu booking nếu chưa có
        if (bookingRepository.count() == 0 && userRepository.count() > 0 && roomRepository.count() > 0) {
            log.info("Creating sample bookings...");
            
            // Tạo một số user khách hàng nếu chưa có
            Optional<Role> userRoleOpt = roleRepository.findByName("ROLE_USER");
            if (userRoleOpt.isEmpty()) {
                log.error("User role not found");
                return;
            }
            
            Role userRole = userRoleOpt.get();
            List<User> customers = new ArrayList<>();
            
            // Tạo 10 người dùng để có nhiều dữ liệu đa dạng hơn
            for (int i = 1; i <= 10; i++) {
                String username = "customer" + i;
                if (userRepository.findByUsername(username) == null) {
                    User customer = new User();
                    customer.setUsername(username);
                    customer.setPassword(passwordEncoder.encode("Customer" + i + "!"));
                    customer.setEmail("customer" + i + "@example.com");
                    customer.setFullName("Khách Hàng " + i);
                    customer.setPhoneNumber("09" + (10000000 + i * 111111));
                    customer.setRole(userRole);
                    customer.setNationalId("03" + (1000000000 + i * 11111111));
                    customer.setCreatedAt(LocalDateTime.now().minusDays(i * 5));
                    customers.add(customer);
                }
            }
            
            userRepository.saveAll(customers);
            
            // Lấy danh sách user và room để tạo booking
            List<User> users = userRepository.findAll();
            List<Room> rooms = roomRepository.findAll();
            List<Discount> discounts = discountRepository.findAll();
            
            if (rooms.isEmpty()) {
                log.error("No rooms found for creating bookings");
                return;
            }
            
            // Tạo dữ liệu booking mẫu
            Random random = new Random();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            // Phân bố trạng thái hợp lý
            String[] statuses = {"PENDING", "CONFIRMED", "CHECKED_IN", "CHECKED_OUT", "CANCELLED"};
            String[] paymentStatuses = {"PENDING", "PAID", "REFUNDED"};
            
            // Tạo 30 booking mẫu
            for (int i = 0; i < 30; i++) {
                User user = users.get(random.nextInt(users.size()));
                Discount discount = discounts.get(random.nextInt(discounts.size()));
                
                // Tạo ngày check-in và check-out với phân bố hợp lý
                LocalDate now = LocalDate.now();
                LocalDate checkInDate;
                LocalDate checkOutDate;
                LocalDateTime createdAt;
                
                // Phân bố các booking trong quá khứ, hiện tại và tương lai
                String status;
                if (i < 10) {
                    // 10 booking cho tháng 3/2025 (tháng trước)
                    // Tạo booking vào nhiều ngày khác nhau trong tháng 3
                    int day = random.nextInt(31) + 1;
                    checkInDate = LocalDate.of(2025, 3, day);
                    checkOutDate = checkInDate.plusDays(random.nextInt(5) + 1);
                    
                    // Ngày tạo booking luôn trước ngày check-in
                    int bookingBeforeCheckIn = random.nextInt(10) + 1;
                    createdAt = LocalDate.of(2025, 3, Math.max(1, day - bookingBeforeCheckIn)).atStartOfDay().plusHours(random.nextInt(24));
                    
                    status = "CHECKED_OUT"; // Đã hoàn thành
                } else if (i < 20) {
                    // 10 booking cho tháng 4/2025 (tháng hiện tại)
                    // Phân bố đều trong tháng 4
                    int day = random.nextInt(30) + 1;
                    checkInDate = LocalDate.of(2025, 4, day);
                    checkOutDate = checkInDate.plusDays(random.nextInt(5) + 1);
                    
                    // Ngày tạo booking nằm trong tháng 4, trước ngày check-in
                    int bookingDay = Math.max(1, day - random.nextInt(7) - 1);
                    createdAt = LocalDate.of(2025, 4, bookingDay).atStartOfDay().plusHours(random.nextInt(24));
                    
                    if (day < 5) {
                        // Trước ngày hiện tại (05/04/2025)
                        status = "CHECKED_OUT";
                    } else if (day == 5) {
                        // Đúng ngày hiện tại
                        status = "CHECKED_IN";
                    } else {
                        // Sau ngày hiện tại
                        status = "CONFIRMED";
                    }
                } else {
                    // 10 booking cho tương lai
                    checkInDate = LocalDate.of(2025, random.nextInt(8) + 5, random.nextInt(28) + 1); // Từ tháng 5-12/2025
                    checkOutDate = checkInDate.plusDays(random.nextInt(7) + 1);
                    
                    // Ngày tạo booking trong tháng 4/2025
                    int bookingDay = random.nextInt(5) + 1; // 1-5 tháng 4
                    createdAt = LocalDate.of(2025, 4, bookingDay).atStartOfDay().plusHours(random.nextInt(24));
                    
                    status = random.nextBoolean() ? "CONFIRMED" : "PENDING";
                }
                
                // Tính số ngày lưu trú
                int stayDuration = (int) (checkOutDate.toEpochDay() - checkInDate.toEpochDay());
                if (stayDuration < 1) stayDuration = 1;
                
                // Tạo booking detail (1-3 phòng mỗi booking)
                int roomCount = random.nextInt(3) + 1;
                // Đảm bảo doanh thu dương và hợp lý
                double basePrice = 500000.0; // Giá cơ bản là 500,000 VND / đêm
                double totalPrice = 0.0;
                
                List<Room> selectedRooms = new ArrayList<>();
                for (int j = 0; j < roomCount && j < rooms.size(); j++) {
                    Room room = rooms.get(random.nextInt(rooms.size()));
                    // Tránh chọn trùng phòng
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
                    // Nếu đã hủy
                    payment.setStatus("REFUNDED");
                } else if ("PENDING".equals(status)) {
                    // Nếu đang chờ xử lý
                    payment.setStatus("PENDING");
                } else {
                    // Các trường hợp khác (CONFIRMED, CHECKED_IN, CHECKED_OUT)
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
            
            log.info("Sample bookings (30 entries) created successfully");
        }
    }
} 