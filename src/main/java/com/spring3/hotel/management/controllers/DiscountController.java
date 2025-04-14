package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dto.DiscountDTO;
import com.spring3.hotel.management.dto.GenerateDiscountRequest;
import com.spring3.hotel.management.exceptions.DiscountExpiredException;
import com.spring3.hotel.management.exceptions.DiscountNotFoundException;
import com.spring3.hotel.management.exceptions.DiscountUsageExceededException;
import com.spring3.hotel.management.services.interfaces.DiscountService;
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
     * Lấy thông tin mã giảm giá theo mã code
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<DiscountDTO> getDiscountByCode(@PathVariable String code) {
        try {
            DiscountDTO discount = discountService.getDiscountByCode(code);
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
     * Tạo nhiều mã giảm giá ngẫu nhiên
     */
    @PostMapping("/generate")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Object> generateDiscounts(@RequestBody GenerateDiscountRequest request) {
        try {
            List<DiscountDTO> generatedDiscounts = discountService.generateRandomDiscounts(request);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đã tạo " + generatedDiscounts.size() + " mã giảm giá ngẫu nhiên");
            response.put("discounts", generatedDiscounts);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
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
     * Kiểm tra mã giảm giá có hợp lệ
     */
    @GetMapping("/validate/{code}")
    public ResponseEntity<Boolean> validateDiscount(@PathVariable String code) {
        boolean isValid = discountService.isDiscountValid(code);
        return ResponseEntity.ok(isValid);
    }

    /**
     * Áp dụng mã giảm giá vào số tiền
     */
    @GetMapping("/apply")
    public ResponseEntity<Object> applyDiscount(
            @RequestParam String code,
            @RequestParam double amount) {
        try {
            double discountedAmount = discountService.applyDiscount(code, amount);
            Map<String, Object> response = new HashMap<>();
            response.put("originalAmount", amount);
            response.put("discountedAmount", discountedAmount);
            response.put("discountAmount", amount - discountedAmount);
            response.put("discountCode", code);
            return ResponseEntity.ok(response);
        } catch (DiscountNotFoundException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (DiscountExpiredException | DiscountUsageExceededException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Tăng số lần sử dụng của mã giảm giá
     */
    @PostMapping("/use/{code}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_STAFF')")
    public ResponseEntity<Object> useDiscount(@PathVariable String code) {
        try {
            discountService.incrementUsedCount(code);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đã cập nhật số lần sử dụng của mã giảm giá: " + code);
            return ResponseEntity.ok(response);
        } catch (DiscountNotFoundException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }
}
