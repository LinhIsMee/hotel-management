package com.spring3.hotel.management.repositories;

import com.spring3.hotel.management.models.Statistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;

@Repository
public interface StatisticRepository extends JpaRepository<Statistic, Integer> {

    Collection<Statistic> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}
