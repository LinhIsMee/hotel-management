package com.spring3.hotel.management.repositories;

import com.spring3.hotel.management.models.Offering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfferingRepository extends JpaRepository<Offering, Integer> {
} 