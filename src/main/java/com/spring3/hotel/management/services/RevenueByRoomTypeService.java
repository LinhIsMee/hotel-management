package com.spring3.hotel.management.services;

import java.util.List;

import com.spring3.hotel.management.dto.response.RevenueByRoomTypeResponse;

public interface RevenueByRoomTypeService {
    List<RevenueByRoomTypeResponse> getAllRevenueByRoomType();
    List<RevenueByRoomTypeResponse> getRevenueByRoomTypeForStatistic(Integer statisticId);
}
