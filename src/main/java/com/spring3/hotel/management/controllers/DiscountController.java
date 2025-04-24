package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dto.DiscountDTO;
import com.spring3.hotel.management.dto.GenerateDiscountRequest;
import com.spring3.hotel.management.dto.response.ApiResponse;
import com.spring3.hotel.management.exceptions.DiscountExpiredException;
import com.spring3.hotel.management.exceptions.DiscountNotFoundException;
import com.spring3.hotel.management.exceptions.DiscountUsageExceededException;
import com.spring3.hotel.management.services.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/discounts")
@CrossOrigin(origins = "*")
public class DiscountController {

    @Autowired
    private DiscountService discountService;

    /**
     * Lấy danh sách tất cả mã giảm giá
     */
    @GetMapping
    public ResponseEntity<List<DiscountDTO>> getAllDiscounts() {
        return ResponseEntity.ok(discountService.getAllDiscounts());
    }

    /**
     * Lấy danh sách mã giảm giá đang hoạt động
     */
    @GetMapping("/active")
    public ResponseEntity<List<DiscountDTO>> getActiveDiscounts() {
        return ResponseEntity.ok(discountService.getActiveDiscounts());
    }

    /**
     * Lấy thông tin mã giảm giá theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<DiscountDTO> getDiscountById(@PathVariable Integer id) {
        try {
            DiscountDTO discount = discountService.getDiscountById(id);
            return ResponseEntity.ok(discount);
        } catch (DiscountNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Tạo mới mã giảm giá
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Object> createDiscount(@RequestBody DiscountDTO discountDTO) {
        try {
            DiscountDTO createdDiscount = discountService.createDiscount(discountDTO);
            return new ResponseEntity<>(createdDiscount, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Cập nhật thông tin mã giảm giá
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Object> updateDiscount(@PathVariable Integer id, @RequestBody DiscountDTO discountDTO) {
        try {
            DiscountDTO updatedDiscount = discountService.updateDiscount(id, discountDTO);
            return ResponseEntity.ok(updatedDiscount);
        } catch (DiscountNotFoundException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Xóa mã giảm giá
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Object> deleteDiscount(@PathVariable Integer id) {
        try {
            discountService.deleteDiscount(id);
            return ResponseEntity.noContent().build();
        } catch (DiscountNotFoundException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * API tổng hợp để kiểm tra, lấy thông tin và áp dụng mã giảm giá
     * Thay thế cho các API riêng lẻ: validate, apply, code
     */
    @GetMapping("/check")
    public ResponseEntity<Object> checkDiscount(
            @RequestParam String code,
            @RequestParam(required = false) Double amount) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Kiểm tra có tồn tại và còn hiệu lực không
            boolean isValid = discountService.isDiscountValid(code);
            response.put("valid", isValid);
            
            if (isValid) {
                // Lấy thông tin mã giảm giá
                DiscountDTO discount = discountService.getDiscountByCode(code);
                response.put("discount", discount);
                
                // Nếu có amount thì tính giá sau khi áp dụng giảm giá
                if (amount != null) {
                    double discountedAmount = discountService.applyDiscount(code, amount);
                    response.put("originalAmount", amount);
                    response.put("discountedAmount", discountedAmount);
                    response.put("discountAmount", amount - discountedAmount);
                }
            }
            
            return ResponseEntity.ok(response);
        } catch (DiscountNotFoundException e) {
            response.put("valid", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        } catch (DiscountExpiredException | DiscountUsageExceededException e) {
            response.put("valid", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * Đánh dấu mã giảm giá đã được sử dụng
     * Chỉ dành cho admin/hệ thống
     */
    @PostMapping("/mark-used")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_STAFF')")
    public ResponseEntity<Object> markDiscountAsUsed(@RequestParam String code) {
        try {
            discountService.incrementUsedCount(code);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã đánh dấu sử dụng mã giảm giá: " + code);
            return ResponseEntity.ok(response);
        } catch (DiscountNotFoundException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
