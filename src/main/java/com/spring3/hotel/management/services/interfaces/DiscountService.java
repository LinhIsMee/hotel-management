package com.spring3.hotel.management.services.interfaces;

import com.spring3.hotel.management.dto.DiscountDTO;
import com.spring3.hotel.management.dto.GenerateDiscountRequest;

import java.util.List;

public interface DiscountService {
    
    // Lấy tất cả mã giảm giá
    List<DiscountDTO> getAllDiscounts();
    
    // Lấy mã giảm giá theo ID
    DiscountDTO getDiscountById(Integer id);
    
    // Lấy mã giảm giá theo code
    DiscountDTO getDiscountByCode(String code);
    
    // Tạo mã giảm giá mới
    DiscountDTO createDiscount(DiscountDTO discountDTO);
    
    // Cập nhật mã giảm giá
    DiscountDTO updateDiscount(Integer id, DiscountDTO discountDTO);
    
    // Xóa mã giảm giá
    void deleteDiscount(Integer id);
    
    // Kiểm tra tính hợp lệ của mã giảm giá
    boolean isDiscountValid(String discountCode);
    
    // Áp dụng mã giảm giá vào số tiền
    double applyDiscount(String discountCode, double amount);
    
    // Tăng số lần sử dụng mã giảm giá
    void incrementUsedCount(String discountCode);
    
    // Lấy danh sách mã giảm giá có hiệu lực
    List<DiscountDTO> getActiveDiscounts();
    
    // Tạo mã giảm giá ngẫu nhiên với số lượng cho trước
    List<DiscountDTO> generateRandomDiscounts(GenerateDiscountRequest request);
} 