package com.spring3.hotel.management.repositories;

import com.spring3.hotel.management.models.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Integer> {
    
    Optional<Service> findByCode(String code);
    
    List<Service> findByType(String type);
    
    List<Service> findByNameContainingIgnoreCase(String name);
    
    List<Service> findByPriceLessThanEqual(Double price);
    
    List<Service> findByIsAvailable(Boolean isAvailable);
} 