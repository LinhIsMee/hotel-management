package com.spring3.hotel.management.repositories;

import com.spring3.hotel.management.models.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Integer> {
    Discount findDiscountById(Integer id);

    boolean existsByCode(String code);
}
