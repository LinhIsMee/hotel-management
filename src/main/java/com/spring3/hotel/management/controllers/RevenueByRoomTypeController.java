package com.spring3.hotel.management.controllers;


import com.spring3.hotel.management.dtos.response.RevenueByRoomTypeResponse;
import com.spring3.hotel.management.services.RevenueByRoomTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/revenues-by-room-type")
public class RevenueByRoomTypeController {

    @Autowired
    private RevenueByRoomTypeService revenueByRoomTypeService;

    // Lấy tất cả doanh thu theo loại phòng
    @GetMapping("/all")
    public ResponseEntity<List<RevenueByRoomTypeResponse>> getAllRevenueByRoomType() {
        List<RevenueByRoomTypeResponse> revenues = revenueByRoomTypeService.getAllRevenueByRoomType();
        return ResponseEntity.ok(revenues);
    }

    // Lấy doanh thu theo loại phòng của một thống kê cụ thể
    @GetMapping("/statistic/{statisticId}")
    public ResponseEntity<List<RevenueByRoomTypeResponse>> getRevenueByRoomTypeForStatistic(@PathVariable Integer statisticId) {
        List<RevenueByRoomTypeResponse> revenues = revenueByRoomTypeService.getRevenueByRoomTypeForStatistic(statisticId);
        return ResponseEntity.ok(revenues);
    }
}
