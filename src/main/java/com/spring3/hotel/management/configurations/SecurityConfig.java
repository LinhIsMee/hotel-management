package com.spring3.hotel.management.configurations;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;

import com.spring3.hotel.management.helpers.UserDetailsServiceImpl;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    JwtAuthFilter jwtAuthFilter;

    @Value("#{'${app.allow-origin}'.split(',')}")
    private List<String> allowedOrigins;

    @Bean
    public UserDetailsService userDetailsService(){
        return new UserDetailsServiceImpl();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://127.0.0.1:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(
                            "/api/v1/login",
                            "/api/v1/register",
                            "/api/v1/validate-token",
                            "/api/v1/refresh-token",
                            "/api/v1/forgot-password",
                            "/api/v1/reset-password",
                            "/api/v1/logout",
                            "/api/v1/payments/callback",
                            "/api/v1/payments/check-status/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/reviews", "/api/v1/reviews/", "/api/v1/reviews/{id}", "/api/v1/reviews/room/{roomId}", "/api/v1/reviews/statistics").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/reviews/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/reviews/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/reviews/**").permitAll()
                        .requestMatchers("/api/v1/user", "/api/v1/user/profile/**", "/api/v1/user/change-password").authenticated()
                        .requestMatchers(
                            "/api/v1/users", 
                            "/api/v1/users/**", 
                            "/api/v1/user/create", 
                            "/api/v1/user/update/**"
                        ).hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/v1/employees/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/v1/statistics/**", "/api/v1/bookings/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/v1/user/bookings/**").hasAuthority("ROLE_USER")
                        .requestMatchers("/api/v1/bookings/create").hasAuthority("ROLE_USER")
                        .requestMatchers("/api/v1/bookings/recent").hasAuthority("ROLE_EMPLOYEE")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;

    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
