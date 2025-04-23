package com.spring3.hotel.management.repositories;

import com.spring3.hotel.management.enums.ServiceType;
import com.spring3.hotel.management.models.HotelService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<HotelService, Integer> {
    
    Optional<HotelService> findByCode(String code);
    
    List<HotelService> findByType(ServiceType type);
    
    List<HotelService> findByNameContainingIgnoreCase(String name);
    
    List<HotelService> findByPriceLessThanEqual(BigDecimal price);
    
    List<HotelService> findByIsAvailable(Boolean isAvailable);
} 