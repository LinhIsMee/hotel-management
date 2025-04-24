package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.models.Offering;
import com.spring3.hotel.management.services.OfferingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/offerings")
public class OfferingController {

    @Autowired
    private OfferingService offeringService;

    /**
     * Lấy tất cả dịch vụ
     */
    @GetMapping
    public ResponseEntity<List<Offering>> getAllOfferings() {
        List<Offering> offerings = offeringService.getAllServices();
        return ResponseEntity.ok(offerings);
    }

    /**
     * Lấy thông tin dịch vụ theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Offering> getOfferingById(@PathVariable Integer id) {
        Offering offering = offeringService.getServiceById(id);
        return ResponseEntity.ok(offering);
    }

    /**
     * Tạo mới một dịch vụ
     */
    @PostMapping
    public ResponseEntity<Offering> createOffering(@RequestBody Offering offering) {
        Offering newOffering = offeringService.createService(offering);
        return new ResponseEntity<>(newOffering, HttpStatus.CREATED);
    }

    /**
     * Cập nhật thông tin dịch vụ theo ID
     */
    @PutMapping("/{id}")
    public ResponseEntity<Offering> updateOffering(@PathVariable Integer id, @RequestBody Offering offering) {
        Offering updatedOffering = offeringService.updateService(id, offering);
        return ResponseEntity.ok(updatedOffering);
    }

    /**
     * Xóa dịch vụ theo ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOffering(@PathVariable Integer id) {
        offeringService.deleteService(id);
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Đã xóa dịch vụ thành công",
            "offeringId", id
        ));
    }
}