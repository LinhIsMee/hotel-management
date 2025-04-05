package com.spring3.hotel.management.repositories;

import com.spring3.hotel.management.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    // Tìm payment bằng transactionNo
    Optional<Payment> findByTransactionNo(String transactionNo);

    // Lấy tất cả payment của một booking
    List<Payment> findAllByBookingId(Integer bookingId);

    Payment findFirstByBookingIdOrderByIdDesc(Integer bookingId);

    Optional<Payment> findByBookingId(Integer id);

    @Modifying
    @Transactional
    void deleteByBookingId(Integer bookingId);
}
