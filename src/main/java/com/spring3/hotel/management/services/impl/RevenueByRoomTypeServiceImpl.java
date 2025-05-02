package com.spring3.hotel.management.services.impl;

import com.spring3.hotel.management.dto.response.RevenueByRoomTypeResponse;
import com.spring3.hotel.management.models.RevenueByRoomType;
import com.spring3.hotel.management.repositories.RevenueByRoomTypeRepository;
import com.spring3.hotel.management.services.RevenueByRoomTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RevenueByRoomTypeServiceImpl implements RevenueByRoomTypeService {

    @Autowired
    private RevenueByRoomTypeRepository revenueByRoomTypeRepository;

    // Lấy tất cả doanh thu theo loại phòng
    public List<RevenueByRoomTypeResponse> getAllRevenueByRoomType() {
        return revenueByRoomTypeRepository.findAll().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    // Lấy doanh thu theo loại phòng của một thống kê cụ thể
    public List<RevenueByRoomTypeResponse> getRevenueByRoomTypeForStatistic(Integer statisticId) {
        return revenueByRoomTypeRepository.findByStatisticId(statisticId).stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    // Chuyển đổi từ RevenueByRoomType sang RevenueByRoomTypeResponse
    private RevenueByRoomTypeResponse convertToResponse(RevenueByRoomType revenue) {
        return new RevenueByRoomTypeResponse(
            revenue.getId(),
            revenue.getRoomType().getId(),
            revenue.getRoomType().getName(),
            revenue.getTotalRevenue()
        );
    }
}
