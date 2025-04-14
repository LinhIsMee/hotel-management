package com.spring3.hotel.management.repositories;

import com.spring3.hotel.management.models.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {
    
    @Query(value = "SELECT * FROM rooms WHERE room_number = :roomNumber", nativeQuery = true)
    Optional<Room> findByRoomNumber(@Param("roomNumber") String roomNumber);
    
    @Query(value = "SELECT * FROM rooms WHERE hotel_id = :hotelId", nativeQuery = true)
    List<Room> findByHotelId(@Param("hotelId") Integer hotelId);
    
    @Query(value = "SELECT * FROM rooms WHERE room_type_id = :roomTypeId", nativeQuery = true)
    List<Room> findByRoomTypeId(@Param("roomTypeId") Integer roomTypeId);
    
    @Query(value = "SELECT * FROM rooms WHERE status = :status", nativeQuery = true)
    List<Room> findByStatus(@Param("status") String status);
    
    @Query(value = "SELECT * FROM rooms WHERE hotel_id = :hotelId AND room_type_id = :roomTypeId", nativeQuery = true)
    List<Room> findByHotelIdAndRoomTypeId(@Param("hotelId") Integer hotelId, @Param("roomTypeId") Integer roomTypeId);
    
    List<Room> findByIsActive(Boolean isActive);
    
    List<Room> findByIsActiveTrue();
    
    @Query("SELECT r FROM Room r JOIN BookingDetail bd ON bd.room.id = r.id JOIN Booking b ON bd.booking.id = b.id " +
           "WHERE ((b.checkInDate <= ?2 AND b.checkOutDate >= ?1) " +
           "OR (b.checkInDate >= ?1 AND b.checkInDate < ?2) " +
           "OR (b.checkOutDate > ?1 AND b.checkOutDate <= ?2)) " +
           "AND b.status NOT IN ('CANCELLED', 'COMPLETED')")
    List<Room> findBookedRoomsBetweenDates(LocalDate checkInDate, LocalDate checkOutDate);

    @Modifying
    @Query(value = "UPDATE rooms SET status = :status WHERE room_number = :roomNumber", nativeQuery = true)
    void updateRoomStatus(@Param("roomNumber") String roomNumber, @Param("status") String status);

    @Modifying
    @Query(value = "UPDATE rooms SET status = :status WHERE room_number IN :roomNumbers", nativeQuery = true)
    void updateRoomStatusBatch(@Param("roomNumbers") List<String> roomNumbers, @Param("status") String status);
}
