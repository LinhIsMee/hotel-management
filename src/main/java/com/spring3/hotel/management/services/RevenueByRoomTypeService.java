package com.spring3.hotel.management.services;

import com.spring3.hotel.management.dto.response.RevenueByRoomTypeResponse;

import java.util.List;

public interface RevenueByRoomTypeService {
    List<RevenueByRoomTypeResponse> getAllRevenueByRoomType();
    List<RevenueByRoomTypeResponse> getRevenueByRoomTypeForStatistic(Integer statisticId);
}
