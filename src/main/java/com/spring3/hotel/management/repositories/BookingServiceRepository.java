package com.spring3.hotel.management.repositories;

import com.spring3.hotel.management.models.BookingService;
import com.spring3.hotel.management.models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingServiceRepository extends JpaRepository<BookingService, Integer> {
    List<BookingService> findByBooking(Booking booking);
}
