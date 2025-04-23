package com.spring3.hotel.management.repositories;

import com.spring3.hotel.management.models.RevenueByRoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RevenueByRoomTypeRepository extends JpaRepository<RevenueByRoomType, Integer> {
    List<RevenueByRoomType> findByStatisticId(Integer statisticId);
}
