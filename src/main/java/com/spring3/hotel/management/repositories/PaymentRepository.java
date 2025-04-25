package com.spring3.hotel.management.repositories;

import com.spring3.hotel.management.models.Booking;
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
    List<Payment> findAllByBooking_Id(Integer bookingId);

    Payment findFirstByBooking_IdOrderByIdDesc(Integer bookingId);

    List<Payment> findByBooking_Id(Integer bookingId);

    // Lấy 10 payment gần đây nhất theo thời gian tạo
    List<Payment> findTop10ByOrderByCreatedAtDesc();

    @Modifying
    @Transactional
    @Query("DELETE FROM Payment p WHERE p.booking.id = :bookingId")
    void deleteByBookingId(Integer bookingId);
    
    // Thống kê số lượng giao dịch theo trạng thái
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
    long countByStatus(@Param("status") String status);
    
    // Tính tổng số tiền của các giao dịch thành công
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = '00'")
    Long sumSuccessfulPaymentAmount();
    
    // Lấy danh sách payment theo trạng thái
    List<Payment> findByStatus(String status);
    
    // Tìm kiếm payment theo trạng thái và mã ngân hàng
    List<Payment> findByStatusAndBankCode(String status, String bankCode);
    
    // Thống kê số lượng giao dịch theo ngân hàng
    @Query("SELECT p.bankCode, COUNT(p) FROM Payment p WHERE p.bankCode IS NOT NULL GROUP BY p.bankCode")
    List<Object[]> countPaymentsByBank();
    
    // Tìm payment theo bookingId và transactionNo
    @Query("SELECT p FROM Payment p WHERE p.booking.id = :bookingId AND p.transactionNo = :transactionNo")
    List<Payment> findByBookingIdAndTransactionNo(@Param("bookingId") Integer bookingId, @Param("transactionNo") String transactionNo);
}
