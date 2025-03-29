package com.spring3.hotel.management.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.spring3.hotel.management.models.Department;
import com.spring3.hotel.management.models.Employee;
import com.spring3.hotel.management.models.Position;
import com.spring3.hotel.management.models.Role;
import com.spring3.hotel.management.models.User;
import com.spring3.hotel.management.repositories.EmployeeRepository;
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