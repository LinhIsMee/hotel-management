package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dto.response.DiscountDTO;
import com.spring3.hotel.management.dto.request.GenerateDiscountRequest;
import com.spring3.hotel.management.dto.request.UpsertDiscountRequest;
import com.spring3.hotel.management.dto.response.ApiResponse;
import com.spring3.hotel.management.exceptions.DiscountExpiredException;
import com.spring3.hotel.management.exceptions.DiscountNotFoundException;
import com.spring3.hotel.management.exceptions.DiscountUsageExceededException;
import com.spring3.hotel.management.models.Discount;
import com.spring3.hotel.management.services.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        List<Discount> discounts = discountService.getAllDiscounts();
        List<DiscountDTO> discountDTOs = discounts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(discountDTOs);
    }

    /**
     * Lấy danh sách mã giảm giá đang hoạt động
     */
    @GetMapping("/active")
    public ResponseEntity<List<DiscountDTO>> getActiveDiscounts() {
        List<Discount> discounts = discountService.getActiveDiscounts();
        List<DiscountDTO> discountDTOs = discounts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(discountDTOs);
    }

    /**
     * Lấy thông tin mã giảm giá theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<DiscountDTO> getDiscountById(@PathVariable Integer id) {
        try {
            Discount discount = discountService.getDiscountById(id);
            DiscountDTO discountDTO = convertToDTO(discount);
            return ResponseEntity.ok(discountDTO);
        } catch (DiscountNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Tạo mới mã giảm giá
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Object> createDiscount(@RequestBody UpsertDiscountRequest request) {
        try {
            Discount createdDiscount = discountService.createDiscount(request);
            DiscountDTO discountDTO = convertToDTO(createdDiscount);
            return new ResponseEntity<>(discountDTO, HttpStatus.CREATED);
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
    public ResponseEntity<Object> updateDiscount(@PathVariable Integer id, @RequestBody UpsertDiscountRequest request) {
        try {
            Discount updatedDiscount = discountService.updateDiscount(request, id);
            DiscountDTO discountDTO = convertToDTO(updatedDiscount);
            return ResponseEntity.ok(discountDTO);
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
            Discount discount = discountService.deleteDiscount(id);
            return ResponseEntity.noContent().build();
        } catch (DiscountNotFoundException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * API tổng hợp để kiểm tra, lấy thông tin và áp dụng mã giảm giá
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
                Discount discount = discountService.getDiscountByCode(code);
                DiscountDTO discountDTO = convertToDTO(discount);
                response.put("discount", discountDTO);
                
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
    
    /**
     * Chuyển đổi Discount thành DiscountDTO
     */
    private DiscountDTO convertToDTO(Discount discount) {
        DiscountDTO dto = new DiscountDTO();
        dto.setId(discount.getId());
        dto.setCode(discount.getCode());
        dto.setDiscountValue(discount.getDiscountValue());
        dto.setValidFrom(discount.getValidFrom());
        dto.setValidTo(discount.getValidTo());
        
        // Kiểm tra tính hợp lệ
        boolean isValid = discountService.isDiscountValid(discount.getCode());
        dto.setValid(isValid);
        
        return dto;
    }
}
