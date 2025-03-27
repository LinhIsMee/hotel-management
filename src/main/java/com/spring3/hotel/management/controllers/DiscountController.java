package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dtos.request.UpsertDiscountRequest;
import com.spring3.hotel.management.models.Discount;
import com.spring3.hotel.management.services.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/discounts")
public class DiscountController {
    @Autowired
    private DiscountService discountService;

    // Lấy thông tin discount theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Discount> getDiscountById(@PathVariable Integer id) {
        Discount discount = discountService.getDiscountById(id);
        return ResponseEntity.ok(discount);
    }

    // Lấy danh sách discount
    @GetMapping("/")
    public ResponseEntity<List<Discount>> getAllDiscounts() {
        List<Discount> discounts = discountService.getAllDiscounts();
        return ResponseEntity.ok(discounts);
    }

    // Tạo mới discount
    @PostMapping("/create")
    public ResponseEntity<Discount> createDiscount(@RequestBody UpsertDiscountRequest discount) {
        Discount createdDiscount = discountService.createDiscount(discount);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDiscount);
    }

    // Cập nhật thông tin discount
    @PutMapping("/update/{id}")
    public ResponseEntity<Discount> updateDiscount(
            @PathVariable Integer id,
            @RequestBody UpsertDiscountRequest discount) {
        Discount updatedDiscount = discountService.updateDiscount(discount, id);
        return ResponseEntity.ok(updatedDiscount);
    }

    // Xóa discount
    public ResponseEntity<Void> deleteDiscount(@PathVariable Integer id) {
        discountService.deleteDiscount(id);
        return ResponseEntity.noContent().build();
    }
}
