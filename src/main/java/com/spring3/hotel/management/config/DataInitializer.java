package com.spring3.hotel.management.config;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.spring3.hotel.management.models.Role;
import com.spring3.hotel.management.models.User;
import com.spring3.hotel.management.repositories.RoleRepository;
import com.spring3.hotel.management.repositories.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
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
    }
} 