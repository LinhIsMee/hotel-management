package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dtos.response.DashboardInfoCountResponse;
import com.spring3.hotel.management.dtos.response.StatisticResponse;
import com.spring3.hotel.management.services.StatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/statistics")
public class StatisticController {

    @Autowired
    private StatisticService statisticService;

    // Lấy số liệu tổng hợp của 4 bảng thống kê của dashboard
    @GetMapping("/count-info")
    public ResponseEntity<DashboardInfoCountResponse> getAllStatistics() {
        DashboardInfoCountResponse infoCountResponse = statisticService.getAllCountInfo();
        return ResponseEntity.ok(infoCountResponse);
    }

    // Lấy thống kê theo khoảng thời gian
    @GetMapping("/date-range")
    public ResponseEntity<List<StatisticResponse>> getStatisticsByDateRange(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<StatisticResponse> statistics = statisticService.getStatisticsByDateRange(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

}