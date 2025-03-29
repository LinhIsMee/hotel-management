package com.spring3.hotel.management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // API quản lý người dùng chỉ dành cho ADMIN
                .requestMatchers("/api/v1/users").hasRole("ADMIN")
                .requestMatchers("/api/v1/users/{userId}").hasRole("ADMIN")
                .requestMatchers("/api/v1/user/create").hasRole("ADMIN")
                .requestMatchers("/api/v1/user/update/{userId}").hasRole("ADMIN")
                .requestMatchers("/api/v1/users/{userId}").hasRole("ADMIN")
                // Cấu hình mặc định cho các request khác
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        
        return http.build();
    }
} 