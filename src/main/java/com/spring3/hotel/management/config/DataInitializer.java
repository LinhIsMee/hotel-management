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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.spring3.hotel.management.models.Booking;
import com.spring3.hotel.management.models.BookingDetail;
import com.spring3.hotel.management.models.Department;
import com.spring3.hotel.management.models.Discount;
import com.spring3.hotel.management.models.Employee;
import com.spring3.hotel.management.models.Payment;
import com.spring3.hotel.management.models.Position;
import com.spring3.hotel.management.models.Role;
import com.spring3.hotel.management.models.Room;
import com.spring3.hotel.management.models.User;
import com.spring3.hotel.management.repositories.BookingDetailRepository;
import com.spring3.hotel.management.repositories.BookingRepository;
import com.spring3.hotel.management.repositories.DiscountRepository;
import com.spring3.hotel.management.repositories.EmployeeRepository;
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
    private EmployeeRepository employeeRepository;
    
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
        
        // Khởi tạo dữ liệu nhân viên mẫu nếu chưa có
        if (employeeRepository.count() == 0) {
            log.info("Creating sample employees...");
            
            List<Employee> employees = new ArrayList<>();
            
            // Dữ liệu mẫu từ JSON
            addEmployee(employees, "Nguyễn Thị Anh", "nguyenthianh@hotel.com", "0901122334", Department.MANAGEMENT, Position.MANAGER, "2022-01-10", true);
            addEmployee(employees, "Trần Văn Bình", "tranvanbinh@hotel.com", "0912233445", Department.FRONT_DESK, Position.RECEPTIONIST, "2022-02-15", true);
            addEmployee(employees, "Phạm Thị Châu", "phamthichau@hotel.com", "0923344556", Department.FRONT_DESK, Position.RECEPTIONIST, "2022-03-01", true);
            addEmployee(employees, "Lê Văn Đức", "levanduc@hotel.com", "0934455667", Department.RESTAURANT, Position.SERVER, "2022-03-15", false);
            addEmployee(employees, "Hoàng Thị Giang", "hoangthigiang@hotel.com", "0945566778", Department.HOUSEKEEPING, Position.CLEANING, "2022-04-01", true);
            addEmployee(employees, "Đỗ Văn Hùng", "dovanhung@hotel.com", "0956677889", Department.SECURITY, Position.SECURITY, "2022-04-15", true);
            addEmployee(employees, "Ngô Thị Lan", "ngothilan@hotel.com", "0967788990", Department.RESTAURANT, Position.SERVER, "2022-05-01", true);
            addEmployee(employees, "Vũ Văn Minh", "vuvanminh@hotel.com", "0978899001", Department.TECHNICAL, Position.MANAGER, "2022-05-15", true);
            addEmployee(employees, "Lý Thị Ngọc", "lythingoc@hotel.com", "0989900112", Department.HOUSEKEEPING, Position.CLEANING, "2022-06-01", true);
            addEmployee(employees, "Phan Văn Phúc", "phanvanphuc@hotel.com", "0990011223", Department.FRONT_DESK, Position.MANAGER, "2022-06-15", true);
            addEmployee(employees, "Trịnh Thị Quỳnh", "trinhthiquynh@hotel.com", "0901122335", Department.RESTAURANT, Position.MANAGER, "2022-07-01", true);
            addEmployee(employees, "Mai Văn Sơn", "maivanson@hotel.com", "0912233446", Department.SECURITY, Position.SECURITY, "2022-07-15", true);
            addEmployee(employees, "Đặng Thị Thảo", "dangthithao@hotel.com", "0923344557", Department.FRONT_DESK, Position.RECEPTIONIST, "2022-08-01", true);
            addEmployee(employees, "Bùi Văn Uy", "buivanuy@hotel.com", "0934455668", Department.TECHNICAL, Position.MANAGER, "2022-08-15", false);
            addEmployee(employees, "Hồ Thị Vân", "hothivan@hotel.com", "0945566779", Department.HOUSEKEEPING, Position.MANAGER, "2022-09-01", true);
            addEmployee(employees, "Dương Văn X", "duongvanx@hotel.com", "0956677890", Department.RESTAURANT, Position.CHEF, "2022-09-15", true);
            addEmployee(employees, "Trương Thị Y", "truongthiy@hotel.com", "0967788991", Department.HOUSEKEEPING, Position.SUPERVISOR, "2022-10-01", true);
            addEmployee(employees, "Đinh Văn Z", "dinhvanz@hotel.com", "0978899002", Department.SECURITY, Position.SUPERVISOR, "2022-10-15", true);
            addEmployee(employees, "Lương Thị W", "luongthiw@hotel.com", "0989900113", Department.SPA, Position.THERAPIST, "2022-11-01", true);
            addEmployee(employees, "Võ Văn T", "vovant@hotel.com", "0990011224", Department.FRONT_DESK, Position.SUPERVISOR, "2022-11-15", true);
            
            employeeRepository.saveAll(employees);
            
            log.info("Sample employees created successfully");
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
        if (roomRepository.count() == 0 && roomTypeRepository.count() > 0) {
            log.info("Initializing rooms from JSON...");
            try {
                roomService.initRoomsFromJson();
                log.info("Rooms initialized successfully");
            } catch (Exception e) {
                log.error("Failed to initialize rooms from JSON: {}", e.getMessage());
            }
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
            
            if (userRepository.findByUsername("customer1") == null) {
                User customer1 = new User();
                customer1.setUsername("customer1");
                customer1.setPassword(passwordEncoder.encode("Customer1!"));
                customer1.setEmail("customer1@example.com");
                customer1.setFullName("Nguyễn Văn A");
                customer1.setPhoneNumber("0912345678");
                customer1.setRole(userRole);
                customer1.setNationalId("031234567890");
                customer1.setCreatedAt(LocalDateTime.now());
                customers.add(customer1);
            }
            
            if (userRepository.findByUsername("customer2") == null) {
                User customer2 = new User();
                customer2.setUsername("customer2");
                customer2.setPassword(passwordEncoder.encode("Customer2!"));
                customer2.setEmail("customer2@example.com");
                customer2.setFullName("Trần Thị B");
                customer2.setPhoneNumber("0923456789");
                customer2.setRole(userRole);
                customer2.setNationalId("032345678901");
                customer2.setCreatedAt(LocalDateTime.now());
                customers.add(customer2);
            }
            
            if (userRepository.findByUsername("customer3") == null) {
                User customer3 = new User();
                customer3.setUsername("customer3");
                customer3.setPassword(passwordEncoder.encode("Customer3!"));
                customer3.setEmail("customer3@example.com");
                customer3.setFullName("Lê Văn C");
                customer3.setPhoneNumber("0934567890");
                customer3.setRole(userRole);
                customer3.setNationalId("033456789012");
                customer3.setCreatedAt(LocalDateTime.now());
                customers.add(customer3);
            }
            
            userRepository.saveAll(customers);
            
            // Lấy danh sách user và room để tạo booking
            List<User> users = userRepository.findAll();
            List<Room> rooms = roomRepository.findAll();
            List<Discount> discounts = discountRepository.findAll();
            
            // Tạo dữ liệu booking mẫu
            List<Booking> bookings = new ArrayList<>();
            List<BookingDetail> bookingDetails = new ArrayList<>();
            List<Payment> payments = new ArrayList<>();
            
            Random random = new Random();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            String[] statuses = {"PENDING", "CONFIRMED", "CHECKED_IN", "CHECKED_OUT", "CANCELLED"};
            String[] paymentStatuses = {"PENDING", "PAID", "REFUNDED"};
            
            for (int i = 0; i < 10; i++) {
                User user = users.get(random.nextInt(users.size()));
                Discount discount = discounts.get(random.nextInt(discounts.size()));
                
                // Tạo ngày check-in và check-out
                LocalDate checkInDate = LocalDate.now().plusDays(random.nextInt(60) - 30); // +/- 30 ngày
                LocalDate checkOutDate = checkInDate.plusDays(random.nextInt(5) + 1); // Từ 1 đến 5 đêm
                
                // Tạo booking mới
                Booking booking = new Booking();
                booking.setUser(user);
                booking.setCheckInDate(checkInDate);
                booking.setCheckOutDate(checkOutDate);
                booking.setDiscount(discount);
                
                // Tính ngày tạo booking
                LocalDateTime createdAt = LocalDateTime.now().minusDays(random.nextInt(10) + 1);
                booking.setCreatedAt(createdAt);
                
                // Xác định trạng thái dựa vào ngày checkin/checkout
                String status;
                LocalDate today = LocalDate.now();
                if (checkInDate.isAfter(today)) {
                    // Chưa đến ngày checkin
                    status = random.nextBoolean() ? "PENDING" : "CONFIRMED";
                } else if (checkOutDate.isBefore(today)) {
                    // Đã qua ngày checkout
                    status = "CHECKED_OUT";
                } else {
                    // Đang ở
                    status = "CHECKED_IN";
                }
                
                // Random một số booking bị hủy
                if (random.nextInt(10) < 1) { // 10% bị hủy
                    status = "CANCELLED";
                }
                
                booking.setStatus(status);
                
                // Lưu booking để có ID cho booking detail
                bookingRepository.save(booking);
                
                // Tạo booking detail (1-3 phòng mỗi booking)
                int roomCount = random.nextInt(3) + 1;
                double totalPrice = 0;
                
                for (int j = 0; j < roomCount; j++) {
                    Room room = rooms.get(random.nextInt(rooms.size()));
                    
                    BookingDetail bookingDetail = new BookingDetail();
                    bookingDetail.setBooking(booking);
                    bookingDetail.setRoom(room);
                    bookingDetail.setPricePerNight(room.getRoomType().getBasePrice());
                    
                    bookingDetails.add(bookingDetail);
                    bookingDetailRepository.save(bookingDetail);
                    
                    // Tính tổng giá
                    long nights = checkInDate.datesUntil(checkOutDate).count();
                    totalPrice += room.getRoomType().getBasePrice() * nights;
                }
                
                booking.setTotalPrice(totalPrice);
                bookingRepository.save(booking);
                
                // Tạo payment
                Payment payment = new Payment();
                payment.setBooking(booking);
                payment.setAmount((long) totalPrice);
                
                if ("CANCELLED".equals(status)) {
                    payment.setStatus("REFUNDED");
                } else if ("PENDING".equals(status)) {
                    payment.setStatus("PENDING");
                } else {
                    payment.setStatus("PAID");
                }
                
                if ("PAID".equals(payment.getStatus()) || "REFUNDED".equals(payment.getStatus())) {
                    payment.setPayDate(createdAt.plusHours(1).format(formatter));
                    payment.setTransactionNo("TRX" + System.currentTimeMillis() + random.nextInt(1000));
                    payment.setOrderInfo("Thanh toán đặt phòng #" + booking.getId());
                    payment.setBankCode("NCB");
                    payment.setResponseCode("00");
                }
                
                payments.add(payment);
                paymentRepository.save(payment);
            }
            
            log.info("Sample bookings created successfully");
        }
    }
    
    // Helper method to create an employee
    private void addEmployee(List<Employee> employees, String name, String email, String phone, 
                           Department department, Position position, String joinDateStr, Boolean status) {
        Employee employee = new Employee();
        employee.setName(name);
        employee.setEmail(email);
        employee.setPhone(phone);
        employee.setDepartment(department);
        employee.setPosition(position);
        employee.setJoinDate(LocalDate.parse(joinDateStr));
        employee.setStatus(status);
        employees.add(employee);
    }
} 