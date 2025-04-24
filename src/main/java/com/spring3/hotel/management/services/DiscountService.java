package com.spring3.hotel.management.services;

import com.spring3.hotel.management.dto.response.DiscountDTO;
import com.spring3.hotel.management.dto.request.GenerateDiscountRequest;
import com.spring3.hotel.management.dto.request.UpsertDiscountRequest;
import com.spring3.hotel.management.models.Discount;

import java.util.List;

/**
 * Service xử lý mã giảm giá
 */
public interface DiscountService {
    // Lấy thông tin mã giảm giá
    Discount getDiscountById(Integer id);
    List<Discount> getAllDiscounts();
    
    // Lấy mã giảm giá theo code
    Discount getDiscountByCode(String code);
    
    // Tạo và cập nhật mã giảm giá
    Discount createDiscount(UpsertDiscountRequest discount);
    Discount updateDiscount(UpsertDiscountRequest discount, Integer id);
    Discount deleteDiscount(Integer id);
    
    // Kiểm tra và áp dụng mã giảm giá
    boolean isDiscountValid(String discountCode);
    double applyDiscount(String discountCode, double amount);
    void incrementUsedCount(String discountCode);
    
    // Danh sách mã giảm giá có hiệu lực
    List<Discount> getActiveDiscounts();
    
    // Tạo mã giảm giá tự động
    List<DiscountDTO> generateRandomDiscounts(GenerateDiscountRequest request);
}
