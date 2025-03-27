package com.spring3.hotel.management.repositories;

import com.spring3.hotel.management.models.Offering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Optional;

@Repository
public interface OfferingsRepository extends JpaRepository<Offering, Integer> {
    Optional<Offering> findByName(String name);

    // tim cac dich vu co gia nho hon gia cho truoc
    List<Offering> findByPriceLessThan(Double price);
}
