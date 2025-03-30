package com.spring3.hotel.management.repositories;

import com.spring3.hotel.management.models.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Integer> {
    Discount findDiscountById(Integer id);

    boolean existsByCode(String code);
    
    Optional<Discount> findByCode(String code);
    
    @Query("SELECT d FROM Discount d WHERE d.validFrom <= :today AND d.validTo >= :today AND d.usedCount < d.maxUses")
    List<Discount> findActiveDiscounts(LocalDate today);
}
