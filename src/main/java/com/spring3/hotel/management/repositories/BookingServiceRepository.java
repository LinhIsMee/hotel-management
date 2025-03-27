package com.spring3.hotel.management.repositories;

import com.spring3.hotel.management.models.BookingService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingServiceRepository extends JpaRepository<BookingService, Integer> {

}
